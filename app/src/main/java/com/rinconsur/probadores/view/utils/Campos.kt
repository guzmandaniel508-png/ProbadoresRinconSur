package com.rinconsur.probadores.view.utils

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.rinconsur.probadores.view.theme.VerdeOk

/**
 * Campo de formulario estándar de la app (IE 2.1.2).
 * - Ícono inicial que describe el dato.
 * - Retroalimentación visual: borde rojo + ícono de error + mensaje cuando hay error;
 *   ícono verde de "válido" cuando el campo tiene contenido sin errores.
 * La validación NO ocurre aquí: el error llega desde el ViewModel.
 */
@Composable
fun CampoTexto(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icono: ImageVector,
    error: String?,
    modifier: Modifier = Modifier,
    ayuda: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    esPassword: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    var verPassword by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icono, contentDescription = null) },
        trailingIcon = {
            when {
                esPassword -> IconButton(onClick = { verPassword = !verPassword }) {
                    Icon(
                        if (verPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (verPassword) "Ocultar contraseña" else "Mostrar contraseña"
                    )
                }
                error != null -> Icon(
                    Icons.Default.Error, contentDescription = "Error", tint = MaterialTheme.colorScheme.error
                )
                value.isNotEmpty() -> Icon(
                    Icons.Default.CheckCircle, contentDescription = "Válido", tint = VerdeOk
                )
                else -> {}
            }
        },
        isError = error != null,
        supportingText = {
            val texto = error ?: ayuda
            if (texto != null) Text(texto)
        },
        visualTransformation = if (esPassword && !verPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        singleLine = singleLine,
        minLines = minLines,
        modifier = modifier.fillMaxWidth()
    )
}

/**
 * Lista desplegable de solo lectura con la misma retroalimentación visual que CampoTexto.
 * onSeleccion entrega el índice elegido dentro de [opciones].
 */
@Composable
fun CampoDesplegable(
    value: String,
    label: String,
    icono: ImageVector,
    opciones: List<String>,
    error: String?,
    onSeleccion: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expandido by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            leadingIcon = { Icon(icono, contentDescription = null) },
            trailingIcon = {
                if (error != null) {
                    Icon(Icons.Default.Error, contentDescription = "Error", tint = MaterialTheme.colorScheme.error)
                } else {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Abrir lista")
                }
            },
            isError = error != null,
            supportingText = { if (error != null) Text(error) },
            modifier = Modifier.fillMaxWidth()
        )
        // Capa transparente que abre el menú al tocar el campo
        Box(modifier = Modifier.matchParentSize().clickable { expandido = true })
        DropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
            opciones.forEachIndexed { indice, opcion ->
                DropdownMenuItem(
                    text = { Text(opcion) },
                    onClick = {
                        onSeleccion(indice)
                        expandido = false
                    }
                )
            }
        }
    }
}
