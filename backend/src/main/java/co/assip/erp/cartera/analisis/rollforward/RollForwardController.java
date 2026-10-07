package co.assip.erp.cartera.analisis.rollforward;

import co.assip.erp.cartera.analisis.rollforward.dto.*;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/cartera/analisis/roll-forward")
@RequiredArgsConstructor
public class RollForwardController {

    private static final String PERMISO_VIEW = "CARTERA_ANALISIS_VIEW";

    private final RollForwardService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    // =========================================================
    // CORTES
    // =========================================================

    @GetMapping("/cortes")
    public List<RollForwardCorteDTO> listarCortes() {

        validarAcceso();

        return service.listarCortes();
    }

    // =========================================================
    // RESUMEN
    // =========================================================

    @GetMapping("/resumen")
    public RollForwardResumenDTO obtenerResumen(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaCorte
    ) {

        validarAcceso();

        return service.obtenerResumen(fechaCorte);
    }

    // =========================================================
    // RESUMEN HISTÓRICO
    // =========================================================

    @GetMapping("/resumen/historico")
    public List<RollForwardResumenDTO> listarHistoricoResumen() {

        validarAcceso();

        return service.listarHistoricoResumen();
    }

    // =========================================================
    // LÍNEAS
    // =========================================================

    @GetMapping("/lineas")
    public List<RollForwardLineaDTO> listarLineas(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaCorte
    ) {

        validarAcceso();

        return service.listarLineas(fechaCorte);
    }

    // =========================================================
    // TIPOS DE MOVIMIENTO
    // =========================================================

    @GetMapping("/tipos-movimiento")
    public List<String> listarTiposMovimiento(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaCorte
    ) {

        validarAcceso();

        return service.listarTiposMovimiento(fechaCorte);
    }

    // =========================================================
    // DETALLE
    // =========================================================

    @GetMapping("/detalle")
    public List<RollForwardDetalleDTO> listarDetalle(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaCorte,

            @RequestParam(required = false)
            String tipoMovimiento,

            @RequestParam(required = false)
            Long idLineaCredito
    ) {

        validarAcceso();

        return service.listarDetalle(
                fechaCorte,
                tipoMovimiento,
                idLineaCredito
        );
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
                PERMISO_VIEW
        );
    }
}