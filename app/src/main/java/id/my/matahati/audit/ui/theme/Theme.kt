package id.my.matahati.audit.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


private val LightColorScheme = lightColorScheme(
    primary = PrimaryRed,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = LightRedBackground,
    surface = Color.White,
    onPrimary = Color.White
)

@Composable
fun matahati_AuditTheme(
    content: @Composable () -> Unit
) {
    // Force Light Mode to maintain visual consistency
    val colorScheme = LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}