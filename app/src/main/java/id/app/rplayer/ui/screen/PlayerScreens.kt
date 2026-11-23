package id.app.rplayer.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import id.app.rplayer.data.media.DeviceSong
import id.app.rplayer.ui.PlayerUiState
import id.app.rplayer.ui.PlayerViewModel
import id.app.rplayer.ui.component.SongArtwork
import id.app.rplayer.ui.component.durationLabel
import id.app.rplayer.ui.theme.Sizes
import id.app.rplayer.ui.theme.Spacing
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(playerViewModel: PlayerViewModel, onBack: () -> Unit) {
    val state by playerViewModel.state.collectAsStateWithLifecycle()
    var showQueue by remember { mutableStateOf(false) }
    var scrub by remember { mutableStateOf<Float?>(null) }

    LaunchedEffect(Unit) {
        while (true) {
            playerViewModel.tick()
            delay(1000)
        }
    }

    val song = DeviceSong(
        uri = "",
        title = state.title,
        artist = state.artist,
        album = "",
        durationMs = state.durationMs,
        artUri = state.artUri,
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sedang diputar") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(Spacing.Lg)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SongArtwork(song = song, size = Sizes.ArtworkPlayer)
            Spacer(Modifier.height(Spacing.Lg))
            Text(
                text = song.displayTitle.ifBlank { "Tidak ada lagu" },
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
            )
            Text(
                text = song.displayArtist,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(Spacing.Lg))

            val duration = state.durationMs.toFloat().coerceAtLeast(1f)
            Slider(
                value = scrub ?: state.positionMs.toFloat().coerceIn(0f, duration),
                onValueChange = { scrub = it },
                valueRange = 0f..duration,
                onValueChangeFinished = {
                    scrub?.let { playerViewModel.seekTo(it.toLong()) }
                    scrub = null
                },
                enabled = state.hasQueue,
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(durationLabel(state.positionMs), style = MaterialTheme.typography.bodySmall)
                Text(durationLabel(state.durationMs), style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(Spacing.Md))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { playerViewModel.setShuffle(!state.shuffle) }) {
                    Icon(
                        Icons.Filled.Shuffle,
                        contentDescription = "Acak",
                        tint = if (state.shuffle) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    )
                }
                IconButton(onClick = playerViewModel::skipPrevious) {
                    Icon(Icons.Filled.SkipPrevious, contentDescription = "Lagu sebelumnya")
                }
                IconButton(onClick = playerViewModel::togglePlay) {
                    Icon(
                        imageVector = if (state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (state.isPlaying) "Jeda" else "Putar",
                        modifier = Modifier.height(Sizes.Touch),
                    )
                }
                IconButton(onClick = playerViewModel::skipNext) {
                    Icon(Icons.Filled.SkipNext, contentDescription = "Lagu berikutnya")
                }
                IconButton(onClick = playerViewModel::cycleRepeat) {
                    Icon(
                        imageVector = if (state.repeatMode == Player.REPEAT_MODE_ONE) {
                            Icons.Filled.RepeatOne
                        } else {
                            Icons.Filled.Repeat
                        },
                        contentDescription = "Mode ulang",
                        tint = if (state.repeatMode != Player.REPEAT_MODE_OFF) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    )
                }
            }

            Spacer(Modifier.height(Spacing.Md))
            Row {
                IconButton(onClick = { showQueue = true }) {
                    Icon(Icons.Filled.QueueMusic, contentDescription = "Buka antrean")
                }
            }
        }
    }

    if (showQueue) {
        QueueSheet(
            state = state,
            playerViewModel = playerViewModel,
            onDismiss = { showQueue = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueSheet(
    state: PlayerUiState,
    playerViewModel: PlayerViewModel,
    onDismiss: () -> Unit,
) {
    val queue = remember(state.currentIndex, state.itemCount) { playerViewModel.queue() }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.Md, vertical = Spacing.Sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Antrean (${queue.size})", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onDismiss) {
                Icon(Icons.Filled.Close, contentDescription = "Tutup antrean")
            }
        }
        if (queue.isEmpty()) {
            Text(
                text = "Belum ada antrean. Putar lagu dari beranda atau pustaka.",
                modifier = Modifier.padding(Spacing.Md),
                style = MaterialTheme.typography.bodyMedium,
            )
        } else {
            LazyColumn {
                itemsIndexed(queue, key = { index, _ -> index }) { index, song ->
                    ListItem(
                        headlineContent = { Text(song.displayTitle) },
                        supportingContent = { Text(song.displayArtist) },
                        modifier = Modifier.clickable { playerViewModel.play(queue, index) },
                        trailingContent = {
                            if (index != state.currentIndex) {
                                IconButton(onClick = { playerViewModel.removeQueueItem(index) }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Hapus dari antrean")
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}
