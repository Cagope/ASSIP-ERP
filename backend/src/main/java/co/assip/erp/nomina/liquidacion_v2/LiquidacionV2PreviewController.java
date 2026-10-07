package co.assip.erp.nomina.liquidacion_v2;

import co.assip.erp.nomina.liquidacion_v2.dto.LiquidacionV2PreviewResponseDTO;
import co.assip.erp.nomina.liquidacion_v2.dto.LiquidacionV2RequestDTO;
import co.assip.erp.nomina.periodos_nomina.PeriodosNominaRepository;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/nomina/liquidacion-v2")
@RequiredArgsConstructor
public class LiquidacionV2PreviewController {

    private final LiquidacionV2PreviewService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    // =========================================================
    // PREVIEW
    // =========================================================
    @PostMapping("/preview")
    public LiquidacionV2PreviewResponseDTO preview(
            @RequestBody LiquidacionV2RequestDTO request
    ) {

        validarAcceso("NOMINA_LIQUIDACION_EXECUTE");

        return service.preview(request);
    }

    // =========================================================
    // PERÍODO AUTOMÁTICO POR AGENCIA
    // =========================================================
    @GetMapping("/periodo-disponible/{idAgencia}")
    public PeriodosNominaRepository.PeriodoLabel obtenerPeriodoDisponible(
            @PathVariable Integer idAgencia
    ) {

        validarAcceso("NOMINA_LIQUIDACION_EXECUTE");

        return service.obtenerPrimerPeriodoDisponible(
                idAgencia
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