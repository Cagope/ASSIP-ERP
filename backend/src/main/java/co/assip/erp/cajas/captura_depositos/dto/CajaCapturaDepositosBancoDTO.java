package co.assip.erp.cajas.captura_depositos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CajaCapturaDepositosBancoDTO {

    private Integer codigoBanco;
    private String nombreBanco;
}