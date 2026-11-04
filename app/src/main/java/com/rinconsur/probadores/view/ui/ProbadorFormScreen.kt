package com.rinconsur.probadores.view.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rinconsur.probadores.repository.models.Probador
import com.rinconsur.probadores.view.utils.AnimacionEntradaFormulario
import com.rinconsur.probadores.view.utils.CampoDesplegable
import com.rinconsur.probadores.view.utils.CampoTexto
import com.rinconsur.probadores.viewmodels.ProbadorFormViewModel

@Composable
fun ProbadorFormScreen(probadorId: Int, onDone: () -> Unit, viewModel: ProbadorFormViewModel = viewModel()) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    var visible by remember { mutableStateOf(false) }
    var confirmarEliminar by remember { mutableStateOf(false) }

    LaunchedEffect(probadorId) {
        viewModel.cargar(probadorId)
        visible = true
    }
    LaunchedEffect(ui.terminado) { if (ui.terminado) onDone() }

    // ANIMACIÓN 1 (view/utils/Animations.kt): fade + deslizamiento vertical
    AnimacionEntradaFormulario(visible = visible) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                if (ui.esEdicion) "Editar probador" else "Nuevo probador",
                style = MaterialTheme.typography.headlineMedium
            )

            CampoTexto(
                value = ui.codigo,
                onValueChange = viewModel::onCodigoChange,
                label = "Código del probador",
                icono = Icons.Default.Badge,
                error = ui.codigoError,
                ayuda = "Ejemplo: P-09"
            )
            CampoDesplegable(
                value = ui.zona,
                label = "Zona de vestuario",
                icono = Icons.Default.Place,
                opciones = Probador.ZONAS,
                error = ui.zonaError,
                onSeleccion = { viewModel.onZonaChange(Probador.ZONAS[it]) }
            )

            ui.mensaje?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Button(onClick = viewModel::guardar, modifier = Modifier.fillMaxWidth()) {
                Text(if (ui.esEdicion) "Guardar cambios" else "Crear probador")
            }
            OutlinedButton(onClick = onDone, modifier = Modifier.fillMaxWidth()) { Text("Cancelar") }

            if (ui.esEdicion) {
                OutlinedButton(
                    onClick = { confirmarEliminar = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Eliminar probador") }
            }
        }
    }

    if (confirmarEliminar) {
        AlertDialog(
            onDismissRequest = { confirmarEliminar = false },
            title = { Text("Eliminar probador") },
            text = { Text("Se eliminarán también su historial e incidencias. ¿Continuar?") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarEliminar = false
                    viewModel.eliminar()
                }) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { confirmarEliminar = false }) { Text("Cancelar") } }
        )
    }
}
