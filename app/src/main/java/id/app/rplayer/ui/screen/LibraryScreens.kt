package id.app.rplayer.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.app.rplayer.data.LibraryRepository
import id.app.rplayer.data.media.DeviceSong
import id.app.rplayer.ui.PlayerViewModel
import id.app.rplayer.ui.component.EmptyState
import id.app.rplayer.ui.component.SectionHeader
import id.app.rplayer.ui.component.SkeletonList
import id.app.rplayer.ui.component.SongAction
import id.app.rplayer.ui.component.SongRow
import id.app.rplayer.ui.theme.Spacing
import kotlinx.coroutines.launch
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(repository: LibraryRepository, playerViewModel: PlayerViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val songs by repository.songs.collectAsStateWithLifecycle()
    val favorites by repository.favorites.collectAsStateWithLifecycle()
    val permission = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }
    var granted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED)
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        granted = ok
        if (ok) scope.launch { repository.refresh() }
    }

    LaunchedEffect(granted) { if (granted) repository.refresh() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Beranda") },
                actions = {
                    IconButton(onClick = { scope.launch { repository.refresh() } }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Pindai ulang pustaka")
                    }
                },
            )
        },
    ) { padding ->
        when {
            !granted -> EmptyState(
                title = "Butuh akses pustaka audio",
                message = "Aplikasi hanya membaca file musik yang sudah ada di perangkatmu. Tidak ada unduhan dari internet.",
                actionLabel = "Izinkan akses",
                onAction = { launcher.launch(permission) },
            )

            songs.isEmpty() -> EmptyState(
                title = "Belum ada lagu",
                message = "Simpan beberapa file audio di perangkat, lalu pindai ulang pustaka.",
                actionLabel = "Pindai ulang",
                onAction = { scope.launch { repository.refresh() } },
            )

            else -> LazyColumn(contentPadding = padding) {
                if (favorites.isNotEmpty()) {
                    item { SectionHeader(title = "Favorit") }
                    items(favorites.take(8), key = { it.uri }) { song ->
                        SongRow(song = song, onPlay = { playerViewModel.play(favorites, favorites.indexOf(song)) })
                    }
                }
                item { SectionHeader(title = "Terakhir ditambahkan") }
                items(songs, key = { it.uri }) { song ->
                    SongRow(
                        song = song,
                        onPlay = { playerViewModel.play(songs, songs.indexOf(song)) },
                        actions = songActions(repository, playerViewModel, song),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(repository: LibraryRepository, playerViewModel: PlayerViewModel) {
    var query by remember { mutableStateOf("") }
    val songs by repository.songs.collectAsStateWithLifecycle()
    val results = remember(query, songs) { repository.search(query) }
    val scope = rememberCoroutineScope()

    Scaffold(topBar = { TopAppBar(title = { Text("Cari") }) }) { padding ->
        Column(Modifier.padding(padding)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Judul, artis, atau album") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.Md),
            )
            when {
                query.isBlank() -> EmptyState(
                    title = "Cari di pustakamu",
                    message = "Pencarian hanya berlangsung di file audio lokal yang sudah kamu izinkan.",
                )

                results.isEmpty() -> EmptyState(
                    title = "Tidak ada hasil",
                    message = "Coba kata kunci lain atau pindai ulang pustaka.",
                )

                else -> LazyColumn {
                    items(results, key = { it.uri }) { song ->
                        SongRow(
                            song = song,
                            onPlay = { playerViewModel.play(results, results.indexOf(song)) },
                            actions = songActions(repository, playerViewModel, song),
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    repository: LibraryRepository,
    playerViewModel: PlayerViewModel,
    onOpenPlaylist: (Long) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val playlists by repository.playlists.collectAsStateWithLifecycle()
    val songs by repository.songs.collectAsStateWithLifecycle()
    val favorites by repository.favorites.collectAsStateWithLifecycle()
    var tab by remember { mutableIntStateOf(0) }
    var showCreate by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var confirmDelete by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pustaka") },
                actions = {
                    IconButton(onClick = { showCreate = true }) {
                        Icon(Icons.Filled.Add, contentDescription = "Buat playlist")
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding)) {
            TabRow(selectedTabIndex = tab) {
                listOf("Playlist", "Lagu", "Favorit").forEachIndexed { index, label ->
                    Tab(selected = tab == index, onClick = { tab = index }, text = { Text(label) })
                }
            }
            when (tab) {
                0 -> {
                    if (playlists.isEmpty()) {
                        EmptyState("Belum ada playlist", "Tekan tombol + untuk membuat playlist pertama.")
                    } else {
                        LazyColumn {
                            items(playlists, key = { it.id }) { playlist ->
                                ListItem(
                                    headlineContent = { Text(playlist.name) },
                                    supportingContent = { Text("Playlist lokal") },
                                    modifier = Modifier.clickable { onOpenPlaylist(playlist.id) },
                                    trailingContent = {
                                        IconButton(onClick = { confirmDelete = playlist.id }) {
                                            Icon(Icons.Filled.Delete, contentDescription = "Hapus playlist")
                                        }
                                    },
                                )
                            }
                        }
                    }
                }

                1 -> LazyColumn {
                    items(songs, key = { it.uri }) { song ->
                        SongRow(
                            song = song,
                            onPlay = { playerViewModel.play(songs, songs.indexOf(song)) },
                            actions = songActions(repository, playerViewModel, song),
                        )
                    }
                }

                2 -> {
                    if (favorites.isEmpty()) {
                        EmptyState("Belum ada favorit", "Tandai lagu lewat menu lagu untuk menyimpannya di sini.")
                    } else {
                        LazyColumn {
                            items(favorites, key = { it.uri }) { song ->
                                SongRow(
                                    song = song,
                                    onPlay = { playerViewModel.play(favorites, favorites.indexOf(song)) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreate) {
        AlertDialog(
            onDismissRequest = { showCreate = false },
            title = { Text("Playlist baru") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Nama playlist") },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newName.isNotBlank()) {
                        scope.launch { repository.createPlaylist(newName); newName = "" }
                        showCreate = false
                    }
                }) { Text("Buat") }
            },
            dismissButton = { TextButton(onClick = { showCreate = false }) { Text("Batal") } },
        )
    }

    confirmDelete?.let { id ->
        AlertDialog(
            onDismissRequest = { confirmDelete = null },
            title = { Text("Hapus playlist?") },
            text = { Text("Lagu di pustaka perangkat tidak ikut terhapus.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch { repository.deletePlaylist(id) }
                    confirmDelete = null
                }) { Text("Hapus") }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = null }) { Text("Batal") } },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    playlistId: Long,
    repository: LibraryRepository,
    playerViewModel: PlayerViewModel,
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val playlists by repository.playlists.collectAsStateWithLifecycle()
    val playlist = playlists.firstOrNull { it.id == playlistId }
    val songs by repository.playlistSongs(playlistId).collectAsStateWithLifecycle(initialValue = emptyList<DeviceSong>())
    val deviceSongs by repository.songs.collectAsStateWithLifecycle()
    var showAdd by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(playlist?.name ?: "Playlist") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding)) {
            Row(Modifier.padding(Spacing.Md)) {
                Button(
                    onClick = {
                        if (songs.isNotEmpty()) {
                            playerViewModel.setShuffle(true)
                            playerViewModel.play(songs, Random.nextInt(songs.size))
                        }
                    },
                    modifier = Modifier.weight(1f),
                ) { Text("Acak") }
                Spacer(Modifier.width(Spacing.Sm))
                Button(
                    onClick = {
                        if (songs.isNotEmpty()) {
                            playerViewModel.setShuffle(false)
                            playerViewModel.play(songs, 0)
                        }
                    },
                    modifier = Modifier.weight(1f),
                ) { Text("Putar semua") }
            }
            IconButton(onClick = { showAdd = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Tambah lagu")
            }
            if (songs.isEmpty()) {
                EmptyState("Playlist kosong", "Tambahkan lagu dari pustaka perangkat.")
            } else {
                LazyColumn {
                    items(songs, key = { it.uri }) { song ->
                        SongRow(
                            song = song,
                            onPlay = { playerViewModel.play(songs, songs.indexOf(song)) },
                            actions = listOf(
                                SongAction("Putar berikutnya") { playerViewModel.playNext(song) },
                                SongAction("Hapus dari playlist") {
                                    scope.launch { repository.removeFromPlaylist(playlistId, song.uri) }
                                },
                            ),
                        )
                    }
                }
            }
        }
    }

    if (showAdd) {
        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("Tambah lagu") },
            text = {
                if (deviceSongs.isEmpty()) {
                    Text("Pustaka perangkat masih kosong.")
                } else {
                    LazyColumn {
                        items(deviceSongs, key = { it.uri }) { song ->
                            ListItem(
                                headlineContent = { Text(song.displayTitle) },
                                supportingContent = { Text(song.displayArtist) },
                                modifier = Modifier.clickable {
                                    scope.launch {
                                        repository.addToPlaylist(playlistId, song)
                                    }
                                    showAdd = false
                                },
                            )
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showAdd = false }) { Text("Selesai") } },
        )
    }
}

@Composable
private fun songActions(
    repository: LibraryRepository,
    playerViewModel: PlayerViewModel,
    song: DeviceSong,
): List<SongAction> {
    val scope = rememberCoroutineScope()
    return listOf(
        SongAction("Putar berikutnya") { playerViewModel.playNext(song) },
        SongAction("Tambah/hapus favorit") { scope.launch { repository.toggleFavorite(song) } },
    )
}
