package co.assip.erp.cartera.originacion.aprobacion.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SolicitudAprobacionFotosDTO {

    private JsonNode fotoSolicitud;

    private JsonNode fotoDeudores;

    private JsonNode fotoFinanciero;

    private JsonNode fotoBienes;

    private JsonNode fotoCentralRiesgo;

    private JsonNode fotoAnalisis;
}