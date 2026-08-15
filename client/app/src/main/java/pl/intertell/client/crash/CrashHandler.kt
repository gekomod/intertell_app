package pl.intertell.client.crash

import android.content.Context
import android.util.Log
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * A minimal crash reporter for a device with no ADB access — installs a
 * default UncaughtExceptionHandler that writes the full stack trace to a
 * file before letting the crash proceed normally, so the *next* launch can
 * show it on-screen (see MainActivity) for the user to copy and send back.
 */
object CrashHandler {
    private const val FILE_NAME = "last_crash.txt"

    fun install(context: Context) {
        val appContext = context.applicationContext
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val writer = StringWriter()
                throwable.printStackTrace(PrintWriter(writer))
                val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
                File(appContext.filesDir, FILE_NAME).writeText("$timestamp\n\n$writer")
            } catch (e: Exception) {
                Log.e("CrashHandler", "failed to persist crash", e)
            }
            previous?.uncaughtException(thread, throwable)
        }
    }

    /** Returns the last crash's text and deletes the file, or null if there wasn't one. */
    fun readAndClearLastCrash(context: Context): String? {
        val file = File(context.applicationContext.filesDir, FILE_NAME)
        if (!file.exists()) return null
        val text = runCatching { file.readText() }.getOrNull()
        file.delete()
        return text
    }
}
