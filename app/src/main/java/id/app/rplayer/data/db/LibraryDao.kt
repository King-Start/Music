package id.app.rplayer.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface LibraryDao {

    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun observePlaylists(): Flow<List<PlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun deletePlaylistRow(id: Long)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :id")
    suspend fun deletePlaylistSongs(id: Long)

    @Transaction
    suspend fun deletePlaylist(id: Long) {
        deletePlaylistSongs(id)
        deletePlaylistRow(id)
    }

    @Query("SELECT * FROM playlist_songs WHERE playlistId = :playlistId ORDER BY position ASC")
    fun observePlaylistSongs(playlistId: Long): Flow<List<PlaylistSongEntity>>

    @Query("SELECT COALESCE(MAX(position), -1) + 1 FROM playlist_songs WHERE playlistId = :playlistId")
    suspend fun nextPosition(playlistId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistSong(song: PlaylistSongEntity)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId AND songUri = :songUri")
    suspend fun deletePlaylistSong(playlistId: Long, songUri: String)

    @Query("SELECT * FROM favorites ORDER BY title ASC")
    fun observeFavorites(): Flow<List<FavoriteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE songUri = :songUri")
    suspend fun deleteFavorite(songUri: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE songUri = :songUri)")
    fun observeIsFavorite(songUri: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE songUri = :songUri)")
    suspend fun isFavoriteOnce(songUri: String): Boolean
}
