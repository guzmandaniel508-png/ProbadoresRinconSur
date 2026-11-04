package com.rinconsur.probadores

import com.rinconsur.probadores.viewmodels.Validaciones
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ValidacionesTest {

    @Test fun nombre_valido() = assertNull(Validaciones.validarNombre("María José Pérez"))
    @Test fun nombre_con_numeros_es_invalido() = assertNotNull(Validaciones.validarNombre("Juan3"))
    @Test fun nombre_con_doble_espacio_es_invalido() = assertNotNull(Validaciones.validarNombre("Juan  Pérez"))
    @Test fun nombre_vacio_es_invalido() = assertNotNull(Validaciones.validarNombre(""))

    @Test fun email_valido() = assertNull(Validaciones.validarEmail("admin@rinconsur.cl"))
    @Test fun email_sin_arroba_es_invalido() = assertNotNull(Validaciones.validarEmail("adminrinconsur.cl"))

    @Test fun password_valida() = assertNull(Validaciones.validarPassword("clave123"))
    @Test fun password_corta_es_invalida() = assertNotNull(Validaciones.validarPassword("ab1"))
    @Test fun password_sin_numero_es_invalida() = assertNotNull(Validaciones.validarPassword("sololetras"))

    @Test fun codigo_probador_valido() = assertNull(Validaciones.validarCodigoProbador("P-09"))
    @Test fun codigo_probador_invalido() = assertNotNull(Validaciones.validarCodigoProbador("9"))

    @Test fun zona_valida() = assertNull(Validaciones.validarZona("Mujer"))
    @Test fun zona_invalida() = assertNotNull(Validaciones.validarZona("Otra"))

    @Test fun prendas_validas() = assertNull(Validaciones.validarPrendas("4"))
    @Test fun prendas_cero_es_invalido() = assertNotNull(Validaciones.validarPrendas("0"))
    @Test fun prendas_mas_de_diez_es_invalido() = assertNotNull(Validaciones.validarPrendas("11"))
    @Test fun prendas_texto_es_invalido() = assertNotNull(Validaciones.validarPrendas("abc"))

    @Test fun descripcion_valida() = assertNull(Validaciones.validarDescripcion("Prendas olvidadas en el probador"))
    @Test fun descripcion_corta_es_invalida() = assertNotNull(Validaciones.validarDescripcion("corta"))

    @Test fun limite_valido() = assertNull(Validaciones.validarLimiteMinutos("15"))
    @Test fun limite_fuera_de_rango() = assertNotNull(Validaciones.validarLimiteMinutos("500"))
}
