package co.assip.erp.contabilidad.informes.especiales.verificar_partida_doble;

import co.assip.erp.contabilidad.informes.especiales.verificar_partida_doble.dto.VerificarPartidaDobleResponseDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import co.assip.erp.seguridad.usuarios.UsuarioRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Consulta de partida doble con permiso de informes especiales y control de agencia. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/contabilidad/informes/especiales/verificar-partida-doble")
public class VerificarPartidaDobleController {
    private static final String PERMISO = "CONTABILIDAD_INFORMES_ESPECIALES_VIEW";
    private final VerificarPartidaDobleService service;
    private final UsuarioSesionService usuarioSesionService;
    private final UsuarioRepository usuarioRepository;
    private final AccessValidator accessValidator;

    /** Valida usuario, permiso y agencia antes de ejecutar la verificación. */
    @GetMapping
    public VerificarPartidaDobleResponseDTO consultar(
            @RequestParam Integer idAgencia,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicial,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFinal
    ) {
        Integer idUsuario = usuarioSesionService.idUsuario();
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .orElseThrow(() -> new SecurityException("El usuario autenticado no está activo."));
        accessValidator.validarAcceso(usuario, PERMISO);
        usuarioSesionService.validarAgencia(idAgencia);
        return service.consultar(idAgencia, fechaInicial, fechaFinal);
    }
}
