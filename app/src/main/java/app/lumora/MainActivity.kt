package app.lumora

import android.Manifest
import android.app.Application
import android.content.*

import android.media.audiofx.Visualizer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.media3.common.*
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.*
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlin.math.*

class App : Application() {
    companion object { lateinit var pl: ExoPlayer }
    override fun onCreate() {
        super.onCreate()
        pl = ExoPlayer.Builder(this)
            .setAudioAttributes(AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).setContentType(C.AUDIO_CONTENT_TYPE_MUSIC).build(), true)
            .setHandleAudioBecomingNoisy(true).build()
    }
}

class PlaybackService : MediaSessionService() {
    private var s: MediaSession? = null
    override fun onCreate() { super.onCreate(); s = MediaSession.Builder(this, App.pl).build() }
    override fun onGetSession(c: MediaSession.ControllerInfo) = s
    override fun onDestroy() { s?.release(); super.onDestroy() }
    override fun onTaskRemoved(i: Intent?) { if (gb("stop", false)) { App.pl.pause(); stopSelf() } }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b); enableEdgeToEdge(); St.init(this)
        MediaController.Builder(this, SessionToken(this, ComponentName(this, PlaybackService::class.java))).buildAsync()
        setContent { Lumora() }
    }
}

data class Song(val id: Long, val title: String, val artist: String, val album: String, val dur: Long, val uri: Uri, val art: Uri)

fun loadSongs(c: Context): List<Song> {
    val l = ArrayList<Song>()
    val base = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    try {
        c.contentResolver.query(base, arrayOf("_id", "title", "artist", "album", "duration", "album_id"), "is_music!=0 AND duration>" + St.minDur * 1000, null, "date_added DESC")?.use { q ->
            while (q.moveToNext()) {
                val id = q.getLong(0)
                l.add(Song(id, q.getString(1) ?: "?", q.getString(2)?.takeIf { it != "<unknown>" } ?: "Tanpa artis", q.getString(3) ?: "", q.getLong(4),
                    ContentUris.withAppendedId(base, id), ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), q.getLong(5))))
            }
        }
    } catch (e: Exception) {}
    return l
}

val PAL = listOf("Aurora" to floatArrayOf(265f, 175f, 320f), "Senja" to floatArrayOf(18f, 340f, 45f), "Samudra" to floatArrayOf(210f, 185f, 250f),
    "Sakura" to floatArrayOf(330f, 350f, 290f), "Rimba" to floatArrayOf(140f, 95f, 175f), "Kopi" to floatArrayOf(28f, 12f, 48f))

class Pg { var playing by mutableStateOf(false); var id by mutableStateOf(""); var shuf by mutableStateOf(false); var rep by mutableIntStateOf(0); var pos by mutableLongStateOf(0); var dur by mutableLongStateOf(1) }

@Composable fun pstate(): Pg {
    val s = remember { Pg() }
    DisposableEffect(Unit) {
        val p = App.pl
        fun sync() { s.playing = p.isPlaying; s.id = p.currentMediaItem?.mediaId ?: ""; s.shuf = p.shuffleModeEnabled; s.rep = p.repeatMode; s.dur = p.duration.coerceAtLeast(1) }
        val l = object : Player.Listener {
            override fun onEvents(pl: Player, e: Player.Events) { sync(); St.saveMode() }
            override fun onMediaItemTransition(m: MediaItem?, r: Int) { if (p.playWhenReady) m?.mediaId?.let { St.rec(it) }; St.saveQ() }
        }
        p.addListener(l); sync(); onDispose { p.removeListener(l) }
    }
    LaunchedEffect(Unit) { while (true) { s.pos = App.pl.currentPosition; delay(250) } }
    return s
}

fun mi(it: Song) = MediaItem.Builder().setMediaId(it.id.toString()).setUri(it.uri).setMediaMetadata(MediaMetadata.Builder().setTitle(it.title).setArtist(it.artist).setArtworkUri(it.art).build()).build()
fun play(l: List<Song>, i: Int) { val p = App.pl; p.setMediaItems(l.map { mi(it) }, i, 0); p.prepare(); p.play() }
fun fmt(ms: Long) = "%d:%02d".format(ms / 60000, ms / 1000 % 60)

fun Modifier.accent(sh: Shape = RoundedCornerShape(99.dp)) = clip(sh).drawBehind { drawRect(Brush.linearGradient(listOf(St.ac(0), St.ac(1)))) }
fun Modifier.glass(r: Dp = 26.dp) = composed {
    val sh = RoundedCornerShape(r); val k = if (St.dark) .35f else 1f; val o = (if (St.glass) 1f else 2.2f) * St.gk
    clip(sh).background(Brush.linearGradient(listOf(Color.White.copy(min(1f, .36f * k * o)), Color.White.copy(min(1f, .1f * k * o)))), sh)
        .border(1.dp, Brush.linearGradient(listOf(Color.White.copy(if (St.dark) .5f else .9f), Color.White.copy(.1f), Color.White.copy(.4f))), sh)
        .drawWithContent { drawContent(); if (St.lens) drawRect(Brush.radialGradient(listOf(Color.White.copy(.25f), Color.Transparent), Offset(size.width * .15f, 0f), size.width * .6f)) }
}

@Composable fun Blobs() {
    val t = rememberInfiniteTransition(label = "b")
    val d by t.animateFloat(0f, 1f, infiniteRepeatable(tween(18000, easing = LinearEasing), RepeatMode.Reverse), label = "d")
    Canvas(Modifier.fillMaxSize()) {
        val w = size.width; val h = size.height
        fun b(i: Int, x: Float, y: Float, r: Float) = drawCircle(Brush.radialGradient(listOf(St.ac(i).copy(.6f), Color.Transparent), Offset(x, y), r), r, Offset(x, y))
        b(0, w * (.1f + .3f * d), h * .12f + 80f * d, w * .9f); b(1, w * (.95f - .3f * d), h * .85f, w); b(2, w * .5f, h * (.45f + .1f * d), w * .6f)
    }
}

@Composable fun Cv(s: Song?, m: Modifier, r: Dp = 18.dp) {
    val h = ((s?.id ?: 0L) * 47 % 360).toFloat()
    Box(m.clip(RoundedCornerShape(if (St.round) r else r / 3)).background(Brush.linearGradient(listOf(Color.hsv(h, .6f, 1f), Color.hsv((h + 70) % 360, .8f, .6f))))) {
        if (s != null) AsyncImage(s.art, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
    }
}
@Composable fun Chip(t: String, on: Boolean, f: () -> Unit) {
    Box(Modifier.padding(end = 8.dp).clip(RoundedCornerShape(99.dp)).clickable(onClick = f).then(if (on) Modifier.accent() else Modifier.glass(99.dp)).padding(16.dp, 8.dp)) {
        Text(t, color = if (on) Color.White else LocalContentColor.current, fontSize = 13.sp)
    }
}
@Composable fun Sec(t: String) = Text(t, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 22.dp, bottom = 10.dp))
@Composable fun Sub(t: String) = Text(t, fontSize = 12.sp, color = LocalContentColor.current.copy(.65f), maxLines = 2, overflow = TextOverflow.Ellipsis)
@Composable fun Top(t: String, back: (() -> Unit)? = null) = Row(Modifier.padding(bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
    if (back != null) IconButton(back) { Icon(Icons.Rounded.ArrowBack, null) }
    Text(t, fontSize = if (back == null) 28.sp else 22.sp, fontWeight = FontWeight.Bold)
}
@Composable fun Empty() = Box(Modifier.fillMaxWidth().glass().padding(20.dp)) { Text("Belum ada lagu. Izinkan akses audio dan simpan musik di perangkatmu.") }

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable fun SRow(s: Song, f: () -> Unit) = Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).combinedClickable(onClick = f, onLongClick = { St.menuSong = s }).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
    Cv(s, Modifier.size(48.dp), 12.dp)
    Column(Modifier.weight(1f).padding(horizontal = 12.dp)) { Text(s.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold); Sub(s.artist + " • " + fmt(s.dur)) }
    if (s.id.toString() in St.likes) Icon(Icons.Rounded.Favorite, null, Modifier.size(18.dp), tint = Color(0xFFFF4D8D))
}
@Composable fun Item(ic: ImageVector, t: String, sub: String = "", f: () -> Unit) = Row(Modifier.fillMaxWidth().clickable(onClick = f).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
    Icon(ic, null); Column(Modifier.weight(1f).padding(horizontal = 14.dp)) { Text(t); if (sub.isNotEmpty()) Sub(sub) }; Icon(Icons.Rounded.ChevronRight, null)
}
@Composable fun SwRow(t: String, sub: String, v: Boolean, f: (Boolean) -> Unit) = Row(Modifier.fillMaxWidth().padding(16.dp, 10.dp), verticalAlignment = Alignment.CenterVertically) {
    Column(Modifier.weight(1f)) { Text(t); Sub(sub) }; Switch(v, f)
}

@Composable fun Mini(s: Song, pg: Pg, open: () -> Unit) = Row(Modifier.fillMaxWidth().glass(32.dp).clickable(onClick = open).then(if (St.swipe) Modifier.pointerInput(Unit) { var dx = 0f; detectHorizontalDragGestures(onDragStart = { dx = 0f }, onDragEnd = { if (dx > 80) App.pl.seekToPrevious() else if (dx < -80) App.pl.seekToNext() }) { _, a -> dx += a } } else Modifier).drawBehind { drawRect(Brush.linearGradient(listOf(St.ac(0), St.ac(1))), Offset(24f, 0f), Size((size.width - 48f) * (pg.pos.toFloat() / pg.dur), 5f)) }.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
    Cv(s, Modifier.size(44.dp), 22.dp)
    Column(Modifier.weight(1f).padding(horizontal = 10.dp)) { Text(s.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold); Sub(s.artist) }
    IconButton({ St.like(s.id.toString()) }) { Icon(if (s.id.toString() in St.likes) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder, null) }
    IconButton({ if (pg.playing) App.pl.pause() else App.pl.play() }) { Icon(if (pg.playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, null) }
    IconButton({ App.pl.seekToNext() }) { Icon(Icons.Rounded.SkipNext, null) }
}

@Composable fun Full(s: Song?, pg: Pg, close: () -> Unit) = CompositionLocalProvider(LocalContentColor provides Color.White) {
    Column(Modifier.fillMaxSize().drawBehind { drawRect(Brush.verticalGradient(listOf(St.ac(0).copy(.85f), St.ac(1).copy(.65f), Color(0xFF0D0B18)))) }.statusBarsPadding().navigationBarsPadding().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { IconButton(close) { Icon(Icons.Rounded.KeyboardArrowDown, null) }; Text("Sedang diputar", Modifier.weight(1f)) }
        Spacer(Modifier.height(10.dp))
        Cv(s, Modifier.fillMaxWidth(.78f).aspectRatio(1f).graphicsLayer { val k = 1f + St.bass * .08f; scaleX = k; scaleY = k }, 32.dp)
        Spacer(Modifier.height(18.dp))
        Text(s?.title ?: "", fontSize = 22.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis); Text(s?.artist ?: "", color = Color.White.copy(.7f))
        Canvas(Modifier.fillMaxWidth().height(80.dp).padding(vertical = 8.dp)) {
            val n = St.fft.size; val w = size.width / n
            for (i in 0 until n) { val h = max(6f, (if (pg.playing) St.fft[i] else .05f) * size.height); drawRoundRect(Color.hsv((St.hue + i * 8 + PAL[St.pal].second[0]) % 360f, .7f, 1f), Offset(i * w + 2f, size.height - h), Size(w - 5f, h), CornerRadius(8f)) }
        }
        Slider(pg.pos.toFloat().coerceIn(0f, pg.dur.toFloat()), { App.pl.seekTo(it.toLong()) }, valueRange = 0f..pg.dur.toFloat())
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Sub(fmt(pg.pos)); Sub(fmt(pg.dur)) }
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            IconButton({ App.pl.shuffleModeEnabled = !pg.shuf }) { Icon(Icons.Rounded.Shuffle, null, tint = if (pg.shuf) St.ac(0) else Color.White) }
            IconButton({ App.pl.seekToPrevious() }) { Icon(Icons.Rounded.SkipPrevious, null, Modifier.size(36.dp)) }
            Box(Modifier.size(72.dp).accent(CircleShape).clickable { if (pg.playing) App.pl.pause() else App.pl.play() }, contentAlignment = Alignment.Center) { Icon(if (pg.playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, null, Modifier.size(36.dp)) }
            IconButton({ App.pl.seekToNext() }) { Icon(Icons.Rounded.SkipNext, null, Modifier.size(36.dp)) }
            IconButton({ App.pl.repeatMode = (pg.rep + 1) % 3 }) { Icon(if (pg.rep == 1) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat, null, tint = if (pg.rep != 0) St.ac(0) else Color.White) }
        }
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            IconButton({ s?.let { St.like(it.id.toString()) } }) { Icon(if (s != null && s.id.toString() in St.likes) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder, null) }
            IconButton({ St.sleepAt = if (St.sleepAt > 0) 0 else System.currentTimeMillis() + 30 * 60000L }) { Icon(Icons.Rounded.Bedtime, null, tint = if (St.sleepAt > 0) St.ac(0) else Color.White) }
        }
    }
}

