package co.assip.erp.depositos.movimientos.cuentasahorro.dto;

import co.assip.erp.sarlaft.evaluacion.dto.EvaluacionResultado;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoCuentaPreviewDTO {

    private Integer idCuentaAhorro;

    private String codigoCuenta;
    private String documento;
    private String nombreAsociado;

    private String codigoForma;
    private String nombreForma;

    private String tipoMovimiento;
    private String descripcionMovimiento;
    private String accionMovimiento;

    private LocalDate fechaMovimiento;

    private BigDecimal saldoActual;
    private BigDecimal valorMovimiento;
    private BigDecimal valorGmf;
    private BigDecimal saldoFinal;

    private Boolean contabilizacionDiaria;
    private Boolean generaGmf;

    private EvaluacionResultado sarlaft;

    public String getCodigoCuenta() {
        return trim(codigoCuenta);
    }

    public String getDocumento() {
        return trim(documento);
    }

    public String getNombreAsociado() {
        return trim(nombreAsociado);
    }

    public String getCodigoForma() {
        return trim(codigoForma);
    }

    public String getNombreForma() {
        return trim(nombreForma);
    }

    public String getTipoMovimiento() {
        return trim(tipoMovimiento);
    }

    public String getDescripcionMovimiento() {
        return trim(descripcionMovimiento);
    }

    public String getAccionMovimiento() {
        return trim(accionMovimiento);
    }

    public BigDecimal getSaldoActual() {
        return nvl(saldoActual);
    }

    public BigDecimal getValorMovimiento() {
        return nvl(valorMovimiento);
    }

    public BigDecimal getValorGmf() {
        return nvl(valorGmf);
    }

    public BigDecimal getSaldoFinal() {
        return nvl(saldoFinal);
    }

    public Boolean getContabilizacionDiaria() {
        return contabilizacionDiaria != null && contabilizacionDiaria;
    }

    public Boolean getGeneraGmf() {
        return generaGmf != null && generaGmf;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}