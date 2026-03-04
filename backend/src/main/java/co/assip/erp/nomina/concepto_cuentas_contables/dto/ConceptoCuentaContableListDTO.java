package co.assip.erp.nomina.concepto_cuentas_contables.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConceptoCuentaContableListDTO {

    private Integer idMapeo;

    private String codigoConcepto;
    private Integer idAgencia;
    private String nombreAgencia;

    private Integer idCuentaDebito;
    private String cuentaDebito;

    private Integer idCuentaCredito;
    private String cuentaCredito;

    private Boolean activo;
}