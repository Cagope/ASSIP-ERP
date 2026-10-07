package co.assip.erp.cartera.cierremensual.causacionintereses;

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
@RequestMapping("/cartera/causacion-intereses")
@RequiredArgsConstructor
public class CausacionInteresesController {

    private static final String PERMISO_EXECUTE =
            "CARTERA_CIERRE_EXECUTE";

    // =========================================================
    // DEPENDENCIAS
    // =========================================================

    private final CausacionInteresesService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    // =========================================================
    // EJECUTAR CAUSACIÓN DE INTERESES
    //
    // Ejemplo:
    //
    // POST
    // /api/v1/cartera/causacion-intereses/1/ejecutar
    //
    // PROCESO ACTUAL:
    //
    // 1. toma la fotografía del cierre
    // 2. usa ultima_fecha_interes
    // 3. calcula días 30/360
    // 4. calcula interés bruto
    // 5. aplica porcentaje de causación
    // 6. genera movimiento débito
    //
    // TODAVÍA NO:
    // - calcula contingentes
    // - consolida saldo al cierre
    // - genera comprobante contable
    // =========================================================

    @PostMapping("/{idCierreCartera}/ejecutar")
    public ResponseEntity<Integer> ejecutar(
            @PathVariable Integer idCierreCartera
    ) {

        validarEjecucion();

        return ResponseEntity.ok(
                service.ejecutar(
                        idCierreCartera
                )
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