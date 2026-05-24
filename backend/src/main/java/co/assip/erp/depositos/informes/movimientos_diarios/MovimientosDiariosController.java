package co.assip.erp.depositos.informes.movimientos_diarios;

import co.assip.erp.depositos.informes.movimientos_diarios.dto.MovimientosDiariosRequestDTO;
import co.assip.erp.depositos.informes.movimientos_diarios.dto.MovimientosDiariosResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/depositos/informes/movimientos-diarios")
@RequiredArgsConstructor
public class MovimientosDiariosController {

    private final MovimientosDiariosService service;

    @PostMapping
    public MovimientosDiariosResponseDTO consultar(
            @RequestBody MovimientosDiariosRequestDTO request
    ) {
        return service.consultar(request);
    }

    @GetMapping("/tipos-movimiento")
    public java.util.List<java.util.Map<String, Object>> listarTiposMovimiento() {

        String sql = """
        SELECT
            TRIM(codigo_movimiento) AS codigo_movimiento,
            descripcion
        FROM depositos.tipo_movimiento
        ORDER BY codigo_movimiento
        """;

        return service.listarTiposMovimiento(sql);
    }

}