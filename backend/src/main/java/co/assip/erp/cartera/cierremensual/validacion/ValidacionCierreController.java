package co.assip.erp.cartera.cierremensual.validacion;

import co.assip.erp.cartera.cierremensual.validacion.dto.ResultadoValidacionCierreDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cartera/cierre-mensual/validacion")
@RequiredArgsConstructor
public class ValidacionCierreController {

    private static final String PERMISO_VIEW =
            "CARTERA_CIERRE_VIEW";

    // =========================================================
    // DEPENDENCIAS
    // =========================================================

    private final ValidacionCierreService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;


    // =========================================================
    // VALIDAR CIERRE
    //
    // Este endpoint:
    //
    // - NO modifica datos.
    // - NO finaliza el cierre.
    // - NO genera comprobantes.
    // - NO genera informes.
    //
    // Solamente ejecuta las validaciones finales
    // actualmente implementadas.
    //
    // Ejemplo:
    //
    // GET
    // /api/v1/cartera/cierre-mensual/validacion/41
    //
    // =========================================================

    @GetMapping("/{idCierreCartera}")
    public ResponseEntity<ResultadoValidacionCierreDTO> validar(
            @PathVariable Integer idCierreCartera
    ) {

        validarConsulta();

        return ResponseEntity.ok(
                service.validar(
                        idCierreCartera
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