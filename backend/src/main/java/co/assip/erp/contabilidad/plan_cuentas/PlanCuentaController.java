package co.assip.erp.contabilidad.plan_cuentas;

import org.springframework.web.bind.annotation.*;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;

import java.util.List;

@RestController
@RequestMapping("/contabilidad/plan-cuentas")
public class PlanCuentaController {

    private final PlanCuentaService service;
    private final AccessValidator accessValidator;
    private final UsuarioSesionService usuarioSesionService;

    public PlanCuentaController(PlanCuentaService service, AccessValidator accessValidator, UsuarioSesionService usuarioSesionService) {
        this.service = service;
        this.accessValidator = accessValidator;
        this.usuarioSesionService = usuarioSesionService;
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
        validarAdministracion();
        return service.guardar(dto);
    }

    // ==========================================================
    // ACTUALIZAR
    // PUT /contabilidad/plan-cuentas/{id}
    // ==========================================================
    @PutMapping("/{id}")
    public PlanCuenta actualizar(@PathVariable Integer id, @RequestBody PlanCuenta dto) {
        dto.setId(id); // nos aseguramos que use el id de la URL
        validarAdministracion();
        return service.guardar(dto);
    }

    // ==========================================================
    // ELIMINAR
    // DELETE /contabilidad/plan-cuentas/{id}
    // ==========================================================
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Integer id) {
        validarAdministracion();
        service.eliminar(id);
    }

    // ==========================================================
// BUSCAR CUENTAS (AUTOCOMPLETE)
// GET /contabilidad/plan-cuentas/buscar
// ==========================================================
    @GetMapping("/buscar")
    public List<PlanCuenta> buscar(
            @RequestParam Integer idAgencia,
            @RequestParam String q
    ) {
        if (q == null || q.trim().length() < 2) {
            return List.of();
        }

        return service.buscarPorAgenciaYTexto(idAgencia, q.trim());
    }
    /** Exige el permiso funcional vigente para modificar la parametrización. */
    private void validarAdministracion() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(usuarioSesionService.idUsuario());
        accessValidator.validarAcceso(usuario, "CONTABILIDAD_PARAMETRIZACION_ADMIN");
    }
}
