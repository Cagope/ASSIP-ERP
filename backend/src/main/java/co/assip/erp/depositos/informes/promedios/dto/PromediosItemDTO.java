package co.assip.erp.depositos.informes.promedios.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class PromediosItemDTO {

    private String codigoForma;
    private String nombreForma;

    private String codigoCuenta;

    private String documento;

    private String nombreCompleto;

    private BigDecimal saldoCorte;

}