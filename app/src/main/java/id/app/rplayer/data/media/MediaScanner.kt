package id.app.rplayer.data.media

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore

/** Membaca file audio yang memang ada di perangkat pengguna. */
class MediaScanner(private val context: Context) {

    fun readSongs(): List<DeviceSong> {
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DATE_ADDED,
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} > 0"
        val sort = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

        val result = mutableListOf<DeviceSong>()
        val cursor = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            sort,
        ) ?: return result

        cursor.use {
            val idCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val albumIdCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

            while (it.moveToNext()) {
                val id = it.getLong(idCol)
                val albumId = it.getLong(albumIdCol)
                val artUri = if (albumId > 0) {
                    ContentUris.withAppendedId(ALBUM_ART_URI, albumId).toString()
                } else {
                    null
                }
                result += DeviceSong(
                    uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id).toString(),
                    title = it.getString(titleCol) ?: "",
                    artist = it.getString(artistCol) ?: "",
                    album = it.getString(albumCol) ?: "",
                    durationMs = it.getLong(durationCol),
                    artUri = artUri,
                )
            }
        }
        return result
    }

    private companion object {
        val ALBUM_ART_URI = android.net.Uri.parse("content://media/external/audio/albumart")
    }
}
