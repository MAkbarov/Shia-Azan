package az.shia.azan.update

import az.shia.azan.BuildConfig
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import javax.net.ssl.HttpsURLConnection

internal object GitHubUrlPolicy {
    const val LATEST_RELEASE_API =
        "https://api.github.com/repos/MAkbarov/Shia-Azan/releases/latest"
    const val RELEASES_PAGE =
        "https://github.com/MAkbarov/Shia-Azan/releases/latest"

    fun parseAllowed(rawUrl: String): URL? {
        val url = runCatching { URL(rawUrl) }.getOrNull() ?: return null
        if (!url.protocol.equals("https", ignoreCase = true)) return null
        if (url.userInfo != null) return null
        if (url.port != -1 && url.port != 443) return null

        val host = url.host.lowercase()
        if (host != "github.com" &&
            host != "api.github.com" &&
            host != "githubusercontent.com" &&
            !host.endsWith(".githubusercontent.com")
        ) {
            return null
        }
        return url
    }

    fun parseReleasePage(rawUrl: String): URL? =
        parseAllowed(rawUrl)?.takeIf { it.host.equals("github.com", ignoreCase = true) }
}

internal object GitHubNetwork {
    private const val MAX_REDIRECTS = 5
    // Yavaş şəbəkələrdə də yenilənmə tamamlansın deyə geniş timeout-lar.
    private const val CONNECT_TIMEOUT_MS = 30_000
    private const val READ_TIMEOUT_MS = 120_000
    private val userAgent = "Shia-Azan-Android/${BuildConfig.VERSION_NAME}"

    @Throws(IOException::class)
    fun openFollowingRedirects(rawUrl: String, accept: String): HttpsURLConnection {
        var current = GitHubUrlPolicy.parseAllowed(rawUrl)
            ?: throw IOException("Disallowed or invalid HTTPS URL")

        repeat(MAX_REDIRECTS + 1) { redirectCount ->
            val connection = (current.openConnection() as? HttpsURLConnection)
                ?: throw IOException("HTTPS connection required")
            connection.instanceFollowRedirects = false
            connection.requestMethod = "GET"
            connection.connectTimeout = CONNECT_TIMEOUT_MS
            connection.readTimeout = READ_TIMEOUT_MS
            connection.useCaches = false
            connection.doInput = true
            connection.setRequestProperty("Accept", accept)
            connection.setRequestProperty("User-Agent", userAgent)
            if (current.host.equals("api.github.com", ignoreCase = true)) {
                connection.setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
            }

            val responseCode = try {
                connection.responseCode
            } catch (error: IOException) {
                connection.disconnect()
                throw error
            } catch (error: RuntimeException) {
                connection.disconnect()
                throw error
            }
            if (responseCode !in setOf(301, 302, 303, 307, 308)) {
                return connection
            }

            val location = connection.getHeaderField("Location")
            connection.disconnect()
            if (location.isNullOrBlank() || redirectCount == MAX_REDIRECTS) {
                throw IOException("Invalid or excessive HTTPS redirects")
            }

            val redirected = runCatching { URL(current, location) }.getOrNull()
                ?: throw IOException("Invalid redirect URL")
            current = GitHubUrlPolicy.parseAllowed(redirected.toExternalForm())
                ?: throw IOException("Redirect host is not allowed")
        }

        throw IOException("Too many redirects")
    }
}

internal class ResponseTooLargeException : IOException("Response exceeded the size limit")

@Throws(IOException::class)
internal fun InputStream.readUtf8Limited(maxBytes: Int): String {
    val output = ByteArrayOutputStream(minOf(maxBytes, 16 * 1024))
    val buffer = ByteArray(8 * 1024)
    var total = 0
    while (true) {
        val count = read(buffer)
        if (count < 0) break
        total += count
        if (total > maxBytes) throw ResponseTooLargeException()
        output.write(buffer, 0, count)
    }
    return output.toString(Charsets.UTF_8.name())
}
