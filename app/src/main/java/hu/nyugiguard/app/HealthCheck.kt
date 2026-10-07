package hu.nyugiguard.app

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import java.net.HttpURLConnection
import java.net.URL

object HealthCheck {
    private fun pendingIntent(context: Context): PendingIntent =
        PendingIntent.getBroadcast(
            context, 1, Intent(context, HealthReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    /** Séma nélküli címhez (pl. 192.168.1.10:8080/ping) http:// kerül. */
    fun normalize(raw: String): String {
        val s = raw.trim()
        return if (s.isEmpty() || s.contains("://")) s else "http://$s"
    }

    fun scheduleNext(context: Context) {
        val am = context.getSystemService(AlarmManager::class.java)
        val triggerAt = SystemClock.elapsedRealtime() + Prefs(context).healthIntervalMin * 60_000L
        am.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAt, pendingIntent(context))
    }

    fun cancel(context: Context) {
        context.getSystemService(AlarmManager::class.java).cancel(pendingIntent(context))
    }

    /** Azonnali hívás, majd az időzítés újraindítása. Nem csinál semmit, ha nincs cím vagy ki van kapcsolva. */
    fun start(context: Context) {
        if (!Prefs(context).healthActive) {
            cancel(context)
            return
        }
        scheduleNext(context)
        ping(context)
    }

    /** Egy GET hívás háttérszálon; az eredményt elmenti. [done] a végén fut le. */
    fun ping(context: Context, done: () -> Unit = {}) {
        val app = context.applicationContext
        val prefs = Prefs(app)
        val url = normalize(prefs.healthUrl)
        if (url.isEmpty()) {
            done()
            return
        }
        Thread {
            val result = try {
                val conn = URL(url).openConnection() as HttpURLConnection
                conn.connectTimeout = 10_000
                conn.readTimeout = 10_000
                conn.requestMethod = "GET"
                try {
                    "HTTP ${conn.responseCode}"
                } finally {
                    conn.disconnect()
                }
            } catch (e: Exception) {
                "Hiba: ${e.javaClass.simpleName}"
            }
            prefs.healthLastResult = result
            prefs.healthLastMillis = System.currentTimeMillis()
            done()
        }.start()
    }
}

class HealthReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (!Prefs(context).healthActive) return
        // Előbb az újraütemezés, hogy egy hibás hívás se szakítsa meg a láncot.
        HealthCheck.scheduleNext(context)
        val pending = goAsync()
        HealthCheck.ping(context) { pending.finish() }
    }
}
