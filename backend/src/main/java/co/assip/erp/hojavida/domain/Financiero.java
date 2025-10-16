package co.assip.erp.hojavida.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "financieros", schema = "hoja_vida",
        uniqueConstraints = @UniqueConstraint(name = "uk_financiero_persona", columnNames = "id_datos_personal"))
public class Financiero {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_financiero")
    private Integer idFinanciero;

    @Column(name = "id_datos_personal", nullable = false)
    private Integer idDatosPersonal;

    @Column(name = "valor_salario", nullable = false)
    private BigDecimal valorSalario = BigDecimal.ZERO;

    @Column(name = "valor_pension", nullable = false)
    private BigDecimal valorPension = BigDecimal.ZERO;

    @Column(name = "ingresos_arriendo", nullable = false)
    private BigDecimal ingresosArriendo = BigDecimal.ZERO;

    @Column(name = "ingresos_comisiones", nullable = false)
    private BigDecimal ingresosComisiones = BigDecimal.ZERO;

    @Column(name = "otros_ingresos", nullable = false)
    private BigDecimal otrosIngresos = BigDecimal.ZERO;

    @Column(name = "comentario_otros_ingresos", nullable = false, length = 100)
    private String comentarioOtrosIngresos;

    @Column(name = "egresos_familiares", nullable = false)
    private BigDecimal egresosFamiliares = BigDecimal.ZERO;

    @Column(name = "egresos_arriendo", nullable = false)
    private BigDecimal egresosArriendo = BigDecimal.ZERO;

    @Column(name = "egresos_credito", nullable = false)
    private BigDecimal egresosCredito = BigDecimal.ZERO;

    @Column(name = "otros_egresos", nullable = false)
    private BigDecimal otrosEgresos = BigDecimal.ZERO;

    @Column(name = "comentario_otros_egresos", nullable = false, length = 100)
    private String comentarioOtrosEgresos;

    @Column(name = "total_activos", nullable = false)
    private BigDecimal totalActivos = BigDecimal.ZERO;

    @Column(name = "total_pasivos", nullable = false)
    private BigDecimal totalPasivos = BigDecimal.ZERO;

    @Column(name = "origen_fondos", nullable = false, length = 100)
    private String origenFondos;

    @Column(name = "relacion_financiera", nullable = false, length = 100)
    private String relacionFinanciera;

    @Column(name = "deuda_relacion_financiera", nullable = false)
    private BigDecimal deudaRelacionFinanciera = BigDecimal.ZERO;

    @Column(name = "fk_seguridad_creacion", nullable = false)
    private Integer fkSeguridadCreacion;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fk_seguridad_actualizacion", nullable = false)
    private Integer fkSeguridadActualizacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // Getters/Setters
    public Integer getIdFinanciero() { return idFinanciero; }
    public void setIdFinanciero(Integer idFinanciero) { this.idFinanciero = idFinanciero; }
    public Integer getIdDatosPersonal() { return idDatosPersonal; }
    public void setIdDatosPersonal(Integer idDatosPersonal) { this.idDatosPersonal = idDatosPersonal; }
    public BigDecimal getValorSalario() { return valorSalario; }
    public void setValorSalario(BigDecimal v) { this.valorSalario = v; }
    public BigDecimal getValorPension() { return valorPension; }
    public void setValorPension(BigDecimal v) { this.valorPension = v; }
    public BigDecimal getIngresosArriendo() { return ingresosArriendo; }
    public void setIngresosArriendo(BigDecimal v) { this.ingresosArriendo = v; }
    public BigDecimal getIngresosComisiones() { return ingresosComisiones; }
    public void setIngresosComisiones(BigDecimal v) { this.ingresosComisiones = v; }
    public BigDecimal getOtrosIngresos() { return otrosIngresos; }
    public void setOtrosIngresos(BigDecimal v) { this.otrosIngresos = v; }
    public String getComentarioOtrosIngresos() { return comentarioOtrosIngresos; }
    public void setComentarioOtrosIngresos(String s) { this.comentarioOtrosIngresos = s; }
    public BigDecimal getEgresosFamiliares() { return egresosFamiliares; }
    public void setEgresosFamiliares(BigDecimal v) { this.egresosFamiliares = v; }
    public BigDecimal getEgresosArriendo() { return egresosArriendo; }
    public void setEgresosArriendo(BigDecimal v) { this.egresosArriendo = v; }
    public BigDecimal getEgresosCredito() { return egresosCredito; }
    public void setEgresosCredito(BigDecimal v) { this.egresosCredito = v; }
    public BigDecimal getOtrosEgresos() { return otrosEgresos; }
    public void setOtrosEgresos(BigDecimal v) { this.otrosEgresos = v; }
    public String getComentarioOtrosEgresos() { return comentarioOtrosEgresos; }
    public void setComentarioOtrosEgresos(String s) { this.comentarioOtrosEgresos = s; }
    public BigDecimal getTotalActivos() { return totalActivos; }
    public void setTotalActivos(BigDecimal v) { this.totalActivos = v; }
    public BigDecimal getTotalPasivos() { return totalPasivos; }
    public void setTotalPasivos(BigDecimal v) { this.totalPasivos = v; }
    public String getOrigenFondos() { return origenFondos; }
    public void setOrigenFondos(String s) { this.origenFondos = s; }
    public String getRelacionFinanciera() { return relacionFinanciera; }
    public void setRelacionFinanciera(String s) { this.relacionFinanciera = s; }
    public BigDecimal getDeudaRelacionFinanciera() { return deudaRelacionFinanciera; }
    public void setDeudaRelacionFinanciera(BigDecimal v) { this.deudaRelacionFinanciera = v; }
    public Integer getFkSeguridadCreacion() { return fkSeguridadCreacion; }
    public void setFkSeguridadCreacion(Integer v) { this.fkSeguridadCreacion = v; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime v) { this.fechaCreacion = v; }
    public Integer getFkSeguridadActualizacion() { return fkSeguridadActualizacion; }
    public void setFkSeguridadActualizacion(Integer v) { this.fkSeguridadActualizacion = v; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime v) { this.fechaActualizacion = v; }
}
