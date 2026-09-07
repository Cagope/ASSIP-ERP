package co.assip.erp.depositos.informes.gmf_semanal.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class GmfSemanalItemDTO {

    private LocalDate fechaMovimiento;

    private String codigoCuenta;

    private String tipoMovimiento;

    private String descripcionMovimiento;

    private String documentoSoporte;

    private String documentoAsociado;

    private String nombreAsociado;

    private String codigoForma;

    private String nombreForma;

    private String estadoGmfCuenta;

    // =========================================================
    // GMF ASUMIDO
    // =========================================================

    private BigDecimal baseAsumido;

    private BigDecimal gmfAsumido;

    // =========================================================
    // GMF ASOCIADO
    // =========================================================

    private BigDecimal baseAsociado;

    private BigDecimal gmfAsociado;

    // =========================================================
    // RETIROS
    // =========================================================

    private BigDecimal baseRetiro;

    private BigDecimal gmfRetiro;

    private BigDecimal valorExento;

    // =========================================================
    // CHEQUES
    // =========================================================

    private BigDecimal baseChequeAsumido;

    private BigDecimal gmfChequeAsumido;

    private BigDecimal baseChequeExento;

    private BigDecimal gmfChequeExento;

}