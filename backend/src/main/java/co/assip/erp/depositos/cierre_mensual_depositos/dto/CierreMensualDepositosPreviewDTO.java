package co.assip.erp.depositos.cierre_mensual_depositos.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class CierreMensualDepositosPreviewDTO {

    // =========================================================
    // CABECERA GENERAL
    //
    // El cierre es centralizado.
    // Ya NO existe idAgencia en la cabecera.
    // =========================================================

    private Long idCierreMensual;

    private LocalDate fechaCierre;

    private Integer anio;

    private Integer mes;

    private String estado;

    // =========================================================
    // TOTALES GENERALES DE LA ENTIDAD
    // =========================================================

    private Integer totalCuentas;

    private BigDecimal saldoTotal;

    private BigDecimal totalDebitos;

    private BigDecimal totalCreditos;

    // =========================================================
    // RESULTADOS
    // =========================================================

    // Resumen general de toda la entidad.
    private CierreMensualDepositosResumenDTO resumen;

    // Resumen totalizado por agencia.
    private List<CierreMensualDepositosResumenAgenciaDTO> resumenAgencias;

    // Resumen por agencia + forma de ahorro.
    private List<CierreMensualDepositosResumenFormaDTO> resumenFormas;

    // Fotografía detallada de las cuentas con saldo al corte.
    private List<CierreMensualDepositosDetalleDTO> detalle;
}