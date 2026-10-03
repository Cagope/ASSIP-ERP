package co.assip.erp.cdat.liquidacion_diaria;

import co.assip.erp.cdat.liquidacion_diaria.dto.CdatLiquidacionDiariaEntradaDTO;
import co.assip.erp.cdat.liquidacion_diaria.dto.CdatLiquidacionDiariaPreviewDTO;
import co.assip.erp.contabilidad.consecutivos_comprobantes.ConsecutivosComprobantesService;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/cdat/liquidacion-diaria")
@RequiredArgsConstructor
public class CdatLiquidacionDiariaController {

    private static final String PERMISO =
            "CDAT_LIQUIDACION_DIARIA_EXECUTE";

    private final CdatLiquidacionDiariaService service;
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
    public CdatLiquidacionDiariaPreviewDTO preview(
            @RequestBody CdatLiquidacionDiariaEntradaDTO input
    ) {

        validarAcceso();

        return service.preview(input);
    }

    @PostMapping("/aplicar")
    public void aplicar(
            @RequestBody CdatLiquidacionDiariaEntradaDTO input
    ) {

        validarAcceso();

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        service.aplicar(
                input,
                idUsuario
        );
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