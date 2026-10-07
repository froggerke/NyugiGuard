package hu.nyugiguard.app

import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager
import android.provider.Settings

/** Beállítja a fényerőt és a hangerőt, kikapcsolja a némítást. Minden lépés külön védett. */
object Enforcer {
    private val STREAMS = intArrayOf(
        AudioManager.STREAM_RING,
        AudioManager.STREAM_VOICE_CALL,
        AudioManager.STREAM_NOTIFICATION,
        AudioManager.STREAM_ALARM,
    )

    fun apply(context: Context) {
        val prefs = Prefs(context)
        applyVolume(context, prefs.volumePct)
        applyBrightness(context, prefs.brightnessPct)
        prefs.lastRunMillis = System.currentTimeMillis()
    }

    private fun applyVolume(context: Context, pct: Int) {
        val nm = context.getSystemService(NotificationManager::class.java)
        val am = context.getSystemService(AudioManager::class.java)

        try {
            if (nm.isNotificationPolicyAccessGranted &&
                nm.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL
            ) {
                nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
            }
        } catch (_: SecurityException) {
        }

        try {
            am.ringerMode = AudioManager.RINGER_MODE_NORMAL
        } catch (_: SecurityException) {
        }

        for (stream in STREAMS) {
            try {
                val max = am.getStreamMaxVolume(stream)
                val target = Math.round(max * pct / 100f).coerceIn(1, max)
                am.setStreamVolume(stream, target, 0)
            } catch (_: SecurityException) {
            }
        }
    }

    private fun applyBrightness(context: Context, pct: Int) {
        if (!Settings.System.canWrite(context)) return
        val value = Math.round(255 * pct / 100f).coerceIn(1, 255)
        val cr = context.contentResolver
        Settings.System.putInt(cr, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL)
        Settings.System.putInt(cr, Settings.System.SCREEN_BRIGHTNESS, value)
    }
}
