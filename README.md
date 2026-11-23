# Lumora

Pemutar musik Android (Kotlin + Jetpack Compose) bergaya liquid glass. Tema bisa diganti dan warna tampilan berubah mengikuti irama musik.

## Build APK lewat GitHub
1. Upload seluruh isi folder ini ke repositori GitHub (pastikan folder `.github/workflows/build.yml` ikut).
2. Buka tab **Actions**, workflow **Build APK** berjalan otomatis setiap push (bisa juga dijalankan manual lewat *Run workflow*).
3. Setelah selesai (sekitar 5 menit), buka run tersebut lalu unduh artifact **Lumora-APK**.
4. Untuk APK langsung di halaman **Releases**, buat tag, contoh: `git tag v1.0 && git push origin v1.0`.

APK yang dihasilkan adalah debug build yang sudah bertanda tangan debug, jadi langsung bisa dipasang.

## Build lokal
Buka di Android Studio (Hedgehog atau lebih baru), JDK 17, Gradle 8.7, lalu Run.
