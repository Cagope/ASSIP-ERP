
package co.assip.erp.contabilidad.informes.especiales.resumen_diario_detallado;

import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import co.assip.erp.seguridad.usuarios.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * Expone el informe especial Resumen diario detallado.
 * Verifica el permiso funcional y el acceso a la agencia
 * antes de consultar los movimientos contables.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/contabilidad/informes/especiales/resumen-diario-detallado")
public class ResumenDiarioDetalladoController {

    private static final String PERMISO_CONSULTAR =
            "CONTABILIDAD_INFORMES_ESPECIALES_VIEW";

    private final ResumenDiarioDetalladoService service;
    private final UsuarioSesionService usuarioSesionService;
    private final UsuarioRepository usuarioRepository;
    private final AccessValidator accessValidator;

    /**
     * Consulta los movimientos de una agencia y período.
     * Devuelve el detalle por fecha con subtotales y totales generales.
     */
    @GetMapping
    public ResumenDiarioDetalladoService.Resultado consultar(
            @RequestParam Integer idAgencia,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaInicial,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaFinal
    ) {
        validarAcceso(idAgencia);

        return service.consultar(
                idAgencia,
                fechaInicial,
                fechaFinal
        );
    }

    /**
     * Comprueba que el usuario esté activo y tenga el permiso funcional.
     * Restringe la consulta a las agencias autorizadas en su sesión.
     */
    private void validarAcceso(Integer idAgencia) {
        Integer idUsuario = usuarioSesionService.idUsuario();

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .orElseThrow(() -> new SecurityException(
                        "El usuario autenticado no está activo."
                ));

        accessValidator.validarAcceso(
                usuario,
                PERMISO_CONSULTAR
        );

        usuarioSesionService.validarAgencia(idAgencia);
    }
}
