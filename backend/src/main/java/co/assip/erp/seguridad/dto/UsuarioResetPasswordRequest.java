package co.assip.erp.seguridad.dto;

public class UsuarioResetPasswordRequest {
    // Opcional: si viene, la usamos; si no, generamos una temporal fuerte
    private String nuevaPassword;

    public String getNuevaPassword() { return nuevaPassword; }
    public void setNuevaPassword(String nuevaPassword) { this.nuevaPassword = nuevaPassword; }
}
