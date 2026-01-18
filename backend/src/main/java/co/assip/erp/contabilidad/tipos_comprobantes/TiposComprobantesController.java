package co.assip.erp.contabilidad.tipos_comprobantes;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contabilidad/tipos-comprobantes")
@RequiredArgsConstructor
public class TiposComprobantesController {

    private final TiposComprobantesService service;

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
        service.crear(t);
    }

    // =================================================
    // ACTUALIZAR
    // =================================================
    @PutMapping
    public void actualizar(@RequestBody TipoComprobante t) {
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

    

}
