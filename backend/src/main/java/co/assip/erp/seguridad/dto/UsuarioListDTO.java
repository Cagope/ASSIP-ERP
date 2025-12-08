package co.assip.erp.seguridad.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioListDTO {

    private Integer idUsuario;
    private String username;
    private String nombreCompleto;
    private String email;
    private Boolean activo;

    private Integer idRol;
    private String nombreRol;

    private Integer idAgenciaPrincipal;
}
