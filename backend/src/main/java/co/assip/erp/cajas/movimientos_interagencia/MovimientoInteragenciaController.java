package co.assip.erp.cajas.movimientos_interagencia;

import co.assip.erp.cajas.movimientos_interagencia.dto.MovimientoInteragenciaCuentaDTO;
import co.assip.erp.cajas.movimientos_interagencia.dto.MovimientoInteragenciaPreviewDTO;
import co.assip.erp.cajas.movimientos_interagencia.dto.MovimientoInteragenciaRequestDTO;
import co.assip.erp.cajas.movimientos_interagencia.dto.MovimientoInteragenciaResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cajas/movimientos_interagencia")
@RequiredArgsConstructor
public class MovimientoInteragenciaController {

    private final MovimientoInteragenciaService service;

    @GetMapping("/buscar-cuentas")
    public List<MovimientoInteragenciaCuentaDTO> buscarCuentas(

            @RequestParam Integer idAgenciaCaja,

            @RequestParam(required = false) String documento,

            @RequestParam(required = false) String nombres,

            @RequestParam(required = false) String primerApellido,

            @RequestParam(required = false) String segundoApellido
    ) {

        return service.buscarCuentas(
                idAgenciaCaja,
                documento,
                nombres,
                primerApellido,
                segundoApellido
        );
    }

    @PostMapping("/preview")
    public MovimientoInteragenciaPreviewDTO preview(
            @RequestBody MovimientoInteragenciaRequestDTO request
    ) {
        return service.preview(request);
    }

    @PostMapping("/aplicar")
    public MovimientoInteragenciaResponseDTO aplicar(
            @RequestBody MovimientoInteragenciaRequestDTO request
    ) {
        return service.aplicar(request);
    }
}