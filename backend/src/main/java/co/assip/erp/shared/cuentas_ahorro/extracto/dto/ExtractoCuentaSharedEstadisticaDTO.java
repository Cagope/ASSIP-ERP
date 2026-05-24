package co.assip.erp.shared.cuentas_ahorro.extracto.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class ExtractoCuentaSharedEstadisticaDTO {

    private Integer cantidadMovimientos;

    private BigDecimal promedioCreditos;
    private BigDecimal promedioDebitos;

    private BigDecimal creditoMaximo;
    private BigDecimal debitoMaximo;

    private BigDecimal creditoMinimo;
    private BigDecimal debitoMinimo;

    private BigDecimal mediaMovimientos;
    private BigDecimal medianaMovimientos;

    private BigDecimal valorMovilizado;
}