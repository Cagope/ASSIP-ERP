package co.assip.erp.cdat.causacion_mensual_cdat;

import co.assip.erp.cdat.causacion_mensual_cdat.dto.CdatCausacionMensualEntradaDTO;
import co.assip.erp.cdat.causacion_mensual_cdat.dto.CdatCausacionMensualPreviewDTO;
import co.assip.erp.contabilidad.consecutivos_comprobantes.ConsecutivosComprobantesService;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cdat/causacion-mensual-cdat")
@RequiredArgsConstructor
public class CdatCausacionMensualController {

    private static final String PERMISO =
            "CDAT_CAUSACION_MENSUAL_EXECUTE";

    private final CdatCausacionMensualService service;
    private final ConsecutivosComprobantesService consecutivosComprobantesService;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    @GetMapping("/proximo-comprobante/{idAgencia}/{tipoComprobante}")
    public Map<String, String> obtenerProximoComprobante(
            @PathVariable Integer idAgencia,
            @PathVariable String tipoComprobante
    ) {

        validarAcceso();

        String numero =
                consecutivosComprobantesService.obtenerNumeroSugerido(
                        tipoComprobante,
                        idAgencia
                );

        return Map.of(
                "numeroComprobante",
                numero
        );
    }

    @PostMapping("/preview")
    public CdatCausacionMensualPreviewDTO preview(
            @RequestBody CdatCausacionMensualEntradaDTO dto
    ) {

        validarAcceso();

        return service.preview(dto);
    }

    @PostMapping("/aplicar")
    public CdatCausacionMensualPreviewDTO aplicar(
            @RequestBody CdatCausacionMensualEntradaDTO dto
    ) {

        validarAcceso();

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        return service.aplicar(
                dto,
                idUsuario
        );
    }

    @GetMapping("/listar")
    public List<CdatCausacionMensualPreviewDTO> listar() {

        validarAcceso();

        return service.listar();
    }

    @GetMapping("/{idCausacion}")
    public CdatCausacionMensualPreviewDTO obtenerPorId(
            @PathVariable Long idCausacion
    ) {

        validarAcceso();

        return service.obtenerPorId(idCausacion);
    }

    private void validarAcceso() {

        Usuario usuarioActual = new Usuario();
        usuarioActual.setIdUsuario(
                usuarioSesionService.idUsuario()
        );

        accessValidator.validarAcceso(
                usuarioActual,
                PERMISO
        );
    }
}