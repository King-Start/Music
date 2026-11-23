package app.lumora

import android.Manifest
import android.content.*
import android.media.audiofx.BassBoost
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Visualizer
import android.net.Uri
import android.os.Build
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.media3.common.*
import coil.imageLoader
import kotlinx.coroutines.delay
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

fun gb(k: String, d: Boolean) = St.sp.getBoolean(k, d)
data class H(val id: String, val t: Long)

object St {
    lateinit var sp: SharedPreferences
    var pal by mutableIntStateOf(0); var mode by mutableIntStateOf(0); var startTab by mutableIntStateOf(0); var grid by mutableIntStateOf(3)
    var minDur by mutableIntStateOf(30); var topN by mutableIntStateOf(50); var srchMax by mutableIntStateOf(75); var reload by mutableIntStateOf(0)
    var dyn by mutableStateOf(true); var glass by mutableStateOf(true); var lens by mutableStateOf(true); var round by mutableStateOf(true)
    var noLabel by mutableStateOf(false); var collapse by mutableStateOf(true); var swipe by mutableStateOf(true); var keepOn by mutableStateOf(false)
    var secure by mutableStateOf(false); var shufHome by mutableStateOf(false); var streakOn by mutableStateOf(true); var dark by mutableStateOf(false); var sleepEnd by mutableStateOf(false)
    var gk by mutableFloatStateOf(1f); var dspd by mutableFloatStateOf(1f); var scale by mutableFloatStateOf(1f)
    var hue by mutableFloatStateOf(0f); var bass by mutableFloatStateOf(0f); var fft by mutableStateOf(FloatArray(32)); var sleepAt by mutableLongStateOf(0L)
    var likes by mutableStateOf(setOf<String>()); var hist by mutableStateOf(listOf<H>()); var pls by mutableStateOf(mapOf<String, List<String>>())
    var menuSong by mutableStateOf<Song?>(null)
    fun init(c: Context) {
        sp = c.getSharedPreferences("l", 0)
        pal = sp.getInt("pal", 0); mode = sp.getInt("mode", 0); startTab = sp.getInt("st", 0); grid = sp.getInt("grid", 3); minDur = sp.getInt("md", 30); topN = sp.getInt("tn", 50); srchMax = sp.getInt("sm", 75)
        dyn = sp.getBoolean("dyn", true); glass = sp.getBoolean("glass", true); lens = sp.getBoolean("lens", true); round = sp.getBoolean("round", true)
        noLabel = sp.getBoolean("nl", false); collapse = sp.getBoolean("col", true); swipe = sp.getBoolean("sw", true); keepOn = sp.getBoolean("ko", false)
        secure = sp.getBoolean("sec", false); shufHome = sp.getBoolean("shh", false); streakOn = sp.getBoolean("stk", true)
        gk = sp.getFloat("gk", 1f); dspd = sp.getFloat("ds", 1f); scale = sp.getFloat("sc", 1f)
        likes = sp.getStringSet("likes", emptySet())!!.toSet()
        hist = sp.getString("hist", "")!!.split(",").filter { it.contains(":") }.map { val p = it.split(":"); H(p[0], p[1].toLongOrNull() ?: 0L) }
        pls = (sp.getString("pln", "") ?: "").split("|").filter { it.isNotEmpty() }.associateWith { n -> (sp.getString("pl:$n", "") ?: "").split(",").filter { it.isNotEmpty() } }
    }
    fun sv() {
        sp.edit().putInt("pal", pal).putInt("mode", mode).putInt("st", startTab).putInt("grid", grid).putInt("md", minDur).putInt("tn", topN).putInt("sm", srchMax)
            .putBoolean("dyn", dyn).putBoolean("glass", glass).putBoolean("lens", lens).putBoolean("round", round).putBoolean("nl", noLabel).putBoolean("col", collapse)
            .putBoolean("sw", swipe).putBoolean("ko", keepOn).putBoolean("sec", secure).putBoolean("shh", shufHome).putBoolean("stk", streakOn)
            .putFloat("gk", gk).putFloat("ds", dspd).putFloat("sc", scale).putStringSet("likes", likes)
            .putString("hist", hist.joinToString(",") { "${it.id}:${it.t}" }).apply()
    }
    fun savePl() { val e = sp.edit().putString("pln", pls.keys.joinToString("|")); pls.forEach { (n, l) -> e.putString("pl:$n", l.joinToString(",")) }; e.apply() }
    fun newPl(n: String) { val k = n.replace("|", "").replace(",", "").trim(); if (k.isNotEmpty() && k !in pls) { pls = pls + (k to emptyList()); savePl() } }
    fun delPl(n: String) { pls = pls - n; sp.edit().remove("pl:$n").apply(); savePl() }
    fun addTo(n: String, id: String) { val l = pls[n].orEmpty(); if (id in l) return; pls = pls + (n to if (sp.getInt("padd", 0) == 0) listOf(id) + l else l + id); savePl() }
    fun rec(id: String) { if (gb("ph", false)) return; hist = (listOf(H(id, System.currentTimeMillis())) + hist).take(2000); sv() }
    fun like(id: String) { likes = if (id in likes) likes - id else likes + id; sv() }
    fun saveQ() { val p = App.pl; if (p.mediaItemCount > 0) sp.edit().putString("q", (0 until p.mediaItemCount).joinToString(",") { p.getMediaItemAt(it).mediaId }).putInt("qi", p.currentMediaItemIndex).apply() }
    fun saveMode() { if (gb("ps", true)) sp.edit().putBoolean("shv", App.pl.shuffleModeEnabled).putInt("rpv", App.pl.repeatMode).apply() }
    fun ac(i: Int) = Color.hsv((((PAL[pal].second[i] + hue) % 360f) + 360f) % 360f, .72f, 1f)
}

var eqObj: android.media.audiofx.Equalizer? = null; var bbObj: BassBoost? = null; var ldObj: LoudnessEnhancer? = null
fun initFx() {
    val s = App.pl.audioSessionId
    try { if (eqObj == null) eqObj = android.media.audiofx.Equalizer(0, s) } catch (e: Exception) {}
    try { if (bbObj == null) bbObj = BassBoost(0, s) } catch (e: Exception) {}
    try { if (ldObj == null) ldObj = LoudnessEnhancer(s) } catch (e: Exception) {}
    applyFx()
}
fun applyFx() {
    val p = App.pl; p.skipSilenceEnabled = gb("skip", false)
    try { ldObj?.let { it.setTargetGain(if (gb("norm", false)) intArrayOf(300, 600, 1000)[St.sp.getInt("lvl", 1)] else 0); it.enabled = gb("norm", false) } } catch (e: Exception) {}
    val sp = St.sp.getFloat("spd", 1f); p.playbackParameters = PlaybackParameters(sp, if (gb("vari", false)) sp else 1f)
}
fun topBy(sg: List<Song>, days: Int, n: Int): List<Song> {
    val lim = if (days == 0) 0L else System.currentTimeMillis() - days * 86400000L
    val c = St.hist.filter { it.t >= lim }.groupingBy { it.id }.eachCount()
    return sg.filter { c.containsKey(it.id.toString()) }.sortedByDescending { c[it.id.toString()] }.take(n)
}
fun listFor(k: String, sg: List<Song>): List<Song> = when {
    k == "like" -> sg.filter { it.id.toString() in St.likes }; k == "all" -> sg
    k == "top" -> topBy(sg, 0, St.topN); k == "week" -> topBy(sg, 7, St.topN); k == "month" -> topBy(sg, 30, St.topN)
    k.startsWith("pl:") -> St.pls[k.drop(3)].orEmpty().mapNotNull { id -> sg.find { it.id.toString() == id } }
    k.startsWith("al:") -> sg.filter { it.album == k.drop(3) }; k.startsWith("ar:") -> sg.filter { it.artist == k.drop(3) }
    else -> emptyList()
}
fun titleFor(k: String) = when { k == "like" -> "Disukai"; k == "all" -> "Di perangkat"; k == "top" -> "Teratas saya"; k == "week" -> "Terpopuler Mingguan"; k == "month" -> "Terpopuler Bulanan"; else -> k.drop(3) }
fun exportJson(): String { St.sv(); val o = JSONObject(); St.sp.all.forEach { (k, v) -> o.put(k, if (v is Set<*>) JSONArray(v.toList()) else v) }; return o.toString() }
fun importJson(s: String, c: Context) {
    val o = JSONObject(s); val e = St.sp.edit()
    o.keys().forEach { k -> when (val v = o.get(k)) { is Boolean -> e.putBoolean(k, v); is Int -> e.putInt(k, v); is Long -> e.putLong(k, v); is Double -> e.putFloat(k, v.toFloat()); is String -> e.putString(k, v)
        is JSONArray -> e.putStringSet(k, (0 until v.length()).map { v.getString(it) }.toSet()) } }
    e.apply(); St.init(c); St.reload++
}

@Composable fun TgP(k: String, t: String, sub: String = "", d: Boolean = false, on: () -> Unit = {}) {
    var v by remember { mutableStateOf(St.sp.getBoolean(k, d)) }
    SwRow(t, sub, v) { v = it; St.sp.edit().putBoolean(k, it).apply(); on() }
}
@Composable fun Sl(t: String, v: Float, a: Float, b: Float, u: String, f: (Float) -> Unit, done: () -> Unit = {}) = Column(Modifier.padding(16.dp, 8.dp)) {
    Row { Text(t, Modifier.weight(1f)); Sub((if (b - a >= 10f) v.toInt().toString() else "%.2f".format(v)) + u) }
    Slider(v, f, valueRange = a..b, onValueChangeFinished = done)
}
@Composable fun ChP(k: String, t: String, o: List<String>, d: Int, on: () -> Unit = {}) {
    var v by remember { mutableIntStateOf(St.sp.getInt(k, d)) }
    Column(Modifier.padding(16.dp, 10.dp)) { Text(t); Row(Modifier.horizontalScroll(rememberScrollState()).padding(top = 6.dp)) { o.forEachIndexed { i, n -> Chip(n, v == i) { v = i; St.sp.edit().putInt(k, i).apply(); on() } } } }
}
@Composable fun Grp(c: @Composable ColumnScope.() -> Unit) = Column(Modifier.fillMaxWidth().glass(), content = c)
@Composable fun Hd(t: String) = Text(t, Modifier.padding(start = 6.dp, top = 18.dp, bottom = 6.dp), fontSize = 12.sp, color = LocalContentColor.current.copy(.65f))
@Composable fun GCard(s: Song?, t: String, sub: String, round: Boolean, mod: Modifier, f: () -> Unit) = Column(mod.clickable(onClick = f).padding(4.dp)) {
    Cv(s, Modifier.fillMaxWidth().aspectRatio(1f), if (round) 70.dp else 18.dp); Text(t, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp)); Sub(sub)
}
@Composable fun HRow(t: String, l: List<Song>, go: (List<Song>, Int) -> Unit) = Column {
    Sec(t); LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) { itemsIndexed(l) { i, s -> Column(Modifier.width(132.dp).clickable { go(l, i) }) { Cv(s, Modifier.size(132.dp)); Text(s.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 13.sp); Sub(s.artist) } } }
}

fun streak(): Int {
    val days = St.hist.map { it.t / 86400000L }.toSet(); var d = System.currentTimeMillis() / 86400000L; if (d !in days) d--
    var n = 0; while (d in days) { n++; d-- }; return n
}

@Composable fun Home(sg: List<Song>, go: (List<Song>, Int) -> Unit, open: (String) -> Unit) {
    val hr = Calendar.getInstance().get(Calendar.HOUR_OF_DAY); val cnt = St.hist.groupingBy { it.id }.eachCount(); val now = System.currentTimeMillis()
    val forgot = sg.filter { (cnt[it.id.toString()] ?: 0) >= 2 && St.hist.none { h -> h.id == it.id.toString() && h.t > now - 14 * 86400000L } }
    val order = remember(St.shufHome, sg.size) { (0 until 4).let { if (St.shufHome) it.shuffled() else it.toList() } }
    val arts = sg.groupBy { it.artist }.entries.sortedByDescending { it.value.size }.take(8)
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 190.dp)) {
        item { Column { Top(if (hr < 11) "Selamat pagi" else if (hr < 15) "Selamat siang" else if (hr < 18) "Selamat sore" else "Selamat malam")
            if (St.streakOn && streak() > 0) Row(Modifier.padding(bottom = 8.dp).glass(99.dp).padding(14.dp, 6.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Rounded.LocalFireDepartment, null, Modifier.size(18.dp), tint = Color(0xFFFF8A3D)); Text(" ${streak()} hari beruntun", fontSize = 13.sp) }
            Row(Modifier.horizontalScroll(rememberScrollState())) { arts.forEach { e -> Chip(e.key, false) { go(e.value, 0) } } } } }
        if (sg.isEmpty()) item { Empty() } else {
            item { Sec("Andalanmu") }
            items(sg.take(St.grid * 3).chunked(St.grid)) { r -> Row(Modifier.padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                r.forEach { s -> GCard(s, s.title, s.artist, false, Modifier.weight(1f)) { go(sg, sg.indexOf(s)) } }; repeat(St.grid - r.size) { Spacer(Modifier.weight(1f)) } } }
            item { Column { Sec("Mix untukmu"); LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                item { Column(Modifier.width(132.dp).clickable { open("like") }) { Box(Modifier.size(132.dp).accent(RoundedCornerShape(20.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Favorite, null, tint = Color.White, modifier = Modifier.size(44.dp)) }; Text("Disukai"); Sub("${St.likes.size} lagu") } }
                item { Column(Modifier.width(132.dp).clickable { open("month") }) { Box(Modifier.size(132.dp).glass(20.dp), contentAlignment = Alignment.Center) { Icon(Icons.Rounded.TrendingUp, null, Modifier.size(44.dp)) }; Text("Terpopuler Bulanan"); Sub("Otomatis") } } } } }
            items(order) { i -> when (i) {
                0 -> HRow("Rilis baru", sg.take(12), go)
                1 -> if (forgot.isNotEmpty()) HRow("Favorit yang terlupakan", forgot.take(12), go)
                2 -> Column { Sec("Pilihan cepat"); remember(sg.size) { sg.shuffled().take(4) }.let { q -> q.forEachIndexed { j, s -> SRow(s) { go(q, j) } } } }
                else -> arts.firstOrNull()?.let { HRow("Lainnya dari ${it.key}", it.value, go) } } }
        }
    }
}

@Composable fun Search(sg: List<Song>, go: (List<Song>, Int) -> Unit) {
    var q by remember { mutableStateOf("") }
    val r = if (q.isBlank()) emptyList() else sg.filter { (it.title + it.artist + it.album).contains(q, true) }.take(St.srchMax)
    Column(Modifier.fillMaxSize().padding(16.dp, 12.dp)) {
        Top("Cari")
        TextField(q, { q = it }, Modifier.fillMaxWidth().glass(30.dp), placeholder = { Text("Cari lagu, artis, album…") }, singleLine = true, trailingIcon = { if (q.isNotEmpty()) IconButton({ q = "" }) { Icon(Icons.Rounded.Close, null) } },
            colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent))
        LazyColumn(contentPadding = PaddingValues(top = 12.dp, bottom = 190.dp)) {
            if (q.isBlank()) { item { Sec("Jelajahi semua") }; items(sg.map { it.artist }.distinct().chunked(2)) { r2 -> Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { r2.forEach { a -> Box(Modifier.weight(1f).padding(bottom = 10.dp).glass(20.dp).clickable { go(listFor("ar:$a", sg), 0) }.padding(16.dp)) { Text(a, maxLines = 1) } }; if (r2.size == 1) Spacer(Modifier.weight(1f)) } } }
            else itemsIndexed(r) { i, s -> SRow(s) { go(r, i) } }
        }
    }
}

@Composable fun Library(sg: List<Song>, go: (List<Song>, Int) -> Unit, open: (String) -> Unit) {
    var t by remember { mutableIntStateOf(0) }; var sort by remember { mutableIntStateOf(0) }; var rev by remember { mutableStateOf(false) }
    var q by remember { mutableStateOf("") }; var sq by remember { mutableStateOf(false) }; var grid by remember { mutableStateOf(true) }; var sub2 by remember { mutableIntStateOf(1) }; var newPl by remember { mutableStateOf(false) }; var nn by remember { mutableStateOf("") }
    val tabs = listOf("Semua", "Playlist", "Lagu", "Album", "Artis"); val sorts = listOf("Tanggal ditambahkan", "Nama", "Artis", "Durasi")
    var l = (if (t == 2 && sub2 == 0) sg.filter { it.id.toString() in St.likes } else sg).filter { q.isBlank() || (it.title + it.artist + it.album).contains(q, true) }
    l = when (sort) { 1 -> l.sortedBy { it.title.lowercase() }; 2 -> l.sortedBy { it.artist.lowercase() }; 3 -> l.sortedBy { it.dur }; else -> l }; if (rev) l = l.reversed()
    var gr = (if (t == 3) sg.groupBy { it.album } else sg.groupBy { it.artist }).entries.filter { q.isBlank() || it.key.contains(q, true) }
    gr = if (sort == 3) gr.sortedBy { it.value.size } else gr.sortedBy { it.key.lowercase() }; if (rev) gr = gr.reversed()
    Column(Modifier.fillMaxSize().padding(16.dp, 12.dp)) {
        Top("Pustaka")
        Row(Modifier.horizontalScroll(rememberScrollState())) { tabs.forEachIndexed { i, n -> Chip(n, t == i) { t = i } } }
        if (t >= 2) Row(Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Chip(sorts[sort], false) { sort = (sort + 1) % 4 }; IconButton({ rev = !rev }) { Icon(if (rev) Icons.Rounded.ArrowUpward else Icons.Rounded.ArrowDownward, null) }; Spacer(Modifier.weight(1f))
            IconButton({ sq = !sq; if (!sq) q = "" }) { Icon(Icons.Rounded.Search, null) }; if (t != 2) IconButton({ grid = !grid }) { Icon(if (grid) Icons.Rounded.Menu else Icons.Rounded.GridView, null) } }
        if (sq) TextField(q, { q = it }, Modifier.fillMaxWidth().glass(30.dp), singleLine = true, placeholder = { Text("Cari di pustaka…") }, colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent))
        when (t) {
            0 -> LazyColumn(contentPadding = PaddingValues(top = 12.dp, bottom = 190.dp)) {
                item { Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { GCard(topBy(sg, 30, 1).firstOrNull(), "Terpopuler Bulanan", "${topBy(sg, 30, 99).size} lagu", false, Modifier.weight(1f)) { open("month") }; GCard(topBy(sg, 7, 1).firstOrNull(), "Terpopuler Mingguan", "${topBy(sg, 7, 99).size} lagu", false, Modifier.weight(1f)) { open("week") } } }
                item { Column { Sec("Koleksi"); Grp { Item(Icons.Rounded.Favorite, "Disukai", "${St.likes.size} lagu") { open("like") }; Item(Icons.Rounded.Storage, "Di perangkat", "${sg.size} lagu") { open("all") }; Item(Icons.Rounded.TrendingUp, "Teratas saya ${St.topN}") { open("top") }; Item(Icons.Rounded.History, "Riwayat") { open("hist") }; Item(Icons.Rounded.Add, "Playlist baru") { newPl = true } } } } }
            1 -> LazyVerticalGrid(GridCells.Fixed(2), contentPadding = PaddingValues(top = 12.dp, bottom = 190.dp)) {
                item { GCard(null, "Playlist baru", "Buat", false, Modifier) { newPl = true } }
                items(St.pls.keys.toList()) { n -> GCard(listFor("pl:$n", sg).firstOrNull(), n, "${St.pls[n]!!.size} lagu", false, Modifier) { open("pl:$n") } } }
            2 -> { Row(Modifier.padding(top = 6.dp)) { Chip("Disukai", sub2 == 0) { sub2 = 0 }; Chip("Pustaka", sub2 == 1) { sub2 = 1 } }
                LazyColumn(contentPadding = PaddingValues(bottom = 190.dp)) { itemsIndexed(l) { i, s -> SRow(s) { go(l, i) } } } }
            else -> if (grid) LazyVerticalGrid(GridCells.Fixed(St.grid), contentPadding = PaddingValues(top = 8.dp, bottom = 190.dp)) { items(gr) { e -> GCard(e.value[0], e.key.ifBlank { "Tanpa album" }, "${e.value.size} lagu", t == 4, Modifier) { open((if (t == 3) "al:" else "ar:") + e.key) } } }
            else LazyColumn(contentPadding = PaddingValues(top = 8.dp, bottom = 190.dp)) { items(gr) { e -> Row(Modifier.fillMaxWidth().clickable { open((if (t == 3) "al:" else "ar:") + e.key) }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) { Cv(e.value[0], Modifier.size(52.dp), if (t == 4) 26.dp else 12.dp); Column(Modifier.padding(start = 12.dp)) { Text(e.key.ifBlank { "Tanpa album" }, maxLines = 1); Sub("${e.value.size} lagu") } } } }
        }
    }
    if (newPl) AlertDialog({ newPl = false }, confirmButton = { TextButton({ St.newPl(nn); nn = ""; newPl = false }) { Text("Buat") } }, title = { Text("Playlist baru") }, text = { TextField(nn, { nn = it }, singleLine = true, placeholder = { Text("Nama playlist") }) })
}

@Composable fun Me(sub: (String) -> Unit) = Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp, 12.dp, 16.dp, 190.dp)) {
    Top("Kamu")
    Grp { Item(Icons.Rounded.History, "Riwayat") { sub("hist") }; Item(Icons.Rounded.BarChart, "Statistik dan recap") { sub("stat") } }
    Spacer(Modifier.height(14.dp))
    Grp { Item(Icons.Rounded.GraphicEq, "Ekualiser") { sub("eq") }; Item(Icons.Rounded.Settings, "Pengaturan") { sub("set") }; Item(Icons.Rounded.Info, "Tentang Lumora") { sub("abt") } }
}

fun parentOf(k: String) = if (k.startsWith("s_")) "set" else null

@Composable fun SubScreen(k: String, sg: List<Song>, go: (List<Song>, Int) -> Unit, open: (String) -> Unit, back: () -> Unit) {
    val c = LocalContext.current
    var ver by remember { mutableIntStateOf(0) }; var ask by remember { mutableStateOf(false) }; var per by remember { mutableIntStateOf(0) }; var sf by remember { mutableStateOf("") }; var ss by remember { mutableStateOf("") }
    val exp = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { u: Uri? -> u?.let { try { c.contentResolver.openOutputStream(it)?.use { o -> o.write(exportJson().toByteArray()) } } catch (e: Exception) {} } }
    val imp = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { u: Uri? -> u?.let { try { importJson(c.contentResolver.openInputStream(it)!!.bufferedReader().readText(), c) } catch (e: Exception) {} } }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp, 12.dp, 16.dp, 190.dp)) {
        when {
            k in listOf("like", "all", "top", "week", "month") || k.startsWith("pl:") || k.startsWith("al:") || k.startsWith("ar:") -> {
                val l = listFor(k, sg); Top(titleFor(k), back)
                Row(Modifier.padding(bottom = 8.dp)) { Box(Modifier.accent().clickable { if (l.isNotEmpty()) go(l, 0) }.padding(22.dp, 10.dp)) { Text("Putar", color = Color.White) }; Spacer(Modifier.width(8.dp)); Box(Modifier.glass(99.dp).clickable { if (l.isNotEmpty()) { go(l.shuffled(), 0) } }.padding(22.dp, 10.dp)) { Text("Acak") }
                    if (k.startsWith("pl:")) { Spacer(Modifier.width(8.dp)); IconButton({ St.delPl(k.drop(3)); back() }) { Icon(Icons.Rounded.Delete, null) } } }
                l.forEachIndexed { i, s -> SRow(s) { go(l, i) } }; if (l.isEmpty()) Sub("Belum ada lagu") }
            k == "hist" -> { Top("Riwayat", back); val df = SimpleDateFormat("d MMM yyyy", Locale("id")); val today = System.currentTimeMillis() / 86400000L
                OutlinedTextField(sf, { sf = it }, Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text("Cari di riwayat…") })
                var last = ""; St.hist.take(300).forEach { h -> val s = sg.find { it.id.toString() == h.id } ?: return@forEach
                    if (sf.isNotBlank() && !(s.title + s.artist).contains(sf, true)) return@forEach
                    val d = h.t / 86400000L; val lab = if (d == today) "Hari ini" else if (d == today - 1) "Kemarin" else df.format(Date(h.t)); if (lab != last) { Sec(lab); last = lab }; SRow(s) { go(listOf(s), 0) } }
                if (St.hist.isEmpty()) Sub("Belum ada riwayat") }
            k == "stat" -> { Top("Statistik", back); val pr = listOf("1 minggu" to 7, "1 bulan" to 30, "3 bulan" to 90, "6 bulan" to 180, "1 tahun" to 365, "Semua" to 0)
                Box(Modifier.fillMaxWidth().glass().clickable { open("recap") }.padding(16.dp)) { Column { Text("Recap kamu ${Calendar.getInstance().get(Calendar.YEAR)}", fontWeight = FontWeight.Bold); Sub("Lagu, artis, dan menit teratas") } }
                Row(Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 10.dp)) { pr.forEachIndexed { i, p -> Chip(p.first, per == i) { per = i } } }
                val d = pr[per].second; val lim = if (d == 0) 0L else System.currentTimeMillis() - d * 86400000L; val hs = St.hist.filter { it.t >= lim }; val ids = hs.groupingBy { it.id }.eachCount()
                val ps = sg.filter { ids.containsKey(it.id.toString()) }; val mins = ps.sumOf { (ids[it.id.toString()] ?: 0) * it.dur } / 60000
                Grp { Item(Icons.Rounded.PlayArrow, "${hs.size} putaran", "$mins menit mendengarkan") {}; Item(Icons.Rounded.MusicNote, "${ps.size} lagu • ${ps.map { it.artist }.distinct().size} artis • ${ps.map { it.album }.distinct().size} album") {} }
                Sec("Lagu teratas"); val ts = ps.sortedByDescending { ids[it.id.toString()] }.take(8); ts.forEachIndexed { i, s -> SRow(s) { go(ts, i) } }
                Sec("Artis teratas"); ps.groupBy { it.artist }.entries.sortedByDescending { e -> e.value.sumOf { ids[it.id.toString()] ?: 0 } }.take(5).forEach { e -> Row(Modifier.padding(8.dp)) { Text(e.key, Modifier.weight(1f), maxLines = 1); Text("${e.value.sumOf { ids[it.id.toString()] ?: 0 }}x") } } }
            k == "recap" -> { Top("Recap ${Calendar.getInstance().get(Calendar.YEAR)}", back); val y = Calendar.getInstance().get(Calendar.YEAR)
                val hs = St.hist.filter { Calendar.getInstance().apply { timeInMillis = it.t }.get(Calendar.YEAR) == y }; val ids = hs.groupingBy { it.id }.eachCount(); val ps = sg.filter { ids.containsKey(it.id.toString()) }
                Grp { Item(Icons.Rounded.PlayArrow, "${ps.sumOf { (ids[it.id.toString()] ?: 0) * it.dur } / 60000} menit", "${hs.size} putaran tahun ini") {}
                    ps.groupBy { it.artist }.maxByOrNull { e -> e.value.sumOf { ids[it.id.toString()] ?: 0 } }?.let { Item(Icons.Rounded.Person, "Artis teratas: ${it.key}") {} } }
                Sec("5 lagu teratas"); ps.sortedByDescending { ids[it.id.toString()] }.take(5).forEachIndexed { i, s -> Text("${i + 1}. ${s.title} (${ids[s.id.toString()]}x)", Modifier.padding(8.dp)) } }
            k == "eq" -> { Top("Ekualiser", back); val e = eqObj
                if (e == null) Text("Ekualiser tidak tersedia di perangkat ini") else {
                    var on by remember { mutableStateOf(e.enabled) }; Grp { SwRow("Aktifkan ekualiser", "", on) { on = it; e.enabled = it } }
                    val r = e.bandLevelRange
                    for (b in 0 until e.numberOfBands) key(b) { var v by remember { mutableFloatStateOf(e.getBandLevel(b.toShort()).toFloat()) }
                        Text("${e.getCenterFreq(b.toShort()) / 1000} Hz", Modifier.padding(top = 12.dp), fontSize = 13.sp); Slider(v, { v = it; e.setBandLevel(b.toShort(), it.toInt().toShort()) }, valueRange = r[0].toFloat()..r[1].toFloat()) }
                    Sec("Preset"); Row(Modifier.horizontalScroll(rememberScrollState())) { for (p in 0 until e.numberOfPresets) Chip(e.getPresetName(p.toShort()), false) { e.usePreset(p.toShort()); e.enabled = true; on = true; ver++ } }
                    bbObj?.let { b -> if (b.strengthSupported) { var bv by remember { mutableFloatStateOf(b.roundedStrength.toFloat()) }; Sl("Penguat bass", bv, 0f, 1000f, "", { bv = it; b.setStrength(it.toInt().toShort()); b.enabled = it > 0 }) } } } }
            k == "set" -> { Top("Pengaturan", back); val all = listOf(Triple(Icons.Rounded.PlayArrow, "Suara dan pemutaran", "s_play"), Triple(Icons.Rounded.Palette, "Tampilan", "s_look"), Triple(Icons.Rounded.MusicNote, "Konten", "s_content"), Triple(Icons.Rounded.Lock, "Privasi dan riwayat", "s_priv"), Triple(Icons.Rounded.Storage, "Penyimpanan", "s_store"), Triple(Icons.Rounded.Backup, "Cadangan dan impor", "s_backup"), Triple(Icons.Rounded.Info, "Tentang Lumora", "abt"))
                OutlinedTextField(ss, { ss = it }, Modifier.fillMaxWidth().padding(bottom = 12.dp), singleLine = true, placeholder = { Text("Cari pengaturan…") })
                Grp { all.filter { ss.isBlank() || it.second.contains(ss, true) }.forEach { (ic, n, key) -> Item(ic, n) { open(key) } } } }
            k == "s_play" -> { Top("Suara dan pemutaran", back)
                Grp { TgP("skip", "Lewati keheningan", "Lewati bagian hening di dalam lagu") { applyFx() }; TgP("norm", "Normalisasi audio", "Samakan kelantangan antar lagu") { applyFx() }; ChP("lvl", "Tingkat kelantangan", listOf("Lembut", "Sedang", "Agresif"), 1) { applyFx() }; TgP("vari", "Varispeed", "Nada ikut berubah bersama kecepatan") { applyFx() } }
                Hd("KECEPATAN"); Grp { ChP("spdi", "Kecepatan pemutaran", listOf("0.75x", "1x", "1.25x", "1.5x", "2x"), 1) { St.sp.edit().putFloat("spd", listOf(.75f, 1f, 1.25f, 1.5f, 2f)[St.sp.getInt("spdi", 1)]).apply(); applyFx() } }
                Hd("ANTREAN"); Grp { TgP("pq", "Antrean persisten", "Pulihkan antrean saat aplikasi dibuka", true); TgP("auto", "Putar otomatis", "Lanjut ke lagu acak saat antrean habis", true); TgP("ps", "Ingat status acak dan ulang", "", true); TgP("errskip", "Lewati otomatis jika terjadi kesalahan", "", true) }
                Hd("TIMER TIDUR"); Grp { Row(Modifier.horizontalScroll(rememberScrollState()).padding(16.dp)) { listOf(0, 15, 30, 60).forEach { m -> Chip(if (m == 0) "Mati" else "$m menit", false) { St.sleepAt = if (m == 0) 0 else System.currentTimeMillis() + m * 60000L } } }; SwRow("Berhenti di akhir lagu saat ini", "", St.sleepEnd) { St.sleepEnd = it } }
                Hd("LAINNYA"); Grp { TgP("stop", "Hentikan musik saat aplikasi ditutup"); SwRow("Jaga layar tetap menyala", "Saat pemutar penuh dibuka", St.keepOn) { St.keepOn = it; St.sv() } } }
            k == "s_look" -> { Top("Tampilan", back); Text("Tema warna", Modifier.padding(bottom = 8.dp))
                PAL.chunked(3).forEachIndexed { r, row -> Row(Modifier.padding(bottom = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) { row.forEachIndexed { cc, (n, h) -> val i = r * 3 + cc
                    Box(Modifier.weight(1f).height(64.dp).clip(RoundedCornerShape(18.dp)).background(Brush.linearGradient(listOf(Color.hsv(h[0], .75f, 1f), Color.hsv(h[1], .75f, 1f)))).border(if (St.pal == i) 3.dp else 0.dp, LocalContentColor.current, RoundedCornerShape(18.dp)).clickable { St.pal = i; St.sv() }.padding(8.dp), contentAlignment = Alignment.BottomStart) { Text(n, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold) } } } }
                Text("Mode", Modifier.padding(vertical = 8.dp)); Row { listOf("Sistem", "Terang", "Gelap").forEachIndexed { i, n -> Chip(n, St.mode == i) { St.mode = i; St.sv() } } }
                Hd("EFEK"); Grp { SwRow("Warna dinamis saat musik", "Warna tema berubah mengikuti dentuman", St.dyn) { St.dyn = it; if (!it) St.hue = 0f; St.sv() }; Sl("Kecepatan warna dinamis", St.dspd, .3f, 3f, "x", { St.dspd = it }) { St.sv() }
                    SwRow("Efek kaca (blur)", "Matikan jika terasa berat", St.glass) { St.glass = it; St.sv() }; Sl("Kekuatan kaca", St.gk, .5f, 1.8f, "x", { St.gk = it }) { St.sv() }; SwRow("Efek lensa kaca", "Kilau pada tepi panel", St.lens) { St.lens = it; St.sv() } }
                Hd("GAYA"); Grp { SwRow("Ikon dan sampul bulat", "", St.round) { St.round = it; St.sv() }; SwRow("Streak di Beranda", "", St.streakOn) { St.streakOn = it; St.sv() }; SwRow("Geser mini-player untuk ganti lagu", "", St.swipe) { St.swipe = it; St.sv() } }
                Hd("NAVIGASI"); Grp { Column(Modifier.padding(16.dp, 10.dp)) { Text("Tab awal"); Row(Modifier.horizontalScroll(rememberScrollState())) { listOf("Beranda", "Cari", "Pustaka", "Kamu").forEachIndexed { i, n -> Chip(n, St.startTab == i) { St.startTab = i; St.sv() } } } }
                    SwRow("Sembunyikan label bilah navigasi", "", St.noLabel) { St.noLabel = it; St.sv() }; SwRow("Ciutkan bilah navigasi saat menggulir", "", St.collapse) { St.collapse = it; St.sv() }
                    Column(Modifier.padding(16.dp, 10.dp)) { Text("Ukuran sel grid"); Row { listOf(2, 3, 4).forEach { n -> Chip("$n kolom", St.grid == n) { St.grid = n; St.sv() } } } }; Sl("Skala tampilan", St.scale, .8f, 1.2f, "x", { St.scale = it }) { St.sv() } } }
            k == "s_content" -> { Top("Konten", back)
                Grp { Sl("Durasi minimum lagu (detik)", St.minDur.toFloat(), 0f, 120f, " dtk", { St.minDur = it.toInt() }) { St.sv(); St.reload++ }; Sl("Panjang daftar Teratas Saya", St.topN.toFloat(), 10f, 100f, "", { St.topN = it.toInt() }) { St.sv() }; Sl("Batas hasil pencarian", St.srchMax.toFloat(), 20f, 200f, "", { St.srchMax = it.toInt() }) { St.sv() } }
                Hd("LAINNYA"); Grp { SwRow("Acak urutan layar Beranda", "", St.shufHome) { St.shufHome = it; St.sv() }; ChP("padd", "Posisi penambahan di playlist", listOf("Awal playlist", "Akhir playlist"), 0) } }
            k == "s_priv" -> { Top("Privasi dan riwayat", back)
                Grp { TgP("ph", "Jeda riwayat mendengarkan", "Lagu yang diputar tidak dicatat"); Item(Icons.Rounded.Delete, "Hapus riwayat mendengarkan") { ask = true } }
                Hd("LAINNYA"); Grp { SwRow("Nonaktifkan tangkapan layar", "", St.secure) { St.secure = it; St.sv() } }
                if (ask) AlertDialog({ ask = false }, confirmButton = { TextButton({ St.hist = emptyList(); St.sv(); ask = false }) { Text("Hapus") } }, dismissButton = { TextButton({ ask = false }) { Text("Batal") } }, title = { Text("Hapus riwayat?") }, text = { Text("Riwayat, statistik, dan streak akan direset.") }) }
            k == "s_store" -> { Top("Penyimpanan", back); key(ver) { val dc = c.imageLoader.diskCache
                Grp { Item(Icons.Rounded.MusicNote, "Lagu di perangkat", "${sg.size} lagu") {}; Item(Icons.Rounded.Storage, "Cache gambar", "%.1f MB".format((dc?.size ?: 0L) / 1048576.0)) {}; Item(Icons.Rounded.Delete, "Hapus cache gambar") { dc?.clear(); c.imageLoader.memoryCache?.clear(); ver++ } } } }
            k == "s_backup" -> { Top("Cadangan dan impor", back)
                Grp { Item(Icons.Rounded.Backup, "Cadangkan data", "Pengaturan, disukai, playlist, riwayat") { exp.launch("lumora-backup.json") }; Item(Icons.Rounded.Add, "Impor cadangan", "Pulihkan dari file .json") { imp.launch(arrayOf("application/json", "*/*")) } } }
            else -> { Top("Tentang", back); Text("Lumora 1.0: pemutar musik bergaya liquid glass dengan warna yang hidup mengikuti irama.") }
        }
    }
}

@Composable fun Lumora() {
    val c = LocalContext.current; val act = c as ComponentActivity
    val dark = when (St.mode) { 1 -> false; 2 -> true; else -> isSystemInDarkTheme() }
    SideEffect { St.dark = dark }
    val tx = if (dark) Color(0xFFF4F2FF) else Color(0xFF171428); val bg = if (dark) Color(0xFF0D0B18) else Color(0xFFEEF0FA)
    var songs by remember { mutableStateOf(listOf<Song>()) }
    var tab by remember { mutableIntStateOf(St.startTab) }; var sub by remember { mutableStateOf<String?>(null) }; var full by remember { mutableStateOf(false) }; var collapsed by remember { mutableStateOf(false) }
    val pg = pstate()
    val perm = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { songs = loadSongs(c); St.reload++ }
    LaunchedEffect(Unit) { perm.launch(buildList { add(if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO else Manifest.permission.READ_EXTERNAL_STORAGE); add(Manifest.permission.RECORD_AUDIO); if (Build.VERSION.SDK_INT >= 33) add(Manifest.permission.POST_NOTIFICATIONS) }.toTypedArray()); initFx() }
    LaunchedEffect(St.reload) { songs = loadSongs(c) }
    LaunchedEffect(songs) { if (songs.isNotEmpty() && App.pl.mediaItemCount == 0) {
        if (gb("pq", true)) { val l = St.sp.getString("q", "")!!.split(",").mapNotNull { id -> songs.find { it.id.toString() == id } }; if (l.isNotEmpty()) { App.pl.setMediaItems(l.map { mi(it) }, St.sp.getInt("qi", 0).coerceIn(0, l.size - 1), 0); App.pl.prepare() } }
        if (gb("ps", true)) { App.pl.shuffleModeEnabled = St.sp.getBoolean("shv", false); App.pl.repeatMode = St.sp.getInt("rpv", 0) } } }
    LaunchedEffect(pg.playing, St.dyn) { while (true) { withFrameNanos { }; if (St.dyn && pg.playing) St.hue = (St.hue + (.25f + St.bass * 5f) * St.dspd) % 360f } }
    LaunchedEffect(St.sleepAt) { if (St.sleepAt > 0) { delay(St.sleepAt - System.currentTimeMillis()); if (St.sleepAt > 0) { App.pl.pause(); St.sleepAt = 0 } } }
    LaunchedEffect(St.secure) { if (St.secure) act.window.addFlags(WindowManager.LayoutParams.FLAG_SECURE) else act.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE) }
    LaunchedEffect(full, St.keepOn) { if (full && St.keepOn) act.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) else act.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    val sgState = rememberUpdatedState(songs)
    DisposableEffect(Unit) {
        val p = App.pl
        val l = object : Player.Listener {
            override fun onPlaybackStateChanged(s: Int) { if (s == Player.STATE_ENDED && gb("auto", true) && sgState.value.isNotEmpty()) play(sgState.value.shuffled().take(30), 0) }
            override fun onPlayerError(e: PlaybackException) { if (gb("errskip", true)) { p.prepare(); if (p.hasNextMediaItem()) p.seekToNext() } }
            override fun onMediaItemTransition(m: MediaItem?, r: Int) { if (St.sleepEnd && r == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO) { p.pause(); St.sleepEnd = false } }
        }
        p.addListener(l); onDispose { p.removeListener(l) }
    }
    DisposableEffect(pg.playing) {
        var v: Visualizer? = null
        if (pg.playing) try {
            v = Visualizer(App.pl.audioSessionId).apply {
                captureSize = Visualizer.getCaptureSizeRange()[1]
                setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
                    override fun onWaveFormDataCapture(a: Visualizer?, b: ByteArray?, r: Int) {}
                    override fun onFftDataCapture(a: Visualizer?, f: ByteArray?, r: Int) {
                        if (f == null) return
                        val m = FloatArray(32) { i -> val k = 1 + i * 3; min(1f, sqrt((f[2 * k].toInt().let { it * it } + f[2 * k + 1].toInt().let { it * it }).toFloat()) / 90f) }
                        St.fft = m; St.bass = (m[0] + m[1] + m[2]) / 3f
                    }
                }, Visualizer.getMaxCaptureRate() / 2, false, true)
                enabled = true
            }
        } catch (e: Exception) {}
        onDispose { v?.release(); St.bass = 0f }
    }
    val go: (List<Song>, Int) -> Unit = { l, i -> play(l, i) }
    val open: (String) -> Unit = { sub = it }
    BackHandler(full || sub != null) { if (full) full = false else sub = parentOf(sub!!) }
    val conn = remember { object : NestedScrollConnection { override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset { if (available.y < -6f) collapsed = true else if (available.y > 6f) collapsed = false; return Offset.Zero } } }
    val d = LocalDensity.current
    CompositionLocalProvider(LocalDensity provides Density(d.density * St.scale, d.fontScale)) {
        MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) {
            CompositionLocalProvider(LocalContentColor provides tx) {
                Box(Modifier.fillMaxSize().background(bg)) {
                    Blobs()
                    Box(Modifier.fillMaxSize().statusBarsPadding().nestedScroll(conn)) {
                        val k = sub
                        if (k != null) SubScreen(k, songs, go, open) { sub = parentOf(k) } else when (tab) { 0 -> Home(songs, go, open); 1 -> Search(songs, go); 2 -> Library(songs, go, open); else -> Me { sub = it } }
                    }
                    Column(Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(12.dp, 0.dp, 12.dp, 10.dp)) {
                        val cur = songs.find { it.id.toString() == pg.id }
                        if (cur != null) { Mini(cur, pg) { full = true }; Spacer(Modifier.height(8.dp)) }
                        val lab = !St.noLabel && !(St.collapse && collapsed)
                        Row(Modifier.fillMaxWidth().glass(34.dp).padding(7.dp)) {
                            listOf(Icons.Rounded.Home to "Beranda", Icons.Rounded.Search to "Cari", Icons.Rounded.LibraryMusic to "Pustaka", Icons.Rounded.Person to "Kamu").forEachIndexed { i, (ic, n) ->
                                Column(Modifier.weight(1f).clip(RoundedCornerShape(26.dp)).then(if (tab == i) Modifier.glass(26.dp) else Modifier).clickable { tab = i; sub = null }.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(ic, null, tint = if (tab == i) tx else tx.copy(.55f)); if (lab) Text(n, fontSize = 11.sp, color = if (tab == i) tx else tx.copy(.55f)) } }
                        }
                    }
                    AnimatedVisibility(full, enter = slideInVertically { it }, exit = slideOutVertically { it }) { Full(songs.find { it.id.toString() == pg.id }, pg) { full = false } }
                    St.menuSong?.let { s ->
                        AlertDialog({ St.menuSong = null }, confirmButton = { TextButton({ St.menuSong = null }) { Text("Tutup") } }, title = { Text(s.title, maxLines = 1) }, text = { Column {
                            TextButton({ if (App.pl.mediaItemCount == 0) play(listOf(s), 0) else App.pl.addMediaItem(mi(s)); St.menuSong = null }) { Text("Tambah ke antrean") }
                            TextButton({ val p = App.pl; if (p.mediaItemCount == 0) play(listOf(s), 0) else p.addMediaItem(p.currentMediaItemIndex + 1, mi(s)); St.menuSong = null }) { Text("Putar berikutnya") }
                            TextButton({ St.like(s.id.toString()); St.menuSong = null }) { Text(if (s.id.toString() in St.likes) "Hapus dari Disukai" else "Suka") }
                            St.pls.keys.forEach { n -> TextButton({ St.addTo(n, s.id.toString()); St.menuSong = null }) { Text("Ke playlist: $n") } } } })
                    }
                }
            }
        }
    }
}
