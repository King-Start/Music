# Build log — Fase 1 (Android Kotlin/Compose)

Tanggal: 2026-11-23. Commit awal: `build: Phase 1 shell and core loop`.

| Screen | status | missing / catatan |
| --- | --- | --- |
| S01 Home | done | permission audio, empty/loading/filled; artwork masih placeholder ikon |
| S02 Search | done | filter lokal saja |
| S03 Library | done | tab playlist/lagu/favorit, buat & hapus playlist |
| S04 Playlist detail | partial | drag reorder belum; add-song via dialog |
| S07 Player | done | progress, transport, shuffle/repeat, queue sheet; EQ & lirik belum |
| S08 Queue sheet | done | play/remove; remove lagu aktif sengaja dinonaktifkan |
| S12 Settings | partial | hanya rescan + about; integrasi belum |
| S19 Song menu | partial | play next + favorit; add-to-playlist picker belum di screen ini |
| S20 Mini player + notifikasi | done | MediaSessionService + MiniPlayer |

Must-have: 9 dari 9 tersentuh (6 yes, 3 partial). Screens Phase 1: 9 dari 23.

## Yang lebih sulit dari perkiraan

- `MediaController` terhubung asynchronously ke `MediaSessionService`; state player perlu listener eksplisit supaya Compose tidak stale.
- Reorder queue butuh reconstruct seluruh `MediaItem` list; sengaja ditunda ke Fase 2.
- Tanpa JDK/Gradle di environment ini, project **belum bisa dikompilasi di sini**. Perlu dibuka di Android Studio (Gradle 8.9, AGP 8.7.3, Kotlin 2.0.21). Juga belum ada `gradlew`/`gradle-wrapper.jar`; Android Studio akan membuatnya, atau jalankan `gradle wrapper` setelah Gradle terpasang.

## Belum ada

Screenshot clone untuk `replica-diff`, unit test, EQ, lirik, sleep timer, download in-app, login, Listen Together, Device Connect, cast, brand final.
