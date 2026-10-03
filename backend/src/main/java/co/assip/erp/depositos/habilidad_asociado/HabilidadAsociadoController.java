package co.assip.erp.depositos.habilidad_asociado;

import co.assip.erp.depositos.habilidad_asociado.dto.HabilidadAsociadoEntradaDTO;
import co.assip.erp.depositos.habilidad_asociado.dto.HabilidadAsociadoItemDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 🎯 HabilidadAsociadoController
 * ----------------------------------------------------
 * Expone el proceso técnico para determinar la
 * HABILIDAD / INHABILIDAD de los asociados según:
 *   - Tipo de persona
 *   - Edad
 *   - Aportes en el rango
 *   - Estado de la cuenta
 *   - Saldo actual
 *
 * Endpoint principal:
 *   POST /depositos/habilidad-asociado/ejecutar
 *
 * Procesos adicionales:
 *   POST /depositos/habilidad-asociado/actualizar-estado
 */
@RestController
@RequestMapping("/depositos/habilidad-asociado")
@RequiredArgsConstructor
public class HabilidadAsociadoController {

    private static final String PERMISO_EXECUTE =
            "DEPOSITOS_HABILIDAD_ASOCIADO_EXECUTE";

    private final HabilidadAsociadoService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    // ===============================================================
    // 1. EJECUTAR HABILIDAD / INHABILIDAD
    // ===============================================================
    @PostMapping("/ejecutar")
    public List<HabilidadAsociadoItemDTO> ejecutar(
            @RequestBody HabilidadAsociadoEntradaDTO input
    ) {

        validarAcceso();

        return service.ejecutar(input);
    }

    // ===============================================================
    // 2. ACTUALIZAR ESTADO DE LA CUENTA (A <-> I)
    // ===============================================================
    @PostMapping("/actualizar-estado")
    public String actualizarEstado(
            @RequestParam Integer idCuentaAhorro,
            @RequestParam String nuevoEstado
    ) {

        validarAcceso();

        service.actualizarEstado(
                idCuentaAhorro,
                nuevoEstado
        );

        return "OK";
    }

    private void validarAcceso() {

        Usuario usuarioActual = new Usuario();
        usuarioActual.setIdUsuario(
                usuarioSesionService.idUsuario()
        );

        accessValidator.validarAcceso(
                usuarioActual,
                PERMISO_EXECUTE
        );
    }
}