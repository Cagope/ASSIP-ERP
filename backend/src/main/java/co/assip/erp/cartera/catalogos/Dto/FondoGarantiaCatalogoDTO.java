package co.assip.erp.cartera.catalogos.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class FondoGarantiaCatalogoDTO {

    private Integer idFondoGarantia;

    private String codigoFondo;

    private String nombreFondo;

    private BigDecimal porcentajeFondo;

    private Boolean activo;
}