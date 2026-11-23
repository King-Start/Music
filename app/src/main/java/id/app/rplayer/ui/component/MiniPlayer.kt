package id.app.rplayer.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import id.app.rplayer.data.media.DeviceSong
import id.app.rplayer.ui.PlayerUiState
import id.app.rplayer.ui.PlayerViewModel
import id.app.rplayer.ui.theme.Sizes
import id.app.rplayer.ui.theme.Spacing

@Composable
fun MiniPlayer(
    state: PlayerUiState,
    playerViewModel: PlayerViewModel,
    onOpen: () -> Unit,
) {
    val song = DeviceSong(
        uri = "",
        title = state.title,
        artist = state.artist,
        album = "",
        durationMs = state.durationMs,
        artUri = state.artUri,
    )
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(Sizes.MiniPlayerHeight)
            .clickable(onClick = onOpen),
        tonalElevation = Spacing.Sm,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.Md, vertical = Spacing.Sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SongArtwork(song = song, size = Sizes.ArtworkRow - Spacing.Sm)
            Spacer(Modifier.width(Spacing.Md))
            Column(Modifier.weight(1f)) {
                Text(text = state.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    text = state.artist,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = playerViewModel::togglePlay) {
                Icon(
                    imageVector = if (state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (state.isPlaying) "Jeda" else "Putar",
                )
            }
            IconButton(onClick = playerViewModel::skipNext) {
                Icon(Icons.Filled.SkipNext, contentDescription = "Lagu berikutnya")
            }
        }
    }
}
