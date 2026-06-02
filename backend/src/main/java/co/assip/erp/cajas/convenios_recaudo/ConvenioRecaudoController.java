package co.assip.erp.cajas.convenios_recaudo;

import co.assip.erp.cajas.convenios_recaudo.dto.ConvenioRecaudoCuentaDTO;
import co.assip.erp.cajas.convenios_recaudo.dto.ConvenioRecaudoDTO;
import co.assip.erp.cajas.convenios_recaudo.dto.ConvenioRecaudoRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import co.assip.erp.cajas.convenios_recaudo.dto.ConvenioRecaudoBusquedaDTO;

@RestController
@RequestMapping("/cajas/convenios_recaudo")
@RequiredArgsConstructor
public class ConvenioRecaudoController {

    private final ConvenioRecaudoService service;

    @GetMapping
    public List<ConvenioRecaudoDTO> listar() {
        return service.listar();
    }

    @GetMapping("/{idConvenio}")
    public ConvenioRecaudoDTO obtener(
            @PathVariable Long idConvenio
    ) {
        return service.obtener(idConvenio);
    }

    @GetMapping("/buscar-cuentas")
    public ConvenioRecaudoBusquedaDTO buscarCuentasPorDocumento(
            @RequestParam Integer idAgencia,
            @RequestParam String documento
    ) {
        return service.buscarCuentasPorDocumento(
                idAgencia,
                documento
        );
    }

    @PostMapping
    public ConvenioRecaudoDTO crear(
            @RequestBody ConvenioRecaudoRequestDTO request
    ) {
        return service.crear(request);
    }

    @PutMapping("/{idConvenio}")
    public ConvenioRecaudoDTO actualizar(
            @PathVariable Long idConvenio,
            @RequestBody ConvenioRecaudoRequestDTO request
    ) {
        return service.actualizar(
                idConvenio,
                request
        );
    }

    @DeleteMapping("/{idConvenio}")
    public void eliminar(
            @PathVariable Long idConvenio
    ) {
        service.eliminar(idConvenio);
    }
}