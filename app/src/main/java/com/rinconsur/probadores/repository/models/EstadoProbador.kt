package com.rinconsur.probadores.repository.models

/** Estados posibles de un probador. Se guarda en Room como texto (ver Converters). */
enum class EstadoProbador(val etiqueta: String) {
    LIBRE("Libre"),
    OCUPADO("Ocupado"),
    MANTENCION("En mantención")
}
