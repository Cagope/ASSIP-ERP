package co.assip.erp.cartera.cierremensual.cuadre;

import co.assip.erp.cartera.cierremensual.cuadre.dto.CuadreCierreDTO;
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
@RequestMapping("/cartera/cierre-mensual/cuadre")
@RequiredArgsConstructor
public class CuadreCierreController {

    private static final String PERMISO_VIEW =
            "CARTERA_CIERRE_VIEW";

    // =========================================================
    // DEPENDENCIAS
    // =========================================================

    private final CuadreCierreService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;


    // =========================================================
    // CUADRE CONSOLIDADO FINAL DEL CIERRE
    //
    // GET
    // /api/v1/cartera/cierre-mensual/cuadre/{idCierreCartera}
    //
    // IMPORTANTE:
    //
    // - NO recalcula cartera.
    // - NO modifica resultados.
    // - NO modifica PE.
    // - NO cambia estados.
    // - NO genera comprobantes.
    //
    // Solamente consolida y valida:
    //
    // 1. TOTAL = A1 + PE
    // 2. PE general = pe_resultados
    // 3. deterioro PE = pérdida esperada
    //
    // =========================================================

    @GetMapping("/{idCierreCartera}")
    public ResponseEntity<CuadreCierreDTO> cuadrar(
            @PathVariable Integer idCierreCartera
    ) {

        validarConsulta();

        return ResponseEntity.ok(
                service.cuadrar(
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