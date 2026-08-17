package pl.intertell.technik.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit
import pl.intertell.technik.data.Job
import pl.intertell.technik.data.api.ApiTechnicianRepository

private fun Job.seenKey() = "$kind:$id"

/**
 * Background check for new zlecenia, so a technician gets notified even
 * with the app closed. Runs at WorkManager's practical minimum interval
 * (15 minutes — there's no push/FCM wiring here, this is plain polling).
 * Silently does nothing if nobody's logged in yet.
 *
 * The same [checkOnce] also backs a much more frequent foreground poll loop
 * (see TechnicianViewModel's init{}) that catches new tasks while the app
 * process is alive — WorkManager's periodic floor can't go below 15
 * minutes, so relying on it alone made notifications feel like they "don't
 * come immediately". Both share the same seen-task bookkeeping
 * (ServerConfig), so neither double-notifies for what the other already
 * caught.
 */
class TaskPollWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = try {
        checkOnce(applicationContext)
        Result.success()
    } catch (e: Exception) {
        Result.retry()
    }

    companion object {
        private const val UNIQUE_NAME = "task-poll"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<TaskPollWorker>(15, TimeUnit.MINUTES).build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(UNIQUE_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        }

        suspend fun checkOnce(context: Context) {
            val repository = ApiTechnicianRepository(context)
            if (repository.serverConfig.getToken().isNullOrBlank()) return
            val tasks = repository.getTasks()
            val seen = repository.serverConfig.getSeenTaskKeys()
            val newTasks = tasks.filter { it.seenKey() !in seen }
            newTasks.forEach { NotificationHelper.notifyNewJob(context, it) }
            repository.serverConfig.addSeenTaskKeys(tasks.map { it.seenKey() })
        }
    }
}
