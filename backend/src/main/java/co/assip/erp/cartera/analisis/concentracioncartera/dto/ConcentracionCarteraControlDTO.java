package co.assip.erp.cartera.analisis.concentracioncartera.dto;

import java.time.LocalDate;

public record ConcentracionCarteraControlDTO(
        LocalDate primerCorteDisponible,
        LocalDate ultimoCorteDisponible,
        LocalDate corteSugerido
) {
}
