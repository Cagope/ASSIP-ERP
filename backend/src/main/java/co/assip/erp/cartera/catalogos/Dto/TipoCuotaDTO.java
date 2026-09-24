package co.assip.erp.cartera.catalogos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TipoCuotaDTO {

    private String codigoTipoCuota;

    private String descripcionTipoCuota;

    private Boolean activo;
}