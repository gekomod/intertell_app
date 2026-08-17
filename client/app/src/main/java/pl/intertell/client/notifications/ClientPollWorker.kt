package pl.intertell.client.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit
import pl.intertell.client.data.api.ApiIntertellRepository

/**
 * Background check for new invoices and BOK ticket-status changes, so a
 * customer gets notified even with the app closed. Runs at WorkManager's
 * practical minimum interval (15 minutes — there's no push/FCM wiring here,
 * this is plain polling). Silently does nothing if nobody's logged in yet.
 *
 * The same [checkOnce] also backs a much more frequent foreground poll loop
 * (see ClientViewModel's init{}) that catches new invoices/updates while
 * the app process is alive — WorkManager's periodic floor can't go below 15
 * minutes, so relying on it alone made notifications feel like they "don't
 * come immediately". Both share the same seen-invoice/ticket-status
 * bookkeeping (ServerConfig), so neither double-notifies for what the
 * other (or loadHome()/loadInvoices() just being visited on-screen)
 * already caught.
 */
class ClientPollWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = try {
        checkOnce(applicationContext)
        Result.success()
    } catch (e: Exception) {
        Result.retry()
    }

    companion object {
        private const val UNIQUE_NAME = "client-poll"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<ClientPollWorker>(15, TimeUnit.MINUTES).build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(UNIQUE_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        }

        suspend fun checkOnce(context: Context) {
            val repository = ApiIntertellRepository(context)
            if (repository.serverConfig.getToken().isNullOrBlank()) return

            val invoices = repository.getInvoices()
            val seenInvoiceIds = repository.serverConfig.getSeenInvoiceIds()
            val newInvoices = invoices.filter { it.id.toString() !in seenInvoiceIds }
            newInvoices.forEach { ClientNotificationHelper.notifyNewInvoice(context, it) }
            repository.serverConfig.addSeenInvoiceIds(invoices.map { it.id.toString() })

            // Ticket status snapshot doubles as its own baseline: on the
            // very first poll ever, `previous` is null for every ticket (no
            // snapshot exists yet), so nothing notifies — it just records
            // where things stand. Only a real transition after that fires.
            val tickets = repository.getTickets()
            val previousStatuses = repository.serverConfig.getTicketStatusSnapshot()
                .associate { pair -> pair.substringBefore(':') to pair.substringAfter(':') }
            for (ticket in tickets) {
                val previous = previousStatuses[ticket.id.toString()]
                if (previous != null && previous != ticket.status) {
                    ClientNotificationHelper.notifyTicketUpdate(context, ticket)
                }
            }
            repository.serverConfig.setTicketStatusSnapshot(tickets.map { "${it.id}:${it.status}" })
        }
    }
}
