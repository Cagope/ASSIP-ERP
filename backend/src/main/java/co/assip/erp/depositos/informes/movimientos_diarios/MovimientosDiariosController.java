package co.assip.erp.depositos.informes.movimientos_diarios;

import co.assip.erp.depositos.informes.movimientos_diarios.dto.MovimientosDiariosRequestDTO;
import co.assip.erp.depositos.informes.movimientos_diarios.dto.MovimientosDiariosResponseDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/depositos/informes/movimientos-diarios")
@RequiredArgsConstructor
public class MovimientosDiariosController {

    private static final String PERMISO_VIEW =
            "DEPOSITOS_INFORMES_VIEW";

    private final MovimientosDiariosService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    @PostMapping
    public MovimientosDiariosResponseDTO consultar(
            @RequestBody MovimientosDiariosRequestDTO request
    ) {

        validarAcceso();

        return service.consultar(request);
    }

    @GetMapping("/tipos-movimiento")
    public List<Map<String, Object>> listarTiposMovimiento() {

        validarAcceso();

        String sql = """
                SELECT
                    TRIM(codigo_movimiento) AS codigo_movimiento,
                    descripcion
                FROM depositos.tipo_movimiento
                ORDER BY codigo_movimiento
                """;

        return service.listarTiposMovimiento(sql);
    }

    private void validarAcceso() {

        Usuario usuarioActual = new Usuario();
        usuarioActual.setIdUsuario(
                usuarioSesionService.idUsuario()
        );

        accessValidator.validarAcceso(
                usuarioActual,
                PERMISO_VIEW
        );
    }
}