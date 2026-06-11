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
    // 🟦 2. LISTAR POR AGENCIA ESPECÍFICA
    // ============================================================
    @GetMapping(value = "/agencia/{idAgencia}", produces = "application/json")
    public List<CuentaAhorroDTO> listarPorAgencia(
            @PathVariable Integer idAgencia
    ) {
        return service.listarPorAgencia(idAgencia);
    }

    // ============================================================
    // 🟦 3. DETALLE COMPLETO
    // ============================================================
    @GetMapping(value = "/{id}", produces = "application/json")
    public CuentaAhorroDetalleDTO obtenerPorId(
            @PathVariable Integer id
    ) {
        return service.obtenerPorId(id);
    }

    // ============================================================
    // 🟦 4. VALIDAR ANTES DE CREAR
    // ============================================================
    @PostMapping(
            value = "/validar",
            consumes = "application/json",
            produces = "application/json"
    )
    public CuentaAhorroGuardarRespuesta validar(
            @RequestBody CuentaAhorroGuardarDTO dto
    ) {
        return service.validarAntesDeGuardar(dto);
    }

    // ============================================================
    // 🟦 5. CREAR CUENTA
    // ============================================================
    @PostMapping(
            consumes = "application/json",
            produces = "application/json"
    )
    public CuentaAhorroGuardarRespuesta guardar(
            @RequestBody CuentaAhorroGuardarDTO dto
    ) {
        return service.guardar(dto);
    }

    // ============================================================
    // 🟦 CUENTAS CONJUNTAS — LISTAR
    // ============================================================
    @GetMapping(value = "/{idCuenta}/cuentas-conjuntas", produces = "application/json")
    public List<CuentaConjuntaDTO> listarCuentasConjuntas(
            @PathVariable Integer idCuenta
    ) {
        return service.listarCuentasConjuntas(idCuenta);
    }

    // ============================================================
    // 🟦 CUENTAS CONJUNTAS — AGREGAR
    // ============================================================
    @PostMapping(
            value = "/{idCuenta}/cuentas-conjuntas",
            consumes = "application/json",
            produces = "application/json"
    )
    public CuentaAhorroGuardarRespuesta agregarCuentaConjunta(
            @PathVariable Integer idCuenta,
            @RequestBody CuentaConjuntaDTO dto
    ) {
        return service.agregarCuentaConjunta(idCuenta, dto);
    }

    // ============================================================
    // 🟦 CUENTAS CONJUNTAS — ELIMINAR
    // ============================================================
    @DeleteMapping(
            value = "/{idCuenta}/cuentas-conjuntas/{idCuentaConjunta}",
            produces = "application/json"
    )
    public CuentaAhorroGuardarRespuesta eliminarCuentaConjunta(
            @PathVariable Integer idCuenta,
            @PathVariable Integer idCuentaConjunta
    ) {
        return service.eliminarCuentaConjunta(idCuenta, idCuentaConjunta);
    }

    // ============================================================
    // 🟦 BENEFICIARIOS — LISTAR
    // ============================================================
    @GetMapping(value = "/{idCuenta}/beneficiarios", produces = "application/json")
    public List<BeneficiarioDTO> listarBeneficiarios(
            @PathVariable Integer idCuenta
    ) {
        return service.listarBeneficiarios(idCuenta);
    }

    // ============================================================
    // 🟦 BENEFICIARIOS — AGREGAR
    // ============================================================
    @PostMapping(
            value = "/{idCuenta}/beneficiarios",
            consumes = "application/json",
            produces = "application/json"
    )
    public CuentaAhorroGuardarRespuesta agregarBeneficiario(
            @PathVariable Integer idCuenta,
            @RequestBody BeneficiarioDTO dto
    ) {
        return service.agregarBeneficiario(idCuenta, dto);
    }

    // ============================================================
    // 🟦 BENEFICIARIOS — ACTUALIZAR
    // ============================================================
    @PutMapping(
            value = "/{idCuenta}/beneficiarios/{idBeneficiario}",
            consumes = "application/json",
            produces = "application/json"
    )
    public CuentaAhorroGuardarRespuesta actualizarBeneficiario(
            @PathVariable Integer idCuenta,
            @PathVariable Integer idBeneficiario,
            @RequestBody BeneficiarioDTO dto
    ) {
        return service.actualizarBeneficiario(
                idCuenta,
                idBeneficiario,
                dto
        );
    }

    // ============================================================
    // 🟦 BENEFICIARIOS — ELIMINAR
    // ============================================================
    @DeleteMapping(
            value = "/{idCuenta}/beneficiarios/{idBeneficiario}",
            produces = "application/json"
    )
    public CuentaAhorroGuardarRespuesta eliminarBeneficiario(
            @PathVariable Integer idCuenta,
            @PathVariable Integer idBeneficiario
    ) {
        return service.eliminarBeneficiario(idCuenta, idBeneficiario);
    }

    // ============================================================
    // 🟦 PODERES — LISTAR
    // ============================================================
    @GetMapping(value = "/{idCuenta}/poderes", produces = "application/json")
    public List<PoderDTO> listarPoderes(
            @PathVariable Integer idCuenta
    ) {
        return service.listarPoderes(idCuenta);
    }

    // ============================================================
    // 🟦 PODERES — AGREGAR
    // ============================================================
    @PostMapping(
            value = "/{idCuenta}/poderes",
            consumes = "application/json",
            produces = "application/json"
    )
    public CuentaAhorroGuardarRespuesta agregarPoder(
            @PathVariable Integer idCuenta,
            @RequestBody PoderDTO dto
    ) {
        return service.agregarPoder(idCuenta, dto);
    }

    // ============================================================
    // 🟦 PODERES — ACTUALIZAR
    // ============================================================
    @PutMapping(
            value = "/{idCuenta}/poderes/{idPoder}",
            consumes = "application/json",
            produces = "application/json"
    )
    public CuentaAhorroGuardarRespuesta actualizarPoder(
            @PathVariable Integer idCuenta,
            @PathVariable Integer idPoder,
            @RequestBody PoderDTO dto
    ) {
        return service.actualizarPoder(
                idCuenta,
                idPoder,
                dto
        );
    }

    // ============================================================
    // 🟦 PODERES — ELIMINAR
    // ============================================================
    @DeleteMapping(
            value = "/{idCuenta}/poderes/{idPoder}",
            produces = "application/json"
    )
    public CuentaAhorroGuardarRespuesta eliminarPoder(
            @PathVariable Integer idCuenta,
            @PathVariable Integer idPoder
    ) {
        return service.eliminarPoder(idCuenta, idPoder);
    }

    // ============================================================
    // 🟦 6. ELIMINAR CUENTA
    // ============================================================
    @DeleteMapping(value = "/{idCuenta}", produces = "application/json")
    public CuentaAhorroGuardarRespuesta eliminar(
            @PathVariable Integer idCuenta
    ) {
        return service.eliminar(idCuenta);
    }
}