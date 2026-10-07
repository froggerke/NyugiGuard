package hu.nyugiguard.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TickReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (!Prefs(context).enabled) return
        Enforcer.apply(context)
        Scheduler.scheduleNext(context)
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Prefs(context).enabled) Scheduler.start(context)
        if (Prefs(context).healthActive) HealthCheck.start(context)
    }
}
