package id.app.rplayer.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long,
)

@Entity(tableName = "playlist_songs", primaryKeys = ["playlistId", "songUri"])
data class PlaylistSongEntity(
    val playlistId: Long,
    val songUri: String,
    val position: Int,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val artUri: String?,
)

@Entity(tableName = "favorites", primaryKeys = ["songUri"])
data class FavoriteEntity(
    val songUri: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val artUri: String?,
)
