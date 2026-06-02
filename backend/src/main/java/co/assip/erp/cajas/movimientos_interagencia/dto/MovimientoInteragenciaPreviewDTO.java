package co.assip.erp.cajas.movimientos_interagencia.dto;

import co.assip.erp.sarlaft.evaluacion.dto.EvaluacionResultado;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoInteragenciaPreviewDTO {

    private Long idCaja;
    private Long idProvision;

    private Integer idAgenciaCaja;
    private String nombreAgenciaCaja;

    private LocalDate fechaContable;

    private Long idCuentaAhorro;

    private Integer idAgenciaCuenta;
    private String codigoAgenciaCuenta;
    private String nombreAgenciaCuenta;

    private String codigoCuenta;
    private String documento;
    private String nombreAsociado;

    private String codigoForma;
    private String nombreForma;

    private String codigoOperacion;
    private String nombreOperacion;

    private String tipoMovimiento;
    private String nombreTipoMovimiento;

    private String naturaleza;

    private String tipoComprobante;
    private String numeroComprobante;

    private BigDecimal valorEfectivo;
    private BigDecimal valorCheques;
    private BigDecimal valorTotal;

    private BigDecimal saldoAnterior;
    private BigDecimal valorCanje;
    private BigDecimal saldoDisponible;
    private BigDecimal saldoFinal;

    private Boolean permiteAplicar;
    private String mensaje;

    private EvaluacionResultado sarlaft;

    @Builder.Default
    private List<String> errores = new ArrayList<>();

    public Boolean getPermiteAplicar() {
        return permiteAplicar != null
                && permiteAplicar;
    }
}