package com.rinconsur.probadores.view.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rinconsur.probadores.navegations.Routes
import com.rinconsur.probadores.recursos.NotificacionHelper
import com.rinconsur.probadores.repository.models.EstadoProbador
import com.rinconsur.probadores.view.theme.coloresDeEstado
import com.rinconsur.probadores.view.utils.Cronometro
import com.rinconsur.probadores.view.utils.pulsoAlerta
import com.rinconsur.probadores.viewmodels.FiltroProbadores
import com.rinconsur.probadores.viewmodels.ProbadorViewModel

/** Panel de inicio: resumen de ocupación, fila de espera y alertas de uso prolongado. */
@Composable
fun HomeScreen(
    userName: String,
    viewModel: ProbadorViewModel,
    onNavigate: (String) -> Unit
) {
    val resumen by viewModel.resumen.collectAsStateWithLifecycle()
    val alertas by viewModel.enAlerta.collectAsStateWithLifecycle()

    // RECURSO NATIVO 2: permiso de notificaciones (Android 13+)
    val context = LocalContext.current
    var permisoNotif by remember { mutableStateOf(NotificacionHelper.tienePermiso(context)) }
    val lanzadorPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        permisoNotif = it
    }
    LaunchedEffect(Unit) {
        if (!permisoNotif && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            lanzadorPermiso.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Hola, $userName", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Estado de los probadores en este momento",
            style = MaterialTheme.typography.bodyLarge
        )

        // Aviso si el permiso de notificaciones fue denegado
        if (!permisoNotif) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.NotificationsOff, contentDescription = null)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Notificaciones desactivadas", fontWeight = FontWeight.Bold)
                        Text("Actívalas para recibir alertas de uso prolongado.", style = MaterialTheme.typography.bodySmall)
                    }
                    OutlinedButton(onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            lanzadorPermiso.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }) { Text("Activar") }
                }
            }
        }

        // Contadores (animados con animateIntAsState)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TarjetaResumen(
                "Libres", resumen.libres, Icons.Default.CheckCircle,
                EstadoProbador.LIBRE, false, Modifier.weight(1f)
            ) { viewModel.setFiltro(FiltroProbadores.LIBRES); onNavigate(Routes.PROBADORES) }
            TarjetaResumen(
                "Ocupados", resumen.ocupados, Icons.Default.MeetingRoom,
                EstadoProbador.OCUPADO, false, Modifier.weight(1f)
            ) { viewModel.setFiltro(FiltroProbadores.OCUPADOS); onNavigate(Routes.PROBADORES) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TarjetaResumen(
                "En alerta", resumen.enAlerta, Icons.Default.Warning,
                EstadoProbador.OCUPADO, true, Modifier.weight(1f)
            ) { viewModel.setFiltro(FiltroProbadores.ALERTA); onNavigate(Routes.PROBADORES) }
            TarjetaResumen(
                "Mantención", resumen.mantencion, Icons.Default.ReportProblem,
                EstadoProbador.MANTENCION, false, Modifier.weight(1f)
            ) { viewModel.setFiltro(FiltroProbadores.MANTENCION); onNavigate(Routes.PROBADORES) }
        }

        // Fila de espera
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("Clientes esperando", style = MaterialTheme.typography.titleMedium)
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalIconButton(
                        onClick = { viewModel.cambiarEspera(-1) },
                        enabled = resumen.clientesEnEspera > 0
                    ) { Icon(Icons.Default.Remove, contentDescription = "Menos un cliente") }

                    // Animación del número al cambiar (sube o baja deslizando)
                    AnimatedContent(
                        targetState = resumen.clientesEnEspera,
                        transitionSpec = {
                            if (targetState > initialState) {
                                (slideInVertically { -it } + fadeIn()) togetherWith (slideOutVertically { it } + fadeOut())
                            } else {
                                (slideInVertically { it } + fadeIn()) togetherWith (slideOutVertically { -it } + fadeOut())
                            }
                        },
                        label = "clientesEnEspera",
                        modifier = Modifier.padding(horizontal = 32.dp)
                    ) { cantidad ->
                        Text(
                            "$cantidad",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    FilledTonalIconButton(onClick = { viewModel.cambiarEspera(1) }) {
                        Icon(Icons.Default.Add, contentDescription = "Más un cliente")
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.HourglassTop, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (resumen.esperaEstimadaMin == 0) "Sin espera estimada"
                        else "Espera estimada: ~${resumen.esperaEstimadaMin} min",
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
        }

        // Alertas activas
        Text("Alertas de uso prolongado", style = MaterialTheme.typography.titleMedium)
        if (alertas.isEmpty()) {
            Text(
                "No hay probadores sobre el límite de ${resumen.limiteMinutos} min.",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            alertas.forEach { p ->
                val colores = coloresDeEstado(p.estado, true)
                Card(
                    colors = CardDefaults.cardColors(containerColor = colores.contenedor, contentColor = colores.contenido),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Warning, contentDescription = null, tint = colores.acento,
                            modifier = Modifier.size(32.dp).pulsoAlerta()
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Probador ${p.codigo} · ${p.zona}", fontWeight = FontWeight.Bold)
                            Text("${p.prendas} prendas dentro", style = MaterialTheme.typography.bodySmall)
                        }
                        p.ocupadoDesde?.let { desde ->
                            Cronometro(desde = desde, ahora = viewModel.ahora, color = colores.acento)
                        }
                    }
                }
            }
        }

        // Accesos rápidos
        Button(onClick = { viewModel.setFiltro(FiltroProbadores.TODOS); onNavigate(Routes.PROBADORES) }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.MeetingRoom, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Ver todos los probadores")
        }
        OutlinedButton(onClick = { onNavigate(Routes.INCIDENCIAS) }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.ReportProblem, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Ver incidencias")
        }
    }
}

@Composable
private fun TarjetaResumen(
    titulo: String,
    valor: Int,
    icono: ImageVector,
    estado: EstadoProbador,
    alerta: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val colores = coloresDeEstado(estado, alerta)
    // Animación del contador: el número "corre" hasta el nuevo valor
    val valorAnimado by animateIntAsState(targetValue = valor, animationSpec = tween(600), label = "contador")
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = colores.contenedor, contentColor = colores.contenido),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icono, contentDescription = null, tint = colores.acento)
                Spacer(Modifier.width(8.dp))
                Text(titulo, style = MaterialTheme.typography.titleSmall)
            }
            Text(
                "$valorAnimado",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = if (valor > 0 || estado == EstadoProbador.LIBRE) colores.contenido else Color.Unspecified
            )
        }
    }
}
