package com.rinconsur.probadores

import com.rinconsur.probadores.repository.models.EstadoProbador
import com.rinconsur.probadores.repository.models.Probador
import com.rinconsur.probadores.viewmodels.ReglasProbadores
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReglasProbadoresTest {

    private val ahora = 10_000_000L

    private fun ocupado(minutosAtras: Int) = Probador(
        id = 1, codigo = "P-01", zona = "Mujer", estado = EstadoProbador.OCUPADO,
        ocupadoDesde = ahora - minutosAtras * 60_000L, prendas = 3
    )

    @Test fun alerta_cuando_supera_el_limite() =
        assertTrue(ReglasProbadores.enAlerta(ocupado(16), ahora, 15))

    @Test fun sin_alerta_bajo_el_limite() =
        assertFalse(ReglasProbadores.enAlerta(ocupado(5), ahora, 15))

    @Test fun libre_nunca_esta_en_alerta() {
        val libre = Probador(id = 2, codigo = "P-02", zona = "Mujer")
        assertFalse(ReglasProbadores.enAlerta(libre, ahora, 15))
    }

    @Test fun espera_cero_si_hay_libres() =
        assertEquals(0, ReglasProbadores.esperaEstimadaMin(esperando = 4, libres = 1, ocupados = 3, promedioMin = 10))

    @Test fun espera_cero_si_nadie_espera() =
        assertEquals(0, ReglasProbadores.esperaEstimadaMin(esperando = 0, libres = 0, ocupados = 3, promedioMin = 10))

    @Test fun espera_se_reparte_entre_ocupados() =
        // 5 clientes y 2 probadores ocupados => 3 rondas x 10 min
        assertEquals(30, ReglasProbadores.esperaEstimadaMin(esperando = 5, libres = 0, ocupados = 2, promedioMin = 10))

    @Test fun progreso_se_limita_a_uno() =
        assertEquals(1f, ReglasProbadores.progreso(ocupado(40), ahora, 15), 0.0001f)
}
