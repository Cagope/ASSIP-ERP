package co.assip.erp.depositos.movimientos.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DepositosMovimientoDTO {

    private Integer idCuentaAhorro;
    private LocalDate fechaMovimiento;

    private String tipoComprobante;
    private String numeroComprobante;
    private String tipoMovimiento;
    private String modulo;

    private BigDecimal valorDebito = BigDecimal.ZERO;
    private BigDecimal valorCredito = BigDecimal.ZERO;

    private String tarjeta = "N";
    private String establecimiento;

    private String moduloOrigen;
    private String procesoOrigen;
    private Long idOrigen;

    public Integer getIdCuentaAhorro() {
        return idCuentaAhorro;
    }

    public void setIdCuentaAhorro(Integer idCuentaAhorro) {
        this.idCuentaAhorro = idCuentaAhorro;
    }

    public LocalDate getFechaMovimiento() {
        return fechaMovimiento;
    }

    public void setFechaMovimiento(LocalDate fechaMovimiento) {
        this.fechaMovimiento = fechaMovimiento;
    }

    public String getTipoComprobante() {
        return tipoComprobante;
    }

    public void setTipoComprobante(String tipoComprobante) {
        this.tipoComprobante = tipoComprobante;
    }

    public String getNumeroComprobante() {
        return numeroComprobante;
    }

    public void setNumeroComprobante(String numeroComprobante) {
        this.numeroComprobante = numeroComprobante;
    }

    public String getTipoMovimiento() {
        return tipoMovimiento;
    }

    public void setTipoMovimiento(String tipoMovimiento) {
        this.tipoMovimiento = tipoMovimiento;
    }

    public String getModulo() {
        return modulo;
    }

    public void setModulo(String modulo) {
        this.modulo = modulo;
    }

    public BigDecimal getValorDebito() {
        return valorDebito;
    }

    public void setValorDebito(BigDecimal valorDebito) {
        this.valorDebito = valorDebito;
    }

    public BigDecimal getValorCredito() {
        return valorCredito;
    }

    public void setValorCredito(BigDecimal valorCredito) {
        this.valorCredito = valorCredito;
    }

    public String getTarjeta() {
        return tarjeta;
    }

    public void setTarjeta(String tarjeta) {
        this.tarjeta = tarjeta;
    }

    public String getEstablecimiento() {
        return establecimiento;
    }

    public void setEstablecimiento(String establecimiento) {
        this.establecimiento = establecimiento;
    }

    public String getModuloOrigen() {
        return moduloOrigen;
    }

    public void setModuloOrigen(String moduloOrigen) {
        this.moduloOrigen = moduloOrigen;
    }

    public String getProcesoOrigen() {
        return procesoOrigen;
    }

    public void setProcesoOrigen(String procesoOrigen) {
        this.procesoOrigen = procesoOrigen;
    }

    public Long getIdOrigen() {
        return idOrigen;
    }

    public void setIdOrigen(Long idOrigen) {
        this.idOrigen = idOrigen;
    }
}