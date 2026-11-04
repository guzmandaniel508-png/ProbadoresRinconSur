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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rinconsur.probadores.view.utils.AnimacionEntradaFormulario
import com.rinconsur.probadores.view.utils.CameraCapture
import com.rinconsur.probadores.view.utils.CampoDesplegable
import com.rinconsur.probadores.view.utils.CampoTexto
import com.rinconsur.probadores.view.utils.PhotoBox
import com.rinconsur.probadores.viewmodels.PersonalFormViewModel

@Composable
fun PersonalFormScreen(userId: Int, onDone: () -> Unit, viewModel: PersonalFormViewModel = viewModel()) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val roles by viewModel.roles.collectAsStateWithLifecycle()
    var mostrarCamara by rememberSaveable { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        viewModel.cargar(userId)
        visible = true
    }
    LaunchedEffect(ui.terminado) { if (ui.terminado) onDone() }

    if (mostrarCamara) {
        CameraCapture(
            carpeta = "personal",
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
                    if (ui.esEdicion) "Editar personal" else "Nuevo personal",
                    style = MaterialTheme.typography.headlineMedium
                )

                PhotoBox(imageUri = ui.fotoUri, circle = true, onTakePhoto = { mostrarCamara = true })

                CampoTexto(
                    value = ui.nombre,
                    onValueChange = viewModel::onNombreChange,
                    label = "Nombre completo",
                    icono = Icons.Default.Person,
                    error = ui.nombreError,
                    ayuda = "Solo letras y un espacio entre palabras"
                )
                CampoTexto(
                    value = ui.email,
                    onValueChange = viewModel::onEmailChange,
                    label = "Correo",
                    icono = Icons.Default.Email,
                    error = ui.emailError,
                    keyboardType = KeyboardType.Email
                )
                CampoTexto(
                    value = ui.password,
                    onValueChange = viewModel::onPasswordChange,
                    label = "Contraseña",
                    icono = Icons.Default.Lock,
                    error = ui.passwordError,
                    ayuda = "Mínimo 6 caracteres, con letras y números",
                    esPassword = true,
                    keyboardType = KeyboardType.Password
                )
                CampoDesplegable(
                    value = roles.firstOrNull { it.id == ui.rolId }?.nombre ?: "",
                    label = "Rol",
                    icono = Icons.Default.Badge,
                    opciones = roles.map { it.nombre },
                    error = ui.rolError,
                    onSeleccion = { viewModel.onRolChange(roles[it].id) }
                )

                Button(onClick = viewModel::guardar, modifier = Modifier.fillMaxWidth()) {
                    Text(if (ui.esEdicion) "Guardar cambios" else "Crear usuario")
                }
                OutlinedButton(onClick = onDone, modifier = Modifier.fillMaxWidth()) { Text("Cancelar") }
            }
        }
    }
}
