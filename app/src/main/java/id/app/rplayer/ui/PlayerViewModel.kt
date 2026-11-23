package id.app.rplayer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import id.app.rplayer.data.media.DeviceSong
import id.app.rplayer.player.PlayerController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class PlayerUiState(
    val hasQueue: Boolean = false,
    val title: String = "",
    val artist: String = "",
    val artUri: String? = null,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val currentIndex: Int = -1,
    val itemCount: Int = 0,
    val shuffle: Boolean = false,
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
)

class PlayerViewModel(private val player: PlayerController) : ViewModel() {

    private val _state = MutableStateFlow(PlayerUiState())
    val state: StateFlow<PlayerUiState> = _state.asStateFlow()

    private var listener: Player.Listener? = null

    init {
        viewModelScope.launch {
            val c = player.controller.filterNotNull().first()
            listener = object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) = syncFrom(c)
                override fun onIsPlayingChanged(isPlaying: Boolean) = syncFrom(c)
                override fun onMediaItemTransition(mediaItem: androidx.media3.common.MediaItem?, reason: Int) = syncFrom(c)
                override fun onTimelineChanged(timeline: androidx.media3.common.Timeline, reason: Int) = syncFrom(c)
                override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) = syncFrom(c)
                override fun onRepeatModeChanged(repeatMode: Int) = syncFrom(c)
            }
            c.addListener(listener!!)
            syncFrom(c)
        }
    }

    fun play(songs: List<DeviceSong>, index: Int) { player.play(songs, index) }
    fun playNext(song: DeviceSong) { player.playNext(song) }
    fun togglePlay() { player.togglePlay() }
    fun skipNext() { player.skipNext() }
    fun skipPrevious() { player.skipPrevious() }
    fun seekTo(ms: Long) { player.seekTo(ms) }
    fun setShuffle(enabled: Boolean) { player.setShuffle(enabled) }
    fun cycleRepeat() { player.cycleRepeat() }
    fun removeQueueItem(index: Int) { player.removeQueueItem(index) }
    fun queue(): List<DeviceSong> = player.queueSnapshot()

    /** Dipanggil UI setiap detik saat player terbuka. */
    fun tick() {
        player.controller.value?.let(::syncFrom)
    }

    private fun syncFrom(c: MediaController) {
        val item = c.currentMediaItem
        val duration = if (c.duration == C.TIME_UNSET) 0L else c.duration
        _state.value = PlayerUiState(
            hasQueue = c.mediaItemCount > 0,
            title = item?.mediaMetadata?.title?.toString().orEmpty(),
            artist = item?.mediaMetadata?.artist?.toString().orEmpty(),
            artUri = item?.mediaMetadata?.artworkUri?.toString(),
            isPlaying = c.isPlaying,
            positionMs = c.currentPosition.coerceAtLeast(0L),
            durationMs = duration.coerceAtLeast(0L),
            currentIndex = c.currentMediaItemIndex,
            itemCount = c.mediaItemCount,
            shuffle = c.shuffleModeEnabled,
            repeatMode = c.repeatMode,
        )
    }

    override fun onCleared() {
        listener?.let { l -> player.controller.value?.removeListener(l) }
        listener = null
        super.onCleared()
    }
}
