package id.app.rplayer.player

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import id.app.rplayer.data.media.DeviceSong
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Jembatan aman dari Compose ke MediaController milik PlaybackService. */
class PlayerController(context: Context) {

    private val appContext = context.applicationContext
    private val token = SessionToken(appContext, ComponentName(appContext, PlaybackService::class.java))
    private val future = MediaController.Builder(appContext, token).buildAsync()

    private val _controller = MutableStateFlow<MediaController?>(null)
    val controller: StateFlow<MediaController?> = _controller.asStateFlow()

    init {
        future.addListener(
            { _controller.value = runCatching { future.get() }.getOrNull() },
            ContextCompat.getMainExecutor(appContext),
        )
    }

    fun play(songs: List<DeviceSong>, startIndex: Int) {
        val c = controller.value ?: return
        if (songs.isEmpty()) return
        val index = startIndex.coerceIn(songs.indices)
        c.setMediaItems(songs.map { it.toMediaItem() }, index, 0L)
        c.prepare()
        c.play()
    }

    fun playNext(song: DeviceSong) {
        val c = controller.value ?: return
        val items = currentItems(c).toMutableList()
        val insertAt = if (c.currentMediaItemIndex >= 0) c.currentMediaItemIndex + 1 else items.size
        items.add(insertAt.coerceAtMost(items.size), song.toMediaItem())
        val keepIndex = c.currentMediaItemIndex.coerceAtLeast(0)
        c.setMediaItems(items, keepIndex, c.currentPosition)
        c.prepare()
    }

    fun removeQueueItem(index: Int) {
        val c = controller.value ?: return
        if (index < 0 || index >= c.mediaItemCount || index == c.currentMediaItemIndex) return
        val items = currentItems(c).toMutableList()
        items.removeAt(index)
        val newIndex = if (index < c.currentMediaItemIndex) c.currentMediaItemIndex - 1 else c.currentMediaItemIndex
        c.setMediaItems(items, newIndex.coerceAtLeast(0), c.currentPosition)
        c.prepare()
    }

    fun togglePlay() {
        val c = controller.value ?: return
        if (c.isPlaying) c.pause() else c.play()
    }

    fun skipNext() { controller.value?.seekToNextMediaItem() }
    fun skipPrevious() { controller.value?.seekToPreviousMediaItem() }
    fun seekTo(ms: Long) { controller.value?.seekTo(ms) }

    fun setShuffle(enabled: Boolean) { controller.value?.shuffleModeEnabled = enabled }

    fun cycleRepeat() {
        val c = controller.value ?: return
        c.repeatMode = when (c.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
    }

    fun queueSnapshot(): List<DeviceSong> {
        val c = controller.value ?: return emptyList()
        return currentItems(c).map { item ->
            DeviceSong(
                uri = item.mediaId,
                title = item.mediaMetadata.title?.toString().orEmpty(),
                artist = item.mediaMetadata.artist?.toString().orEmpty(),
                album = item.mediaMetadata.albumTitle?.toString().orEmpty(),
                durationMs = item.mediaMetadata.durationMs ?: 0L,
                artUri = item.mediaMetadata.artworkUri?.toString(),
            )
        }
    }

    private fun currentItems(c: MediaController): List<MediaItem> =
        (0 until c.mediaItemCount).map { c.getMediaItemAt(it) }

    private fun DeviceSong.toMediaItem(): MediaItem = MediaItem.Builder()
        .setUri(Uri.parse(uri))
        .setMediaId(uri)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(displayTitle)
                .setArtist(displayArtist)
                .setAlbumTitle(album)
                .setArtworkUri(artUri?.let { Uri.parse(it) })
                .build(),
        )
        .build()

    fun release() = MediaController.releaseFuture(future)
}
