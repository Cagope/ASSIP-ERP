// src/main/java/co/assip/erp/hojavida/web/ReferenciasPersonalesWriteController.java
package co.assip.erp.hojavida.web;

import co.assip.erp.hojavida.dto.ReferenciasPersonalesDTOs.ReferenciaPersonalRequest;
import co.assip.erp.hojavida.service.ReferenciaPersonalService;
import co.assip.erp.seguridad.domain.Usuario;
import co.assip.erp.seguridad.repository.UsuarioRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;

@RestController
@RequestMapping("/hoja-vida/datos-personales/{idDatosPersonal}/referencia-personal")
public class ReferenciasPersonalesWriteController {

    private final ReferenciaPersonalService service;
    private final UsuarioRepository usuarioRepository;

    public ReferenciasPersonalesWriteController(ReferenciaPersonalService service,
                                                UsuarioRepository usuarioRepository) {
        this.service = service;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping
    public ResponseEntity<?> create(@PathVariable Integer idDatosPersonal,
                                    @RequestBody ReferenciaPersonalRequest req) {
        Integer userId = currentUserId();
        Integer id = service.createForPersona(idDatosPersonal, userId, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    @PutMapping("/{idReferencia}")
    public ResponseEntity<Void> update(@PathVariable Integer idDatosPersonal,
                                       @PathVariable Integer idReferencia,
                                       @RequestBody ReferenciaPersonalRequest req) {
        Integer userId = currentUserId();
        service.updateForPersona(idDatosPersonal, idReferencia, userId, req);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{idReferencia}")
    public ResponseEntity<Void> delete(@PathVariable Integer idDatosPersonal,
                                       @PathVariable Integer idReferencia) {
        service.deleteForPersona(idDatosPersonal, idReferencia);
        return ResponseEntity.noContent().build();
    }

    // ===== Handlers para evitar 500 genéricos y tener mensajes claros =====
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArg(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleFK(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(root(ex));
    }

    // 👇 Catch-all temporal para depurar 500 (quítalo cuando todo esté estable)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAny(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ERR: " + root(ex));
    }

    private String root(Throwable t) {
        Throwable r = t;
        while (r.getCause() != null) r = r.getCause();
        return (r.getMessage() != null ? r.getMessage() : r.toString());
    }

    private Integer currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No hay usuario autenticado");
        }
        String username;
        Object principal = auth.getPrincipal();
        if (principal instanceof UserDetails ud) username = ud.getUsername();
        else username = String.valueOf(principal);

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
                throw new IllegalStateException(
                        "No se pudo leer el ID de Usuario usando " + g + ": " + ex.getMessage(), ex
                );
            }
        }
        throw new IllegalStateException("No se encontró un getter de ID compatible en Usuario.");
    }
}
