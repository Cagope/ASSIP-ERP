package co.assip.erp.nomina.contabilizacion.aportes_parafiscales;

import co.assip.erp.nomina.contabilizacion.aportes_parafiscales.dto.AportesParafiscalesContabilizacionResultadoDTO;
import co.assip.erp.nomina.contabilizacion.liquidacion.dto.LiquidacionMovimientoContableDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/nomina/contabilizacion/aportes-parafiscales")
@RequiredArgsConstructor
public class AportesParafiscalesContabilizacionController {

    private final AportesParafiscalesContabilizacionService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    // ============================================================
    // PREVIEW
    // ============================================================
    @GetMapping("/preview/{idPeriodoNomina}")
    public List<LiquidacionMovimientoContableDTO> preview(
            @PathVariable Integer idPeriodoNomina
    ) {

        validarAcceso("NOMINA_CONTABILIZACION_EXECUTE");

        return service.preview(idPeriodoNomina);
    }

    // ============================================================
    // COMPROBANTE EXISTENTE
    // ============================================================
    @GetMapping("/comprobante/{idPeriodoNomina}")
    public String obtenerComprobante(
            @PathVariable Integer idPeriodoNomina
    ) {

        validarAcceso("NOMINA_CONTABILIZACION_EXECUTE");

        return service.obtenerComprobanteExistente(
                idPeriodoNomina
        );
    }

    // ============================================================
    // CONTABILIZAR
    // ============================================================
    @PostMapping("/ejecutar/{idPeriodoNomina}")
    public AportesParafiscalesContabilizacionResultadoDTO ejecutar(
            @PathVariable Integer idPeriodoNomina
    ) {

        validarAcceso("NOMINA_CONTABILIZACION_EXECUTE");

        return service.contabilizar(
                idPeriodoNomina
        );
    }

    // ============================================================
    // REVERSAR CONTABILIZACIÓN
    // ============================================================
    @PostMapping("/reversar/{idPeriodoNomina}")
    public void reversar(
            @PathVariable Integer idPeriodoNomina
    ) {

        validarAcceso("NOMINA_CONTABILIZACION_EXECUTE");

        service.reversarContabilizacion(
                idPeriodoNomina
        );
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