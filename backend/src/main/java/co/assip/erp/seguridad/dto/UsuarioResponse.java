package co.assip.erp.seguridad.dto;

import java.time.OffsetDateTime;

public class UsuarioResponse {
    private Long id;
    private String username;
    private boolean superuserSeguridad;
    private boolean activo;
    private OffsetDateTime ultimoLoginEn;

    public UsuarioResponse() { }
    public UsuarioResponse(Long id, String username, boolean superuserSeguridad, boolean activo, OffsetDateTime ultimoLoginEn) {
        this.id = id;
        this.username = username;
        this.superuserSeguridad = superuserSeguridad;
        this.activo = activo;
        this.ultimoLoginEn = ultimoLoginEn;
    }
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public boolean isSuperuserSeguridad() { return superuserSeguridad; }
    public boolean isActivo() { return activo; }
    public OffsetDateTime getUltimoLoginEn() { return ultimoLoginEn; }
    public void setId(Long id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setSuperuserSeguridad(boolean superuserSeguridad) { this.superuserSeguridad = superuserSeguridad; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public void setUltimoLoginEn(OffsetDateTime ultimoLoginEn) { this.ultimoLoginEn = ultimoLoginEn; }
}
