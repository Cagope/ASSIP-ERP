package co.assip.erp.activosfijos.depreciacion;

import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/activos-fijos/depreciacion")
@RequiredArgsConstructor
public class DepreciacionController {

    private static final String PERMISO =
            "ACTIVOS_FIJOS_DEPRECIACION";

    private final DepreciacionPreviewService previewService;
    private final DepreciacionEjecucionService ejecucionService;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    /**
     * PREVIEW del proceso de depreciación.
     * No escribe en base de datos.
     */
    @PostMapping("/preview")
    public ResponseEntity<DepreciacionPreviewResult> preview(
            @RequestBody DepreciacionRequestDTO request
    ) {

        validarAcceso();

        DepreciacionPreviewResult result =
                previewService.ejecutarPreview(request);

        return ResponseEntity.ok(result);
    }

    /**
     * EJECUTA definitivamente la depreciación del período.
     * Es transaccional.
     */
    @PostMapping("/ejecutar")
    public ResponseEntity<DepreciacionEjecucionResult> ejecutar(
            @RequestBody DepreciacionRequestDTO request
    ) {

        validarAcceso();

        DepreciacionEjecucionResult result =
                ejecucionService.ejecutar(request);

        return ResponseEntity.ok(result);
    }

    // =========================================================
    // SEGURIDAD
    // =========================================================
    private void validarAcceso() {

        Usuario usuarioActual = new Usuario();
        usuarioActual.setIdUsuario(
                usuarioSesionService.idUsuario()
        );

        accessValidator.validarAcceso(
                usuarioActual,
                PERMISO
        );
    }
}