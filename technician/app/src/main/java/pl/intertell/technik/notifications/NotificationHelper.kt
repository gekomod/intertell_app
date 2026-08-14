package pl.intertell.technik.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import pl.intertell.technik.MainActivity
import pl.intertell.technik.R
import pl.intertell.technik.data.Job
import pl.intertell.technik.data.JobKind

/**
 * Posts one system notification per new task from [TaskPollWorker], styled
 * per the kind/urgency of the incoming zlecenie: red for an urgent contact
 * message (a live fault report), green for a routine one and for
 * installation requests — the same accent colors the app itself uses for
 * those states (see ui/theme/Color.kt).
 */
object NotificationHelper {
    private const val CHANNEL_ID = "zlecenia"
    private val URGENT_COLOR = Color.parseColor("#D93B1E")
    private val ROUTINE_COLOR = Color.parseColor("#22A06B")

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Nowe zlecenia",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Powiadomienia o nowych zgłoszeniach i instalacjach przypisanych do Ciebie."
            enableLights(true)
        }
        manager.createNotificationChannel(channel)
    }

    fun notifyNewJob(context: Context, job: Job) {
        ensureChannel(context)

        val urgent = job.isUrgent
        val color = if (urgent) URGENT_COLOR else ROUTINE_COLOR
        val kindLabel = if (job.kind == JobKind.INSTALL) "Nowe zgłoszenie instalacyjne" else "Nowe zapytanie kontaktowe"
        val title = if (urgent) "🔴 $kindLabel" else "🟢 $kindLabel"
        val body = listOfNotNull(job.title.takeIf { it != kindLabel }, job.clientName, job.address.takeIf { it.isNotBlank() })
            .joinToString(" · ")

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = android.app.PendingIntent.getActivity(
            context,
            job.id.toInt(),
            openIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(color)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return // POST_NOTIFICATIONS not granted (or user disabled them) — nothing to do
        val notificationId = "${job.kind}:${job.id}".hashCode()
        manager.notify(notificationId, notification)
    }
}
