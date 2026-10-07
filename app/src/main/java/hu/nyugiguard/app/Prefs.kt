package hu.nyugiguard.app

import android.content.Context

// Egyelőre beégetve; később beállításból / konfigból jöjjön.
private const val DEFAULT_HEALTH_URL =
    "https://incidents.betterstack.com/api/v1/heartbeat/byhUf6dRWMagC4VJMvGmbo7M"

class Prefs(context: Context) {
    private val sp = context.applicationContext.getSharedPreferences("settings", Context.MODE_PRIVATE)

    var enabled: Boolean
        get() = sp.getBoolean("enabled", false)
        set(v) = sp.edit().putBoolean("enabled", v).apply()

    var intervalMin: Int
        get() = sp.getInt("intervalMin", 15)
        set(v) = sp.edit().putInt("intervalMin", v).apply()

    var brightnessPct: Int
        get() = sp.getInt("brightnessPct", 100)
        set(v) = sp.edit().putInt("brightnessPct", v).apply()

    var volumePct: Int
        get() = sp.getInt("volumePct", 100)
        set(v) = sp.edit().putInt("volumePct", v).apply()

    var lastRunMillis: Long
        get() = sp.getLong("lastRun", 0L)
        set(v) = sp.edit().putLong("lastRun", v).apply()

    // Health check: a fő kapcsolótól független
    var healthEnabled: Boolean
        get() = sp.getBoolean("healthEnabled", true)
        set(v) = sp.edit().putBoolean("healthEnabled", v).apply()

    var healthIntervalMin: Int
        get() = sp.getInt("healthIntervalMin", 15)
        set(v) = sp.edit().putInt("healthIntervalMin", v).apply()

    /** A hívott cím (IP vagy link). Üres mentés esetén az alapértelmezett (beégetett) cím él. */
    var healthUrl: String
        get() = sp.getString("healthUrl", null)?.takeIf { it.isNotBlank() } ?: DEFAULT_HEALTH_URL
        set(v) = sp.edit().putString("healthUrl", v).apply()

    var healthLastMillis: Long
        get() = sp.getLong("healthLast", 0L)
        set(v) = sp.edit().putLong("healthLast", v).apply()

    var healthLastResult: String
        get() = sp.getString("healthLastResult", "") ?: ""
        set(v) = sp.edit().putString("healthLastResult", v).apply()

    // Az első indításkori engedélykérés: mindegyiket csak egyszer kérjük automatikusan
    var askedWrite: Boolean
        get() = sp.getBoolean("askedWrite", false)
        set(v) = sp.edit().putBoolean("askedWrite", v).apply()

    var askedDnd: Boolean
        get() = sp.getBoolean("askedDnd", false)
        set(v) = sp.edit().putBoolean("askedDnd", v).apply()

    var askedBattery: Boolean
        get() = sp.getBoolean("askedBattery", false)
        set(v) = sp.edit().putBoolean("askedBattery", v).apply()

    val healthActive: Boolean get() = healthEnabled && healthUrl.isNotBlank()
}
