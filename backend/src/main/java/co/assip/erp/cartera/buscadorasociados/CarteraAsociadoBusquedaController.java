package co.assip.erp.cartera.buscadorasociados;

import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador transversal de búsqueda de asociados
 * para los procesos del módulo de Cartera.
 */
@RestController
@RequestMapping("/cartera/asociados")
@RequiredArgsConstructor
public class CarteraAsociadoBusquedaController {

    private static final String PERMISO_VIEW = "CARTERA_VIEW";

    // =========================================================
    // DEPENDENCIAS
    // =========================================================

    private final CarteraAsociadoBusquedaService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    // =========================================================
    // BUSCAR ASOCIADOS
    // =========================================================

    /**
     * Busca asociados que tengan al menos un crédito registrado.
     *
     * Ejemplos:
     *
     * GET /cartera/asociados/buscar?documento=91071719
     *
     * GET /cartera/asociados/buscar?nombres=CARLOS
     *
     * GET /cartera/asociados/buscar
     *     ?primerApellido=GONZALEZ
     *     &segundoApellido=PEREZ
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<CarteraAsociadoBusquedaDTO>> buscar(
            @RequestParam(required = false)
            String documento,

            @RequestParam(required = false)
            String nombres,

            @RequestParam(required = false)
            String primerApellido,

            @RequestParam(required = false)
            String segundoApellido
    ) {

        validarAcceso();

        List<CarteraAsociadoBusquedaDTO> resultados =
                service.buscar(
                        documento,
                        nombres,
                        primerApellido,
                        segundoApellido
                );

        return ResponseEntity.ok(
                resultados
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