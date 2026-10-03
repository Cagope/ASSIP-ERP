package co.assip.erp.depositos.informes.resumen_tipo_movimiento;

import co.assip.erp.depositos.informes.resumen_tipo_movimiento.dto.ResumenTipoMovimientoRequestDTO;
import co.assip.erp.depositos.informes.resumen_tipo_movimiento.dto.ResumenTipoMovimientoResponseDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/depositos/informes/resumen-tipo-movimiento")
@RequiredArgsConstructor
public class ResumenTipoMovimientoController {

    private static final String PERMISO_VIEW =
            "DEPOSITOS_INFORMES_VIEW";

    private final ResumenTipoMovimientoService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    @PostMapping
    public ResumenTipoMovimientoResponseDTO consultar(
            @RequestBody ResumenTipoMovimientoRequestDTO request
    ) {

        validarAcceso();

        return service.consultar(request);
    }

    @PostMapping("/detalle")
    public ResumenTipoMovimientoResponseDTO detalle(
            @RequestBody ResumenTipoMovimientoRequestDTO request
    ) {

        validarAcceso();

        return service.detalle(request);
    }

    @GetMapping("/tipos-movimiento")
    public List<Map<String, Object>> listarTiposMovimiento() {

        validarAcceso();

        return service.listarTiposMovimiento();
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