# Feladat

## Háttér
Idős felhasználók (nyuggerek) okostelefonján gyakran előfordul, hogy véletlenül
elnyomnak valamit: lehalkul a telefon, némítva marad, vagy lesötétedik a kijelző.
Nem tudják visszaállítani, ezért lemaradnak hívásokról.

## Cél (jelenlegi, egyszerűsített verzió)
Egy Android app (Kotlin), ami háttérben, **állítható időközönként** (alapértelmezett: 15 perc):

- felteszi a **fényerőt** (állítható, alapértelmezett: max),
- felteszi a **hangerőt** (állítható, alapértelmezett: max),
- **kikapcsolja a némítást** (csengő mód, Ne zavarjanak ki).

Az appban egy **kapcsolóval** be/ki lehet kapcsolni a háttérműködést.
Bekapcsolt állapotban **telefon-újraindítás után is magától fusson**.

Cél eszköz: Xiaomi Poco (HyperOS/MIUI), de általános Androidon is működjön.
Elsődleges szempont: **alacsony akkumulátor-fogyasztás**, a usernek ne kelljen 5 percenként tölteni.

## Elvetett / későbbi irány
Eredetileg hangalapú megszólítás ("hey siri" jellegű wake word) volt az ötlet:
a nyugger hangparancsára emelné a fényerőt/hangerőt. Ezt **elhalasztottuk**, mert:

- folyamatos mikrofon-figyelés nagyobb akkumulátor-fogyasztás, foreground service + állandó értesítés kell,
- újraindítás után (Android 14+) mikrofonos szolgáltatás nem indítható automatikusan,
- az időzített "őr" megoldás a lényegi problémát (némítás, halk hang) hang nélkül is megoldja.

Ha később kell: két lépcsős megoldás (wake word motor, pl. Porcupine/openWakeWord/Vosk, majd
Android `SpeechRecognizer` a parancsra). A magyar nyelvtámogatást ellenőrizni kell.
Szükséges hozzá: `RECORD_AUDIO`, `FOREGROUND_SERVICE_MICROPHONE`, `POST_NOTIFICATIONS`.

## Megfontolások
- A Xiaomi agresszívan öli a háttérfolyamatokat: Autostart + "Nincs korlátozás" akkumulátor-mód kell.
- Automatikus fényerő kikapcsolódik, fix fényerőt állít (éjszaka vakító lehet).
- Zene hangerőt jelenleg nem állítja (csak csengő, hívás, értesítés, ébresztő).
