package co.assip.erp.cdat.analisis.concentracion;

import co.assip.erp.cdat.analisis.concentracion.dto.ConcentracionCdatDepositanteDTO;
import co.assip.erp.cdat.analisis.concentracion.dto.ConcentracionCdatDetalleDTO;
import co.assip.erp.cdat.analisis.concentracion.dto.ConcentracionCdatResumenDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/cdat/analisis/concentracion")
@RequiredArgsConstructor
public class ConcentracionCdatController {

    private static final String PERMISO =
            "CDAT_ANALISIS_VIEW";

    private final ConcentracionCdatService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    // =========================================================
    // CORTES DISPONIBLES
    // =========================================================

    @GetMapping("/cortes")
    public List<LocalDate> listarCortes() {

        validarAcceso();

        return service.listarCortes();
    }

    // =========================================================
    // RESUMEN
    // =========================================================

    @GetMapping("/resumen")
    public ConcentracionCdatResumenDTO consultarResumen(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaCorte,

            @RequestParam(required = false)
            Integer idAgencia
    ) {

        validarAcceso();

        return service.consultarResumen(
                fechaCorte,
                idAgencia
        );
    }

    // =========================================================
    // RANKING DE DEPOSITANTES
    // =========================================================

    @GetMapping("/ranking")
    public List<ConcentracionCdatDepositanteDTO> consultarRanking(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaCorte,

            @RequestParam(required = false)
            Integer idAgencia
    ) {

        validarAcceso();

        return service.consultarRanking(
                fechaCorte,
                idAgencia
        );
    }

    // =========================================================
    // DETALLE DE CDAT POR DEPOSITANTE
    // =========================================================

    @GetMapping("/depositantes/{idDatosPersonal}/detalle")
    public List<ConcentracionCdatDetalleDTO> consultarDetalle(
            @PathVariable Long idDatosPersonal,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaCorte,

            @RequestParam(required = false)
            Integer idAgencia
    ) {

        validarAcceso();

        return service.consultarDetalle(
                fechaCorte,
                idDatosPersonal,
                idAgencia
        );
    }

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