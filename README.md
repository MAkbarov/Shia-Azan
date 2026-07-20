# 🕌 Şiə Azan - Android Tətbiqi

Azərbaycan dili dəstəyi ilə Şiə məzhəbi üçün namaz vaxtları və azan tətbiqi.

## ✨ Xüsusiyyətlər

### 📿 Namaz Vaxtları
- **Şiə hesablama metodologiyası** (Fajr: 16°, Isha: 14°)
- Astronomik hesablamalar (Julian tarixi, günəş deklinasiyası)
- 6 namaz vaxtı: Sübh, Günəş, Zöhr, Əsr, Məğrib, İşa
- Növbəti namaz xatırlatması və geri sayım
- Gündəlik namaz cədvəli

### 🎵 Azan Səsləri
- **Şiə azan mətni** ilə audio
- İki fərqli azan: Standart və Sübh azanı
- Play/Pause/Stop funksiyaları
- Background audio oxutma
- Gözəl player interfeysi

### 🔔 Bildirişlər
- Avtomatik namaz vaxtı bildirişləri
- Dəqiq vaxtda alarm sistemi
- Bildirişdən birbaşa azan oxutma
- Xatırlatma funksiyası

### 📍 Yer Seçimi
- **GPS avtomatik yer təyini**
- Manual şəhər seçimi
- Azərbaycan şəhərləri siyahısı:
  - Bakı
  - Sumqayıt
  - Gəncə
  - Lənkəran
  - Quba
  - Şamaxı
  - Şuşa
- Axtarış funksiyası

### 🎨 İnterfeys
- **Material Design 3**
- Jetpack Compose
- Yaşıl-qızılı rəng sxemi
- Azərbaycan dili
- Modern və intuitiv dizayn

## 🛠 Texnologiyalar

- **Kotlin** - Proqramlaşdırma dili
- **Jetpack Compose** - UI framework
- **Material Design 3** - Dizayn sistemi
- **Coroutines & Flow** - Async əməliyyatlar
- **ViewModel** - State management
- **AlarmManager** - Dəqiq alarmlar
- **MediaPlayer** - Audio oxutma
- **Google Play Services Location** - GPS
- **Notification Channels** - Bildirişlər

## 📱 Minimum Tələblər

- Android 7.0 (API 24) və yuxarı
- GPS (yer təyini üçün)
- İnternet (ilk yükləmə üçün)

## 🚀 Quraşdırma

1. **Android Studio-da açın**
2. **Gradle Sync edin**
3. **Audio faylları əlavə edin:**
   - `/app/src/main/res/raw/azan_default.mp3`
   - `/app/src/main/res/raw/azan_fajr.mp3`
4. **Build edin və Run edin**

## 📂 Layihə Strukturu

```
ShiaAzanApp/
├── app/
│   ├── src/main/
│   │   ├── java/az/shia/azan/
│   │   │   ├── audio/           # Audio player
│   │   │   ├── calculator/      # Namaz vaxtı hesablamaları
│   │   │   ├── data/            # Data classlar
│   │   │   ├── location/        # GPS və yer
│   │   │   ├── notification/    # Bildiriş sistemi
│   │   │   ├── service/         # Background servislər
│   │   │   ├── ui/              # UI komponentləri
│   │   │   │   ├── components/
│   │   │   │   ├── screens/
│   │   │   │   └── theme/
│   │   │   ├── utils/           # Utility funksiyalar
│   │   │   ├── viewmodel/       # ViewModels
│   │   │   └── MainActivity.kt
│   │   └── res/                 # Resources
│   └── build.gradle.kts
└── README.md
```

## 🎯 İstifadə

1. **İlk açılış:** GPS və bildiriş icazələri verin
2. **Şəhər seçin:** GPS və ya manual seçim
3. **Namaz vaxtlarını görün:** Ana səhifədə bütün vaxtlar
4. **Azan dinləyin:** Play düyməsinə toxunun
5. **Bildirişləri alın:** Avtomatik namaz vaxtında

## 📝 Audio Faylları Haqqında

Şiə azan səslərini əldə etmək üçün:
- Şiə azan MP3 fayllarını yükləyin
- Faylları `/app/src/main/res/raw/` qovluğuna əlavə edin
- Fayl adları: `azan_default.mp3` və `azan_fajr.mp3`

### Şiə Azan Mətni:
```
الله أكبر (4 dəfə)
أشهد أن لا إله إلا الله (2 dəfə)
أشهد أن محمدا رسول الله (2 dəfə)
حي على الصلاة (2 dəfə)
حي على الفلاح (2 dəfə)
حي على خير العمل (2 dəfə) - Şiə xüsusiyyəti
الله أكبر (2 dəfə)
لا إله إلا الله (2 dəfə)
```

## 🔒 İcazələr

- `ACCESS_FINE_LOCATION` - GPS koordinatları
- `ACCESS_COARSE_LOCATION` - Təqribi yer
- `POST_NOTIFICATIONS` - Bildirişlər (Android 13+)
- `SCHEDULE_EXACT_ALARM` - Dəqiq alarmlar
- `WAKE_LOCK` - Ekran bağlı olduqda işləmə
- `INTERNET` - İlk setup üçün

## 👨‍💻 Developer

Kiro AI Assistant tərəfindən hazırlanmışdır.

## 📄 Lisenziya

Bu tətbiq açıq mənbə olaraq təqdim edilir.

---

**Not:** Bu tətbiq Şiə məzhəbinə uyğun namaz vaxtları hesablayır. Digər məzhəblər üçün hesablama parametrləri fərqlidir.
