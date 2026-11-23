package id.app.rplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import id.app.rplayer.ui.AppRoot
import id.app.rplayer.ui.theme.RPlayerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as RPlayerApplication
        setContent {
            RPlayerTheme {
                CompositionLocalProvider(LocalAppContainer provides app.container) {
                    AppRoot()
                }
            }
        }
    }
}
