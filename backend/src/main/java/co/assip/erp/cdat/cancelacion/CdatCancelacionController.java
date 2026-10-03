package co.assip.erp.cdat.cancelacion;

import co.assip.erp.cdat.cancelacion.dto.CdatCancelacionEntradaDTO;
import co.assip.erp.cdat.cancelacion.dto.CdatCancelacionFiltroDTO;
import co.assip.erp.cdat.cancelacion.dto.CdatCancelacionItemDTO;
import co.assip.erp.cdat.cancelacion.dto.CdatCancelacionPreviewDTO;
import co.assip.erp.contabilidad.consecutivos_comprobantes.ConsecutivosComprobantesService;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cdat/cancelacion")
@RequiredArgsConstructor
public class CdatCancelacionController {

    private static final String PERMISO =
            "CDAT_CANCELACION_EXECUTE";

    private final CdatCancelacionService service;
    private final ConsecutivosComprobantesService consecutivosComprobantesService;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    @GetMapping("/{idCuentaCdat}")
    public CdatCancelacionItemDTO obtenerPorId(
            @PathVariable Long idCuentaCdat
    ) {

        validarAcceso();

        return service.obtenerPorId(idCuentaCdat);
    }

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

    @GetMapping("/cuentas-ahorro")
    public List<Map<String, Object>> buscarCuentasAhorroDisponibles(
            @RequestParam Integer idDatosPersonal
    ) {
        validarAcceso();

        Integer idAgencia =
                usuarioSesionService.agenciaPrincipal();

        return service.buscarCuentasAhorroDisponibles(
                idDatosPersonal,
                idAgencia
        );
    }

    @PostMapping("/preview")
    public CdatCancelacionPreviewDTO preview(
            @RequestBody CdatCancelacionEntradaDTO input
    ) {

        validarAcceso();

        return service.preview(input);
    }

    @PostMapping("/aplicar")
    public void aplicar(
            @RequestBody CdatCancelacionEntradaDTO input
    ) {

        validarAcceso();

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        service.aplicar(
                input,
                idUsuario
        );
    }

    @PostMapping("/buscar")
    public List<CdatCancelacionItemDTO> buscar(
            @RequestBody CdatCancelacionFiltroDTO filtro
    ) {

        validarAcceso();

        return service.buscar(filtro);
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