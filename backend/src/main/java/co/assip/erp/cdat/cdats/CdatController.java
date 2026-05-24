package co.assip.erp.cdat.cdats;
import co.assip.erp.seguridad.service.UsuarioSesionService;
import co.assip.erp.cdat.cdats.dto.CdatAperturaPreviewDTO;
import co.assip.erp.cdat.cdats.dto.CdatAsociadoValidacionDTO;
import co.assip.erp.cdat.cdats.dto.CdatFormDTO;
import co.assip.erp.cdat.cdats.dto.CdatListDTO;
import co.assip.erp.cdat.cdats.dto.CdatSaveDTO;
import co.assip.erp.cdat.cdats.dto.CdatSaveResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import co.assip.erp.contabilidad.consecutivos_comprobantes.ConsecutivosComprobantesService;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cdat/cdats")
@RequiredArgsConstructor
public class CdatController {

    private final CdatService service;
    private final CdatAperturaContabilizacionService contabilizacionService;
    private final ConsecutivosComprobantesService consecutivosComprobantesService;
    private final UsuarioSesionService usuarioSesionService;

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
        Integer idAgencia =
                usuarioSesionService.agenciaPrincipal();
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

    @GetMapping("/proximo-comprobante/{idAgencia}/{tipoComprobante}")
    public Map<String, String> obtenerProximoComprobante(
            @PathVariable Integer idAgencia,
            @PathVariable String tipoComprobante
    ) {

        String numero = consecutivosComprobantesService.obtenerNumeroSugerido(
                tipoComprobante,
                idAgencia
        );

        return Map.of(
                "numeroComprobante",
                numero
        );
    }

    @PostMapping("/apertura/preview-contable")
    public CdatAperturaPreviewDTO previewContable(@RequestBody CdatSaveDTO dto) {
        return contabilizacionService.previewApertura(dto);
    }

    @PostMapping
    public CdatSaveResponseDTO guardar(@RequestBody CdatSaveDTO dto) {
        Integer idUsuario =
                usuarioSesionService.idUsuario();

        Integer idAgencia =
                usuarioSesionService.agenciaPrincipal();
        return service.guardar(dto, idUsuario, idAgencia);
    }
}