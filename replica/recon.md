# Recon map: Auralys / Metrolist (Android)

Scope: seluruh fitur yang terlihat pada Auralys 4.3.5 (`com.auralyst.app`), dibangun ulang sebagai aplikasi Android dengan kode dan identitas baru.
For: produk milik sendiri yang akan dijual/(monetisasi), bukan fork resmi Auralys maupun Metrolist.
Date: 2026-11-23

## Catatan identitas (penting)

- APK yang diberikan (`Auralys-4.3.5.apk`) adalah **rebrand dari Metrolist**, klien YouTube Music GPL-3.0. Temuan publik: [issue Metrolist #5149](https://github.com/MetrolistGroup/Metrolist/issues/5149) menyebutkan Auralys memakai komponen `com.metrolist.music.*` tanpa sumber terbuka.
- Situs [`auralys.net`](https://auralys.net/) menyebut Auralys sebagai "pemutar offline lokal", tetapi situs resmi distributor [`auralys.my.id`](https://auralys.my.id/) menyebutnya aplikasi musik dengan fitur *Listen Together* real-time. Keduanya tidak konsisten; recon ini memakai dokumentasi Metrolist sebagai sumber fitur yang paling lengkap.
- APK **tidak didekompilasi**. Tidak ada kode, endpoint privat, aset, atau string dari binary yang dipakai. Ini sesuai aturan clean-room skill.

## Sources

| # | source | URL | notes |
| --- | --- | --- | --- |
| 1 | README resmi Metrolist | https://github.com/MetrolistGroup/Metrolist | Daftar fitur, platform, lisensi GPL-3.0, disclaimer YouTube ToS |
| 2 | Situs resmi Metrolist | https://metrolist.cc/ | Showcase Home/Library/Player, FAQ, DAFTAR fitur, screenshot publik |
| 3 | Deskripsi Play Store (fastlane) | https://raw.githubusercontent.com/MetrolistGroup/Metrolist/main/fastlane/metadata/android/en-US/full_description.txt | Ringkasan fitur Android |
| 4 | Docs: playback | https://raw.githubusercontent.com/MetrolistGroup/Metrolist/main/docs/playback.md | Sleep timer, auto-skip error, crossfade/gapless, SponsorBlock, video mode |
| 5 | Docs: media menus | https://raw.githubusercontent.com/MetrolistGroup/Metrolist/main/docs/media-menus.md | Menu lagu/playlist/queue, selection mode, header album/playlist, MetroEQ |
| 6 | Docs: audio processing | https://raw.githubusercontent.com/MetrolistGroup/Metrolist/main/docs/audio-processing.md | EQ, preamp, tempo/pitch, meter, silence skip |
| 7 | Docs: audio cache | https://raw.githubusercontent.com/MetrolistGroup/Metrolist/main/docs/audio-cache.md | Download, pre-cache, penyimpanan, format ekspor |
| 8 | Docs: Listen Together | https://raw.githubusercontent.com/MetrolistGroup/Metrolist/main/docs/listen-together.md | Ruang bersama, host/guest, undangan, sinkronisasi |
| 9 | Docs: Device Connect | https://raw.githubusercontent.com/MetrolistGroup/Metrolist/main/docs/device-connect.md | Pairing, transfer playback, cast, direct IP |
| 10 | Situs distributor Auralys | https://auralys.my.id/ | Dua tema (Editorial, Liquid Glass), fokus Listen Together, Android + Windows, gratis + donasi QRIS |
| 11 | Situs APK Auralys | https://auralys.net/ | Klaim pemutar offline, EQ, playlist, visualizer (inkonsisten dengan #10) |
| 12 | Issue lisensi Metrolist #5149 | https://github.com/MetrolistGroup/Metrolist/issues/5149 | Status fork/rebrand dan kewajiban GPL-3.0 |
| 13 | Changelog Metrolist | https://raw.githubusercontent.com/MetrolistGroup/Metrolist/main/docs/changelog.md | Riwayat fitur terbaru |

Screenshot referensi tidak berhasil diunduh (situs memblokir hotlink, browser panel tidak merespons). `replica/screens/` sengaja kosong; jangan pernah memasukkan screenshot/aset orisinal ke produk.

## Core loop

Buka aplikasi → temukan lagu (Home/Search/Library) → putar tanpa iklan, di latar belakang atau offline → atur antrean, EQ, dan opsi dengar → simpan ke playlist/favorit atau dengar bareng teman.

## Screens

| ID | screen | route / how to reach | purpose | key components | states seen |
| --- | --- | --- | --- | --- | --- |
| S01 | Home | tab Home | Quick picks, riwayat, radio, pin Speed Dial | shelf card, chip feed, pin | kosong (guest), terisi, loading |
| S02 | Search | tab Search | Cari lagu/album/artist/playlist/video | kolom search, tab hasil, daftar hasil | kosong, hasil, error |
| S03 | Library | tab Library | Koleksi pengguna dan auto-koleksi | daftar playlist, filter, Speed Dial | kosong, terisi, guest |
| S04 | Playlist detail | Library → playlist | Putar/kelola satu playlist | header cover, ButtonGroup Shuffle/Play all/Like, song row, sort/search, FAB Add song | kosong, terisi, selection, remote/local |
| S05 | Album detail | hasil/Album → album | Lihat dan putar album | header, daftar track, Like | terisi, loading |
| S06 | Artist detail | hasil/Artist | Shelves lagu, video, album, playlist, related | shelf, tombol putar | terisi |
| S07 | Now Playing / Player | tap mini player | Kontrol pemutaran utama | artwork, progress, transport, queue, lyrics, EQ, device picker, video | play/pause, buffering, error, video |
| S08 | Queue sheet | Player → Queue | Lihat dan ubah antrean | song row occurrence, drag reorder, remove, shuffle/repeat, sleep timer | kosong, terisi, selection |
| S09 | Lyrics | Player → Lyrics | Lirik sinkron + terjemahan/romanisasi | baris lirik, offset, share card | ada lirik, tanpa lirik, translating |
| S10 | MetroEQ | Player → EQ | Equalizer, profil, tempo & pitch | grafik band, slider, preamp, profil, AutoEq/import, meter | profil aktif/flat, editor |
| S11 | Sleep timer dialog | Player/Queue → Sleep timer | Stop setelah durasi/finish song | input durasi, toggle fade & finish song | aktif, tidak aktif |
| S12 | Settings | tab/profil → Settings | Preferensi aplikasi | grup tampilan, audio, integrasi, bahasa, about | default, tersimpan |
| S13 | Integrations | Settings → Integrations | Listen Together, Device Connect, scrobble, Discord | switch, akun, server | on/off, perlu login |
| S14 | Listen Together room | Integrations → Create/Join, invite link | Dengar sinkron bareng | kode ruang, host/guest, approval, suggestion | kosong, host, guest, reconnect |
| S15 | Device picker | Player → device icon | Pilih target playback | perangkat paired, discovery, direct IP, cast | tidak ada perangkat, connected, pairing |
| S16 | Sign in / Account | Settings → Sign in | Masuk dengan akun YouTube Music; guest didukung | WebView login, status akun | guest, signed in, error |
| S17 | Downloads / Offline | Library → Downloaded/Cached; Settings → Storage | Unduh dan kelola audio offline | daftar unduhan, progress, ukuran cache, clear | queue, completed, failed, offline |
| S18 | Import backup | Settings → Import | Import playlist dari klien/layanan lain | pemilih sumber, konfirmasi, progress | idle, importing, selesai |
| S19 | Song details / menu sheet | overflow pada song row | Detail, add to playlist, share, dislike, block, radio, delete upload | sheet/menu, metadata, konfirmasi | tersedia, unavailable, busy |
| S20 | Mini player + notification | muncul setelah play | Kontrol dari layar lain | mini player, media session notification | collapsed, expanded |
| S21 | Video playback | Player → video mode | Putar video Musik | video surface, kontrol, resolusi | audio-only, video, recovery |
| S22 | Music recognition | menu/player → Shazam | Kenali lagu | tombol recognize, hasil | idle, recognizing, result |
| S23 | Song selection mode | long-press di album/playlist/history | Aksi massal | SelectionTopBar, Select all, shared action sheet | 0 selected, n selected |

## Flows

```
F01 Putar lagu dari Home/Search
    S01/S02 -> S07 -> S20
    happy path clicks: 3
    edge: hasil kosong, lagu unavailable, guest tanpa login, error stream -> retry/auto-skip

F02 Putar offline
    S03/S04 -> S17 -> S07
    happy path clicks: 4
    edge: izin penyimpanan ditolak, download gagal, cache dihapus saat diputar, storage penuh

F03 Buat dan atur playlist
    S03 -> S04 -> S19 -> S04
    happy path clicks: 5
    edge: duplikat lagu, playlist remote tidak bisa diedit, drag reorder gagal

F04 Masuk / mode guest
    S16 -> S01 -> S03
    happy path clicks: 4
    edge: WebView gagal, akun tanpa ID resolvable, guest tidak bisa radio/start room

F05 Dengar bareng (Listen Together)
    S13 -> S14 -> invite link -> S14 -> S07
    happy path clicks: 6
    edge: kode salah, host keluar, guest disconnect, queue konflik, latency/drift

F06 Transfer ke perangkat/cast
    S15 -> S07
    happy path clicks: 4
    edge: pairing ditolak, perangkat offline, cast gagal, public IP diblokir

F07 Atur suara
    S07 -> S10 -> S11
    happy path clicks: 4
    edge: profil hilang, AutoEq gagal, offload tidak mendukung efek, pitch di HLS tidak tersedia

F08 Lirik + bagikan
    S07 -> S09 -> share sheet
    happy path clicks: 4
    edge: tidak ada lirik, terjemahan gagal, lagu lokal tanpa link

F09 Impor backup
    S18 -> S03
    happy path clicks: 5
    edge: format tidak dikenal, sebagian playlist gagal, duplikat

F10 Mode video
    S07 -> S21 -> S07
    happy path clicks: 3
    edge: pergantian audio/video gagal, seek saat transisi, video unavailable
```

## Components

| component | variants | states | used on |
| --- | --- | --- | --- |
| Adaptive nav (Home/Library/Search/Player) | phone, tablet, two-pane | selected, unselected | S01-S03, S07 |
| Song row | standalone, queue occurrence, selection | default, playing, explicit badge, unavailable, busy | S02-S05, S08, S18, S19 |
| Media card / shelf | album, artist, playlist, radio | empty, loading, filled | S01, S03, S06 |
| Mini player | collapsed, expanded | playing, paused, buffering, error | S01-S06, S20 |
| Player sheet | compact, full, landscape/video | play, buffer, error, no lyrics | S07, S21 |
| ButtonGroup | Shuffle / Play all / Like | enabled, disabled (empty list), loading | S04, S05 |
| Sort + search row | ascending/descending, manual | idle, filtering | S04, S08 |
| SelectionTopBar | 0 selected, n selected | close, select all, more | S04, S08, S23 |
| Menu sheet / dropdown | song, queue, player, collection | available vs hidden callback, grouped rows | S04-S09, S19 |
| FAB Add song | expanded, collapsed | visible, hidden on scroll | S04 |
| Chips | radio/feed chip | selected, default, restored | S01, S07 |
| Dialog | sleep timer, confirm delete, download | open, active, cancelled | S08, S11, S19 |
| Lyrics view | plain, translated, romanized | synced, missing, offset | S09 |
| EQ graph + sliders | profile, custom, AutoEq | active, flat, disabled band | S10 |
| Toggle / switch | integrations, settings | on, off, pending | S12, S13 |
| Toast / snackbar | info, error | shown, dismissed | semua |

## Inferred data model

```
Song  id, title, artists[], albumId, durationMs, artworkUrl, explicit, source(remote|local|download), liked
      evidence: S02/S04/S07/S19, docs/media-menus.md, docs/audio-cache.md
      confidence: high

Album  id, title, artists[], year, description, otherEditions[], artworkUrl
       evidence: S05, docs/audio-cache.md (deskripsi + edisi tersimpan)
       confidence: high

Artist  id, name, artworkUrl
        evidence: S06, docs/media-menus.md (shelf related artists)
        confidence: high

Playlist  id, name, owner, editable, songIds[], sort, hiddenLocally, cover
          evidence: S03/S04, docs/media-menus.md (playlist menu, hiding, sort)
          confidence: high

AutoCollection  kind(liked|downloaded|cached|top100|uploaded|history|newEpisodes|forLater), songIds[]
                evidence: docs/media-menus.md (built-in collections)
                confidence: high

Queue  queueId, occurrences[{songId, occurrenceId}], currentIndex, title, repeatMode, shuffle, radioIntent
       evidence: S08, docs/playback.md, docs/device-connect.md
       confidence: high

DownloadedAudio / CacheEntry  songId, state, path, bytes, completedAt, source
                              evidence: S17, docs/audio-cache.md
                              confidence: high

EQProfile  id, name, bands[{freqHz, gainDb}], preampDb, source(flat|preset|autoEq|import)
           evidence: S10, docs/audio-processing.md
           confidence: high

Account  id, displayName, signedIn, hiddenLibrarySetting
         evidence: S16, docs/device-connect.md (identity)
         confidence: medium

Room  code, host, guests[], queueSnapshot, trackRevision, capabilities, moderation
      evidence: S14, docs/listen-together.md
      confidence: high

PairedDevice  id, name, endpoint, capabilities, trusted, default
              evidence: S15, docs/device-connect.md
              confidence: high

History  playedAt, songId, source, scrobbleSession
         evidence: docs/media-menus.md (weekly/monthly history), docs/device-connect.md
         confidence: medium
```

Relationships: Song n-1 Album, Song n-m Artist, Playlist 1-n Song (occurrence), Queue 1-n Song (occurrence), Song 1-0..1 DownloadedAudio, EQProfile 1-n settings, Room 1-n Guest + 1 Queue, Account 1-n Playlist/History.

## Feature matrix

See `features.csv`. Must: 9, should: 14, could: 7, skip: 9.

## Out of scope (cannot or should not be cloned)

- **Katalog YouTube Music, akun Google, stream Premium, DRM, dan endpoint privat (InnerTube/SABR).** Tidak ada API publik untuk mengambil katalog/stream; situs resmi sendiri memperingatkan risiko ToS. Clone harus memakai sumber musik yang user miliki atau API resmi berlisensi.
- **Konten pihak ketiga**: lirik, artwork/Canvas, metadata Shazam, data SponsorBlock (CC BY-NC-SA 4.0 = non-komersial), dan recommendation feed. Tidak bisa dibundling untuk produk berbayar tanpa lisensi.
- **Identitas Auralys/Metrolist**: nama, logo, ikon, tema "Editorial/Liquid Glass", copy, screenshot, fonts, dan kode sumber. Wajib diganti sebelum rilis (lihat `replica-brand` + `sweep.py`).
- **Kode source Metrolist/Auralys**: GPL-3.0. Menyalin/mengadaptasi kode akan membuat produk wajib open-source sesuai GPL. Clean-room: tulis ulang dari perilaku, bukan kode.
- **Server dan jaringan milik orang lain**: metroserver (Listen Together), layanan scrobble, layanan lyrics, dan komunitas pengguna tidak bisa diwariskan. Boleh membangun server sendiri jika ada sumber daya.
- **Platform lain** (iOS, desktop Windows/macOS/Linux): di luar keputusan scope Android.
- **Kepastian hukum**: ini bukan nasihat hukum. Sebelum menjual: cek merek/domain, baca ToS penyedia musik, dan konsultasi hukum.

## Size

Screens 23, flows 10, entities 12. Hard parts: (1) sumber musik/stream tanpa API privat, (2) sinkronisasi Listen Together + Device Connect, (3) DSP + offline cache yang andal, (4) integrasi pihak ketiga (cast, scrobble, recognition), (5) rebrand + kepatuhan lisensi untuk produk komersial.

Size: **XL (rescope disarankan)**. Permintaan "semua fitur" tidak realistis untuk satu rilis. Rekomendasi fase:

1. **Fase 1 — inti (M)**: S01-S05, S07, S08, S19, S20 + F01-F03 dengan sumber musik lokal/user-owned.
2. **Fase 2 — suara & offline (M)**: S10, S11, S17 + F02, F07.
3. **Fase 3 — akun, lirik, integrasi ringan (L)**: S09, S12, S13, S16, S18 + F04, F08, F09.
4. **Fase 4 — social & perangkat (L)**: S14, S15, S21, S22 + F05, F06, F10 (butuh server sendiri).
5. **Fase 5 — brand & launch**: `replica-brand` → `replica-launch` → `replica-deploy`.

## Next step

`/replica-architect` — setelah scope fase diputuskan. Belum ada kode yang ditulis; ini baru peta recon.
