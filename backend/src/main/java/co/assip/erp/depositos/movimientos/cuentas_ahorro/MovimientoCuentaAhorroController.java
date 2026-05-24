package co.assip.erp.depositos.movimientos.cuentasahorro;

import co.assip.erp.depositos.movimientos.cuentasahorro.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/depositos/movimientos/cuentas-ahorro")
@RequiredArgsConstructor
public class MovimientoCuentaAhorroController {

    private final MovimientoCuentaAhorroService service;

    @GetMapping("/buscar-cuentas")
    public List<CuentaMovimientoDTO> buscarCuentas(
            @RequestParam Integer idAgencia,
            @RequestParam(required = false) String documento,
            @RequestParam(required = false) String nombres,
            @RequestParam(required = false) String primerApellido,
            @RequestParam(required = false) String segundoApellido
    ) {
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
        return service.listarTiposMovimiento();
    }

    @PostMapping("/preview")
    public MovimientoCuentaPreviewDTO preview(
            @RequestBody MovimientoCuentaRequestDTO request
    ) {
        return service.preview(request);
    }

    @PostMapping("/aplicar")
    public MovimientoCuentaResponseDTO aplicar(
            @RequestBody MovimientoCuentaRequestDTO request
    ) {
        return service.aplicar(request);
    }
}