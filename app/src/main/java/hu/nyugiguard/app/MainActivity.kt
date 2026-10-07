package hu.nyugiguard.app

import android.app.Activity
import android.app.AlertDialog
import android.app.NotificationManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.text.format.DateFormat
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import java.util.Date

class MainActivity : Activity() {
    private lateinit var prefs: Prefs

    private lateinit var switchEnabled: Switch
    private lateinit var textLastRun: TextView
    private lateinit var textInterval: TextView
    private lateinit var textBrightness: TextView
    private lateinit var textVolume: TextView
    private lateinit var seekInterval: SeekBar
    private lateinit var seekBrightness: SeekBar
    private lateinit var seekVolume: SeekBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        prefs = Prefs(this)

        switchEnabled = findViewById(R.id.switchEnabled)
        textLastRun = findViewById(R.id.textLastRun)
        textInterval = findViewById(R.id.textInterval)
        textBrightness = findViewById(R.id.textBrightness)
        textVolume = findViewById(R.id.textVolume)
        seekInterval = findViewById(R.id.seekInterval)
        seekBrightness = findViewById(R.id.seekBrightness)
        seekVolume = findViewById(R.id.seekVolume)

        // Intervallum: 1..120 perc (a skála 0..119, +1 eltolással)
        seekInterval.max = 119
        seekInterval.progress = prefs.intervalMin - 1
        seekBrightness.max = 99
        seekBrightness.progress = prefs.brightnessPct - 1
        seekVolume.max = 99
        seekVolume.progress = prefs.volumePct - 1
        switchEnabled.isChecked = prefs.enabled
        updateLabels()

        seekInterval.setOnSeekBarChangeListener(listener { value ->
            prefs.intervalMin = value + 1
            updateLabels()
            // Új intervallum azonnal érvényes legyen
            if (prefs.enabled) Scheduler.scheduleNext(this)
        })
        seekBrightness.setOnSeekBarChangeListener(listener { value ->
            prefs.brightnessPct = value + 1
            updateLabels()
        })
        seekVolume.setOnSeekBarChangeListener(listener { value ->
            prefs.volumePct = value + 1
            updateLabels()
        })

        switchEnabled.setOnCheckedChangeListener { _, checked ->
            prefs.enabled = checked
            if (checked) Scheduler.start(this) else Scheduler.cancel(this)
            updateLabels()
        }

        // Friss telepítés után az első megnyitáskor induljon el a health check
        if (prefs.healthActive) HealthCheck.start(this)

        // Health check: 1..120 perc, alapból 5 perc, alapból bekapcsolva
        val seekHealth = findViewById<SeekBar>(R.id.seekHealthInterval)
        val editUrl = findViewById<EditText>(R.id.editHealthUrl)
        seekHealth.max = 119
        seekHealth.progress = prefs.healthIntervalMin - 1
        findViewById<Switch>(R.id.switchHealth).isChecked = prefs.healthEnabled
        editUrl.setText(prefs.healthUrl)

        seekHealth.setOnSeekBarChangeListener(listener { value ->
            prefs.healthIntervalMin = value + 1
            updateLabels()
            if (prefs.healthActive) HealthCheck.scheduleNext(this)
        })
        findViewById<Switch>(R.id.switchHealth).setOnCheckedChangeListener { _, checked ->
            prefs.healthEnabled = checked
            if (checked) HealthCheck.start(this) else HealthCheck.cancel(this)
            updateLabels()
        }
        findViewById<Button>(R.id.buttonHealthSave).setOnClickListener {
            prefs.healthUrl = editUrl.text.toString().trim()
            HealthCheck.start(this)
            updateLabels()
            // A hívás háttérszálon fut: az eredményt kicsit később frissítjük a képernyőn.
            editUrl.postDelayed({ updateLabels() }, 2000)
        }

        findViewById<Button>(R.id.buttonApplyNow).setOnClickListener {
            Enforcer.apply(this)
            updateLabels()
        }
        findViewById<Button>(R.id.buttonPermWrite).setOnClickListener { openWriteSettings() }
        findViewById<Button>(R.id.buttonPermDnd).setOnClickListener { openDndSettings() }
        findViewById<Button>(R.id.buttonPermBattery).setOnClickListener { openBatterySettings() }
    }

    override fun onResume() {
        super.onResume()
        updateLabels()
        requestNextPermission()
    }

    private fun openWriteSettings() = startActivity(
        Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:$packageName"))
    )

    private fun openDndSettings() =
        startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))

    private fun openBatterySettings() = startActivity(
        Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:$packageName"))
    )

    private class PermStep(
        val missing: Boolean,
        val message: String,
        val markAsked: () -> Unit,
        val open: () -> Unit,
    )

    /**
     * Első indításkor egyesével kéri a hiányzó engedélyeket. Visszatéréskor (onResume) jön a következő.
     * Mindegyiket csak egyszer kéri automatikusan, így elutasításnál nem ismétlődik.
     */
    private fun requestNextPermission() {
        if (isFinishing) return
        val steps = listOf(
            PermStep(
                !Settings.System.canWrite(this) && !prefs.askedWrite,
                "Az app a fényerő visszaállításához engedélyt kér. A következő képernyőn kapcsold be a „Rendszerbeállítások módosítása” kapcsolót.",
                { prefs.askedWrite = true }, ::openWriteSettings
            ),
            PermStep(
                !getSystemService(NotificationManager::class.java).isNotificationPolicyAccessGranted && !prefs.askedDnd,
                "A némítás kikapcsolásához a „Ne zavarjanak” hozzáférés kell. A következő képernyőn válaszd ki az appot, és engedélyezd.",
                { prefs.askedDnd = true }, ::openDndSettings
            ),
            PermStep(
                !getSystemService(PowerManager::class.java).isIgnoringBatteryOptimizations(packageName) && !prefs.askedBattery,
                "Hogy az app a háttérben is megbízhatóan fusson, ki kell kapcsolni rá az akkumulátor-optimalizálást. A következő ablakban válaszd az „Engedélyezés” lehetőséget.",
                { prefs.askedBattery = true }, ::openBatterySettings
            ),
        )
        val step = steps.firstOrNull { it.missing } ?: return
        step.markAsked()
        AlertDialog.Builder(this)
            .setTitle("Engedély szükséges")
            .setMessage(step.message)
            .setCancelable(false)
            .setPositiveButton("Tovább") { _, _ -> step.open() }
            .setNegativeButton("Most nem") { _, _ -> requestNextPermission() }
            .show()
    }

    private fun listener(onChange: (Int) -> Unit) = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            if (fromUser) onChange(progress)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {}
        override fun onStopTrackingTouch(seekBar: SeekBar) {}
    }

    private fun updateLabels() {
        textInterval.text = "Ismétlés: ${prefs.intervalMin} percenként"
        textBrightness.text = "Fényerő: ${prefs.brightnessPct}%"
        textVolume.text = "Hangerő: ${prefs.volumePct}%"

        val last = prefs.lastRunMillis
        textLastRun.text = if (last == 0L) "Még nem futott le"
        else "Utolsó futás: " + DateFormat.format("yyyy.MM.dd HH:mm:ss", Date(last))

        findViewById<TextView>(R.id.textHealthInterval).text =
            "Health check: ${prefs.healthIntervalMin} percenként"
        val healthLast = prefs.healthLastMillis
        findViewById<TextView>(R.id.textHealthStatus).text = when {
            prefs.healthUrl.isBlank() -> "Nincs cím megadva, nem fut"
            !prefs.healthEnabled -> "Kikapcsolva"
            healthLast == 0L -> "Még nem futott le"
            else -> "Utolsó hívás: " + DateFormat.format("HH:mm:ss", Date(healthLast)) +
                " – " + prefs.healthLastResult
        }

        val canWrite =Settings.System.canWrite(this)
        val dnd = getSystemService(NotificationManager::class.java).isNotificationPolicyAccessGranted
        val battery = getSystemService(PowerManager::class.java).isIgnoringBatteryOptimizations(packageName)
        findViewById<TextView>(R.id.textPermWrite).text =
            "Fényerő-módosítás: " + if (canWrite) "megadva ✔" else "HIÁNYZIK ✘"
        findViewById<TextView>(R.id.textPermDnd).text =
            "Ne zavarjanak hozzáférés: " + if (dnd) "megadva ✔" else "HIÁNYZIK ✘"
        findViewById<TextView>(R.id.textPermBattery).text =
            "Akkumulátor-korlátozás kikapcsolva: " + if (battery) "igen ✔" else "NEM ✘"
    }
}
