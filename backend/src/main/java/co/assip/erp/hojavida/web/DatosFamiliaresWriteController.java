package co.assip.erp.hojavida.web;

import co.assip.erp.hojavida.dto.DatosFamiliaresDTOs.FamiliarCreate;
import co.assip.erp.hojavida.dto.DatosFamiliaresDTOs.FamiliarUpdate;
import co.assip.erp.hojavida.service.DatosFamiliaresService;
import co.assip.erp.seguridad.domain.Usuario;
import co.assip.erp.seguridad.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;

@RestController
@RequestMapping("/hoja-vida/datos-personales/{idPersona}/familiares")
public class DatosFamiliaresWriteController {

    private final DatosFamiliaresService service;
    private final UsuarioRepository usuarioRepository;

    public DatosFamiliaresWriteController(DatosFamiliaresService service,
                                          UsuarioRepository usuarioRepository) {
        this.service = service;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping
    public ResponseEntity<Integer> create(@PathVariable Integer idPersona,
                                          @RequestBody FamiliarCreate req) {
        Integer userId = currentUserId();
        Integer id = service.create(idPersona, req, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    @PutMapping("/{idFamiliar}")
    public ResponseEntity<Void> update(@PathVariable Integer idPersona,
                                       @PathVariable Integer idFamiliar,
                                       @RequestBody FamiliarUpdate req) {
        Integer userId = currentUserId();
        service.update(idPersona, idFamiliar, req, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{idFamiliar}")
    public ResponseEntity<Void> delete(@PathVariable Integer idPersona,
                                       @PathVariable Integer idFamiliar) {
        service.delete(idPersona, idFamiliar);
        return ResponseEntity.noContent().build();
    }

    // === Igual que en DatosPersonalesWriteController ===
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
