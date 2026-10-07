package co.assip.erp.activosfijos.informes;

import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/activos-fijos/informes")
@RequiredArgsConstructor
public class ActivosFijosInformesController {

    private static final String PERMISO =
            "ACTIVOS_FIJOS_INFORMES_VIEW";

    private final ActivosFijosInformesService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    // ============================================================
    // INFORME MAESTRO DE ACTIVOS FIJOS
    // GET /activos-fijos/informes/maestro-activos
    // ============================================================
    @GetMapping("/maestro-activos")
    public List<Map<String, Object>> maestroActivos() {

        validarAcceso();

        return service.obtenerMaestroActivos();
    }

    // ============================================================
    // MOVIMIENTOS / KARDEX GENERAL DE ACTIVOS FIJOS
    // GET /activos-fijos/informes/movimientos-activos
    //
    // Filtros opcionales:
    // - idAgencia
    // - codigoMovimiento
    // - fechaIni (yyyy-MM-dd)
    // - fechaFin (yyyy-MM-dd)
    // - idActivoFijo
    // ============================================================
    @GetMapping("/movimientos-activos")
    public List<Map<String, Object>> movimientosActivos(
            @RequestParam(required = false) Integer idAgencia,
            @RequestParam(required = false) String codigoMovimiento,
            @RequestParam(required = false) String fechaIni,
            @RequestParam(required = false) String fechaFin,
            @RequestParam(required = false) Long idActivoFijo
    ) {

        validarAcceso();

        return service.obtenerMovimientosActivos(
                idAgencia,
                codigoMovimiento,
                fechaIni,
                fechaFin,
                idActivoFijo
        );
    }

    // ============================================================
    // RESUMEN DE MOVIMIENTOS
    // ============================================================
    @GetMapping("/resumen-movimientos")
    public List<Map<String, Object>> resumenMovimientos(
            @RequestParam(required = false) Integer idAgencia,
            @RequestParam(required = false) String codigoMovimiento
    ) {

        validarAcceso();

        return service.obtenerResumenMovimientos(
                idAgencia,
                codigoMovimiento
        );
    }

    // ============================================================
    // ACTIVOS PARA CONSULTA DE MOVIMIENTOS
    // ============================================================
    @GetMapping("/activos-movimientos")
    public List<Map<String, Object>> activosMovimientos() {

        validarAcceso();

        return service.obtenerActivosMovimientos();
    }

    // ============================================================
    // SEGURIDAD
    // ============================================================
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