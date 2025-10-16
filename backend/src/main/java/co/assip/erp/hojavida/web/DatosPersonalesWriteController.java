package co.assip.erp.hojavida.web;

import co.assip.erp.hojavida.dto.DatosPersonalesDTOs.DatosPersonaCreate;
import co.assip.erp.hojavida.dto.DatosPersonalesDTOs.DatosPersonaUpdate;
import co.assip.erp.hojavida.service.DatosPersonalesService;
import co.assip.erp.seguridad.domain.Usuario;
import co.assip.erp.seguridad.repository.UsuarioRepository;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;

@Slf4j
@RestController
@RequestMapping("/hoja-vida/datos-personales")
public class DatosPersonalesWriteController {

    private final DatosPersonalesService service;
    private final UsuarioRepository usuarioRepository;

    public DatosPersonalesWriteController(
            DatosPersonalesService service,
            UsuarioRepository usuarioRepository
    ) {
        this.service = service;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping
    public ResponseEntity<Integer> create(@Valid @RequestBody DatosPersonaCreate req) {
        Integer userId = currentUserId();
        Integer id = service.create(req, userId);
        log.info("POST /datos-personales -> creado id={}", id);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Integer id, @Valid @RequestBody DatosPersonaUpdate req) {
        Integer userId = currentUserId();
        service.update(id, req, userId);
        log.info("PUT /datos-personales/{} -> ok", id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        log.info("DELETE /datos-personales/{} -> ok", id);
        return ResponseEntity.noContent().build();
    }

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
