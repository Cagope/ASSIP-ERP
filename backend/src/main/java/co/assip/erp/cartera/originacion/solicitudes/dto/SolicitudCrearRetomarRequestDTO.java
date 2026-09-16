package co.assip.erp.cartera.originacion.solicitudes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudCrearRetomarRequestDTO {

    /**
     * Si viene null, se crea una nueva solicitud.
     * Si viene informado, PostgreSQL intenta retomar la solicitud existente.
     */
    private Integer idSolicitudCredito;

    /**
     * Agencia desde la cual se está originando la solicitud.
     */
    private Integer idAgencia;

    /**
     * Persona principal de la solicitud.
     */
    private Integer idDatosPersonal;
}