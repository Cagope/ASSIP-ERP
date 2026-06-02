package co.assip.erp.cajas.convenios_recaudo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConvenioRecaudoRequestDTO {

    private Integer idAgencia;

    private String codigoConvenio;
    private String nombreConvenio;

    private Long idCuentaAhorro;

    private String estado;
}