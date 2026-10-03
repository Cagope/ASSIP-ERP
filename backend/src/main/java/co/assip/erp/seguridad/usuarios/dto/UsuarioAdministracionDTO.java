package co.assip.erp.seguridad.usuarios.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Datos administrativos para crear o actualizar
 * un usuario con sus roles y agencias autorizadas.
 *
 * No recibe contraseñas, tokens ni datos de auditoría.
 */
@Getter
@Setter
public class UsuarioAdministracionDTO {

    // Identificación
    private Integer idUsuario;
    private Integer idDatosPersonal;
    private String username;

    // Información general
    private String nombreCompleto;
    private String email;
    private Boolean activo;

    // Vigencia
    private LocalDateTime fechaInicioVigencia;
    private LocalDateTime fechaFinVigencia;

    // Agencia principal
    private Integer idAgenciaPrincipal;

    // Asignaciones
    private List<Integer> roles;
    private List<Integer> agencias;
}