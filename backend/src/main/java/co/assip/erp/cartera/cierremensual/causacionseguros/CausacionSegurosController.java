package co.assip.erp.cartera.cierremensual.causacionseguros;

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
@RequestMapping("/cartera/causacion-seguros")
@RequiredArgsConstructor
public class CausacionSegurosController {

    private static final String PERMISO_EXECUTE =
            "CARTERA_CIERRE_EXECUTE";

    // =========================================================
    // DEPENDENCIAS
    // =========================================================

    private final CausacionSegurosService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    // =========================================================
    // EJECUTAR CAUSACIÓN DE SEGUROS
    //
    // POST
    // /cartera/causacion-seguros/{idCierreCartera}/ejecutar
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