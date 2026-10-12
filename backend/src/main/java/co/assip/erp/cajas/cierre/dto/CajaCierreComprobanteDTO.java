package co.assip.erp.cajas.cierre.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CajaCierreComprobanteDTO {

    private String tipo;
    private String numero;
}