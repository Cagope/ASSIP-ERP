package co.assip.erp.depositos.cierre_mensual_depositos.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class CierreMensualDepositosPreviewDTO {

    private Long idCierreMensual;

    private Integer idAgencia;

    private LocalDate fechaCierre;

    private Integer anio;

    private Integer mes;

    private String estado;

    private Integer totalCuentas;

    private BigDecimal saldoTotal;

    private BigDecimal totalDebitos;

    private BigDecimal totalCreditos;

    private CierreMensualDepositosResumenDTO resumen;

    private List<CierreMensualDepositosResumenFormaDTO> resumenFormas;

    private List<CierreMensualDepositosDetalleDTO> detalle;

}