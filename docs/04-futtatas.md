# Fordítás és futtatás

## Gép környezete (feltárt adatok)
- Windows 11, PowerShell.
- Android Studio: `D:\Programming_tools\Android Studio` (bundled JDK: `...\jbr`, JDK 25).
- Android SDK: `D:\Programming_tools\.android_sdk\Sdk` (platforms: android-30, android-36; build-tools 30.0.2, 35.0.0, 36.0.0).
  Van másik SDK is: `C:\Users\frog_powerplant\AppData\Local\Android\Sdk` (azonos platformok).
- Telepített Gradle disztribúciók: `C:\Users\frog_powerplant\.gradle\wrapper\dists\` (8.13, 9.0.0, 9.4.0, 9.8.0).
- A projektben **nincs Gradle wrapper** (`gradlew`), kézzel írtuk.
- Cél telefon: Xiaomi Poco, Android 14+ feltételezve.

## Ajánlott: Android Studio
1. File -> Open -> `D:\Programming\claude-test\nyugi-guard`.
2. Gradle sync. Ha az AGP/Gradle verziót kifogásolja, fogadd el az Android Studio javaslatát.
3. Telefon: Fejlesztői beállítások -> USB-hibakeresés be, USB-n csatlakoztatás.
4. Run (zöld háromszög).

## Parancssor – MŰKÖDIK (2026-10-07)
A loopback hiba oka: a JDK 25 (Android Studio jbr) + hosszú alapértelmezett TEMP út. Megoldás: JDK 17 és rövid TEMP.
```bash
export TEMP='C:\jtmp' TMP='C:\jtmp' JAVA_HOME=~/.jdks/ms-17.0.15
cd /d/Programming/claude-test/nyugi-guard
~/.gradle/wrapper/dists/gradle-9.4.0-bin/*/gradle-9.4.0/bin/gradle.bat assembleDebug --no-daemon -q
```
`adb`: `D:\Programming_tools\.android_sdk\Sdk\platform-tools\adb.exe` (nincs a PATH-on). Csatlakoztatott eszköz: tablet `R52M70M5M6F`.

## Régi parancssor (hibás volt, lásd fent)
```powershell
$env:JAVA_HOME = 'D:\Programming_tools\Android Studio\jbr'
& 'C:\Users\frog_powerplant\.gradle\wrapper\dists\gradle-9.4.0-bin\lcvyxq3t37f6mx9miaydrrgs\gradle-9.4.0\bin\gradle.bat' assembleDebug
```
Kimenet (siker esetén): `app\build\outputs\apk\debug\app-debug.apk`.
Eddig minden futás ezzel állt meg: `Unable to establish loopback connection`.

## Telepítés a telefonra és első beállítás
1. Az APK-t telepítsd (adb vagy Android Studio Run).
2. Nyisd meg az appot, és add meg a három engedélyt a gombokkal (fényerő, Ne zavarjanak, akkumulátor).
3. Xiaomi: Biztonság app / Alkalmazások kezelése / NyugiGuard -> Automatikus indítás be,
   Akkumulátor-takarékosság: Nincs korlátozás.
4. Kapcsold be a szolgáltatást; teszthez csúsztasd az intervallumot 1 percre.

## Ellenőrzés
- Némítsd le a telefont, halkítsd le, vedd le a fényerőt, várj az intervallumot: az app rendet rak.
- Indítsd újra a telefont, nézd meg, hogy az "Utolsó futás" frissül-e.
