package id.app.rplayer.data.media

/** Lagu dari pustaka perangkat pengguna. Bukan katalog layanan mana pun. */
data class DeviceSong(
    val uri: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val artUri: String? = null,
) {
    val displayTitle: String get() = title.ifBlank { uri.substringAfterLast('/') }
    val displayArtist: String get() = artist.ifBlank { "Artis tidak diketahui" }
}
