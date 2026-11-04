package com.rinconsur.probadores.view.ui

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.rinconsur.probadores.repository.models.Incidencia
import com.rinconsur.probadores.view.theme.VerdeOk
import com.rinconsur.probadores.view.utils.Formato
import com.rinconsur.probadores.viewmodels.IncidenciaViewModel

@Composable
fun IncidenciaListScreen(
    onAdd: () -> Unit,
    onEdit: (Int) -> Unit,
    viewModel: IncidenciaViewModel = viewModel()
) {
    val lista by viewModel.incidencias.collectAsStateWithLifecycle()
    var aEliminar by remember { mutableStateOf<Incidencia?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (lista.isEmpty()) {
            Text("No hay incidencias. Presiona + para reportar una.", modifier = Modifier.align(Alignment.Center))
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(lista, key = { it.incidencia.id }) { item ->
                    val inc = item.incidencia
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (inc.resuelta) MaterialTheme.colorScheme.surfaceVariant
                            else MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                            if (inc.fotoUri != null) {
                                AsyncImage(
                                    model = inc.fotoUri,
                                    contentDescription = "Evidencia",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp))
                                )
                            } else {
                                Icon(
                                    if (inc.resuelta) Icons.Default.CheckCircle else Icons.Default.ReportProblem,
                                    contentDescription = null,
                                    tint = if (inc.resuelta) VerdeOk else MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Probador ${item.codigo} · ${if (inc.resuelta) "Resuelta" else "Pendiente"}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(inc.descripcion, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "${Formato.fechaHora(inc.fecha)} · ${inc.reportadoPor}",
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Row {
                                    IconButton(onClick = { viewModel.alternarResuelta(inc) }) {
                                        Icon(
                                            if (inc.resuelta) Icons.Default.Replay else Icons.Default.CheckCircle,
                                            contentDescription = if (inc.resuelta) "Reabrir" else "Marcar resuelta"
                                        )
                                    }
                                    IconButton(onClick = { onEdit(inc.id) }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                                    }
                                    IconButton(onClick = { aEliminar = inc }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onAdd,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) { Icon(Icons.Default.Add, contentDescription = "Reportar incidencia") }
    }

    aEliminar?.let { inc ->
        AlertDialog(
            onDismissRequest = { aEliminar = null },
            title = { Text("Eliminar incidencia") },
            text = { Text("¿Eliminar este reporte?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminar(inc)
                    aEliminar = null
                }) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { aEliminar = null }) { Text("Cancelar") } }
        )
    }
}
