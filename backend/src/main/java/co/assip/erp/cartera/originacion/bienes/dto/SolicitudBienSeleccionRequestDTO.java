package co.assip.erp.cartera.originacion.bienes.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SolicitudBienSeleccionRequestDTO {

    // =========================================================
    // Deudor de la solicitud
    // =========================================================

    private Integer idSolicitudDeudor;


    // =========================================================
    // Bien seleccionado
    // =========================================================

    private Long idBienPersona;


    // =========================================================
    // Selección
    // =========================================================

    private Boolean seleccionado;


    // =========================================================
    // Observación
    // =========================================================

    private String observacion;
}