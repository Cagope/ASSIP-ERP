package co.assip.erp.depositos.movimientos.dto;

import java.math.BigDecimal;

public class DepositosMovimientoResultadoDTO {

    private Integer idCuentaAhorro;
    private BigDecimal saldoAnterior;
    private BigDecimal saldoNuevo;

    public DepositosMovimientoResultadoDTO() {
    }

    public DepositosMovimientoResultadoDTO(
            Integer idCuentaAhorro,
            BigDecimal saldoAnterior,
            BigDecimal saldoNuevo
    ) {
        this.idCuentaAhorro = idCuentaAhorro;
        this.saldoAnterior = saldoAnterior;
        this.saldoNuevo = saldoNuevo;
    }

    public Integer getIdCuentaAhorro() {
        return idCuentaAhorro;
    }

    public void setIdCuentaAhorro(Integer idCuentaAhorro) {
        this.idCuentaAhorro = idCuentaAhorro;
    }

    public BigDecimal getSaldoAnterior() {
        return saldoAnterior;
    }

    public void setSaldoAnterior(BigDecimal saldoAnterior) {
        this.saldoAnterior = saldoAnterior;
    }

    public BigDecimal getSaldoNuevo() {
        return saldoNuevo;
    }

    public void setSaldoNuevo(BigDecimal saldoNuevo) {
        this.saldoNuevo = saldoNuevo;
    }
}