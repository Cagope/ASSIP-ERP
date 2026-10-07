package co.assip.erp.cartera.analisis.curacionreincidencia;

import co.assip.erp.cartera.analisis.curacionreincidencia.dto.*;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/cartera/analisis/curacion-reincidencia")
@RequiredArgsConstructor
public class CuracionReincidenciaController {

    private static final String PERMISO_VIEW = "CARTERA_ANALISIS_VIEW";

    private final CuracionReincidenciaService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    @GetMapping("/control")
    public CuracionReincidenciaControlDTO control() {

        validarAcceso();

        return service.control();
    }

    @GetMapping("/resumen")
    public CuracionReincidenciaResumenDTO resumen(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate periodoDesde,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate periodoHasta,

            @RequestParam(required = false)
            Integer idAgencia,

            @RequestParam(required = false)
            Integer idLineaCredito,

            @RequestParam(required = false)
            String edadEntrada
    ) {

        validarAcceso();

        return service.resumen(
                periodoDesde,
                periodoHasta,
                idAgencia,
                idLineaCredito,
                edadEntrada
        );
    }

    @GetMapping("/periodos")
    public List<CuracionReincidenciaPeriodoDTO> periodos(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate periodoDesde,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate periodoHasta,

            @RequestParam(required = false)
            Integer idAgencia,

            @RequestParam(required = false)
            Integer idLineaCredito,

            @RequestParam(required = false)
            String edadEntrada
    ) {

        validarAcceso();

        return service.periodos(
                periodoDesde,
                periodoHasta,
                idAgencia,
                idLineaCredito,
                edadEntrada
        );
    }

    @GetMapping("/agencias")
    public List<CuracionReincidenciaSegmentoDTO> agencias(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate periodoDesde,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate periodoHasta,

            @RequestParam(required = false)
            Integer idAgencia,

            @RequestParam(required = false)
            Integer idLineaCredito,

            @RequestParam(required = false)
            String edadEntrada
    ) {

        validarAcceso();

        return service.agencias(
                periodoDesde,
                periodoHasta,
                idAgencia,
                idLineaCredito,
                edadEntrada
        );
    }

    @GetMapping("/lineas")
    public List<CuracionReincidenciaSegmentoDTO> lineas(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate periodoDesde,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate periodoHasta,

            @RequestParam(required = false)
            Integer idAgencia,

            @RequestParam(required = false)
            Integer idLineaCredito,

            @RequestParam(required = false)
            String edadEntrada
    ) {

        validarAcceso();

        return service.lineas(
                periodoDesde,
                periodoHasta,
                idAgencia,
                idLineaCredito,
                edadEntrada
        );
    }

    @GetMapping("/edades-entrada")
    public List<CuracionReincidenciaEdadEntradaDTO> edadesEntrada(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate periodoDesde,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate periodoHasta,

            @RequestParam(required = false)
            Integer idAgencia,

            @RequestParam(required = false)
            Integer idLineaCredito,

            @RequestParam(required = false)
            String edadEntrada
    ) {

        validarAcceso();

        return service.edadesEntrada(
                periodoDesde,
                periodoHasta,
                idAgencia,
                idLineaCredito,
                edadEntrada
        );
    }

    @GetMapping("/distribucion-cura")
    public List<CuracionReincidenciaDistribucionCuraDTO> distribucionCura(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate periodoDesde,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate periodoHasta,

            @RequestParam(required = false)
            Integer idAgencia,

            @RequestParam(required = false)
            Integer idLineaCredito,

            @RequestParam(required = false)
            String edadEntrada
    ) {

        validarAcceso();

        return service.distribucionCura(
                periodoDesde,
                periodoHasta,
                idAgencia,
                idLineaCredito,
                edadEntrada
        );
    }

    @GetMapping("/primera-reincidencia")
    public List<CuracionReincidenciaPrimeraReincidenciaDTO> primeraReincidencia(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate periodoDesde,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate periodoHasta,

            @RequestParam(required = false)
            Integer idAgencia,

            @RequestParam(required = false)
            Integer idLineaCredito,

            @RequestParam(required = false)
            String edadEntrada
    ) {

        validarAcceso();

        return service.primeraReincidencia(
                periodoDesde,
                periodoHasta,
                idAgencia,
                idLineaCredito,
                edadEntrada
        );
    }

    @GetMapping("/detalle")
    public List<CuracionReincidenciaDetalleDTO> detalle(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate periodoDesde,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate periodoHasta,

            @RequestParam(required = false)
            Integer idAgencia,

            @RequestParam(required = false)
            Integer idLineaCredito,

            @RequestParam(required = false)
            String edadEntrada,

            @RequestParam(defaultValue = "TODOS")
            String indicador
    ) {

        validarAcceso();

        return service.detalle(
                periodoDesde,
                periodoHasta,
                idAgencia,
                idLineaCredito,
                edadEntrada,
                indicador
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