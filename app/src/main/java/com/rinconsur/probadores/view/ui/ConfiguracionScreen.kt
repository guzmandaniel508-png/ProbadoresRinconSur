package com.rinconsur.probadores.view.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rinconsur.probadores.view.theme.VerdeOk
import com.rinconsur.probadores.view.utils.AnimacionEntradaFormulario
import com.rinconsur.probadores.view.utils.CampoTexto
import com.rinconsur.probadores.viewmodels.ConfiguracionViewModel

@Composable
fun ConfiguracionScreen(viewModel: ConfiguracionViewModel = viewModel()) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimacionEntradaFormulario(visible = visible) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Configuración de alertas", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Cuando un probador supera este tiempo de uso, la app envía una notificación y vibra para avisar al personal.",
                style = MaterialTheme.typography.bodyMedium
            )

            CampoTexto(
                value = ui.limite,
                onValueChange = viewModel::onLimiteChange,
                label = "Tiempo máximo de uso (minutos)",
                icono = Icons.Default.Timer,
                error = ui.limiteError,
                ayuda = "Entre 1 y 120 minutos",
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            )

            Button(onClick = viewModel::guardar, modifier = Modifier.fillMaxWidth()) { Text("Guardar") }

            if (ui.guardado) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = VerdeOk)
                    Spacer(Modifier.width(8.dp))
                    Text("Configuración guardada", color = VerdeOk)
                }
            }
        }
    }
}
