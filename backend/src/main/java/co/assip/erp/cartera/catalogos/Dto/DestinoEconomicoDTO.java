package co.assip.erp.cartera.catalogos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DestinoEconomicoDTO {

    private String codigoDestinoEconomico;

    private String descripcionDestinoEconomico;

    private Boolean activo;
}