package co.assip.erp.cajas.provisiones.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CajaEstadoDTO {

    private Long idCaja;
    private String codigoCaja;
    private String descripcionCaja;

    private Long idProvision;
    private String estado;
    private Integer fkUsuarioApertura;
    private LocalDateTime fechaApertura;

    private Boolean disponible;

}