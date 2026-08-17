package pl.intertell.client

import android.app.Application
import pl.intertell.client.crash.CrashHandler
import pl.intertell.client.notifications.ClientNotificationHelper
import pl.intertell.client.notifications.ClientPollWorker

class IntertellClientApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CrashHandler.install(this)
        ClientNotificationHelper.ensureChannel(this)
        ClientPollWorker.schedule(this)
    }
}
