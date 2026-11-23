package id.app.rplayer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Satu-satunya tempat nilai warna mentah muncul; berasal dari tokens.json. */
private object Palette {
    val Primary = Color(0xFF7C5CFC)
    val OnPrimary = Color(0xFFFFFFFF)
    val PrimaryContainer = Color(0xFFE9E0FF)
    val OnPrimaryContainer = Color(0xFF25165B)
    val Secondary = Color(0xFF4F6472)
    val Surface = Color(0xFFFDFBFF)
    val SurfaceVariant = Color(0xFFEDE9F2)
    val OnSurface = Color(0xFF1A1720)
    val OnSurfaceVariant = Color(0xFF49454F)
    val Error = Color(0xFFB3261E)
}

private val LightColors = lightColorScheme(
    primary = Palette.Primary,
    onPrimary = Palette.OnPrimary,
    primaryContainer = Palette.PrimaryContainer,
    onPrimaryContainer = Palette.OnPrimaryContainer,
    secondary = Palette.Secondary,
    surface = Palette.Surface,
    surfaceVariant = Palette.SurfaceVariant,
    onSurface = Palette.OnSurface,
    onSurfaceVariant = Palette.OnSurfaceVariant,
    error = Palette.Error,
)

private val DarkColors = darkColorScheme(
    primary = Palette.Primary,
    onPrimary = Palette.OnPrimary,
    primaryContainer = Palette.OnPrimaryContainer,
    onPrimaryContainer = Palette.PrimaryContainer,
    secondary = Palette.Secondary,
    surface = Palette.OnSurface,
    surfaceVariant = Palette.OnSurfaceVariant,
    onSurface = Palette.Surface,
    onSurfaceVariant = Palette.SurfaceVariant,
    error = Palette.Error,
)

private val AppTypography = Typography()

@Composable
fun RPlayerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content,
    )
}
