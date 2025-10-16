package co.assip.erp.seguridad.dto;

public class UsuarioEstadoRequest {
    private Boolean activo; // obligatorio

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}
