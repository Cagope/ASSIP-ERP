package co.assip.erp.seguridad.service;

public interface PasswordService {
    /**
     * Cambia la contraseña del usuario autenticado (por ID).
     *
     * @param userId          ID del usuario (desde el JWT)
     * @param passwordActual  contraseña actual (texto plano)
     * @param passwordNueva   nueva contraseña (texto plano)
     */
    void cambiarPassword(Long userId, String passwordActual, String passwordNueva);
}
