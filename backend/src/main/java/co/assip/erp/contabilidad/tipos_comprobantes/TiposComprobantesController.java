package co.assip.erp.contabilidad.tipos_comprobantes;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;

import java.util.List;

@RestController
@RequestMapping("/contabilidad/tipos-comprobantes")
@RequiredArgsConstructor
public class TiposComprobantesController {

    private final TiposComprobantesService service;
    private final AccessValidator accessValidator;
    private final UsuarioSesionService usuarioSesionService;

    // =================================================
    // LISTAR POR AGENCIA (ACTIVOS + INACTIVOS)
    // =================================================
    @GetMapping
    public List<TipoComprobante> listar() {
        // CRUD ADMIN → todos (globales + todas las agencias)
        return service.listar(null, null);
    }

    // =================================================
    // OBTENER (EDITAR)
    // =================================================
    @GetMapping("/{tipo}/{idAgencia}")
    public TipoComprobante obtener(
            @PathVariable String tipo,
            @PathVariable Integer idAgencia
    ) {
        return service.obtener(tipo, idAgencia);
    }

    // =================================================
    // CREAR
    // =================================================
    @PostMapping
    public void crear(@RequestBody TipoComprobante t) {
        validarAdministracion();
        service.crear(t);
    }

    // =================================================
    // ACTUALIZAR
    // =================================================
    @PutMapping
    public void actualizar(@RequestBody TipoComprobante t) {
        validarAdministracion();
        service.actualizar(t);
    }

    // =========================================================
    // LISTAR PARA PROCESOS (FILTRADO POR AGENCIA)
    // =========================================================
    @GetMapping("/por-agencia")
    public List<TipoComprobante> listarPorAgencia(
            @RequestParam Integer idAgencia
    ) {
        return service.listar(idAgencia, true); // o null si quieres activos+inactivos
    }

    

    /** Exige el permiso funcional vigente para modificar la parametrización. */
    private void validarAdministracion() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(usuarioSesionService.idUsuario());
        accessValidator.validarAcceso(usuario, "CONTABILIDAD_PARAMETRIZACION_ADMIN");
    }
}
