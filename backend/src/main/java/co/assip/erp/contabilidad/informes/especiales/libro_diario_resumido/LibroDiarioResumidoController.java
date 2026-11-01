
package co.assip.erp.contabilidad.informes.especiales.libro_diario_resumido;

import co.assip.erp.contabilidad.informes.especiales.libro_diario_resumido.dto.LibroDiarioResumidoFiltroDTO;
import co.assip.erp.contabilidad.informes.especiales.libro_diario_resumido.dto.LibroDiarioResumidoResponseDTO;

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
 * Expone el informe especial Libro diario resumido.
 *
 * Verifica el permiso funcional y el acceso a la agencia
 * antes de ejecutar las consultas contables.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/contabilidad/informes/especiales/libro-diario-resumido")
public class LibroDiarioResumidoController {

    private static final String PERMISO_CONSULTAR =
            "CONTABILIDAD_INFORMES_ESPECIALES_VIEW";

    private final LibroDiarioResumidoService service;
    private final UsuarioSesionService usuarioSesionService;
    private final UsuarioRepository usuarioRepository;
    private final AccessValidator accessValidator;

    /**
     * Consulta el libro diario resumido para una agencia y período.
     *
     * Permite agrupar por cuenta contable o tipo de comprobante.
     * Valida los permisos antes de consultar información financiera.
     */
    @GetMapping
    public LibroDiarioResumidoResponseDTO consultar(
            @RequestParam Integer idAgencia,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaInicial,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaFinal,

            @RequestParam String modalidad
    ) {

        validarAcceso(idAgencia);

        LibroDiarioResumidoFiltroDTO filtro =
                new LibroDiarioResumidoFiltroDTO(
                        idAgencia,
                        fechaInicial,
                        fechaFinal,
                        modalidad
                );

        return service.consultar(filtro);
    }

    /**
     * Comprueba que el usuario esté activo y tenga el permiso requerido.
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
