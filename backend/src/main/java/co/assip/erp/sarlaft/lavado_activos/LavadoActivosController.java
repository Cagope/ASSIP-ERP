package co.assip.erp.sarlaft.lavado_activos;

import co.assip.erp.sarlaft.lavado_activos.dto.LavadoActivosPreviewDTO;
import co.assip.erp.sarlaft.lavado_activos.dto.LavadoActivosRequestDTO;
import co.assip.erp.sarlaft.lavado_activos.dto.LavadoActivosResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import co.assip.erp.sarlaft.lavado_activos.dto.LavadoActivosPersonaDTO;

import co.assip.erp.sarlaft.lavado_activos.dto.LavadoActivosFormatoDTO;

@RestController
@RequestMapping("/sarlaft/lavado-activos")
@RequiredArgsConstructor
public class LavadoActivosController {

    private final LavadoActivosService service;

    @GetMapping("/preview")
    public LavadoActivosPreviewDTO preview(
            @RequestParam Integer idAgencia,
            @RequestParam BigDecimal valorTransaccion
    ) {
        return service.preview(
                idAgencia,
                valorTransaccion
        );
    }

    @PostMapping("/generar")
    public LavadoActivosResponseDTO generar(
            @RequestBody LavadoActivosRequestDTO request
    ) {
        return service.generarSiAplica(
                request
        );
    }

    @PutMapping("/{id}/impreso")
    public void marcarImpreso(
            @PathVariable Long id
    ) {
        service.marcarImpreso(id);
    }

    @PutMapping("/{id}/firmado")
    public void marcarFirmado(
            @PathVariable Long id,
            @RequestParam(required = false) String observacion
    ) {
        service.marcarFirmado(
                id,
                observacion
        );
    }

    @GetMapping("/persona")
    public LavadoActivosPersonaDTO buscarPersona(
            @RequestParam String documento
    ) {
        return service.buscarPersonaPorDocumento(documento);
    }

    @GetMapping("/{id}")
    public LavadoActivosFormatoDTO obtenerPorId(
            @PathVariable Long id
    ) {
        return service.obtenerPorId(id);
    }
}