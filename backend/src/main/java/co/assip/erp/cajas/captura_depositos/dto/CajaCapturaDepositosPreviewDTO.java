package co.assip.erp.cajas.captura_depositos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import co.assip.erp.sarlaft.evaluacion.dto.EvaluacionResultado;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CajaCapturaDepositosPreviewDTO {

    private Long idCaja;
    private Integer idAgencia;
    private LocalDate fechaContable;

    private Long idProvision;
    private Long idCuentaAhorro;
    private String codigoCuenta;

    private String documento;
    private String nombreAsociado;

    private String codigoForma;
    private String nombreForma;

    private String codigoOperacion;
    private String nombreOperacion;
    private String naturaleza;

    private BigDecimal valorEfectivo;
    private BigDecimal valorCheques;
    private BigDecimal valorTransferencias;
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
        return permiteAplicar != null && permiteAplicar;
    }

    private Boolean movimientoInteragencia;
    private String mensajeInteragencia;

}