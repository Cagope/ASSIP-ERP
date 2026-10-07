package co.assip.erp.cartera.originacion.asociados;

import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cartera/originacion/asociados")
public class OriginacionAsociadoController {

    private static final String PERMISO_VIEW =
            "CARTERA_ORIGINACION_VIEW";

    // =========================================================
    // DEPENDENCIAS
    // =========================================================

    private final OriginacionAsociadoService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    // =========================================================
    // BUSCAR
    // =========================================================

    @GetMapping("/buscar")
    public ResponseEntity<List<OriginacionAsociadoDTO>> buscar(

            @RequestParam
            Integer idAgencia,

            @RequestParam(
                    required = false,
                    defaultValue = ""
            )
            String documento,

            @RequestParam(
                    required = false,
                    defaultValue = ""
            )
            String nombres,

            @RequestParam(
                    required = false,
                    defaultValue = ""
            )
            String primerApellido,

            @RequestParam(
                    required = false,
                    defaultValue = ""
            )
            String segundoApellido
    ) {

        validarConsulta();

        return ResponseEntity.ok(
                service.buscar(
                        documento,
                        nombres,
                        primerApellido,
                        segundoApellido,
                        idAgencia
                )
        );
    }

    // =========================================================
    // BUSCAR POR ID
    // =========================================================

    @GetMapping("/{idDatosPersonal}")
    public ResponseEntity<OriginacionAsociadoDTO> buscarPorId(

            @PathVariable
            Integer idDatosPersonal,

            @RequestParam
            Integer idAgencia
    ) {

        validarConsulta();

        return ResponseEntity.ok(
                service.buscarPorId(
                        idDatosPersonal,
                        idAgencia
                )
        );
    }

    // =========================================================
    // SEGURIDAD
    // =========================================================

    private void validarConsulta() {

        Usuario usuarioActual = new Usuario();

        usuarioActual.setIdUsuario(
                usuarioSesionService.idUsuario()
        );

        accessValidator.validarAcceso(
                usuarioActual,
                PERMISO_VIEW
        );
    }
}