package co.assip.erp.cartera.cierremensual.procesamiento;

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
@RequestMapping("/cartera/cierre-mensual")
@RequiredArgsConstructor
public class ProcesamientoCierreController {

    private static final String PERMISO_EXECUTE =
            "CARTERA_CIERRE_EXECUTE";

    // =========================================================
    // DEPENDENCIAS
    // =========================================================

    private final ProcesamientoCierreService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    // =========================================================
    // PROCESAR CÁLCULOS DE CIERRE
    //
    // POST
    // /api/v1/cartera/cierre-mensual/{idCierreCartera}/procesar-calculos
    //
    // REQUISITOS:
    //
    // - el cierre debe existir
    // - estado_cierre = C
    // - fotografía cerrada en firme
    //
    // ORDEN:
    //
    // 1. Intereses
    // 2. Seguros
    // 3. Alivios
    // 4. Cálculos previos
    // 5. Anexo 1
    // 6. Anexo 2 / PE
    // 7. Validaciones finales
    //
    // IMPORTANTE:
    //
    // - NO recibe idUsuario desde frontend
    // - el usuario se obtiene de la sesión autenticada
    // - NO modifica la fotografía
    // - NO cambia estado_cierre
    // - permite reejecución segura según las reglas
    //   internas de cada proceso
    // =========================================================

    @PostMapping("/{idCierreCartera}/procesar-calculos")
    public ResponseEntity<
            ProcesamientoCierreService.ResultadoProcesamientoCierre
            > procesarCalculos(
            @PathVariable Integer idCierreCartera
    ) {

        validarEjecucion();

        return ResponseEntity.ok(
                service.procesar(
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