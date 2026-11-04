package com.rinconsur.probadores.view.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rinconsur.probadores.repository.models.EstadoProbador
import com.rinconsur.probadores.repository.models.Probador
import com.rinconsur.probadores.view.theme.coloresDeEstado
import com.rinconsur.probadores.view.utils.BloqueTiempo
import com.rinconsur.probadores.view.utils.CampoTexto
import com.rinconsur.probadores.view.utils.pulsoAlerta
import com.rinconsur.probadores.viewmodels.FiltroProbadores
import com.rinconsur.probadores.viewmodels.ProbadorViewModel
import com.rinconsur.probadores.viewmodels.ReglasProbadores

@Composable
fun ProbadorListScreen(
    viewModel: ProbadorViewModel,
    esAdmin: Boolean,
    onAdd: () -> Unit,
    onEdit: (Int) -> Unit,
    onReportar: (Int) -> Unit
) {
    val lista by viewModel.probadoresFiltrados.collectAsStateWithLifecycle()
    val filtro by viewModel.filtro.collectAsStateWithLifecycle()
    val cfg by viewModel.configuracion.collectAsStateWithLifecycle()
    val ahoraMs by viewModel.ahora.collectAsStateWithLifecycle()
    val ocupar by viewModel.ocupar.collectAsStateWithLifecycle()
    val mensaje by viewModel.mensaje.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbar.showSnackbar(it)
            viewModel.limpiarMensaje()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Filtros por estado
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(FiltroProbadores.entries.toList()) { f ->
                    FilterChip(
                        selected = f == filtro,
                        onClick = { viewModel.setFiltro(f) },
                        label = { Text(f.etiqueta) }
                    )
                }
            }

            if (lista.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay probadores en este filtro.")
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 88.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(lista, key = { it.id }) { p ->
                        val alerta = ReglasProbadores.enAlerta(p, ahoraMs, cfg.limiteMinutos)
                        TarjetaProbador(
                            p = p,
                            enAlerta = alerta,
                            limiteMinutos = cfg.limiteMinutos,
                            viewModel = viewModel,
                            esAdmin = esAdmin,
                            onEdit = { onEdit(p.id) },
                            onReportar = { onReportar(p.id) }
                        )
                    }
                }
            }
        }

        if (esAdmin) {
            FloatingActionButton(
                onClick = onAdd,
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar probador")
            }
        }

        SnackbarHost(
            hostState = snackbar,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp)
        )
    }

    // Diálogo para ocupar un probador (estado y validación viven en el ViewModel)
    ocupar?.let { estado ->
        AlertDialog(
            onDismissRequest = viewModel::cerrarOcupar,
            icon = { Icon(Icons.Default.Checkroom, contentDescription = null) },
            title = { Text("Ocupar probador ${estado.codigo}") },
            text = {
                CampoTexto(
                    value = estado.prendas,
                    onValueChange = viewModel::onPrendasChange,
                    label = "Cantidad de prendas",
                    icono = Icons.Default.Checkroom,
                    error = estado.error,
                    ayuda = "Entre 1 y 10 prendas",
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                )
            },
            confirmButton = { TextButton(onClick = viewModel::confirmarOcupar) { Text("Ocupar") } },
            dismissButton = { TextButton(onClick = viewModel::cerrarOcupar) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun TarjetaProbador(
    p: Probador,
    enAlerta: Boolean,
    limiteMinutos: Int,
    viewModel: ProbadorViewModel,
    esAdmin: Boolean,
    onEdit: () -> Unit,
    onReportar: () -> Unit
) {
    val colores = coloresDeEstado(p.estado, enAlerta)
    // Animación: el color de la tarjeta cambia suavemente al cambiar de estado
    val contenedor by animateColorAsState(colores.contenedor, tween(500), label = "colorContenedor")
    val contenido by animateColorAsState(colores.contenido, tween(500), label = "colorContenido")
    val acento by animateColorAsState(colores.acento, tween(500), label = "colorAcento")

    Card(
        colors = CardDefaults.cardColors(containerColor = contenedor, contentColor = contenido),
        modifier = Modifier.fillMaxWidth().animateContentSize()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (enAlerta) Icons.Default.Warning else Icons.Default.MeetingRoom,
                    contentDescription = null,
                    tint = acento,
                    modifier = if (enAlerta) Modifier.size(28.dp).pulsoAlerta() else Modifier.size(28.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(p.codigo, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Text("Zona ${p.zona}", style = MaterialTheme.typography.bodySmall)
            Text(
                if (enAlerta) "¡Uso prolongado!" else p.estado.etiqueta,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = acento
            )

            if (p.estado == EstadoProbador.OCUPADO && p.ocupadoDesde != null) {
                BloqueTiempo(
                    desde = p.ocupadoDesde,
                    ahora = viewModel.ahora,
                    limiteMinutos = limiteMinutos,
                    color = acento
                )
                Text("${p.prendas} prendas", style = MaterialTheme.typography.bodySmall)
            }

            // Acción principal según el estado
            val (textoAccion, iconoAccion) = when (p.estado) {
                EstadoProbador.LIBRE -> "Ocupar" to Icons.Default.Checkroom
                EstadoProbador.OCUPADO -> "Liberar" to Icons.Default.LockOpen
                EstadoProbador.MANTENCION -> "Habilitar" to Icons.Default.Build
            }
            Button(
                onClick = {
                    when (p.estado) {
                        EstadoProbador.LIBRE -> viewModel.abrirOcupar(p)
                        EstadoProbador.OCUPADO -> viewModel.liberar(p)
                        EstadoProbador.MANTENCION -> viewModel.alternarMantencion(p)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = acento, contentColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(iconoAccion, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(textoAccion)
            }

            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onReportar, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Default.ReportProblem, contentDescription = "Reportar incidencia")
                }
                if (p.estado != EstadoProbador.OCUPADO) {
                    IconButton(onClick = { viewModel.alternarMantencion(p) }, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Default.Build, contentDescription = "Mantención")
                    }
                }
                if (esAdmin) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar probador")
                    }
                }
            }
        }
    }
}
