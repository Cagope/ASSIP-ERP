package co.assip.erp.depositos.cuentas_ahorro;

import co.assip.erp.depositos.cuentas_ahorro.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/depositos/cuentas-ahorro")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CuentaAhorroController {

    private final CuentaAhorroService service;

    // ============================================================
    // 🟦 1. LISTAR CUENTAS SEGÚN LAS AGENCIAS DEL USUARIO
    // ============================================================
    @GetMapping(value = "/listar", produces = "application/json")
    public List<CuentaAhorroDTO> listarCuentas() {
        return service.listarCuentas();
    }

    // ============================================================
    // 🟦 2. LISTAR POR AGENCIA ESPECÍFICA (validado internamente)
    // ============================================================
    @GetMapping(value = "/agencia/{idAgencia}", produces = "application/json")
    public List<CuentaAhorroDTO> listarPorAgencia(@PathVariable Integer idAgencia) {
        return service.listarPorAgencia(idAgencia);
    }

    // ============================================================
    // 🟦 3. DETALLE COMPLETO (validado por agencia)
    // ============================================================
    @GetMapping(value = "/{id}", produces = "application/json")
    public CuentaAhorroDetalleDTO obtenerPorId(@PathVariable Integer id) {
        return service.obtenerPorId(id);
    }

    // ============================================================
    // 🟦 4. VALIDAR ANTES DE CREAR
    // ============================================================
    @PostMapping(value = "/validar", consumes = "application/json", produces = "application/json")
    public CuentaAhorroGuardarRespuesta validar(@RequestBody CuentaAhorroGuardarDTO dto) {
        return service.validarAntesDeGuardar(dto);
    }

    // ============================================================
    // 🟦 5. CREAR CUENTA
    // ============================================================
    @PostMapping(consumes = "application/json", produces = "application/json")
    public CuentaAhorroGuardarRespuesta guardar(@RequestBody CuentaAhorroGuardarDTO dto) {
        return service.guardar(dto);
    }

    // ============================================================
    // 🟦 6. ELIMINAR CUENTA (validado por agencia)
    // ============================================================
    @DeleteMapping(value = "/{idCuenta}", produces = "application/json")
    public CuentaAhorroGuardarRespuesta eliminar(@PathVariable Integer idCuenta) {
        return service.eliminar(idCuenta);
    }
}
