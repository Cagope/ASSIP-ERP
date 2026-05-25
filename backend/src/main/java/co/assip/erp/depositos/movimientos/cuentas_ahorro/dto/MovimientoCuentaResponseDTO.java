package co.assip.erp.depositos.movimientos.cuentasahorro.dto;

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
public class MovimientoCuentaResponseDTO {

    private Integer idCuentaAhorro;

    private String codigoCuenta;
    private String documento;
    private String nombreAsociado;

    private String tipoMovimiento;
    private String descripcionMovimiento;

    private LocalDate fechaMovimiento;

    private BigDecimal valorMovimiento;
    private BigDecimal valorGmf;

    private BigDecimal saldoAnterior;
    private BigDecimal saldoFinal;

    private String tipoComprobante;
    private String numeroComprobante;

    private String mensaje;

    public String getCodigoCuenta() {
        return trim(codigoCuenta);
    }

    public String getDocumento() {
        return trim(documento);
    }

    public String getNombreAsociado() {
        return trim(nombreAsociado);
    }

    public String getTipoMovimiento() {
        return trim(tipoMovimiento);
    }

    public String getDescripcionMovimiento() {
        return trim(descripcionMovimiento);
    }

    public String getTipoComprobante() {
        return trim(tipoComprobante);
    }

    public String getNumeroComprobante() {
        return trim(numeroComprobante);
    }

    public String getMensaje() {
        return trim(mensaje);
    }

    public BigDecimal getValorMovimiento() {
        return nvl(valorMovimiento);
    }

    public BigDecimal getValorGmf() {
        return nvl(valorGmf);
    }

    public BigDecimal getSaldoAnterior() {
        return nvl(saldoAnterior);
    }

    public BigDecimal getSaldoFinal() {
        return nvl(saldoFinal);
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}