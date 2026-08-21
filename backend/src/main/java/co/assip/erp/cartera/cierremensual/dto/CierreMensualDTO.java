package co.assip.erp.cartera.cierremensual.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CierreMensualDTO {

    // =========================================================
    // Identificación
    // =========================================================

    private Integer idCierreCartera;

    // =========================================================
    // Información del cierre
    // =========================================================

    private Integer idAgencia;

    private LocalDate fechaCorte;

    private String estadoCierre;

    // =========================================================
    // Cuadre
    // =========================================================

    private BigDecimal saldoCarteraMaestro;

    private BigDecimal saldoCarteraContable;

    private BigDecimal diferenciaCuadre;

    // =========================================================
    // Foto
    // =========================================================

    private Integer cantidadCreditos;

    private LocalDateTime fechaInicio;

    private LocalDateTime fechaCuadre;

    private LocalDateTime fechaFotografia;

    private LocalDateTime fechaFinal;

    // =========================================================
    // Observaciones
    // =========================================================

    private String observaciones;

    // =========================================================
    // Auditoría
    // =========================================================

    private Integer fkSeguridadCreacion;

    private LocalDateTime fechaCreacion;

    private Integer fkSeguridadEdicion;

    private LocalDateTime fechaEdicion;
}