package id.app.rplayer

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import id.app.rplayer.data.LibraryRepository
import id.app.rplayer.data.db.AppDatabase
import id.app.rplayer.data.media.MediaScanner
import id.app.rplayer.player.PlayerController

/** Manual DI: cukup untuk Phase 1, tanpa Hilt/kapt. */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val database = AppDatabase.create(appContext)

    val repository = LibraryRepository(MediaScanner(appContext), database.libraryDao())
    val player = PlayerController(appContext)
}

val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer belum disediakan")
}
