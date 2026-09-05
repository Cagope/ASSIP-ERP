package co.assip.erp.cartera.analisis.rollforward.dto;

import java.time.LocalDate;

public record RollForwardCorteDTO(LocalDate fechaCorteAnterior, LocalDate fechaCorte) {
}
