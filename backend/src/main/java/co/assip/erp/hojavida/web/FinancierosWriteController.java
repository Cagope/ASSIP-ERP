
package co.assip.erp.hojavida.web;

import co.assip.erp.hojavida.dto.FinancierosDTOs.FinancieroRequest;
import co.assip.erp.hojavida.service.FinancieroService;
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
@RequestMapping("/hoja-vida/datos-personales/{idDatosPersonal}/financiero")
public class FinancierosWriteController {

    private final FinancieroService service;
    private final UsuarioRepository usuarioRepository;

    public FinancierosWriteController(FinancieroService service, UsuarioRepository usuarioRepository) {
        this.service = service;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping
    public ResponseEntity<?> create(@PathVariable Integer idDatosPersonal,
                                    @RequestBody FinancieroRequest req) {
        Integer uid = currentUserId();
        Integer id = service.createForPersona(idDatosPersonal, uid, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    @PutMapping("/{idFinanciero}")
    public ResponseEntity<Void> update(@PathVariable Integer idDatosPersonal,
                                       @PathVariable Integer idFinanciero,
                                       @RequestBody FinancieroRequest req) {
        Integer uid = currentUserId();
        service.updateForPersona(idDatosPersonal, idFinanciero, uid, req);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{idFinanciero}")
    public ResponseEntity<Void> delete(@PathVariable Integer idDatosPersonal,
                                       @PathVariable Integer idFinanciero) {
        service.deleteForPersona(idDatosPersonal, idFinanciero);
        return ResponseEntity.noContent().build();
    }

    // === errores legibles ===
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> state(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> arg(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> fk(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(root(ex));
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> any(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ERR: " + root(ex));
    }

    private String root(Throwable t) {
        Throwable r = t;
        while (r.getCause() != null) r = r.getCause();
        return r.getMessage() != null ? r.getMessage() : r.toString();
    }

    private Integer currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) throw new IllegalStateException("No hay usuario autenticado");
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
        String[] getters = {"getIdUsuario","getId","getId_usuario"};
        for (String g : getters) {
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
