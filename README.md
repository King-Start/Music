# Replica recon: Auralys / Metrolist (Android)

Hasil tahap **`replica-recon`** dari skill [replica-skill](https://github.com/Jakeschincariol/replica-skill).

Target: aplikasi Android `com.auralyst.app` (Auralys 4.3.5), direncanakan sebagai produk baru dengan kode dan identitas sendiri.

## Isi

- [`replica/recon.md`](replica/recon.md) — peta recon: sumber publik, 23 layar, 10 flow, 12 entitas data, komponen UI, batasan hukum, dan rekomendasi fase.
- [`replica/features.csv`](replica/features.csv) — matriks 39 fitur (must 9, should 14, could 7, skip 9) untuk dipakai skill `replica-diff`.

## Status

Fase 1 sudah ditulis: proyek Android Kotlin + Jetpack Compose (Fase 1 dari `recon.md`).

- Modul `app/` — Kotlin 2.0, Compose + Material 3, Navigation Compose, Media3 ExoPlayer/MediaSessionService, Room.
- Fitur: pustaka lokal (MediaStore), pencarian, playlist, favorit, player dengan antrean, mini player, notifikasi, mode guest.
- Detail dan yang belum selesai: [`replica/build-log.md`](replica/build-log.md).
- Arsitektur: [`replica/architecture.md`](replica/architecture.md). Token: [`replica/design/tokens.json`](replica/design/tokens.json).

Belum bisa dikompilasi di environment ini (tidak ada JDK/Gradle). Buka di Android Studio dengan Gradle 8.9.

APK asli sengaja **tidak** disimpan di repo ini: tidak ada kode, endpoint privat, aset, atau string yang disalin dari binary. Semua temuan berasal dari dokumentasi dan situs publik.

## Langkah berikutnya

1. Build & jalankan di Android Studio, lalu perbaiki masalah kompilasi jika ada.
2. Fase 2: drag reorder, EQ, sleep timer, download in-app, cache.
3. Lanjut pipeline: test → diff → entrepreneur → brand → launch → deploy.

## Catatan

Auralys adalah rebrand dari Metrolist (GPL-3.0). Produk komersial wajib ditulis ulang clean-room, direbrand, dan diperiksa aspek ToS/merek sebelum dirilis. Ini bukan nasihat hukum.
