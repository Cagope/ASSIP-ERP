package co.assip.erp.nomina.liquidacion;

import co.assip.erp.nomina.liquidacion.dto.LiquidacionRequestDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/nomina/liquidacion")
@RequiredArgsConstructor
public class LiquidacionNominaController {

    private final LiquidacionNominaService liquidacionService;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    // =========================================================
    // EJECUTAR LIQUIDACIÓN DE PERÍODO
    // =========================================================
    @PostMapping("/ejecutar")
    public ResponseEntity<Void> ejecutarLiquidacion(
            @RequestBody LiquidacionRequestDTO request
    ) {

        validarAcceso("NOMINA_LIQUIDACION_EXECUTE");

        liquidacionService.liquidarPeriodo(request);

        return ResponseEntity.noContent().build();
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