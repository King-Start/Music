package id.app.rplayer.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import id.app.rplayer.data.LibraryRepository
import id.app.rplayer.ui.theme.Spacing
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(repository: LibraryRepository) {
    val scope = rememberCoroutineScope()
    Scaffold(topBar = { TopAppBar(title = { Text("Setelan") }) }) { padding ->
        Column(Modifier.padding(padding)) {
            ListItem(
                headlineContent = { Text("Pindai ulang pustaka") },
                supportingContent = { Text("Membaca ulang file audio di perangkat") },
                trailingContent = {
                    IconButton(onClick = { scope.launch { repository.refresh() } }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Pindai ulang")
                    }
                },
            )
            Text(
                text = "RPlayer 0.1.0 — build Fase 1. Nama, ikon, dan warna masih placeholder; " +
                    "akan diganti sebelum rilis oleh proses rebrand.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(Spacing.Md),
            )
            Text(
                text = "Semua musik berasal dari file yang sudah kamu simpan di perangkat. " +
                    "Aplikasi ini tidak menyediakan katalog atau layanan streaming.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = Spacing.Md),
            )
        }
    }
}
