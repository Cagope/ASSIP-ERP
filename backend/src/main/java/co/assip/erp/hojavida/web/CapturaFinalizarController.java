package co.assip.erp.hojavida.web;

import co.assip.erp.hojavida.service.CapturaService;
import co.assip.erp.seguridad.domain.Usuario;
import co.assip.erp.seguridad.repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Controlador que orquesta la captura completa de Hoja de Vida en un solo envío.
 * Recibe un JSON con todos los bloques: datosPersonales, ubicaciones, laborales, financieros, familiares, referencias, SARLAFT, permisos.
 */
@Slf4j
@RestController
@RequestMapping("/hoja-vida/captura-finalizar")
public class CapturaFinalizarController {

    private final CapturaService capturaService;
    private final UsuarioRepository usuarioRepository;

    public CapturaFinalizarController(CapturaService capturaService, UsuarioRepository usuarioRepository) {
        this.capturaService = capturaService;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Recibe el JSON completo de hoja de vida y lo guarda de forma transaccional.
     * @param data estructura con todos los bloques (ver paso 9 del front).
     * @return ResponseEntity con el idDatosPersonal confirmado.
     */
    @PostMapping
    public ResponseEntity<?> finalizarCaptura(@RequestBody Map<String, Object> data) {
        try {
            Integer userId = currentUserId();
            Integer idPersona = capturaService.finalizarCaptura(data, userId);
            log.info("Captura finalizada para persona id={} por usuario={}", idPersona, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("idDatosPersonal", idPersona));

        } catch (IllegalStateException ex) {
            log.warn("Error validando datos: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));

        } catch (Exception ex) {
            log.error("Error general al finalizar captura: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno al procesar la captura.", "detalle", ex.getMessage()));
        }
    }

    // ===========================================
    // UTILIDAD: obtener idUsuario autenticado
    // ===========================================
    private Integer currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No hay usuario autenticado.");
        }

        String username;
        Object principal = auth.getPrincipal();
        if (principal instanceof UserDetails ud) {
            username = ud.getUsername();
        } else {
            username = String.valueOf(principal);
        }

        Usuario u = usuarioRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado: " + username));

        Long idLong = extractUserId(u);
        return Math.toIntExact(idLong);
    }

    private Long extractUserId(Usuario u) {
        String[] candidateGetters = { "getIdUsuario", "getId", "getId_usuario" };
        for (String g : candidateGetters) {
            try {
                Method m = u.getClass().getMethod(g);
                Object val = m.invoke(u);
                if (val instanceof Long l) return l;
                if (val instanceof Integer i) return i.longValue();
            } catch (NoSuchMethodException ignored) {
            } catch (Exception ex) {
                throw new IllegalStateException("No se pudo leer el ID de Usuario usando " + g + ": " + ex.getMessage(), ex);
            }
        }
        throw new IllegalStateException("No se encontró un getter de ID compatible en Usuario.");
    }
}
