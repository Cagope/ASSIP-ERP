package co.assip.erp.cajas.provisiones.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CajaDisponibleDTO {

    private Long idCaja;
    private Integer idAgencia;

    private String codigoCaja;
    private String descripcionCaja;

    private Boolean permiteEfectivo;
    private Boolean permiteCheques;
    private Boolean permiteTarjetas;
    private Boolean permiteTransferencias;
}