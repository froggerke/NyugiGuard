# Jelenlegi állapot

Utolsó frissítés: 2026-10-07

## Kész és tesztelve (Samsung SM-T515 tablet)
- Android projekt (Gradle Kotlin DSL, egy `app` modul), app neve **NyugiGuard**, csomag `hu.nyugiguard.app`.
- Főképernyő: kapcsoló, 3 csúszka (intervallum 1–120 perc, fényerő 1–100%, hangerő 1–100%),
  "Alkalmazás most" gomb, engedély-állapotok és gombok a beállító képernyőkhöz.
- Őr: `AlarmManager.setAndAllowWhileIdle` láncolt ébresztés; `BootReceiver` indítja újra boot/frissítés után.
- Beállítások: `SharedPreferences` (`Prefs`).
- **Engedélyek első indításkor (2026-10-07):** az app egyesével kéri a hiányzó engedélyeket
  (fényerő → Ne zavarjanak → akkumulátor), magyarázó dialoggal („Tovább” / „Most nem”). Mindegyiket csak egyszer kéri
  automatikusan (`askedWrite/askedDnd/askedBattery` a `Prefs`-ben); utána a főképernyő gombjai maradnak. Tableten tiszta
  telepítéssel kipróbálva, mindhárom megadva. Nincs tesztelve: „Most nem” ág, újraindítás utáni nem-kérdezés.
- Health check: külön szakasz (kapcsoló, cím, intervallum 1–120 perc, utolsó hívás állapota), a fő kapcsolótól független
  ütemezés (`HealthCheck.kt`, `HealthReceiver`). Tesztelve: `HTTP 200`, 1 perces ismétlés, új csomagnévvel is.
- A hívott link egyelőre **beégetve** (`DEFAULT_HEALTH_URL`, `Prefs.kt`); üres mentésnél ez él. A token titok: publikus repó előtt ki kell venni.
- Videók: `docs/teszt-tablet.mp4`, `docs/teszt-tablet-health.mp4`.

## Nyitott
- Újraindítás utáni automatikus indulás tesztje.
- Poco/Xiaomi háttérműködés (Autostart, „Nincs korlátozás”).
- Health check token kivétele a forráskódból.
- Nincs app-ikon, nincs saját UI-téma.

## Lehetséges következő lépések
1. Reboot teszt a tableten, majd a Poco tesztelése.
2. Token kivétele (config / beállítás / build-konfig).
3. Finomítás: fényerő csak akkor emelkedjen, ha a cél alatt van; zene hangerő opció; éjszakai mód; ikon, téma.
4. Később opcionálisan: hangvezérlés (lásd `01-feladat.md`).
