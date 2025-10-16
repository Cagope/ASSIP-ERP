package co.assip.erp.general.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "parametros", schema = "general",
        uniqueConstraints = @UniqueConstraint(name = "uk_parametros", columnNames = {"id_agencia", "codigo_parametro"}))
public class Parametro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_parametro")
    private Integer idParametro;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_agencia", nullable = false)
    private DatosAgencia agencia;

    @Column(name = "codigo_parametro", nullable = false)
    private Integer codigoParametro;

    @Column(name = "nombre_parametro", length = 100, nullable = false)
    private String nombreParametro;

    @Column(name = "valor_parametro", precision = 18, scale = 2)
    private BigDecimal valorParametro = BigDecimal.ZERO;

    // true = valor, false = porcentaje
    @Column(name = "tipo_valor", nullable = false)
    private Boolean tipoValor;

    @Column(name = "fk_seguridad_creacion", nullable = false)
    private Integer fkSeguridadCreacion;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fk_seguridad_actualizacion", nullable = false)
    private Integer fkSeguridadActualizacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    public Integer getIdParametro() { return idParametro; }
    public void setIdParametro(Integer idParametro) { this.idParametro = idParametro; }

    public DatosAgencia getAgencia() { return agencia; }
    public void setAgencia(DatosAgencia agencia) { this.agencia = agencia; }

    public Integer getCodigoParametro() { return codigoParametro; }
    public void setCodigoParametro(Integer codigoParametro) { this.codigoParametro = codigoParametro; }

    public String getNombreParametro() { return nombreParametro; }
    public void setNombreParametro(String nombreParametro) { this.nombreParametro = nombreParametro; }

    public BigDecimal getValorParametro() { return valorParametro; }
    public void setValorParametro(BigDecimal valorParametro) { this.valorParametro = valorParametro; }

    public Boolean getTipoValor() { return tipoValor; }
    public void setTipoValor(Boolean tipoValor) { this.tipoValor = tipoValor; }

    public Integer getFkSeguridadCreacion() { return fkSeguridadCreacion; }
    public void setFkSeguridadCreacion(Integer fkSeguridadCreacion) { this.fkSeguridadCreacion = fkSeguridadCreacion; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public Integer getFkSeguridadActualizacion() { return fkSeguridadActualizacion; }
    public void setFkSeguridadActualizacion(Integer fkSeguridadActualizacion) { this.fkSeguridadActualizacion = fkSeguridadActualizacion; }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
}
