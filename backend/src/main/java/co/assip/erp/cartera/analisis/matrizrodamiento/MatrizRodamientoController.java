package co.assip.erp.cartera.analisis.matrizrodamiento;

import co.assip.erp.cartera.analisis.matrizrodamiento.dto.MatrizRodamientoCorteDTO;
import co.assip.erp.cartera.analisis.matrizrodamiento.dto.MatrizRodamientoDTO;
import co.assip.erp.cartera.analisis.matrizrodamiento.dto.MatrizRodamientoDetalleDTO;
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

@RestController
@RequestMapping("/cartera/analisis/matriz-rodamiento")
@RequiredArgsConstructor
public class MatrizRodamientoController {

    private static final String PERMISO_VIEW = "CARTERA_ANALISIS_VIEW";

    // =========================================================
    // DEPENDENCIAS
    // =========================================================

    private final MatrizRodamientoService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    // =========================================================
    // CORTES DISPONIBLES
    // =========================================================

    @GetMapping("/cortes")
    public ResponseEntity<List<MatrizRodamientoCorteDTO>>
    listarCortesDisponibles() {

        validarAcceso();

        return ResponseEntity.ok(
                service.listarCortesDisponibles()
        );
    }

    // =========================================================
    // CALCULAR MATRIZ
    // =========================================================

    /**
     * ACTUAL CONTRA CORTE:
     *
     * /calcular
     * ?tipoPartida=ACTUAL
     * &fechaComparacion=2026-05-31
     *
     *
     * CORTE CONTRA CORTE:
     *
     * /calcular
     * ?tipoPartida=CORTE
     * &fechaPartida=2026-05-31
     * &fechaComparacion=2026-04-30
     */
    @GetMapping("/calcular")
    public ResponseEntity<MatrizRodamientoDTO> calcular(
            @RequestParam
            String tipoPartida,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaPartida,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaComparacion
    ) {

        validarAcceso();

        return ResponseEntity.ok(
                service.calcular(
                        tipoPartida,
                        fechaPartida,
                        fechaComparacion
                )
        );
    }

    // =========================================================
    // DETALLE DE CELDA
    // =========================================================

    /**
     * Ejemplo:
     *
     * /detalle
     * ?tipoPartida=CORTE
     * &fechaPartida=2026-05-31
     * &fechaComparacion=2026-04-30
     * &categoriaAnterior=B
     * &categoriaPartida=D
     *
     *
     * Devuelve exclusivamente los créditos
     * que forman la celda B -> D.
     */
    @GetMapping("/detalle")
    public ResponseEntity<List<MatrizRodamientoDetalleDTO>>
    listarDetalleCelda(
            @RequestParam
            String tipoPartida,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaPartida,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaComparacion,

            @RequestParam
            String categoriaAnterior,

            @RequestParam
            String categoriaPartida
    ) {

        validarAcceso();

        return ResponseEntity.ok(
                service.listarDetalleCelda(
                        tipoPartida,
                        fechaPartida,
                        fechaComparacion,
                        categoriaAnterior,
                        categoriaPartida
                )
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