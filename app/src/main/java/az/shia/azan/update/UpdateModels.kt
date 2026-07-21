package az.shia.azan.update

data class UpdateInfo(
    val version: String,
    val downloadUrl: String,
    val releaseUrl: String,
    val releaseNotes: String
)

sealed class UpdateCheckResult {
    data class Available(val info: UpdateInfo) : UpdateCheckResult()
    data class UpToDate(val latestVersion: String) : UpdateCheckResult()
    data class Failure(val message: String) : UpdateCheckResult()
}
