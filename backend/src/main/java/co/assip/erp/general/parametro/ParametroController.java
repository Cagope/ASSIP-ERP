package co.assip.erp.general.parametro;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 🌐 Controlador REST — Parametros
 * ------------------------------------------------------------
 * Endpoint base: /general/parametros
 * Gestiona la administración de parámetros globales y por agencia.
 */
@RestController
@RequestMapping("/general/parametros")
public class ParametroController {

    private final ParametroService service;

    public ParametroController(ParametroService service) {
        this.service = service;
    }

    /** 🔹 Listar todos los parámetros */
    @GetMapping
    public List<Parametro> listar() {
        return service.listar();
    }

    /** 🔹 Obtener por ID */
    @GetMapping("/{id}")
    public Parametro obtenerPorId(@PathVariable Integer id) {
        return service.obtenerPorId(id);
    }

    /** 🔹 Crear nuevo parámetro */
    @PostMapping
    public Parametro crear(@RequestBody Parametro parametro) {
        return service.guardar(parametro);
    }

    /** 🔹 Actualizar existente */
    @PutMapping("/{id}")
    public Parametro actualizar(@PathVariable Integer id, @RequestBody Parametro parametro) {
        return service.actualizar(id, parametro);
    }

    /** 🔹 Eliminar */
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Integer id) {
        service.eliminar(id);
    }

    // =========================================================
    // 🔸 NUEVO ENDPOINT PERSONALIZADO — CON VALOR POR DEFECTO
    // =========================================================
    /**
     * 🔍 Buscar un parámetro por agencia y código.
     * Si no existe, devuelve un objeto con valorParametro = 0.
     * Ejemplo: GET /general/parametros/buscar/1/103
     */
    @GetMapping("/buscar/{idAgencia}/{codigo}")
    public ResponseEntity<Parametro> obtenerPorAgenciaYCodigo(
            @PathVariable Integer idAgencia,
            @PathVariable Integer codigo) {

        return service.obtenerPorAgenciaYCodigo(idAgencia, codigo)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    Parametro vacio = new Parametro();
                    vacio.setIdAgencia(idAgencia);
                    vacio.setCodigoParametro(codigo);
                    vacio.setNombreParametro("NO DEFINIDO");
                    vacio.setValorParametro(BigDecimal.ZERO); // ✅ tipo correcto
                    vacio.setTipoValor(false);
                    return ResponseEntity.ok(vacio);
                });
    }
}
