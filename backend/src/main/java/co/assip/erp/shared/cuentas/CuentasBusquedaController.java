package co.assip.erp.shared.cuentas;

import co.assip.erp.shared.cuentas.dto.CuentaBusquedaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/shared/cuentas")
public class CuentasBusquedaController {

    private final CuentasBusquedaService service;

    @GetMapping("/buscar")
    public List<CuentaBusquedaDTO> buscar(
            @RequestParam("q") String texto,
            @RequestHeader(value = "X-Agencias", required = false) List<Integer> agenciasUsuario
    ) {
        return service.buscar(texto, agenciasUsuario);
    }

    // =========================================================
    // 2) OBTENER POR ID (NUEVO)
    // =========================================================
    @GetMapping("/{id}")
    public CuentaBusquedaDTO obtenerPorId(
            @PathVariable Long id,
            @RequestHeader(value = "X-Agencias", required = false) List<Integer> agenciasUsuario
    ) {
        return service.obtenerPorId(id, agenciasUsuario);
    }

    @GetMapping("/buscar-bancos")
    public List<CuentaBusquedaDTO> buscarBancos(
            @RequestParam("q") String texto,
            @RequestHeader(value = "X-Agencias", required = false) List<Integer> agenciasUsuario
    ) {
        return service.buscarBancos(texto, agenciasUsuario);
    }

    @GetMapping("/buscar-traslados-agencias")
    public List<CuentaBusquedaDTO> buscarTrasladosAgencias(
            @RequestParam("q") String texto,
            @RequestHeader(value = "X-Agencias", required = false) List<Integer> agenciasUsuario
    ) {
        return service.buscarTrasladosAgencias(texto, agenciasUsuario);
    }

}
