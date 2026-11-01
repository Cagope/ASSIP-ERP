package co.assip.erp.contabilidad.informes.especiales.comprobante_resumen;

import co.assip.erp.contabilidad.informes.especiales.comprobante_resumen.dto.ComprobanteResumenResponseDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.UsuarioRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/contabilidad/informes/especiales/comprobante-resumen")
public class ComprobanteResumenController {
    private static final String PERMISO = "CONTABILIDAD_INFORMES_ESPECIALES_VIEW";
    private final ComprobanteResumenService service;
    private final UsuarioSesionService usuarioSesionService;
    private final UsuarioRepository usuarioRepository;
    private final AccessValidator accessValidator;

    /** Autoriza la consulta por permiso funcional y por agencia de sesión. */
    @GetMapping
    public ComprobanteResumenResponseDTO consultar(
            @RequestParam Integer idAgencia,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicial,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFinal
    ) {
        Integer idUsuario = usuarioSesionService.idUsuario();
        var usuario = usuarioRepository.findById(idUsuario)
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .orElseThrow(() -> new SecurityException("El usuario autenticado no está activo."));
        accessValidator.validarAcceso(usuario, PERMISO);
        usuarioSesionService.validarAgencia(idAgencia);
        return service.consultar(idAgencia, fechaInicial, fechaFinal);
    }
}
