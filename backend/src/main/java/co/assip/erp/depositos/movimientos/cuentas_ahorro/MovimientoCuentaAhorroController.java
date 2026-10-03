package co.assip.erp.depositos.movimientos.cuentasahorro;

import co.assip.erp.depositos.movimientos.cuentasahorro.dto.CuentaMovimientoDTO;
import co.assip.erp.depositos.movimientos.cuentasahorro.dto.MovimientoCuentaPreviewDTO;
import co.assip.erp.depositos.movimientos.cuentasahorro.dto.MovimientoCuentaRequestDTO;
import co.assip.erp.depositos.movimientos.cuentasahorro.dto.MovimientoCuentaResponseDTO;
import co.assip.erp.depositos.movimientos.cuentasahorro.dto.TipoMovimientoDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/depositos/movimientos/cuentas-ahorro")
@RequiredArgsConstructor
public class MovimientoCuentaAhorroController {

    private static final String PERMISO_VIEW =
            "DEPOSITOS_MOVIMIENTOS_VIEW";

    private static final String PERMISO_EDIT =
            "DEPOSITOS_MOVIMIENTOS_EDIT";

    private final MovimientoCuentaAhorroService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    @GetMapping("/buscar-cuentas")
    public List<CuentaMovimientoDTO> buscarCuentas(
            @RequestParam Integer idAgencia,
            @RequestParam(required = false) String documento,
            @RequestParam(required = false) String nombres,
            @RequestParam(required = false) String primerApellido,
            @RequestParam(required = false) String segundoApellido
    ) {

        validarAcceso(PERMISO_VIEW);

        return service.buscarCuentas(
                idAgencia,
                documento,
                nombres,
                primerApellido,
                segundoApellido
        );
    }

    @GetMapping("/tipos-movimiento")
    public List<TipoMovimientoDTO> listarTiposMovimiento() {

        validarAcceso(PERMISO_VIEW);

        return service.listarTiposMovimiento();
    }

    @PostMapping("/preview")
    public MovimientoCuentaPreviewDTO preview(
            @RequestBody MovimientoCuentaRequestDTO request
    ) {

        validarAcceso(PERMISO_VIEW);

        return service.preview(request);
    }

    @PostMapping("/aplicar")
    public MovimientoCuentaResponseDTO aplicar(
            @RequestBody MovimientoCuentaRequestDTO request
    ) {

        validarAcceso(PERMISO_EDIT);

        return service.aplicar(request);
    }

    private void validarAcceso(String permiso) {

        Usuario usuarioActual = new Usuario();
        usuarioActual.setIdUsuario(usuarioSesionService.idUsuario());

        accessValidator.validarAcceso(usuarioActual, permiso);
    }
}