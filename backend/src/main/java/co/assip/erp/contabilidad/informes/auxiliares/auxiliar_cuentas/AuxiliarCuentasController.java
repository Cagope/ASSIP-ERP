
package co.assip.erp.contabilidad.informes.auxiliares.auxiliar_cuentas;

import co.assip.erp.contabilidad.informes.auxiliares.auxiliar_cuentas.dto.AuxiliarCuentasFiltroDTO;
import co.assip.erp.contabilidad.informes.auxiliares.auxiliar_cuentas.dto.AuxiliarCuentasResponseDTO;
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
 * Expone el informe de auxiliares contables por rango de cuentas.
 * Valida el permiso funcional y la agencia antes de consultar.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/contabilidad/informes/auxiliares/cuentas")
public class AuxiliarCuentasController {
    private static final String PERMISO_CONSULTAR = "CONTABILIDAD_AUXILIAR_CUENTAS_VIEW";
    private final AuxiliarCuentasService service;
    private final UsuarioSesionService usuarioSesionService;
    private final UsuarioRepository usuarioRepository;
    private final AccessValidator accessValidator;

    /**
     * Genera el informe con saldos iniciales, movimientos y saldos finales.
     * Verifica el acceso antes de ejecutar la consulta.
     */
    @GetMapping
    public AuxiliarCuentasResponseDTO consultar(
            @RequestParam Integer idAgencia,
            @RequestParam String cuentaInicial,
            @RequestParam String cuentaFinal,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaInicial,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaFinal
    ) {
        validarAcceso(idAgencia);
        AuxiliarCuentasFiltroDTO filtros = new AuxiliarCuentasFiltroDTO(
                idAgencia,
                cuentaInicial,
                cuentaFinal,
                fechaInicial,
                fechaFinal
        );
        return service.generar(filtros);
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
        accessValidator.validarAcceso(usuario, PERMISO_CONSULTAR);
        usuarioSesionService.validarAgencia(idAgencia);
    }
}
