package co.assip.erp.cartera.analisis.reciprocidadaportes;

import co.assip.erp.cartera.analisis.reciprocidadaportes.dto.*;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cartera/analisis/reciprocidad-aportes")
@RequiredArgsConstructor
public class ReciprocidadAportesController {

    private static final String PERMISO_VIEW = "CARTERA_ANALISIS_VIEW";

    private final ReciprocidadAportesService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    // =========================================================
    // CONTROL
    // =========================================================

    @GetMapping("/control")
    public Map<String, Object> control() {

        validarAcceso();

        return Map.of(
                "proceso", "Reciprocidad de Aportes",
                "estado", "OK",
                "soloLectura", true
        );
    }

    // =========================================================
    // CORTES
    // =========================================================

    @GetMapping("/cortes")
    public List<LocalDate> cortes() {

        validarAcceso();

        return service.listarCortes();
    }

    // =========================================================
    // RESUMEN
    // =========================================================

    @GetMapping("/resumen")
    public ReciprocidadAportesResumenDTO resumen(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaCorte
    ) {

        validarAcceso();

        return service.obtenerResumen(fechaCorte);
    }

    // =========================================================
    // PERSONAS
    // =========================================================

    @GetMapping("/personas")
    public List<ReciprocidadAportesPersonaDTO> personas(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaCorte
    ) {

        validarAcceso();

        return service.listarPersonas(fechaCorte);
    }

    // =========================================================
    // DETALLE
    // =========================================================

    @GetMapping("/detalle")
    public List<ReciprocidadAportesDetalleDTO> detalle(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaCorte
    ) {

        validarAcceso();

        return service.listarDetalle(fechaCorte);
    }

    // =========================================================
    // DETALLE POR PERSONA
    // =========================================================

    @GetMapping("/detalle/persona/{idDatosPersonal}")
    public List<ReciprocidadAportesDetalleDTO> detallePersona(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaCorte,

            @PathVariable
            Long idDatosPersonal
    ) {

        validarAcceso();

        return service.listarDetallePorPersona(
                fechaCorte,
                idDatosPersonal
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