package pl.intertell.technik

import android.app.Application
import org.maplibre.android.MapLibre
import pl.intertell.technik.crash.CrashHandler
import pl.intertell.technik.notifications.NotificationHelper
import pl.intertell.technik.notifications.TaskPollWorker

class IntertellTechnikApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CrashHandler.install(this)
        MapLibre.getInstance(this)
        NotificationHelper.ensureChannel(this)
        TaskPollWorker.schedule(this)
    }
}
