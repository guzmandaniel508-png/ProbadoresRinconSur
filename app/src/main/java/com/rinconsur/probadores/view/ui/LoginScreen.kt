package com.rinconsur.probadores.view.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rinconsur.probadores.view.utils.CampoTexto
import com.rinconsur.probadores.view.utils.pulsoYRotacion
import com.rinconsur.probadores.viewmodels.LoginViewModel

@Composable
fun LoginScreen(viewModel: LoginViewModel, onLoginSuccess: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.loggedUser) {
        if (state.loggedUser != null) onLoginSuccess()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.MeetingRoom,
            contentDescription = "Logo",
            tint = MaterialTheme.colorScheme.primary,
            // ANIMACIÓN 2 (view/utils/Animations.kt): pulso + rotación del logo
            modifier = Modifier
                .size(96.dp)
                .pulsoYRotacion()
        )
        Spacer(Modifier.height(16.dp))
        Text("Probadores Rincón Sur", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        Text(
            "Acceso exclusivo para el personal de tienda",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))

        CampoTexto(
            value = state.email,
            onValueChange = viewModel::onEmailChange,
            label = "Correo",
            icono = Icons.Default.Email,
            error = state.emailError,
            keyboardType = KeyboardType.Email
        )
        Spacer(Modifier.height(4.dp))
        CampoTexto(
            value = state.password,
            onValueChange = viewModel::onPasswordChange,
            label = "Contraseña",
            icono = Icons.Default.Lock,
            error = state.passwordError,
            esPassword = true,
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        )

        // Error de credenciales (retroalimentación visual del resultado del login)
        state.error?.let {
            Spacer(Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        Button(
            onClick = viewModel::login,
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
            } else {
                Text("Ingresar")
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "Usuario de prueba: admin@rinconsur.cl / admin123",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}
