package co.assip.erp.depositos.informes.extracto_asociado.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class ExtractoAsociadoBusquedaDTO {

    private Integer idDatosPersonal;

    private String documento;
    private String nombreCompleto;
    private String direccion;

    private Integer totalCuentas;
    private BigDecimal saldoTotal;

}