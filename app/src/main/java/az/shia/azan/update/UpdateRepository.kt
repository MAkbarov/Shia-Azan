package az.shia.azan.update

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.math.BigInteger
import java.net.SocketTimeoutException
import javax.net.ssl.HttpsURLConnection

class UpdateRepository {
    suspend fun check(currentVersion: String): UpdateCheckResult = withContext(Dispatchers.IO) {
        try {
            checkBlocking(currentVersion)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: SocketTimeoutException) {
            UpdateCheckResult.Failure(UpdateFailureMessages.TIMEOUT)
        } catch (_: ResponseTooLargeException) {
            UpdateCheckResult.Failure(UpdateFailureMessages.MALFORMED_RELEASE)
        } catch (_: JSONException) {
            UpdateCheckResult.Failure(UpdateFailureMessages.MALFORMED_RELEASE)
        } catch (_: IOException) {
            UpdateCheckResult.Failure(UpdateFailureMessages.NETWORK)
        } catch (_: SecurityException) {
            UpdateCheckResult.Failure(UpdateFailureMessages.SECURITY)
        } catch (_: RuntimeException) {
            UpdateCheckResult.Failure(UpdateFailureMessages.MALFORMED_RELEASE)
        }
    }

    private fun checkBlocking(currentVersion: String): UpdateCheckResult {
        val current = SemanticVersion.parse(currentVersion)
            ?: return UpdateCheckResult.Failure(UpdateFailureMessages.INVALID_CURRENT_VERSION)

        val connection = GitHubNetwork.openFollowingRedirects(
            GitHubUrlPolicy.LATEST_RELEASE_API,
            "application/vnd.github+json"
        )
        return try {
            val responseCode = connection.responseCode
            if (responseCode !in HttpsURLConnection.HTTP_OK..299) {
                runCatching {
                    connection.errorStream?.use { it.readUtf8Limited(MAX_ERROR_BYTES) }
                }
                return UpdateCheckResult.Failure(UpdateFailureMessages.http(responseCode))
            }

            val response = connection.inputStream.use { it.readUtf8Limited(MAX_RESPONSE_BYTES) }
            val release = JSONObject(response)
            if (release.optBoolean("draft", true) ||
                release.optBoolean("prerelease", false) ||
                release.requiredString("published_at") == null
            ) {
                return UpdateCheckResult.Failure(UpdateFailureMessages.NOT_PUBLISHED)
            }

            val rawTag = release.requiredString("tag_name")
                ?: return UpdateCheckResult.Failure(UpdateFailureMessages.INVALID_LATEST_VERSION)
            val latest = SemanticVersion.parse(rawTag)
                ?: return UpdateCheckResult.Failure(UpdateFailureMessages.INVALID_LATEST_VERSION)
            val latestVersion = rawTag.removePrefixIgnoreCase("v")

            if (latest <= current) {
                return UpdateCheckResult.UpToDate(latestVersion)
            }

            val releaseUrl = release.requiredString("html_url")
                ?.let(GitHubUrlPolicy::parseReleasePage)
                ?.toExternalForm()
                ?: return UpdateCheckResult.Failure(UpdateFailureMessages.MALFORMED_RELEASE)
            val asset = release.optJSONArray("assets")
                ?.let { assets ->
                    (0 until assets.length())
                        .mapNotNull { index -> assets.optJSONObject(index)?.toApkAsset() }
                        .sortedWith(
                            compareBy<ApkAsset> { !it.name.contains("universal", ignoreCase = true) }
                                .thenBy { !it.name.contains("release", ignoreCase = true) }
                                .thenBy { it.name.lowercase() }
                        )
                        .firstOrNull()
                }
                ?: return UpdateCheckResult.Failure(UpdateFailureMessages.NO_APK)

            UpdateCheckResult.Available(
                UpdateInfo(
                    version = latestVersion,
                    downloadUrl = asset.url,
                    releaseUrl = releaseUrl,
                    releaseNotes = release.optionalString("body").orEmpty()
                )
            )
        } finally {
            connection.disconnect()
        }
    }

    private fun JSONObject.toApkAsset(): ApkAsset? {
        val name = requiredString("name") ?: return null
        if (!name.endsWith(".apk", ignoreCase = true)) return null
        if (optString("state", "uploaded") != "uploaded") return null
        if (has("size") && optLong("size", -1L) <= 0L) return null
        val rawUrl = requiredString("browser_download_url") ?: return null
        val url = GitHubUrlPolicy.parseAllowed(rawUrl)?.toExternalForm() ?: return null
        return ApkAsset(name, url)
    }

    private fun JSONObject.requiredString(key: String): String? =
        if (!has(key) || isNull(key)) null else optString(key).trim().takeIf(String::isNotEmpty)

    private fun JSONObject.optionalString(key: String): String? =
        if (!has(key) || isNull(key)) null else optString(key)

    private data class ApkAsset(val name: String, val url: String)

    private companion object {
        const val MAX_RESPONSE_BYTES = 2 * 1024 * 1024
        const val MAX_ERROR_BYTES = 64 * 1024
    }
}

internal object UpdateFailureMessages {
    const val NETWORK = "Yeniləmə yoxlanılarkən şəbəkə xətası baş verdi."
    const val TIMEOUT = "Yeniləmə yoxlamasının vaxtı bitdi."
    const val SECURITY = "Yeniləmə ünvanının təhlükəsizlik yoxlaması uğursuz oldu."
    const val INVALID_CURRENT_VERSION = "Cari tətbiq versiyası düzgün formatda deyil."
    const val INVALID_LATEST_VERSION = "Buraxılışın versiya teqi düzgün formatda deyil."
    const val NOT_PUBLISHED = "GitHub cavabı yayımlanmış stabil buraxılış deyil."
    const val MALFORMED_RELEASE = "Buraxılış məlumatı natamam və ya etibarsızdır."
    const val NO_APK = "Buraxılışda endirilə bilən APK faylı tapılmadı."
    private const val HTTP_PREFIX = "GitHub buraxılış sorğusunu qəbul etmədi"

    fun http(code: Int): String = "$HTTP_PREFIX (HTTP $code)."

    fun recommendsReleaseFallback(message: String): Boolean =
        message == INVALID_LATEST_VERSION ||
            message == NOT_PUBLISHED ||
            message == MALFORMED_RELEASE ||
            message == NO_APK ||
            message.startsWith(HTTP_PREFIX)

    /** Keçici xətalar backoff ilə təkrar cəhdə dəyər. */
    fun isTransient(message: String): Boolean =
        message == NETWORK || message == TIMEOUT
}

private data class SemanticVersion(
    val major: BigInteger,
    val minor: BigInteger,
    val patch: BigInteger,
    val preRelease: List<String>
) : Comparable<SemanticVersion> {
    override fun compareTo(other: SemanticVersion): Int {
        major.compareTo(other.major).takeIf { it != 0 }?.let { return it }
        minor.compareTo(other.minor).takeIf { it != 0 }?.let { return it }
        patch.compareTo(other.patch).takeIf { it != 0 }?.let { return it }

        if (preRelease.isEmpty() && other.preRelease.isNotEmpty()) return 1
        if (preRelease.isNotEmpty() && other.preRelease.isEmpty()) return -1
        for (index in 0 until minOf(preRelease.size, other.preRelease.size)) {
            val left = preRelease[index]
            val right = other.preRelease[index]
            if (left == right) continue
            val leftNumber = left.toBigIntegerOrNull()
            val rightNumber = right.toBigIntegerOrNull()
            return when {
                leftNumber != null && rightNumber != null -> leftNumber.compareTo(rightNumber)
                leftNumber != null -> -1
                rightNumber != null -> 1
                else -> left.compareTo(right)
            }
        }
        return preRelease.size.compareTo(other.preRelease.size)
    }

    companion object {
        private val pattern = Regex(
            "^[vV]?(\\d+)(?:\\.(\\d+))?(?:\\.(\\d+))?" +
                "(?:-([0-9A-Za-z-]+(?:\\.[0-9A-Za-z-]+)*))?" +
                "(?:\\+[0-9A-Za-z-]+(?:\\.[0-9A-Za-z-]+)*)?$"
        )

        fun parse(raw: String): SemanticVersion? {
            val match = pattern.matchEntire(raw.trim()) ?: return null
            return runCatching {
                SemanticVersion(
                    major = match.groupValues[1].toBigInteger(),
                    minor = match.groupValues[2].ifEmpty { "0" }.toBigInteger(),
                    patch = match.groupValues[3].ifEmpty { "0" }.toBigInteger(),
                    preRelease = match.groupValues[4]
                        .takeIf(String::isNotEmpty)
                        ?.split('.')
                        .orEmpty()
                )
            }.getOrNull()
        }
    }
}

private fun String.removePrefixIgnoreCase(prefix: String): String =
    if (startsWith(prefix, ignoreCase = true)) substring(prefix.length) else this
