package pl.intertell.client.update

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.core.net.toUri
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

data class DownloadProgress(val fraction: Float?, val done: Boolean = false, val failed: Boolean = false)

/**
 * Downloads an update APK via the system DownloadManager, polling its status
 * table to report live progress (for an in-app progress dialog) instead of
 * relying only on the system notification-shade progress. Launches the
 * system package installer automatically once the download finishes.
 * Requires REQUEST_INSTALL_PACKAGES (declared in the manifest); the OS shows
 * its own "allow installs from this app" prompt the first time, same as any
 * other sideloaded-APK flow.
 */
object UpdateInstaller {
    fun download(context: Context, update: UpdateInfo): Flow<DownloadProgress> = flow {
        val appContext = context.applicationContext
        val manager = appContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(update.downloadUrl.toUri())
            .setTitle("Intertell — aktualizacja")
            .setDescription(update.versionLabel)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(appContext, Environment.DIRECTORY_DOWNLOADS, "intertell-client-update.apk")
            .setMimeType("application/vnd.android.package-archive")
        val id = manager.enqueue(request)

        emit(DownloadProgress(fraction = 0f))

        while (true) {
            var status = DownloadManager.STATUS_RUNNING
            var bytesDownloaded = 0L
            var bytesTotal = 0L
            manager.query(DownloadManager.Query().setFilterById(id)).use { cursor ->
                if (cursor.moveToFirst()) {
                    status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                    bytesDownloaded = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                    bytesTotal = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                }
            }

            when (status) {
                DownloadManager.STATUS_SUCCESSFUL -> {
                    emit(DownloadProgress(fraction = 1f, done = true))
                    launchInstaller(appContext, manager, id)
                    return@flow
                }
                DownloadManager.STATUS_FAILED -> {
                    emit(DownloadProgress(fraction = null, failed = true))
                    return@flow
                }
                else -> {
                    val fraction = if (bytesTotal > 0) bytesDownloaded.toFloat() / bytesTotal else null
                    emit(DownloadProgress(fraction = fraction))
                }
            }
            delay(200)
        }
    }

    private fun launchInstaller(context: Context, manager: DownloadManager, id: Long) {
        val uri = manager.getUriForDownloadedFile(id) ?: return
        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(installIntent)
    }
}
