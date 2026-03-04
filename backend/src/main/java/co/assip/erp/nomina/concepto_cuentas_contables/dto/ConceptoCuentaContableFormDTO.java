package co.assip.erp.nomina.concepto_cuentas_contables.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConceptoCuentaContableFormDTO {

    private Integer idMapeo;

    private String codigoConcepto;
    private Integer idAgencia;

    private Integer idCuentaDebito;
    private String cuentaDebito;      // ✅ NUEVO (label)

    private Integer idCuentaCredito;
    private String cuentaCredito;     // ✅ NUEVO (label)

    private Boolean activo;
}