# XIV Azan

**XIV Azan** is a native Android application for calculating Shia prayer times, following the next prayer, and playing the azan at the appropriate time. It is designed primarily for Azerbaijani-speaking users and works with the selected location and calculation method rather than a fixed timetable.

**Author:** DeXIV

**Telegram:** [t.me/De_XIV](https://t.me/De_XIV)

## APK yüklə / Download APK

[![XIV Azan APK yüklə](https://img.shields.io/badge/XIV_Azan-Son_APK-21C4C3?style=for-the-badge&logo=android&logoColor=white)](https://github.com/MAkbarov/Shia-Azan/releases/latest/download/XIV-Azan.apk)

**Son versiya / Latest:** v1.0.3

**AZ:** Ən son APK-ni birbaşa GitHub buraxılışından yükləmək üçün yuxarıdakı düyməyə toxunun. Bu link həmişə ən son versiyaya yönləndirir. Android quraşdırma zamanı brauzer üçün “Naməlum tətbiqləri quraşdır” icazəsi istəyə bilər. Tətbiq quraşdırıldıqdan sonra yeniləmələr avtomatik yoxlanılır və Parametrlər → Tətbiq Yeniləməsi bölməsindən idarə oluna bilər.

**EN:** Tap the button above to download the latest APK directly from the GitHub release. This link always points to the newest version. Android may ask you to allow “Install unknown apps” during installation. After installing, updates are checked automatically and can be managed from Settings → App Update.

[Azərbaycan dili](#azərbaycan-dili) · [English](#english)

---

## Azərbaycan dili

### Tətbiq haqqında

XIV Azan gündəlik namaz vaxtlarını seçilmiş məkanın koordinatları, tarix, saat qurşağı və Şiə hesablama metoduna əsasən astronomik şəkildə hesablayır. Məqsəd sadədir: tətbiqi açanda növbəti namazı və ona qalan vaxtı dərhal görmək, vaxt daxil olduqda isə azanı eşitmək.

Tətbiqdə sabit cədvəl istifadə edilmir. Məkan və tarix dəyişdikdə vaxtlar yenidən hesablanır. Növbəti namaz məlumatı tətbiqdə, bildiriş panelində və kilid ekranında yenilənir.

### Əsas imkanlar

- Sübh, Günəş, Zöhr, Əsr, Məğrib və İşa vaxtlarının hesablanması
- Qum (Leva İnstitutu) və Tehran hesablama metodları
- Növbəti namaz və canlı qalan vaxt göstəricisi
- İşadan sonra sabahın Sübhünə avtomatik keçid
- GPS ilə dəqiq məkan aşkarlanması
- Azərbaycan şəhər və rayonları, eləcə də seçilmiş xarici Şiə mərkəzləri
- Dəqiq alarm və namaz vaxtı bildirişləri
- Status paneli və kilid ekranında daimi namaz bildirişi
- Miladi və Hicri-qəməri tarixin ana səhifədə yanaşı göstərilməsi
- Hicri tarixin −7…+7 gün aralığında ay müşahidəsinə uyğun tənzimlənməsi
- GitHub buraxılışlarından avtomatik yeniləmə yoxlaması və təhlükəsiz APK quraşdırması
- Arxa fonda azan oxudulması
- Azan səsi və səs səviyyəsi seçimi
- Material 3 və Jetpack Compose ilə hazırlanmış interfeys

### Azan səsləri

Hazırkı versiyada aşağıdakı seçimlər mövcuddur:

- Standart Şiə Azanı — Sübh üçün ayrıca audio ilə
- Azan - Ali Fani
- Azan - Rəhim Müəzzinzadə
- Azan - Teymur Şirvanlı

Seçim siyahısında hər azanın qarşısındakı oxut düyməsi ilə seçmədən öncə dinləmək olar. Seçilmiş səs həm tətbiq daxilində əl ilə oxudularkən, həm də namaz vaxtında arxa fonda başladılan azan üçün istifadə olunur.

### Quraşdırma və build

Tələblər:

- Android Studio
- Android SDK 34
- JDK 8 və ya daha yeni uyğun JDK
- Minimum cihaz versiyası: Android 7.0 (API 24)

Repozitoriyanı klonladıqdan sonra layihəni Android Studio-da açıb Gradle Sync edin. Terminaldan debug APK yaratmaq üçün:

```powershell
.\gradlew.bat assembleDebug
```

Linux və macOS üçün:

```bash
./gradlew assembleDebug
```

Hazır APK bu qovluqda yaranır:

```text
app/build/outputs/apk/debug/app-debug.apk
```

### İcazələr

Tətbiq funksiyalarına uyğun olaraq bu icazələrdən istifadə edir:

- dəqiq və təqribi məkan — namaz vaxtlarını cari mövqeyə görə hesablamaq üçün;
- bildirişlər — azan və daimi namaz məlumatını göstərmək üçün;
- exact alarm — azanı mümkün qədər dəqiq vaxtda başlatmaq üçün;
- foreground service və wake lock — ekran bağlı olduqda audio və bildiriş axınını davam etdirmək üçün.

Məkan seçimi cihazda saxlanılır və tətbiqin namaz vaxtlarını hesablamaq üçün istifadə olunur.

### Texniki quruluş

- Kotlin
- Jetpack Compose və Material 3
- ViewModel, StateFlow və Coroutines
- DataStore Preferences
- AlarmManager və BroadcastReceiver
- Foreground Services və MediaPlayer
- Google Play Services Location

Əsas paketlər:

```text
app/src/main/java/az/shia/azan/
├── audio/          # Azan playback
├── calculator/     # Astronomik namaz vaxtı hesablamaları
├── data/           # Modellər və saxlanılan parametrlər
├── location/       # GPS və məkan aşkarlanması
├── notification/   # Alarm və bildiriş axını
├── service/        # Arxa fon və foreground servislər
├── ui/             # Compose ekranları, komponentlər və tema
├── utils/          # Vaxt formatlaşdırması və köməkçi funksiyalar
└── viewmodel/      # UI state və tətbiq məntiqi
```

### Yeniləmə necə işləyir

Tətbiq GitHub buraxılışlarını periodik yoxlayır. Yeni versiya olduqda APK tətbiqin özəl qovluğuna endirilir, paket adı, versiya və imza sertifikatı yoxlanır, sonra quraşdırma bildirişi göstərilir. Avtomatik quraşdırma alınmasa (məsələn, bildiriş icazəsi bağlıdırsa), bildiriş və Parametrlər bölməsi GitHub buraxılış səhifəsinə yönləndirir; oradan APK-ni əl ilə endirib köhnə versiyanın üzərinə quraşdırmaq olar.

### Müəllif və əlaqə

XIV Azan **DeXIV** tərəfindən hazırlanır və inkişaf etdirilir.

Telegram: [t.me/De_XIV](https://t.me/De_XIV)

---

## English

### About the app

XIV Azan is a native Android app that calculates daily prayer times astronomically from the selected coordinates, date, time zone, and Shia calculation method. Its purpose is straightforward: show the next prayer and the remaining time clearly, then play the selected azan when prayer time arrives.

The app does not rely on a fixed timetable. Prayer times are recalculated when the date, location, or calculation method changes. The next-prayer state is kept up to date inside the app, in the notification shade, and on the lock screen.

### Main features

- Fajr, Sunrise, Dhuhr, Asr, Maghrib, and Isha calculations
- Qum (Leva Institute) and Tehran calculation methods
- Live next-prayer countdown
- Automatic transition to tomorrow's Fajr after Isha
- Precise GPS-based location detection
- Azerbaijani cities and districts, plus selected international Shia centers
- Exact alarms and prayer-time notifications
- Persistent prayer information in the status area and on the lock screen
- Gregorian and Hijri (lunar) dates shown side by side on the home screen
- Hijri date adjustable within −7…+7 days for local moon sighting
- Automatic update checks from GitHub releases with verified APK installation
- Background azan playback
- Selectable azan voice and volume
- Material 3 interface built with Jetpack Compose

### Included azan voices

The current version includes:

- Default Shia Azan — with a dedicated Fajr recording
- Azan - Ali Fani
- Azan - Rəhim Müəzzinzadə
- Azan - Teymur Şirvanlı

The selected voice is used for both manual playback inside the app and automatic background playback at prayer time.

### Build instructions

Requirements:

- Android Studio
- Android SDK 34
- JDK 8 or a newer compatible JDK
- Minimum Android version: Android 7.0 (API 24)

After cloning the repository, open it in Android Studio and run Gradle Sync. To build a debug APK from a Windows terminal:

```powershell
.\gradlew.bat assembleDebug
```

On Linux or macOS:

```bash
./gradlew assembleDebug
```

The generated APK is available at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

### Permissions

The app uses permissions only for its core functionality:

- precise and approximate location to calculate prayer times for the current position;
- notifications to display azan alerts and persistent prayer information;
- exact alarms to trigger the azan as close to prayer time as possible;
- foreground service and wake lock access to keep playback and notifications working while the screen is off.

The selected location is stored on the device and used for prayer-time calculations.

### Technology

- Kotlin
- Jetpack Compose and Material 3
- ViewModel, StateFlow, and Coroutines
- DataStore Preferences
- AlarmManager and BroadcastReceiver
- Foreground Services and MediaPlayer
- Google Play Services Location

### Author and contact

XIV Azan is created and maintained by **DeXIV**.

Telegram: [t.me/De_XIV](https://t.me/De_XIV)
