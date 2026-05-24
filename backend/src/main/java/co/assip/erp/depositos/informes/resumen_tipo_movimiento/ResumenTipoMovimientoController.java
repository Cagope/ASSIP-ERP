package co.assip.erp.depositos.informes.resumen_tipo_movimiento;

import co.assip.erp.depositos.informes.resumen_tipo_movimiento.dto.ResumenTipoMovimientoRequestDTO;
import co.assip.erp.depositos.informes.resumen_tipo_movimiento.dto.ResumenTipoMovimientoResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/depositos/informes/resumen-tipo-movimiento")
@RequiredArgsConstructor
public class ResumenTipoMovimientoController {

    private final ResumenTipoMovimientoService service;

    @PostMapping
    public ResumenTipoMovimientoResponseDTO consultar(
            @RequestBody ResumenTipoMovimientoRequestDTO request
    ) {
        return service.consultar(request);
    }

    @PostMapping("/detalle")
    public ResumenTipoMovimientoResponseDTO detalle(
            @RequestBody ResumenTipoMovimientoRequestDTO request
    ) {
        return service.detalle(request);
    }

    @GetMapping("/tipos-movimiento")
    public List<Map<String, Object>> listarTiposMovimiento() {
        return service.listarTiposMovimiento();
    }

}