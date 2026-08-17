package pl.intertell.client.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import pl.intertell.client.MainActivity
import pl.intertell.client.R
import pl.intertell.client.data.Invoice
import pl.intertell.client.data.Ticket

/**
 * Posts system notifications for events ClientPollWorker/the foreground poll
 * loop catch — new invoices and BOK ticket-status changes. Mirrors the
 * technician app's NotificationHelper.
 */
object ClientNotificationHelper {
    private const val CHANNEL_ID = "konto"
    private val ACCENT_COLOR = Color.parseColor("#0E86C4")

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Konto i zgłoszenia",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Powiadomienia o nowych fakturach i aktualizacjach zgłoszeń."
            enableLights(true)
        }
        manager.createNotificationChannel(channel)
    }

    fun notifyNewInvoice(context: Context, invoice: Invoice) {
        post(
            context,
            notificationId = "invoice:${invoice.id}".hashCode(),
            title = "Nowa faktura",
            body = "Faktura ${invoice.number} na ${invoice.amountZl} zł, termin płatności ${invoice.dueOn}.",
        )
    }

    fun notifyTicketUpdate(context: Context, ticket: Ticket) {
        post(
            context,
            notificationId = "ticket:${ticket.id}".hashCode(),
            title = "Aktualizacja zgłoszenia",
            body = "${ticket.subject.ifBlank { "Zgłoszenie #${ticket.number}" }} — ${ticket.statusLabel}.",
        )
    }

    private fun post(context: Context, notificationId: Int, title: String, body: String) {
        ensureChannel(context)
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = android.app.PendingIntent.getActivity(
            context,
            notificationId,
            openIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(ACCENT_COLOR)
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
        manager.notify(notificationId, notification)
    }
}
