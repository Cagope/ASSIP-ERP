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
public class MovimientoCuentaRequestDTO {

    private Integer idCuentaAhorro;

    private Integer idAgencia;

    private LocalDate fechaMovimiento;

    private String tipoMovimiento;

    private BigDecimal valorMovimiento;

    private String tipoComprobante;

    private String numeroComprobante;

    private String detalle;

    public String getTipoMovimiento() {
        return trim(tipoMovimiento);
    }

    public String getTipoComprobante() {
        return trim(tipoComprobante);
    }

    public String getNumeroComprobante() {
        return trim(numeroComprobante);
    }

    public String getDetalle() {
        return trim(detalle);
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}