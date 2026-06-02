package co.assip.erp.cajas.captura_depositos;

import co.assip.erp.cajas.captura_depositos.dto.CajaCapturaDepositosCuentaDTO;
import co.assip.erp.cajas.captura_depositos.dto.CajaCapturaDepositosPreviewDTO;
import co.assip.erp.cajas.captura_depositos.dto.CajaCapturaDepositosRequestDTO;
import co.assip.erp.cajas.captura_depositos.dto.CajaCapturaDepositosResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cajas/captura_depositos")
@RequiredArgsConstructor
public class CajaCapturaDepositosController {

    private final CajaCapturaDepositosService service;
    private final CajaCapturaDepositosRepository repository;

    @GetMapping("/cuenta/{idCuentaAhorro}")
    public CajaCapturaDepositosCuentaDTO obtenerCuenta(
            @PathVariable Long idCuentaAhorro
    ) {
        return repository.obtenerCuenta(idCuentaAhorro);
    }

    @GetMapping("/buscar-cuentas")
    public java.util.List<CajaCapturaDepositosCuentaDTO> buscarCuentas(
            @RequestParam Integer idAgencia,
            @RequestParam(required = false) String documento,
            @RequestParam(required = false) String nombres,
            @RequestParam(required = false) String primerApellido,
            @RequestParam(required = false) String segundoApellido
    ) {
        return repository.buscarCuentas(
                idAgencia,
                documento,
                nombres,
                primerApellido,
                segundoApellido
        );
    }

    @GetMapping("/tipos_movimiento")
    public java.util.List<co.assip.erp.depositos.movimientos.cuentasahorro.dto.TipoMovimientoDTO> listarTiposMovimiento() {
        return repository.listarTiposMovimientoCaja();
    }

    @PostMapping("/preview")
    public CajaCapturaDepositosPreviewDTO preview(
            @RequestBody CajaCapturaDepositosRequestDTO request
    ) {
        return service.preview(request);
    }

    @PostMapping("/aplicar")
    public CajaCapturaDepositosResponseDTO aplicar(
            @RequestBody CajaCapturaDepositosRequestDTO request
    ) {
        return service.aplicar(request);
    }
}