package co.assip.erp.seguridad.usuarios.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

import java.util.List;

/**
 * DTO utilizado para exponer de forma segura
 * la información administrativa de un usuario.
 *
 * Permite representar múltiples roles asignados al usuario.
 *
 * No contiene contraseña, tokens de recuperación
 * ni información interna de autenticación.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioListDTO {

    private Integer idUsuario;

    private Integer idDatosPersonal;

    private String username;

    private String nombreCompleto;

    private String email;

    private Boolean activo;

    /**
     * Indica si la cuenta requiere activación inicial.
     * No expone información sensible.
     */
    private Boolean pendienteActivacion;

    private LocalDateTime fechaInicioVigencia;

    private LocalDateTime fechaFinVigencia;

    /**
     * Roles actualmente asignados al usuario.
     */
    private List<RolDTO> roles;

    private Integer idAgenciaPrincipal;

    /**
     * Información básica de un rol asignado al usuario.
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RolDTO {

        private Integer idRol;

        private String nombreRol;
    }
}