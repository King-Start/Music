# Architecture: replica Phase 1 (Android)

Status: keputusan implementasi untuk fase 1 dari `recon.md`. Fase 2-4 belum dibangun.

## Stack

- Kotlin 2.0 + Jetpack Compose + Material 3
- AndroidX Navigation Compose
- Media3 ExoPlayer + MediaSessionService (background playback & notifikasi)
- Room (playlist, lagu playlist, favorit)
- Kotlin Coroutines + Flow
- Manual DI via `AppContainer` (tanpa Hilt, supaya build tanpa kapt/hilt tooling)
- Sumber audio: **MediaStore perangkat user saja**. Tidak ada katalog/stream jaringan.

## Alur data

```
MediaStore (audio lokal) --MediaScanner--> DeviceSong (Flow)
Room (Playlist, PlaylistSong, Favorite) --DAO--> Flow
LibraryRepository menggabungkan keduanya
        |
        v
ViewModel (StateFlow) --> Compose UI
        |
        v
PlayerController --> MediaController --> PlaybackService (ExoPlayer + MediaSession)
```

## Entitas (Room)

- `PlaylistEntity(id, name, createdAt)`
- `PlaylistSongEntity(playlistId, songUri, position, title, artist, album, durationMs, artUri)` — snapshot metadata, PK `(playlistId, songUri)`
- `FavoriteEntity(songUri, title, artist, album, durationMs, artUri)` — PK `songUri`

Model non-Room: `DeviceSong(uri, title, artist, album, durationMs, artUri)`.

## Layar fase 1

| ID | layar | status |
| --- | --- | --- |
| S01 | Home | dibangun |
| S02 | Search | dibangun |
| S03 | Library | dibangun |
| S04 | Playlist detail | dibangun |
| S07 | Player | dibangun |
| S08 | Queue sheet | dibangun |
| S12 | Settings (minimal) | dibangun |
| S19 | Song menu | dibangun sebagai overflow |
| S20 | Mini player + notifikasi | dibangun |
| S05, S06, S09-S11, S13-S18, S21-S23 | | belum (fase 2-4) |

## Keputusan penting

- Queue disimpan in-memory di `MediaController` (Fase 1). Persistensi queue masuk Fase 2.
- Tidak ada login, sinkronisasi, lirik, cast, atau EQ di Fase 1.
- Nama aplikasi dan package bersifat **placeholder** (`RPlayer`, `id.app.rplayer`) dan akan diganti oleh `replica-brand`.
- Semua warna/ukuran berasal dari `replica/design/tokens.json` dan `ui/theme/Tokens.kt`.
