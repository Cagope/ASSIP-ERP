package co.assip.erp.depositos.movimientos.cuentasahorro.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    public String getCodigoMovimiento() {
        return trim(codigoMovimiento);
    }

    public String getDescripcion() {
        return trim(descripcion);
    }

    public String getAccionMovimiento() {
        return trim(accionMovimiento);
    }

    public Boolean getContabilizacionDiaria() {
        return contabilizacionDiaria != null && contabilizacionDiaria;
    }

    public Boolean getGeneraGmf() {
        return generaGmf != null && generaGmf;
    }

    public Boolean getPermiteInclusionManual() {
        return permiteInclusionManual != null && permiteInclusionManual;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}