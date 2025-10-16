package co.assip.erp.seguridad.auth;

public class MeResponse {
    private Long id;
    private String username;
    private boolean superuserSeguridad;
    private boolean activo;
    private boolean debeCambiarPassword;

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public boolean isSuperuserSeguridad() { return superuserSeguridad; }
    public boolean isActivo() { return activo; }
    public boolean isDebeCambiarPassword() { return debeCambiarPassword; }

    public void setId(Long id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setSuperuserSeguridad(boolean superuserSeguridad) { this.superuserSeguridad = superuserSeguridad; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public void setDebeCambiarPassword(boolean debeCambiarPassword) { this.debeCambiarPassword = debeCambiarPassword; }
}
