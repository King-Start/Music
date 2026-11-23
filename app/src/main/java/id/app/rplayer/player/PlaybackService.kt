package id.app.rplayer.player

import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

/** MediaSessionService memberi kita playback latar belakang dan notifikasi Media3. */
class PlaybackService : MediaSessionService() {

    private var player: ExoPlayer? = null
    private var session: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this).build()
        session = MediaSession.Builder(this, player!!).setId("rplayer-session").build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = session

    override fun onDestroy() {
        session?.release()
        session = null
        player?.release()
        player = null
        super.onDestroy()
    }
}
