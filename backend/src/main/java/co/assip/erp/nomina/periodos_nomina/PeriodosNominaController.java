package co.assip.erp.nomina.periodos_nomina;

import co.assip.erp.nomina.periodos_nomina.dto.PeriodoAccionDTO;
import co.assip.erp.nomina.periodos_nomina.dto.PeriodoNominaListDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/nomina/periodos-nomina")
public class PeriodosNominaController {

    private final PeriodosNominaService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    public PeriodosNominaController(
            PeriodosNominaService service,
            UsuarioSesionService usuarioSesionService,
            AccessValidator accessValidator
    ) {
        this.service = service;
        this.usuarioSesionService = usuarioSesionService;
        this.accessValidator = accessValidator;
    }

    // =========================================================
    // LISTAR
    // =========================================================
    @GetMapping
    public ResponseEntity<List<PeriodoNominaListDTO>> listar(
            @RequestParam(value = "agencia", required = false) Integer idAgencia,
            @RequestParam(value = "anio", required = false) Integer anio
    ) {

        validarAcceso("NOMINA_VIEW");

        return ResponseEntity.ok(
                service.listar(idAgencia, anio)
        );
    }

    // =========================================================
    // PERÍODOS DISPONIBLES PARA CONTABILIZACIÓN
    // =========================================================
    @GetMapping("/para-contabilizacion")
    public ResponseEntity<List<PeriodoNominaListDTO>>
    listarParaContabilizacion() {

        validarAcceso("NOMINA_CONTABILIZACION_EXECUTE");

        return ResponseEntity.ok(
                service.listarParaContabilizacion()
        );
    }

    // =========================================================
    // ACCIONES DEL PERÍODO
    // =========================================================
    @PostMapping("/accion")
    public ResponseEntity<Void> accion(
            @RequestBody PeriodoAccionDTO dto
    ) {

        validarPermisoAccion(dto);

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        service.ejecutarAccion(
                dto,
                idUsuario
        );

        return ResponseEntity.ok().build();
    }

    // =========================================================
    // AUTORIZACIÓN SEGÚN ACCIÓN
    // =========================================================
    private void validarPermisoAccion(
            PeriodoAccionDTO dto
    ) {

        if (dto == null
                || dto.getAccion() == null
                || dto.getAccion().isBlank()) {

            throw new IllegalArgumentException(
                    "Acción es obligatoria"
            );
        }

        String accion =
                dto.getAccion()
                        .trim()
                        .toUpperCase();

        switch (accion) {

            case "ABRIR", "CERRAR", "LIQUIDAR" ->
                    validarAcceso(
                            "NOMINA_LIQUIDACION_EXECUTE"
                    );

            case "CONTABILIZAR" ->
                    validarAcceso(
                            "NOMINA_CONTABILIZACION_EXECUTE"
                    );

            default -> throw new IllegalArgumentException(
                    "Acción no soportada: " + accion
            );
        }
    }

    // =========================================================
    // AUTORIZACIÓN
    // =========================================================
    private void validarAcceso(
            String permiso
    ) {

        Usuario usuarioActual =
                new Usuario();

        usuarioActual.setIdUsuario(
                usuarioSesionService.idUsuario()
        );

        accessValidator.validarAcceso(
                usuarioActual,
                permiso
        );
    }
}