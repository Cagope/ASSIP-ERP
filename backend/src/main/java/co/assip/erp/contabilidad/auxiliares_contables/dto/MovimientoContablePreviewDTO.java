package co.assip.erp.contabilidad.auxiliares_contables.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO SOLO PARA VISUALIZACIÓN (PREVIEW CONTABLE).
 *
 * No se usa para guardar en contabilidad.
 * Se utiliza en frontend para mostrar información legible:
 * - Cuenta (código + nombre)
 * - Tercero (documento + nombre)
 * - Valores débito/crédito
 *
 * Este DTO es transversal para todos los módulos:
 * NOMINA, DEPOSITOS, CDAT, ACTIVOS, etc.
 */
public class MovimientoContablePreviewDTO {

    // =========================
    // CONTEXTO
    // =========================
    private Integer idAgencia;

    private LocalDate fechaAuxiliar;

    // =========================
    // CUENTA CONTABLE
    // =========================
    private Integer idCatalogoCuenta;
    private String codigoCuenta;
    private String nombreCuenta;

    // =========================
    // TERCERO
    // =========================
    private Integer idDatosPersonal;
    private String documentoTercero;
    private String nombreTercero;

    // =========================
    // DETALLE
    // =========================
    private String detalleMovimiento;

    // =========================
    // VALORES
    // =========================
    private BigDecimal valorDebito = BigDecimal.ZERO;
    private BigDecimal valorCredito = BigDecimal.ZERO;

    // =========================
    // GETTERS / SETTERS
    // =========================
    public Integer getIdAgencia() {
        return idAgencia;
    }

    public void setIdAgencia(Integer idAgencia) {
        this.idAgencia = idAgencia;
    }

    public LocalDate getFechaAuxiliar() {
        return fechaAuxiliar;
    }

    public void setFechaAuxiliar(LocalDate fechaAuxiliar) {
        this.fechaAuxiliar = fechaAuxiliar;
    }

    public Integer getIdCatalogoCuenta() {
        return idCatalogoCuenta;
    }

    public void setIdCatalogoCuenta(Integer idCatalogoCuenta) {
        this.idCatalogoCuenta = idCatalogoCuenta;
    }

    public String getCodigoCuenta() {
        return codigoCuenta;
    }

    public void setCodigoCuenta(String codigoCuenta) {
        this.codigoCuenta = codigoCuenta;
    }

    public String getNombreCuenta() {
        return nombreCuenta;
    }

    public void setNombreCuenta(String nombreCuenta) {
        this.nombreCuenta = nombreCuenta;
    }

    public Integer getIdDatosPersonal() {
        return idDatosPersonal;
    }

    public void setIdDatosPersonal(Integer idDatosPersonal) {
        this.idDatosPersonal = idDatosPersonal;
    }

    public String getDocumentoTercero() {
        return documentoTercero;
    }

    public void setDocumentoTercero(String documentoTercero) {
        this.documentoTercero = documentoTercero;
    }

    public String getNombreTercero() {
        return nombreTercero;
    }

    public void setNombreTercero(String nombreTercero) {
        this.nombreTercero = nombreTercero;
    }

    public String getDetalleMovimiento() {
        return detalleMovimiento;
    }

    public void setDetalleMovimiento(String detalleMovimiento) {
        this.detalleMovimiento = detalleMovimiento;
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
}