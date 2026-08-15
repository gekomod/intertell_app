package pl.intertell.client.update

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Environment
import androidx.core.net.toUri

/**
 * Downloads an update APK via the system DownloadManager — a native progress
 * notification in the shade, no custom progress UI to build or get wrong —
 * and launches the system package installer automatically once it lands.
 * Requires REQUEST_INSTALL_PACKAGES (declared in the manifest); the OS shows
 * its own "allow installs from this app" prompt the first time, same as any
 * other sideloaded-APK flow.
 */
object UpdateInstaller {
    private var pendingDownloadId: Long = -1L
    private var receiverRegistered = false

    fun startDownload(context: Context, update: UpdateInfo) {
        val appContext = context.applicationContext
        val manager = appContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(update.downloadUrl.toUri())
            .setTitle("Intertell — aktualizacja")
            .setDescription(update.versionLabel)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(appContext, Environment.DIRECTORY_DOWNLOADS, "intertell-client-update.apk")
            .setMimeType("application/vnd.android.package-archive")

        ensureReceiverRegistered(appContext, manager)
        pendingDownloadId = manager.enqueue(request)
    }

    private fun ensureReceiverRegistered(context: Context, manager: DownloadManager) {
        if (receiverRegistered) return
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
                if (id != pendingDownloadId) return
                val uri = manager.getUriForDownloadedFile(id) ?: return
                val installIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/vnd.android.package-archive")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                ctx.startActivity(installIntent)
            }
        }
        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }
        receiverRegistered = true
    }
}
