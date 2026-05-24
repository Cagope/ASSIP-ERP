package co.assip.erp.depositos.movimientos.cuentasahorro.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TipoMovimientoDTO {

    private String codigoMovimiento;
    private String descripcion;
    private String accionMovimiento;
    private Boolean contabilizacionDiaria;
    private Boolean generaGmf;
    private Boolean permiteInclusionManual;
    private String telefonoPoder;
    private String celularPoder;
}