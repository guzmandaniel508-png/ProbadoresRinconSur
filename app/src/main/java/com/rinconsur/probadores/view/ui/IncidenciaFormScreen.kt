package com.rinconsur.probadores.view.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rinconsur.probadores.view.utils.AnimacionEntradaFormulario
import com.rinconsur.probadores.view.utils.CameraCapture
import com.rinconsur.probadores.view.utils.CampoDesplegable
import com.rinconsur.probadores.view.utils.CampoTexto
import com.rinconsur.probadores.view.utils.PhotoBox
import com.rinconsur.probadores.viewmodels.IncidenciaFormViewModel

@Composable
fun IncidenciaFormScreen(
    incidenciaId: Int,
    probadorIdInicial: Int,
    reportadoPor: String,
    onDone: () -> Unit,
    viewModel: IncidenciaFormViewModel = viewModel()
) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val probadores by viewModel.probadores.collectAsStateWithLifecycle()
    var mostrarCamara by rememberSaveable { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(incidenciaId, probadorIdInicial) {
        viewModel.iniciar(incidenciaId, probadorIdInicial)
        visible = true
    }
    LaunchedEffect(ui.terminado) { if (ui.terminado) onDone() }

    if (mostrarCamara) {
        // RECURSO NATIVO 1: cámara (evidencia de prenda abandonada)
        CameraCapture(
            carpeta = "incidencias",
            onImageCaptured = { uri ->
                viewModel.onFotoChange(uri.toString())
                mostrarCamara = false
            },
            onClose = { mostrarCamara = false }
        )
    } else {
        AnimacionEntradaFormulario(visible = visible) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    if (ui.esEdicion) "Editar incidencia" else "Reportar incidencia",
                    style = MaterialTheme.typography.headlineMedium
                )

                val seleccionado = probadores.firstOrNull { it.id == ui.probadorId }
                CampoDesplegable(
                    value = seleccionado?.let { "${it.codigo} · ${it.zona}" } ?: "",
                    label = "Probador",
                    icono = Icons.Default.MeetingRoom,
                    opciones = probadores.map { "${it.codigo} · ${it.zona}" },
                    error = ui.probadorError,
                    onSeleccion = { viewModel.onProbadorChange(probadores[it].id) }
                )

                CampoTexto(
                    value = ui.descripcion,
                    onValueChange = viewModel::onDescripcionChange,
                    label = "Descripción",
                    icono = Icons.Default.Description,
                    error = ui.descripcionError,
                    ayuda = "${ui.descripcion.length}/200 · Ej: prendas abandonadas en el probador",
                    singleLine = false,
                    minLines = 3
                )

                Text("Foto de evidencia (opcional)", style = MaterialTheme.typography.titleSmall)
                PhotoBox(imageUri = ui.fotoUri, circle = false, onTakePhoto = { mostrarCamara = true })

                Button(
                    onClick = { viewModel.guardar(reportadoPor) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(if (ui.esEdicion) "Guardar cambios" else "Enviar reporte") }
                OutlinedButton(onClick = onDone, modifier = Modifier.fillMaxWidth()) { Text("Cancelar") }
            }
        }
    }
}
