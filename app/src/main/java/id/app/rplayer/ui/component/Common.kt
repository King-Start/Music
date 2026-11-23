package id.app.rplayer.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import id.app.rplayer.data.media.DeviceSong
import id.app.rplayer.ui.theme.Radii
import id.app.rplayer.ui.theme.Sizes
import id.app.rplayer.ui.theme.Spacing
import id.app.rplayer.ui.theme.TypeSize

data class SongAction(val label: String, val onClick: () -> Unit)

@Composable
fun SongArtwork(song: DeviceSong, size: androidx.compose.ui.unit.Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(Radii.Sm))
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.MusicNote,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
fun SongRow(
    song: DeviceSong,
    isPlaying: Boolean = false,
    onPlay: () -> Unit,
    actions: List<SongAction> = emptyList(),
    trailing: (@Composable () -> Unit)? = null,
) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPlay)
            .padding(horizontal = Spacing.Md, vertical = Spacing.Sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SongArtwork(song = song, size = Sizes.ArtworkRow)
        Spacer(Modifier.width(Spacing.Md))
        Column(Modifier.weight(1f)) {
            Text(
                text = song.displayTitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "${song.displayArtist} · ${durationLabel(song.durationMs)}",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = TypeSize.Caption),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (trailing != null) {
            Box { trailing() }
        }
        if (actions.isNotEmpty()) {
            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "Opsi lagu")
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    actions.forEach { action ->
                        DropdownMenuItem(
                            text = { Text(action.label) },
                            onClick = { expanded = false; action.onClick() },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState(
    title: String,
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.Xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            Icons.Filled.MusicNote,
            contentDescription = null,
            modifier = Modifier.size(Sizes.ArtworkPlayer / 2),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(Spacing.Md))
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(Spacing.Sm))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(Spacing.Lg))
            Button(onClick = onAction) { Text(actionLabel) }
        }
    }
}

@Composable
fun SectionHeader(title: String, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.Md, vertical = Spacing.Sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium.copy(fontSize = TypeSize.Section),
        )
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction) { Text(actionLabel) }
        }
    }
}

@Composable
private fun TextButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    androidx.compose.material3.TextButton(onClick = onClick, content = content)
}

@Composable
fun SkeletonList(count: Int = 5) {
    Column {
        repeat(count) {
            Row(
                modifier = Modifier.padding(horizontal = Spacing.Md, vertical = Spacing.Sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier
                        .size(Sizes.ArtworkRow)
                        .clip(RoundedCornerShape(Radii.Sm))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                )
                Spacer(Modifier.width(Spacing.Md))
                Column(Modifier.weight(1f)) {
                    Box(
                        Modifier
                            .fillMaxWidth(0.7f)
                            .height(Spacing.Md)
                            .clip(RoundedCornerShape(Radii.Sm))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                    )
                    Spacer(Modifier.height(Spacing.Sm))
                    Box(
                        Modifier
                            .fillMaxWidth(0.45f)
                            .height(Spacing.Sm)
                            .clip(RoundedCornerShape(Radii.Sm))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                    )
                }
            }
        }
    }
}

fun durationLabel(ms: Long): String {
    if (ms <= 0) return "0:00"
    val total = ms / 1000
    return "${total / 60}:${(total % 60).toString().padStart(2, '0')}"
}
