package co.assip.erp.contabilidad.plan_cuentas;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contabilidad/plan-cuentas")
public class PlanCuentaController {

    private final PlanCuentaService service;

    public PlanCuentaController(PlanCuentaService service) {
        this.service = service;
    }

    // ==========================================================
    // LISTAR POR AGENCIA
    // GET /contabilidad/plan-cuentas/agencia/{idAgencia}
    // ==========================================================
    @GetMapping("/agencia/{idAgencia}")
    public List<PlanCuenta> listarPorAgencia(@PathVariable Integer idAgencia) {
        return service.listarPorAgencia(idAgencia);
    }

    // ==========================================================
    // OBTENER DETALLE POR ID
    // GET /contabilidad/plan-cuentas/{id}
    // ==========================================================
    @GetMapping("/{id}")
    public PlanCuenta obtener(@PathVariable Integer id) {
        return service.obtener(id);
    }

    // ==========================================================
    // CREAR
    // POST /contabilidad/plan-cuentas
    // ==========================================================
    @PostMapping
    public PlanCuenta crear(@RequestBody PlanCuenta dto) {
        return service.guardar(dto);
    }

    // ==========================================================
    // ACTUALIZAR
    // PUT /contabilidad/plan-cuentas/{id}
    // ==========================================================
    @PutMapping("/{id}")
    public PlanCuenta actualizar(@PathVariable Integer id, @RequestBody PlanCuenta dto) {
        dto.setId(id); // nos aseguramos que use el id de la URL
        return service.guardar(dto);
    }

    // ==========================================================
    // ELIMINAR
    // DELETE /contabilidad/plan-cuentas/{id}
    // ==========================================================
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Integer id) {
        service.eliminar(id);
    }
}
