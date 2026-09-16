package co.assip.erp.cartera.originacion.catalogos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GarantiaCreditoDTO {

    private String codigoGarantiaCredito;
    private String descripcionGarantiaCredito;
    private String tipoGarantia;
}