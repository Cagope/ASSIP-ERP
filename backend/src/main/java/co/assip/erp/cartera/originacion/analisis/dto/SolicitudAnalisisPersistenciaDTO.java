package co.assip.erp.cartera.originacion.analisis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudAnalisisPersistenciaDTO {

    private Integer analisisGuardados;
    private Integer detallesGuardados;

    private String estadoAnalisis;
    private String recomendacion;
}