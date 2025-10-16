package co.assip.erp.seguridad.auth;

public class LoginResponse {
    private String token;
    private Long id;
    private String username;
    private boolean superuserSeguridad;
    private boolean debeCambiarPassword;
    private boolean activo;

    public LoginResponse() { }

    public LoginResponse(String token, Long id, String username, boolean superuserSeguridad, boolean debeCambiarPassword, boolean activo) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.superuserSeguridad = superuserSeguridad;
        this.debeCambiarPassword = debeCambiarPassword;
        this.activo = activo;
    }

    public String getToken() { return token; }
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public boolean isSuperuserSeguridad() { return superuserSeguridad; }
    public boolean isDebeCambiarPassword() { return debeCambiarPassword; }
    public boolean isActivo() { return activo; }

    public void setToken(String token) { this.token = token; }
    public void setId(Long id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setSuperuserSeguridad(boolean superuserSeguridad) { this.superuserSeguridad = superuserSeguridad; }
    public void setDebeCambiarPassword(boolean debeCambiarPassword) { this.debeCambiarPassword = debeCambiarPassword; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
