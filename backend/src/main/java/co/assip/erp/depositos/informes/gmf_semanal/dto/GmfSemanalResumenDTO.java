package co.assip.erp.depositos.informes.gmf_semanal.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class GmfSemanalResumenDTO {

    // =========================================================
    // GMF ASUMIDO
    // =========================================================

    private BigDecimal totalBaseAsumido;

    private BigDecimal totalGmfAsumido;

    // =========================================================
    // GMF ASOCIADO
    // =========================================================

    private BigDecimal totalBaseAsociado;

    private BigDecimal totalGmfAsociado;

    // =========================================================
    // RETIROS
    // =========================================================

    private BigDecimal totalBaseRetiro;

    private BigDecimal totalGmfRetiro;

    private BigDecimal totalValorExento;

    // =========================================================
    // CHEQUES
    // =========================================================

    private BigDecimal totalBaseChequeAsumido;

    private BigDecimal totalGmfChequeAsumido;

    private BigDecimal totalBaseChequeExento;

    private BigDecimal totalGmfChequeExento;

    // =========================================================
    // TOTALES GENERALES
    // =========================================================

    private BigDecimal totalBaseGravada;

    private BigDecimal totalGmf;

}