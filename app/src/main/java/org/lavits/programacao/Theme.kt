package org.lavits.programacao

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Paleta clara: papel levemente esverdeado, tinta quase preta, verde-petróleo de acento.
private val LightColors = lightColorScheme(
    primary = Color(0xFF1F6F63),
    onPrimary = Color(0xFFF4F5F0),
    primaryContainer = Color(0xFFCFE0DA),
    onPrimaryContainer = Color(0xFF0A2621),
    secondary = Color(0xFF54594C),
    onSecondary = Color(0xFFF4F5F0),
    background = Color(0xFFE7E9E2),
    onBackground = Color(0xFF1B1E19),
    surface = Color(0xFFF4F5F0),
    onSurface = Color(0xFF1B1E19),
    surfaceVariant = Color(0xFFDCDECE),
    onSurfaceVariant = Color(0xFF54594C),
    outline = Color(0xFFC7CBB8),
    outlineVariant = Color(0xFFD5D8C6),
    error = Color(0xFF8C3A1B),
    onError = Color(0xFFF4F5F0)
)

// Paleta escura: mesma lógica invertida, acento em turquesa para manter contraste.
private val DarkColors = darkColorScheme(
    primary = Color(0xFF4FC9B4),
    onPrimary = Color(0xFF0E1613),
    primaryContainer = Color(0xFF1B3B35),
    onPrimaryContainer = Color(0xFFB8E8DE),
    secondary = Color(0xFFAEB29F),
    onSecondary = Color(0xFF14160F),
    background = Color(0xFF14160F),
    onBackground = Color(0xFFE7E7DC),
    surface = Color(0xFF1B1E15),
    onSurface = Color(0xFFE7E7DC),
    surfaceVariant = Color(0xFF23271B),
    onSurfaceVariant = Color(0xFFAEB29F),
    outline = Color(0xFF33372A),
    outlineVariant = Color(0xFF2A2E22),
    error = Color(0xFFEF8256),
    onError = Color(0xFF14160F)
)

@Composable
fun LavitsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}

/** Cor de identificação de cada tipo de atividade, legível nos dois temas. */
@Composable
fun colorOf(type: SessionType): Color {
    val dark = isSystemInDarkTheme()
    return when (type) {
        SessionType.ST -> if (dark) Color(0xFF4FC9B4) else Color(0xFF1F6F63)
        SessionType.SL -> if (dark) Color(0xFFE0BA52) else Color(0xFF8A6D1F)
        SessionType.OF -> if (dark) Color(0xFF8FA6DE) else Color(0xFF38507A)
        SessionType.PA -> if (dark) Color(0xFFDE8FC0) else Color(0xFF7A3B63)
        SessionType.TR -> if (dark) Color(0xFFEF8256) else Color(0xFFA8451F)
        SessionType.EV -> if (dark) Color(0xFF767A69) else Color(0xFF868C7A)
    }
}
