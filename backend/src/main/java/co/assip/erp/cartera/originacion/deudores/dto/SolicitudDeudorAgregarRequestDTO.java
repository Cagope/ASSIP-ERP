package co.assip.erp.cartera.originacion.deudores.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudDeudorAgregarRequestDTO {

    private Integer idSolicitudCredito;
    private Integer idDatosPersonal;
}