package co.assip.erp.hojavida.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "sarlaft",
        schema = "hoja_vida",
        uniqueConstraints = @UniqueConstraint(name = "uk_sarlaft_persona", columnNames = "id_datos_personal")
)
public class Sarlaft {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sarlaft")
    private Integer idSarlaft;

    @Column(name = "id_datos_personal", nullable = false)
    private Integer idDatosPersonal;

    // Exoneración UIAF
    @Column(name = "exoneracion_uiaf", nullable = false)
    private Boolean exoneracionUiaf = false;

    @Column(name = "fecha_exoneracion")
    private LocalDate fechaExoneracion;

    // PEPs asociado
    @Column(name = "asociado_peps", nullable = false)
    private Boolean asociadoPeps = false;

    @Column(name = "tipo_peps", length = 5)
    private String tipoPeps;

    @Column(name = "observaciones_peps", length = 300, nullable = false)
    private String observacionesPeps;

    @Column(name = "fecha_inicial_peps")
    private LocalDate fechaInicialPeps;

    @Column(name = "fecha_final_peps")
    private LocalDate fechaFinalPeps;

    // PEPs familiar
    @Column(name = "familia_peps", nullable = false)
    private Boolean familiaPeps = false;

    @Column(name = "tipo_familia_peps", length = 5)
    private String tipoFamiliaPeps;

    @Column(name = "cedula_familia_peps", length = 20)
    private String cedulaFamiliaPeps;

    @Column(name = "codigo_parentesco", length = 2)
    private String codigoParentesco;

    @Column(name = "nombre_familia_peps", length = 100, nullable = false)
    private String nombreFamiliaPeps;

    // Moneda extranjera (negocios)
    @Column(name = "moneda_extranjera", nullable = false)
    private Boolean monedaExtranjera = false;

    @Column(name = "observacion_moneda_extranjera", length = 200)
    private String observacionMonedaExtranjera;

    // Cuentas en el extranjero
    @Column(name = "cuenta_extranjero", nullable = false)
    private Boolean cuentaExtranjero = false;

    @Column(name = "tipo_moneda_extranjera", length = 20, nullable = false)
    private String tipoMonedaExtranjera;

    @Column(name = "numero_cuenta_extranjero", length = 30, nullable = false)
    private String numeroCuentaExtranjero;

    @Column(name = "nombre_banco_extranjero", length = 100, nullable = false)
    private String nombreBancoExtranjero;

    @Column(name = "ciudad_cuenta_extranjero", length = 50, nullable = false)
    private String ciudadCuentaExtranjero;

    @Column(name = "pais_cuenta_extranjero", length = 50, nullable = false)
    private String paisCuentaExtranjero;

    // Auditoría
    @Column(name = "FK_seguridad_creacion", nullable = false)
    private Integer fkSeguridadCreacion;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "FK_seguridad_actualizacion", nullable = false)
    private Integer fkSeguridadActualizacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // Getters/Setters
    public Integer getIdSarlaft() { return idSarlaft; }
    public void setIdSarlaft(Integer idSarlaft) { this.idSarlaft = idSarlaft; }

    public Integer getIdDatosPersonal() { return idDatosPersonal; }
    public void setIdDatosPersonal(Integer idDatosPersonal) { this.idDatosPersonal = idDatosPersonal; }

    public Boolean getExoneracionUiaf() { return exoneracionUiaf; }
    public void setExoneracionUiaf(Boolean exoneracionUiaf) { this.exoneracionUiaf = exoneracionUiaf; }

    public LocalDate getFechaExoneracion() { return fechaExoneracion; }
    public void setFechaExoneracion(LocalDate fechaExoneracion) { this.fechaExoneracion = fechaExoneracion; }

    public Boolean getAsociadoPeps() { return asociadoPeps; }
    public void setAsociadoPeps(Boolean asociadoPeps) { this.asociadoPeps = asociadoPeps; }

    public String getTipoPeps() { return tipoPeps; }
    public void setTipoPeps(String tipoPeps) { this.tipoPeps = tipoPeps; }

    public String getObservacionesPeps() { return observacionesPeps; }
    public void setObservacionesPeps(String observacionesPeps) { this.observacionesPeps = observacionesPeps; }

    public LocalDate getFechaInicialPeps() { return fechaInicialPeps; }
    public void setFechaInicialPeps(LocalDate fechaInicialPeps) { this.fechaInicialPeps = fechaInicialPeps; }

    public LocalDate getFechaFinalPeps() { return fechaFinalPeps; }
    public void setFechaFinalPeps(LocalDate fechaFinalPeps) { this.fechaFinalPeps = fechaFinalPeps; }

    public Boolean getFamiliaPeps() { return familiaPeps; }
    public void setFamiliaPeps(Boolean familiaPeps) { this.familiaPeps = familiaPeps; }

    public String getTipoFamiliaPeps() { return tipoFamiliaPeps; }
    public void setTipoFamiliaPeps(String tipoFamiliaPeps) { this.tipoFamiliaPeps = tipoFamiliaPeps; }

    public String getCedulaFamiliaPeps() { return cedulaFamiliaPeps; }
    public void setCedulaFamiliaPeps(String cedulaFamiliaPeps) { this.cedulaFamiliaPeps = cedulaFamiliaPeps; }

    public String getCodigoParentesco() { return codigoParentesco; }
    public void setCodigoParentesco(String codigoParentesco) { this.codigoParentesco = codigoParentesco; }

    public String getNombreFamiliaPeps() { return nombreFamiliaPeps; }
    public void setNombreFamiliaPeps(String nombreFamiliaPeps) { this.nombreFamiliaPeps = nombreFamiliaPeps; }

    public Boolean getMonedaExtranjera() { return monedaExtranjera; }
    public void setMonedaExtranjera(Boolean monedaExtranjera) { this.monedaExtranjera = monedaExtranjera; }

    public String getObservacionMonedaExtranjera() { return observacionMonedaExtranjera; }
    public void setObservacionMonedaExtranjera(String observacionMonedaExtranjera) { this.observacionMonedaExtranjera = observacionMonedaExtranjera; }

    public Boolean getCuentaExtranjero() { return cuentaExtranjero; }
    public void setCuentaExtranjero(Boolean cuentaExtranjero) { this.cuentaExtranjero = cuentaExtranjero; }

    public String getTipoMonedaExtranjera() { return tipoMonedaExtranjera; }
    public void setTipoMonedaExtranjera(String tipoMonedaExtranjera) { this.tipoMonedaExtranjera = tipoMonedaExtranjera; }

    public String getNumeroCuentaExtranjero() { return numeroCuentaExtranjero; }
    public void setNumeroCuentaExtranjero(String numeroCuentaExtranjero) { this.numeroCuentaExtranjero = numeroCuentaExtranjero; }

    public String getNombreBancoExtranjero() { return nombreBancoExtranjero; }
    public void setNombreBancoExtranjero(String nombreBancoExtranjero) { this.nombreBancoExtranjero = nombreBancoExtranjero; }

    public String getCiudadCuentaExtranjero() { return ciudadCuentaExtranjero; }
    public void setCiudadCuentaExtranjero(String ciudadCuentaExtranjero) { this.ciudadCuentaExtranjero = ciudadCuentaExtranjero; }

    public String getPaisCuentaExtranjero() { return paisCuentaExtranjero; }
    public void setPaisCuentaExtranjero(String paisCuentaExtranjero) { this.paisCuentaExtranjero = paisCuentaExtranjero; }

    public Integer getFkSeguridadCreacion() { return fkSeguridadCreacion; }
    public void setFkSeguridadCreacion(Integer fkSeguridadCreacion) { this.fkSeguridadCreacion = fkSeguridadCreacion; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public Integer getFkSeguridadActualizacion() { return fkSeguridadActualizacion; }
    public void setFkSeguridadActualizacion(Integer fkSeguridadActualizacion) { this.fkSeguridadActualizacion = fkSeguridadActualizacion; }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
}
