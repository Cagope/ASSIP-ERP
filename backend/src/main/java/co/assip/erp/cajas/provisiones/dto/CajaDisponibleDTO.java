package co.assip.erp.cajas.provisiones.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CajaDisponibleDTO {

    private Long idCaja;
    private String codigoCaja;
    private String descripcionCaja;

}