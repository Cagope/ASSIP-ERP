package co.assip.erp.cdat.cdats;

import co.assip.erp.cdat.cdats.contabilizacion.CdatAperturaContabilizacionService;
import co.assip.erp.cdat.cdats.contabilizacion.dto.CdatAperturaPreviewDTO;
import co.assip.erp.cdat.cdats.dto.CdatAsociadoValidacionDTO;
import co.assip.erp.cdat.cdats.dto.CdatFormDTO;
import co.assip.erp.cdat.cdats.dto.CdatListDTO;
import co.assip.erp.cdat.cdats.dto.CdatSaveDTO;
import co.assip.erp.cdat.cdats.dto.CdatSaveResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cdat/cdats")
@RequiredArgsConstructor
public class CdatController {

    private final CdatService service;
    private final CdatAperturaContabilizacionService contabilizacionService;

    @GetMapping
    public List<CdatListDTO> listar() {
        return service.listar();
    }

    @GetMapping("/validar-asociado/{documento}")
    public CdatAsociadoValidacionDTO validarAsociado(@PathVariable String documento) {
        return service.validarAsociado(documento);
    }

    @GetMapping("/proximo-codigo")
    public String obtenerProximoCodigo() {
        Integer idAgencia = 2;
        return service.obtenerProximoCodigo(idAgencia);
    }

    @GetMapping("/buscar-cotitular/{documento}")
    public CdatAsociadoValidacionDTO buscarCotitular(@PathVariable String documento) {
        return service.buscarCotitular(documento);
    }

    @GetMapping("/{idCuentaCdat}")
    public CdatFormDTO obtenerPorId(@PathVariable Long idCuentaCdat) {
        return service.obtenerPorId(idCuentaCdat);
    }

    @PostMapping("/apertura/preview-contable")
    public CdatAperturaPreviewDTO previewContable(@RequestBody CdatSaveDTO dto) {
        return contabilizacionService.previewApertura(dto);
    }

    @PostMapping
    public CdatSaveResponseDTO guardar(@RequestBody CdatSaveDTO dto) {
        Integer idUsuario = 1;
        Integer idAgencia = 2;
        return service.guardar(dto, idUsuario, idAgencia);
    }
}