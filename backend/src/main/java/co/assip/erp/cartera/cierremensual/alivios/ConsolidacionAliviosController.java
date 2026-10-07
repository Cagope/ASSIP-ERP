package co.assip.erp.cartera.cierremensual.alivios;

import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cartera/consolidacion-alivios")
@RequiredArgsConstructor
public class ConsolidacionAliviosController {

    private static final String PERMISO_EXECUTE =
            "CARTERA_CIERRE_EXECUTE";

    // =========================================================
    // DEPENDENCIAS
    // =========================================================

    private final ConsolidacionAliviosService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    // =========================================================
    // CONSOLIDAR ALIVIOS DEL CIERRE
    //
    // POST
    // /cartera/consolidacion-alivios/{idCierreCartera}/ejecutar
    // =========================================================

    @PostMapping("/{idCierreCartera}/ejecutar")
    public ResponseEntity<Integer> ejecutar(
            @PathVariable Integer idCierreCartera
    ) {

        validarEjecucion();

        int cantidad =
                service.ejecutar(
                        idCierreCartera
                );

        return ResponseEntity.ok(
                cantidad
        );
    }

    // =========================================================
    // SEGURIDAD
    // =========================================================

    private void validarEjecucion() {

        Usuario usuarioActual = new Usuario();
        usuarioActual.setIdUsuario(
                usuarioSesionService.idUsuario()
        );

        accessValidator.validarAcceso(
                usuarioActual,
                PERMISO_EXECUTE
        );
    }
}