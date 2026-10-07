package co.assip.erp.nomina.liquidacion;

import co.assip.erp.nomina.liquidacion.dto.LiquidacionRequestDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/nomina/liquidacion/preview")
@RequiredArgsConstructor
public class LiquidacionPreviewController {

    private final LiquidacionPreviewService previewService;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    // =========================================================
    // PREVISUALIZAR LIQUIDACIÓN DE PERÍODO
    // =========================================================
    @PostMapping
    public ResponseEntity<?> preview(
            @RequestBody LiquidacionRequestDTO request
    ) {

        validarAcceso("NOMINA_LIQUIDACION_EXECUTE");

        return ResponseEntity.ok(
                previewService.previewPeriodo(request)
        );
    }

    // =========================================================
    // AUTORIZACIÓN
    // =========================================================
    private void validarAcceso(String permiso) {

        Usuario usuarioActual = new Usuario();

        usuarioActual.setIdUsuario(
                usuarioSesionService.idUsuario()
        );

        accessValidator.validarAcceso(
                usuarioActual,
                permiso
        );
    }
}