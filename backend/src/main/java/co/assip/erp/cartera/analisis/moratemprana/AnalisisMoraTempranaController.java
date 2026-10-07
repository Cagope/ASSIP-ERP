package co.assip.erp.cartera.analisis.moratemprana;

import co.assip.erp.cartera.analisis.moratemprana.dto.MoraTempranaCosechaDTO;
import co.assip.erp.cartera.analisis.moratemprana.dto.MoraTempranaDetalleDTO;
import co.assip.erp.cartera.analisis.moratemprana.dto.MoraTempranaPrimeraMoraDTO;
import co.assip.erp.cartera.analisis.moratemprana.dto.MoraTempranaResumenDTO;
import co.assip.erp.cartera.analisis.moratemprana.dto.MoraTempranaSegmentoDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cartera/analisis/mora-temprana")
@RequiredArgsConstructor
public class AnalisisMoraTempranaController {

    private static final String PERMISO_VIEW = "CARTERA_ANALISIS_VIEW";

    // =========================================================
    // DEPENDENCIAS
    // =========================================================

    private final AnalisisMoraTempranaService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    // =========================================================
    // INFORMACIÓN DE CONTROL
    // =========================================================

    @GetMapping("/control")
    public ResponseEntity<Map<String, Object>> obtenerControl() {

        validarAcceso();

        LocalDate ultimoCorte =
                service.obtenerUltimoCorteDisponible();

        LocalDate ultimaCosechaMadura =
                ultimoCorte
                        .minusMonths(6)
                        .withDayOfMonth(1);

        return ResponseEntity.ok(
                Map.of(
                        "ultimoCorteDisponible",
                        ultimoCorte,
                        "ultimaCosechaMadura",
                        ultimaCosechaMadura,
                        "mobMinimoEvaluacion",
                        6
                )
        );
    }

    // =========================================================
    // RESUMEN
    // =========================================================

    @GetMapping("/resumen")
    public ResponseEntity<MoraTempranaResumenDTO> consultarResumen(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate cosechaDesde,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate cosechaHasta,

            @RequestParam(required = false)
            Integer idAgencia,

            @RequestParam(required = false)
            Integer idLineaCredito
    ) {

        validarAcceso();

        MoraTempranaResumenDTO resultado =
                service.consultarResumen(
                        cosechaDesde,
                        cosechaHasta,
                        idAgencia,
                        idLineaCredito
                );

        return ResponseEntity.ok(resultado);
    }

    // =========================================================
    // COSECHAS
    // =========================================================

    @GetMapping("/cosechas")
    public ResponseEntity<List<MoraTempranaCosechaDTO>> consultarCosechas(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate cosechaDesde,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate cosechaHasta,

            @RequestParam(required = false)
            Integer idAgencia,

            @RequestParam(required = false)
            Integer idLineaCredito
    ) {

        validarAcceso();

        List<MoraTempranaCosechaDTO> resultado =
                service.consultarCosechas(
                        cosechaDesde,
                        cosechaHasta,
                        idAgencia,
                        idLineaCredito
                );

        return ResponseEntity.ok(resultado);
    }

    // =========================================================
    // ANÁLISIS POR AGENCIA
    // =========================================================

    @GetMapping("/agencias")
    public ResponseEntity<List<MoraTempranaSegmentoDTO>> consultarPorAgencia(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate cosechaDesde,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate cosechaHasta,

            @RequestParam(required = false)
            Integer idAgencia,

            @RequestParam(required = false)
            Integer idLineaCredito
    ) {

        validarAcceso();

        List<MoraTempranaSegmentoDTO> resultado =
                service.consultarPorAgencia(
                        cosechaDesde,
                        cosechaHasta,
                        idAgencia,
                        idLineaCredito
                );

        return ResponseEntity.ok(resultado);
    }

    // =========================================================
    // ANÁLISIS POR LÍNEA
    // =========================================================

    @GetMapping("/lineas")
    public ResponseEntity<List<MoraTempranaSegmentoDTO>> consultarPorLinea(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate cosechaDesde,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate cosechaHasta,

            @RequestParam(required = false)
            Integer idAgencia,

            @RequestParam(required = false)
            Integer idLineaCredito
    ) {

        validarAcceso();

        List<MoraTempranaSegmentoDTO> resultado =
                service.consultarPorLinea(
                        cosechaDesde,
                        cosechaHasta,
                        idAgencia,
                        idLineaCredito
                );

        return ResponseEntity.ok(resultado);
    }

    // =========================================================
    // PRIMERA MORA
    // =========================================================

    @GetMapping("/primera-mora")
    public ResponseEntity<List<MoraTempranaPrimeraMoraDTO>> consultarPrimeraMora(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate cosechaDesde,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate cosechaHasta,

            @RequestParam(required = false)
            Integer idAgencia,

            @RequestParam(required = false)
            Integer idLineaCredito
    ) {

        validarAcceso();

        List<MoraTempranaPrimeraMoraDTO> resultado =
                service.consultarPrimeraMora(
                        cosechaDesde,
                        cosechaHasta,
                        idAgencia,
                        idLineaCredito
                );

        return ResponseEntity.ok(resultado);
    }

    // =========================================================
    // DETALLE AUDITABLE
    // =========================================================

    @GetMapping("/detalle")
    public ResponseEntity<List<MoraTempranaDetalleDTO>> consultarDetalle(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate cosechaDesde,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate cosechaHasta,

            @RequestParam(required = false)
            Integer idAgencia,

            @RequestParam(required = false)
            Integer idLineaCredito,

            @RequestParam(
                    required = false,
                    defaultValue = "TODOS"
            )
            String indicador
    ) {

        validarAcceso();

        List<MoraTempranaDetalleDTO> resultado =
                service.consultarDetalle(
                        cosechaDesde,
                        cosechaHasta,
                        idAgencia,
                        idLineaCredito,
                        indicador
                );

        return ResponseEntity.ok(resultado);
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