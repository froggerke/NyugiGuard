package hu.nyugiguard.app

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock

object Scheduler {
    private fun pendingIntent(context: Context): PendingIntent =
        PendingIntent.getBroadcast(
            context, 0, Intent(context, TickReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    /** A következő futás [Prefs.intervalMin] perc múlva. Doze alatt is lefut. */
    fun scheduleNext(context: Context) {
        val am = context.getSystemService(AlarmManager::class.java)
        val triggerAt = SystemClock.elapsedRealtime() + Prefs(context).intervalMin * 60_000L
        am.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAt, pendingIntent(context))
    }

    fun cancel(context: Context) {
        context.getSystemService(AlarmManager::class.java).cancel(pendingIntent(context))
    }

    /** Azonnali futás, majd az időzítés újraindítása az aktuális beállításokkal. */
    fun start(context: Context) {
        Enforcer.apply(context)
        scheduleNext(context)
    }
}
