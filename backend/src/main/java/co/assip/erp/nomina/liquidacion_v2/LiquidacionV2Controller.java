package co.assip.erp.nomina.liquidacion_v2;

import co.assip.erp.nomina.liquidacion_v2.dto.LiquidacionV2RequestDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/nomina/liquidacion-v2")
@RequiredArgsConstructor
public class LiquidacionV2Controller {

    private final LiquidacionV2Service service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    // ============================================================
    // EJECUTAR LIQUIDACIÓN V2
    // ============================================================
    @PostMapping
    public void liquidar(
            @RequestBody LiquidacionV2RequestDTO request
    ) {

        validarAcceso("NOMINA_LIQUIDACION_EXECUTE");

        service.liquidarPeriodo(request);
    }

    // ============================================================
    // AUTORIZACIÓN
    // ============================================================
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