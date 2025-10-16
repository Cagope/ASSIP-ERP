package co.assip.erp.general.web;

import co.assip.erp.general.dto.PrivilegiadoDTOs.*;
import co.assip.erp.general.service.PrivilegiadoService;
import co.assip.erp.seguridad.repository.UsuarioRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/general/privilegiados")
public class PrivilegiadosWriteController {

    private final PrivilegiadoService service;
    private final UsuarioRepository usuarioRepository;

    public PrivilegiadosWriteController(PrivilegiadoService service, UsuarioRepository usuarioRepository) {
        this.service = service;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<Integer> create(@Valid @RequestBody PrivilegiadoCreateRequest req) {
        Integer userId = currentUserId();
        Integer id = service.create(req, userId);
        return ResponseEntity.ok(id);
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> update(@PathVariable @Min(1) Integer id,
                                       @Valid @RequestBody PrivilegiadoUpdateRequest req) {
        Integer userId = currentUserId();
        service.update(id, req, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> delete(@PathVariable @Min(1) Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    private Integer currentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (auth != null) ? auth.getName() : null;
        if (username == null) throw new IllegalStateException("Usuario no autenticado");

        var user = usuarioRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado: " + username));

        return Math.toIntExact(user.getId());
    }
}
