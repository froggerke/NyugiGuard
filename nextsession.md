# Következő session: indulás

Olvasd el ezeket, ebben a sorrendben, mielőtt bármihez nyúlsz:

1. [docs/01-feladat.md](docs/01-feladat.md): mi a cél, mit vetettünk el és miért
2. [docs/02-allapot.md](docs/02-allapot.md): hol tartunk, mi nyitott, health check állapota
3. [docs/03-felepites.md](docs/03-felepites.md): fájlok, működési folyamat, engedélyek
4. [docs/04-futtatas.md](docs/04-futtatas.md): fordítás, futtatás, gép környezete

## Röviden
Android (Kotlin) app idősek telefonjára. Két, egymástól független háttérfunkció, mindkettő
`AlarmManager`-rel, újraindítás után is fut (`BootReceiver`):

1. **Őr:** állítható időközönként (alap 15 perc) felteszi a fényerőt és a hangerőt, kikapcsolja a némítást.
2. **Health check:** állítható időközönként (alap 15 perc, 1–120 perc) GET hívást küld egy címre
   (Better Stack heartbeat). A cím egyelőre **beégetve** (`DEFAULT_HEALTH_URL`, `Prefs.kt`).

Az app neve **NyugiGuard** (csomag `hu.nyugiguard.app`). Első indításkor egyesével kéri az engedélyeket.

Mindkettő **lefordul, és működik** a Samsung SM-T515 tableten (adb-n csatlakoztatva, `R52M70M5M6F`).
Tesztelve: fő funkció, 1 perces ismétlés, `HTTP 200` a health checknél, lezárt képernyővel is.
Tesztvideók: `docs/teszt-tablet.mp4`, `docs/teszt-tablet-health.mp4`.

## Fordítás (a lényeg, részletek: docs/04-futtatas.md)
A régi „loopback” hiba oka a JDK 25 + hosszú TEMP út volt. Működő parancs (Git Bash):
```bash
export TEMP='C:\jtmp' TMP='C:\jtmp' JAVA_HOME=~/.jdks/ms-17.0.15
cd /d/Programming/claude-test/nyugi-guard
~/.gradle/wrapper/dists/gradle-9.4.0-bin/*/gradle-9.4.0/bin/gradle.bat assembleDebug --no-daemon -q
```
- Ne állítsd be az `MSYS_NO_PATHCONV=1`-et a build parancsnál (a `JAVA_HOME` útvonalat elrontja),
  az adb `/sdcard/...` útvonalaihoz viszont kell.
- `adb`: `D:\Programming_tools\.android_sdk\Sdk\platform-tools\adb.exe` (nincs a PATH-on); az adb `input text` a `/` jelet nem tudja beírni.
- Telepítés: `adb install -r app/build/outputs/apk/debug/app-debug.apk` (a régi `hu.nyugger.support` csomagot `adb uninstall`-lal kell törölni). Új telepítés után az engedélyek
  (fényerő: `appops set hu.nyugiguard.app WRITE_SETTINGS allow`, DND: `cmd notification allow_dnd hu.nyugiguard.app`,
  akkumulátor: `dumpsys deviceidle whitelist +hu.nyugiguard.app`) adb-ből is megadhatók.

## Nyitott / következő lépések
1. **Újraindítás utáni indulás ellenőrzése** (tablet): bekapcsolt kapcsolókkal reboot, majd az app
   megnyitása nélkül nézni az „Utolsó futás” / „Utolsó hívás” időt. Még **nem tesztelt**.
2. **Poco (Xiaomi) tesztelése:** Autostart, „Nincs korlátozás” akkumulátor-mód, háttérműködés. Még nem volt kezünkben a Poco.
3. **Health check cím:** a token jelenleg a forráskódban van. Ha verziókezelésbe vagy publikus helyre kerül a projekt,
   előtte kivenni (config / beállítás / build-konfig). Üres cím mentése most a beégetett linkre esik vissza.
4. Finomítások: fényerő csak akkor emelkedjen, ha a cél alatt van; zene hangerő opció; éjszakai mód; app-ikon, téma.
5. Később, opcionálisan: hangvezérlés (lásd `01-feladat.md`).

## Tudnivalók
- A Samsung/Xiaomi: ha az appot „Kényszerített leállítás”-sal állítják le, az ébresztések törlődnek, és az app
  addig nem indul újra, amíg valaki egyszer meg nem nyitja.
- A tableten az `/sdcard/` alatt maradt két tesztvideó (`test.mp4`, `test2.mp4`), törölhetők.
- A projektben nincs git repó és nincs Gradle wrapper.
- A `.claude/settings.json`-ban csak az adb olvasó jellegű parancsai vannak engedélyezve.

## Utolsó üzenet a felhasználótól
2026-10-07: „csinálj egy next session.md-t hogy innen folytathassuk új sessionben” (az app addigra
minden tesztben jól futott, a felhasználó leállította az appot, beragadt folyamat nem volt).
