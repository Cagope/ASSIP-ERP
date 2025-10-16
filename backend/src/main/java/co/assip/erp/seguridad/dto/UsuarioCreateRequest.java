package co.assip.erp.seguridad.dto;

public class UsuarioCreateRequest {
    private String username;   // obligatorio
    private String password;   // opcional (si no viene, la generamos)
    private Boolean activo;    // opcional (default true)

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public Boolean getActivo() { return activo; }

    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}
