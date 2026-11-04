package com.rinconsur.probadores.view.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.rinconsur.probadores.repository.models.EstadoProbador

private val LightColors = lightColorScheme(
    primary = Color(0xFF00695C),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB2DFDB),
    onPrimaryContainer = Color(0xFF00201C),
    secondary = Color(0xFF3949AB),
    tertiary = Color(0xFFF9A825)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF80CBC4),
    onPrimary = Color(0xFF00201C),
    primaryContainer = Color(0xFF004D40),
    onPrimaryContainer = Color(0xFFB2DFDB),
    secondary = Color(0xFF9FA8DA),
    tertiary = Color(0xFFFFE082)
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content
    )
}

/** Colores semánticos de cada estado de probador (verde / rojo / naranja / gris). */
data class ColoresEstado(val contenedor: Color, val contenido: Color, val acento: Color)

@Composable
fun coloresDeEstado(estado: EstadoProbador, enAlerta: Boolean): ColoresEstado {
    val oscuro = isSystemInDarkTheme()
    return when {
        enAlerta -> if (oscuro) ColoresEstado(Color(0xFF5D3A00), Color(0xFFFFE0B2), Color(0xFFEF6C00))
        else ColoresEstado(Color(0xFFFFE0B2), Color(0xFF5D3A00), Color(0xFFE65100))
        estado == EstadoProbador.LIBRE -> if (oscuro) ColoresEstado(Color(0xFF1B3D1F), Color(0xFFC8E6C9), Color(0xFF2E7D32))
        else ColoresEstado(Color(0xFFC8E6C9), Color(0xFF1B5E20), Color(0xFF2E7D32))
        estado == EstadoProbador.OCUPADO -> if (oscuro) ColoresEstado(Color(0xFF4A1C1C), Color(0xFFFFCDD2), Color(0xFFC62828))
        else ColoresEstado(Color(0xFFFFCDD2), Color(0xFF7F0000), Color(0xFFC62828))
        else -> if (oscuro) ColoresEstado(Color(0xFF3A3A3A), Color(0xFFE0E0E0), Color(0xFF757575))
        else ColoresEstado(Color(0xFFE0E0E0), Color(0xFF303030), Color(0xFF616161))
    }
}

/** Verde de "campo válido" reutilizable en formularios. */
val VerdeOk = Color(0xFF2E7D32)
