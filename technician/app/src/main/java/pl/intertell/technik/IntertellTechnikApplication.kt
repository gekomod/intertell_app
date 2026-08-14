package pl.intertell.technik

import android.app.Application
import pl.intertell.technik.crash.CrashHandler
import pl.intertell.technik.notifications.NotificationHelper
import pl.intertell.technik.notifications.TaskPollWorker

class IntertellTechnikApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CrashHandler.install(this)
        NotificationHelper.ensureChannel(this)
        TaskPollWorker.schedule(this)
    }
}
