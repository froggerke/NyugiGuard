# Projekt felépítése

Gyökér: `D:\Programming\claude-test\nyugi-guard`

| Fájl | Mire való |
|---|---|
| `settings.gradle.kts`, `build.gradle.kts` | Gradle gyökér; AGP 8.13.0, Kotlin 2.2.20 |
| `gradle.properties` | Gradle JVM beállítások, `useAndroidX=false` |
| `local.properties` | SDK útvonal: `D:\Programming_tools\.android_sdk\Sdk` (gépfüggő, ne verziókezeld) |
| `app/build.gradle.kts` | Modul: namespace `hu.nyugiguard.app`, minSdk 26, target/compileSdk 36, JVM 17 |
| `app/src/main/AndroidManifest.xml` | Engedélyek, MainActivity, TickReceiver, BootReceiver |
| `app/src/main/res/layout/activity_main.xml` | Főképernyő elrendezése |
| `.../hu/nyugiguard/app/MainActivity.kt` | UI, csúszkák, kapcsoló, engedély-gombok, első indításkori engedélykérés (`requestNextPermission`) |
| `.../Prefs.kt` | Beállítások (enabled, intervalMin=15, brightnessPct=100, volumePct=100, lastRun, health*, asked*) |
| `.../Enforcer.kt` | A tényleges beállítás: Ne zavarjanak ki, csengő mód, hangerők, fényerő |
| `.../Scheduler.kt` | AlarmManager ütemezés (`start`, `scheduleNext`, `cancel`) |
| `.../Receivers.kt` | `TickReceiver` (ébresztésre fut + újraütemez), `BootReceiver` (boot/frissítés után) |

## Működési folyamat
1. Kapcsoló be: `Prefs.enabled = true`, `Scheduler.start()` (azonnali futás + következő ébresztés).
2. Ébresztés: `TickReceiver` -> ha engedélyezett, `Enforcer.apply()` -> `Scheduler.scheduleNext()`.
3. Boot / app-frissítés: `BootReceiver` -> ha engedélyezett, `Scheduler.start()`.

## Engedélyek
Az első indításkor az app egyesével, dialoggal kéri a három különleges engedélyt (lásd `02-allapot.md`).
- `WRITE_SETTINGS` (különleges: felhasználói jóváhagyás a beállításokban) a fényerőhöz.
- `ACCESS_NOTIFICATION_POLICY` (különleges) a Ne zavarjanak kikapcsolásához.
- `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` az akkumulátor-optimalizálás kikapcsolásához.
- `RECEIVE_BOOT_COMPLETED` az újraindítás utáni indításhoz.
- Hangerőhöz nem kell külön engedély.

## Tudnivaló
- Ébresztés nem pontos: Doze alatt késhet.
- Enforcer minden lépése `SecurityException`-ről védett, hiányzó engedély nem dönti el az egészet.
