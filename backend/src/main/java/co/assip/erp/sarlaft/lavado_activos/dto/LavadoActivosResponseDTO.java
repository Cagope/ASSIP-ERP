package co.assip.erp.sarlaft.lavado_activos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LavadoActivosResponseDTO {

    private Long idFormatoLavadoActivos;

    private Boolean generado;
    private Boolean requiereFormato;

    private String mensaje;
}