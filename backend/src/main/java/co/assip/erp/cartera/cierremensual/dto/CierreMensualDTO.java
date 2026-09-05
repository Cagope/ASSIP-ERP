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
    // Fotografía
    // =========================================================

    private Integer cantidadCreditos;

    private LocalDateTime fechaInicio;

    private LocalDateTime fechaCuadre;

    private LocalDateTime fechaFotografia;

    private String estadoFotografia;

    private LocalDateTime fechaFotografiaFirme;

    // =========================================================
    // Cálculos
    // =========================================================

    private String estadoCalculos;

    private LocalDateTime fechaCalculosInicio;

    private LocalDateTime fechaCalculosFirme;

    // =========================================================
    // Anexo 1
    // =========================================================

    private String estadoAnexo1;

    private LocalDateTime fechaAnexo1Inicio;

    private LocalDateTime fechaAnexo1Firme;

    // =========================================================
    // Anexo 2
    // =========================================================

    private String estadoAnexo2;

    private LocalDateTime fechaAnexo2Inicio;

    private LocalDateTime fechaAnexo2Firme;

    // =========================================================
    // Finalización general
    // =========================================================

    private LocalDateTime fechaFinalizacion;

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