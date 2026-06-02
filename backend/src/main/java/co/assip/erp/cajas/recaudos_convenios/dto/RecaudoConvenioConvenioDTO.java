package co.assip.erp.cajas.recaudos_convenios.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecaudoConvenioConvenioDTO {

    private Long idConvenio;

    private Integer idAgencia;
    private String codigoAgencia;
    private String nombreAgencia;

    private String codigoConvenio;
    private String nombreConvenio;

    private Long idCuentaAhorro;
    private String codigoCuenta;

    private String documentoTitular;
    private String nombreTitular;
}