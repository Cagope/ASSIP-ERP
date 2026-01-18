package co.assip.erp.general.empresas;

import java.time.LocalDate;

public class EmpresaDTO {

    private String tipoDocumento;
    private String documentoEmpresa;
    private String digitoVerificacion;
    private String razonSocial;
    private String siglaEmpresa;
    private LocalDate fechaConstitucion;

    private Integer idPaisDocumento;
    private Integer idDepartamento;
    private Integer idCiudad;

    private String correoCorporativo;
    private String telefono;
    private String celular;
    private String sitioWeb;
    private String logoUrl;

    private Long idDatosPersonalEmpresa;

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getDocumentoEmpresa() {
        return documentoEmpresa;
    }

    public void setDocumentoEmpresa(String documentoEmpresa) {
        this.documentoEmpresa = documentoEmpresa;
    }

    public String getDigitoVerificacion() {
        return digitoVerificacion;
    }

    public void setDigitoVerificacion(String digitoVerificacion) {
        this.digitoVerificacion = digitoVerificacion;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getSiglaEmpresa() {
        return siglaEmpresa;
    }

    public void setSiglaEmpresa(String siglaEmpresa) {
        this.siglaEmpresa = siglaEmpresa;
    }

    public LocalDate getFechaConstitucion() {
        return fechaConstitucion;
    }

    public void setFechaConstitucion(LocalDate fechaConstitucion) {
        this.fechaConstitucion = fechaConstitucion;
    }

    public Integer getIdPaisDocumento() {
        return idPaisDocumento;
    }

    public void setIdPaisDocumento(Integer idPaisDocumento) {
        this.idPaisDocumento = idPaisDocumento;
    }

    public Integer getIdDepartamento() {
        return idDepartamento;
    }

    public void setIdDepartamento(Integer idDepartamento) {
        this.idDepartamento = idDepartamento;
    }

    public Integer getIdCiudad() {
        return idCiudad;
    }

    public void setIdCiudad(Integer idCiudad) {
        this.idCiudad = idCiudad;
    }

    public String getCorreoCorporativo() {
        return correoCorporativo;
    }

    public void setCorreoCorporativo(String correoCorporativo) {
        this.correoCorporativo = correoCorporativo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCelular() {
        return celular;
    }

    public void setCelular(String celular) {
        this.celular = celular;
    }

    public String getSitioWeb() {
        return sitioWeb;
    }

    public void setSitioWeb(String sitioWeb) {
        this.sitioWeb = sitioWeb;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public Long getIdDatosPersonalEmpresa() {
        return idDatosPersonalEmpresa;
    }

    public void setIdDatosPersonalEmpresa(Long idDatosPersonalEmpresa) {
        this.idDatosPersonalEmpresa = idDatosPersonalEmpresa;
    }
}
