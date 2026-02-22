package co.assip.erp.nomina.periodos_nomina.dto;

import java.time.LocalDate;

/**
 * DTO simple para exponer el período de nómina activo
 * (solo lectura, sin lógica)
 */
public class PeriodoActivoDetalleDTO {

    private final Integer idPeriodo;
    private final LocalDate fechaInicio;
    private final LocalDate fechaFin;

    public PeriodoActivoDetalleDTO(
            Integer idPeriodo,
            LocalDate fechaInicio,
            LocalDate fechaFin
    ) {
        this.idPeriodo = idPeriodo;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }

    public Integer getIdPeriodo() {
        return idPeriodo;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }
}