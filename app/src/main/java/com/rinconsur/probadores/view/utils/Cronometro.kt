package com.rinconsur.probadores.view.utils

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.StateFlow

/**
 * Cronómetro de uso de un probador. Observa el reloj del ViewModel (StateFlow) de modo que solo
 * este componente se recompone cada segundo, no toda la pantalla.
 */
@Composable
fun Cronometro(
    desde: Long,
    ahora: StateFlow<Long>,
    modifier: Modifier = Modifier,
    estilo: TextStyle = MaterialTheme.typography.headlineSmall,
    color: Color = Color.Unspecified
) {
    val t by ahora.collectAsStateWithLifecycle()
    Text(
        text = Formato.mmss((t - desde) / 1000),
        style = estilo,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = modifier
    )
}

/** Cronómetro + barra de progreso hacia el tiempo máximo de uso. */
@Composable
fun BloqueTiempo(
    desde: Long,
    ahora: StateFlow<Long>,
    limiteMinutos: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    val t by ahora.collectAsStateWithLifecycle()
    val segundos = ((t - desde) / 1000).coerceAtLeast(0)
    val progreso = (segundos / (limiteMinutos * 60f)).coerceIn(0f, 1f)
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = Formato.mmss(segundos),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progreso },
            color = color,
            trackColor = color.copy(alpha = 0.25f),
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Límite: $limiteMinutos min",
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}
