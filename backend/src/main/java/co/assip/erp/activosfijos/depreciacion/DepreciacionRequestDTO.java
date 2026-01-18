package co.assip.erp.activosfijos.depreciacion;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

/**
 * DTO de entrada para el proceso de depreciación de activos fijos.
 * Representa el encabezado del proceso.
 */
public class DepreciacionRequestDTO {

    // =========================
    // AGENCIA (OBLIGATORIA)
    // =========================
    private Integer idAgencia;

    // =========================
    // PERÍODO Y DOCUMENTO
    // =========================
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate fechaPeriodo;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate fechaContabilizacion;

    private String tipoComprobante;
    private String numeroComprobante;
    private String concepto;

    // =========================
    // GETTERS / SETTERS
    // =========================
    public Integer getIdAgencia() {
        return idAgencia;
    }

    public void setIdAgencia(Integer idAgencia) {
        this.idAgencia = idAgencia;
    }

    public LocalDate getFechaPeriodo() {
        return fechaPeriodo;
    }

    public void setFechaPeriodo(LocalDate fechaPeriodo) {
        this.fechaPeriodo = fechaPeriodo;
    }

    public LocalDate getFechaContabilizacion() {
        return fechaContabilizacion;
    }

    public void setFechaContabilizacion(LocalDate fechaContabilizacion) {
        this.fechaContabilizacion = fechaContabilizacion;
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

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }
}
