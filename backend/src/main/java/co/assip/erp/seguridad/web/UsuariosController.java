package co.assip.erp.seguridad.web;

import co.assip.erp.seguridad.dto.UsuarioResponse;
import co.assip.erp.seguridad.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/seguridad/usuarios")
public class UsuariosController {

    private final UsuarioService usuarioService;

    public UsuariosController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<?> listar(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "activo", required = false) Boolean activo,
            @RequestParam(value = "superuser", required = false) Boolean superuser,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sort", defaultValue = "username,asc") String sort,
            HttpServletRequest request
    ) {
        if (size > 100) {
            return ResponseEntity.badRequest().body(
                    "{\"errors\":[{\"code\":\"INVALID_SIZE\",\"message\":\"size max 100\"}]}"
            );
        }

        Sort s = parseSort(sort);
        Pageable pageable = PageRequest.of(page, size, s);

        // Temporal: actor fijo "admin" hasta tener login/JWT
        String actor = "admin";
        String ip = request.getRemoteAddr();
        String ua = request.getHeader("User-Agent");

        Page<UsuarioResponse> data = usuarioService.listar(q, activo, superuser, pageable, actor, ip, ua);

        return ResponseEntity.ok(new ListResponse<>(data, sort));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> detalle(
            @PathVariable("id") Long id,
            HttpServletRequest request
    ) {
        String actor = "admin";
        String ip = request.getRemoteAddr();
        String ua = request.getHeader("User-Agent");

        try {
            UsuarioResponse dto = usuarioService.detalle(id, actor, ip, ua);
            return ResponseEntity.ok(new SingleResponse<>(dto));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(404).body("{\"errors\":[{\"code\":\"NOT_FOUND\",\"message\":\"Usuario no existe\"}]}");
        }
    }

    private Sort parseSort(String sortParam) {
        try {
            String[] parts = sortParam.split(",");
            String prop = parts[0].trim();
            Sort.Direction dir = (parts.length > 1 && "desc".equalsIgnoreCase(parts[1])) ? Sort.Direction.DESC : Sort.Direction.ASC;
            return Sort.by(new Sort.Order(dir, prop).ignoreCase());
        } catch (Exception e) {
            return Sort.by(Sort.Order.asc("username").ignoreCase());
        }
    }

    // Envoltorios simples de respuesta
    record SingleResponse<T>(T data) {}
    record ListResponse<T>(java.util.List<T> data, Meta meta) {
        ListResponse(Page<T> page, String sort) { this(page.getContent(), new Meta(page, sort)); }
    }
    record Meta(int page, int size, long totalElements, int totalPages, String sort) {
        Meta(Page<?> p, String sort) { this(p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages(), sort); }
    }
}
