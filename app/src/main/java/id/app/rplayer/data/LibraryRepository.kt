package id.app.rplayer.data

import id.app.rplayer.data.db.FavoriteEntity
import id.app.rplayer.data.db.LibraryDao
import id.app.rplayer.data.db.PlaylistEntity
import id.app.rplayer.data.db.PlaylistSongEntity
import id.app.rplayer.data.media.DeviceSong
import id.app.rplayer.data.media.MediaScanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class LibraryRepository(
    private val scanner: MediaScanner,
    private val dao: LibraryDao,
) {
    private val _songs = MutableStateFlow<List<DeviceSong>>(emptyList())
    val songs: StateFlow<List<DeviceSong>> = _songs.asStateFlow()

    val playlists: Flow<List<PlaylistEntity>> = dao.observePlaylists()
    val favorites: Flow<List<DeviceSong>> = dao.observeFavorites().map { list -> list.map { it.toSong() } }

    suspend fun refresh() {
        _songs.value = withContext(Dispatchers.IO) { scanner.readSongs() }
    }

    fun search(query: String): List<DeviceSong> {
        val q = query.trim()
        if (q.isBlank()) return emptyList()
        return songs.value.filter { song ->
            listOf(song.displayTitle, song.displayArtist, song.album).any { it.contains(q, ignoreCase = true) }
        }
    }

    fun playlistSongs(playlistId: Long): Flow<List<DeviceSong>> =
        dao.observePlaylistSongs(playlistId).map { list -> list.map { it.toSong() } }

    fun isFavorite(songUri: String): Flow<Boolean> = dao.observeIsFavorite(songUri)

    suspend fun createPlaylist(name: String): Long = withContext(Dispatchers.IO) {
        dao.insertPlaylist(PlaylistEntity(name = name.trim(), createdAt = System.currentTimeMillis()))
    }

    suspend fun deletePlaylist(id: Long) = withContext(Dispatchers.IO) { dao.deletePlaylist(id) }

    suspend fun addToPlaylist(playlistId: Long, song: DeviceSong) = withContext(Dispatchers.IO) {
        dao.insertPlaylistSong(
            PlaylistSongEntity(
                playlistId = playlistId,
                songUri = song.uri,
                position = dao.nextPosition(playlistId),
                title = song.displayTitle,
                artist = song.displayArtist,
                album = song.album,
                durationMs = song.durationMs,
                artUri = song.artUri,
            ),
        )
    }

    suspend fun removeFromPlaylist(playlistId: Long, songUri: String) = withContext(Dispatchers.IO) {
        dao.deletePlaylistSong(playlistId, songUri)
    }

    suspend fun toggleFavorite(song: DeviceSong) = withContext(Dispatchers.IO) {
        if (dao.isFavoriteOnce(song.uri)) {
            dao.deleteFavorite(song.uri)
        } else {
            dao.insertFavorite(
                FavoriteEntity(
                    songUri = song.uri,
                    title = song.displayTitle,
                    artist = song.displayArtist,
                    album = song.album,
                    durationMs = song.durationMs,
                    artUri = song.artUri,
                ),
            )
        }
    }

    /** Dipakai UI untuk menandai ikon favorit tanpa mengubah database. */
    fun favoriteUris(favorites: List<DeviceSong>): Set<String> = favorites.map { it.uri }.toSet()

    private fun PlaylistSongEntity.toSong() = DeviceSong(
        uri = songUri,
        title = title,
        artist = artist,
        album = album,
        durationMs = durationMs,
        artUri = artUri,
    )

    private fun FavoriteEntity.toSong() = DeviceSong(
        uri = songUri,
        title = title,
        artist = artist,
        album = album,
        durationMs = durationMs,
        artUri = artUri,
    )
}
