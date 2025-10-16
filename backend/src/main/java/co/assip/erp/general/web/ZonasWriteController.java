// src/main/java/co/assip/erp/general/web/ZonasWriteController.java
package co.assip.erp.general.web;

import co.assip.erp.general.dto.ZonaDTOs;
import co.assip.erp.general.service.ZonaService;
import co.assip.erp.seguridad.domain.Usuario;
import co.assip.erp.seguridad.repository.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;

@RestController
@RequestMapping("/general/zonas")
public class ZonasWriteController {

    private final ZonaService service;
    private final UsuarioRepository usuarioRepository;

    public ZonasWriteController(ZonaService service, UsuarioRepository usuarioRepository) {
        this.service = service;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping
    public ResponseEntity<Integer> create(@Valid @RequestBody ZonaDTOs.ZonaCreateRequest req) {
        Integer userId = currentUserId();
        Integer id = service.create(req, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Integer id, @Valid @RequestBody ZonaDTOs.ZonaUpdateRequest req) {
        Integer userId = currentUserId();
        service.update(id, req, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // === Obtiene el ID del usuario autenticado ===
    private Integer currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No hay usuario autenticado");
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

    /**
     * Intenta extraer el ID (Long/Integer) de la entidad Usuario con varios posibles getters:
     * getIdUsuario(), getId(), getId_usuario()
     */
    private Long extractUserId(Usuario u) {
        String[] candidateGetters = new String[] { "getIdUsuario", "getId", "getId_usuario" };
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
        throw new IllegalStateException("No se encontró un getter de ID compatible en Usuario (probé getIdUsuario(), getId(), getId_usuario()).");
    }
}
