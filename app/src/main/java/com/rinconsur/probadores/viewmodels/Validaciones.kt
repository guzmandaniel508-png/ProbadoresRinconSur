package com.rinconsur.probadores.viewmodels

import com.rinconsur.probadores.repository.models.Probador

/**
 * Reglas de validación centralizadas (IE 2.2.1).
 * Cada función devuelve el mensaje de error o null si el valor es válido.
 * No depende de Compose ni de Android: se puede probar con tests unitarios.
 */
object Validaciones {

    private const val LETRA = "[A-Za-zÁÉÍÓÚáéíóúÜüÑñ]"

    /** Solo letras y UN espacio entre palabras (ej: "Juan Pérez"). */
    private val SOLO_LETRAS_Y_ESPACIOS = Regex("^$LETRA+( $LETRA+)*$")
    private val EMAIL = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val CODIGO_PROBADOR = Regex("^P-\\d{2,3}$")

    fun esSoloLetras(valor: String): Boolean = SOLO_LETRAS_Y_ESPACIOS.matches(valor)

    fun validarNombre(valor: String): String? = when {
        valor.isBlank() -> "El nombre es obligatorio"
        valor.length < 3 -> "Debe tener al menos 3 caracteres"
        valor.length > 40 -> "Máximo 40 caracteres"
        !esSoloLetras(valor) -> "Solo letras y un espacio entre palabras (sin números ni símbolos)"
        else -> null
    }

    fun validarEmail(valor: String): String? = when {
        valor.isBlank() -> "El correo es obligatorio"
        !EMAIL.matches(valor.trim()) -> "Formato inválido (ej: nombre@rinconsur.cl)"
        else -> null
    }

    fun validarPassword(valor: String): String? = when {
        valor.isEmpty() -> "La contraseña es obligatoria"
        valor.length < 6 -> "Mínimo 6 caracteres"
        valor.none { it.isLetter() } || valor.none { it.isDigit() } -> "Debe incluir letras y números"
        else -> null
    }

    /** En el login solo se exige que el campo no esté vacío. */
    fun validarPasswordLogin(valor: String): String? =
        if (valor.isEmpty()) "Ingrese su contraseña" else null

    fun validarCodigoProbador(valor: String): String? = when {
        valor.isBlank() -> "El código es obligatorio"
        !CODIGO_PROBADOR.matches(valor) -> "Formato: P- y 2 o 3 dígitos (ej: P-09)"
        else -> null
    }

    fun validarZona(valor: String): String? = when {
        valor.isBlank() -> "Seleccione una zona"
        valor !in Probador.ZONAS -> "Zona no válida"
        else -> null
    }

    fun validarSeleccion(id: Int?, mensaje: String): String? = if (id == null || id <= 0) mensaje else null

    fun validarPrendas(valor: String): String? {
        if (valor.isBlank()) return "Ingrese la cantidad de prendas"
        val n = valor.trim().toIntOrNull() ?: return "Solo números enteros"
        return if (n !in 1..10) "Debe ser entre 1 y 10 prendas" else null
    }

    fun validarDescripcion(valor: String): String? = when {
        valor.isBlank() -> "La descripción es obligatoria"
        valor.trim().length < 10 -> "Describe con al menos 10 caracteres"
        valor.length > 200 -> "Máximo 200 caracteres"
        else -> null
    }

    fun validarLimiteMinutos(valor: String): String? {
        if (valor.isBlank()) return "Ingrese los minutos"
        val n = valor.trim().toIntOrNull() ?: return "Solo números enteros"
        return if (n !in 1..120) "Debe ser entre 1 y 120 minutos" else null
    }
}
