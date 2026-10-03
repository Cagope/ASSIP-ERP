package co.assip.erp.cdat.cdats;

import co.assip.erp.cdat.cdats.dto.CdatAperturaPreviewDTO;
import co.assip.erp.cdat.cdats.dto.CdatAsociadoValidacionDTO;
import co.assip.erp.cdat.cdats.dto.CdatFormDTO;
import co.assip.erp.cdat.cdats.dto.CdatListDTO;
import co.assip.erp.cdat.cdats.dto.CdatSaveDTO;
import co.assip.erp.cdat.cdats.dto.CdatSaveResponseDTO;
import co.assip.erp.contabilidad.consecutivos_comprobantes.ConsecutivosComprobantesService;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cdat/cdats")
@RequiredArgsConstructor
public class CdatController {

    private static final String PERMISO_VIEW =
            "CDAT_CUENTAS_VIEW";

    private static final String PERMISO_APERTURA =
            "CDAT_APERTURA_CREATE";

    private final CdatService service;
    private final CdatAperturaContabilizacionService contabilizacionService;
    private final ConsecutivosComprobantesService consecutivosComprobantesService;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    @GetMapping
    public List<CdatListDTO> listar() {

        validarAcceso(PERMISO_VIEW);

        return service.listar();
    }

    @GetMapping("/validar-asociado/{documento}")
    public CdatAsociadoValidacionDTO validarAsociado(
            @PathVariable String documento
    ) {

        validarAcceso(PERMISO_APERTURA);

        return service.validarAsociado(documento);
    }

    @GetMapping("/proximo-codigo")
    public String obtenerProximoCodigo() {

        validarAcceso(PERMISO_APERTURA);

        Integer idAgencia =
                usuarioSesionService.agenciaPrincipal();

        return service.obtenerProximoCodigo(idAgencia);
    }

    @GetMapping("/buscar-cotitular/{documento}")
    public CdatAsociadoValidacionDTO buscarCotitular(
            @PathVariable String documento
    ) {

        validarAcceso(PERMISO_APERTURA);

        return service.buscarCotitular(documento);
    }

    @GetMapping("/cuentas-ahorro")
    public List<Map<String, Object>> buscarCuentasAhorro(
            @RequestParam(required = false) String documento,
            @RequestParam(required = false) String nombres,
            @RequestParam(required = false) String primerApellido,
            @RequestParam(required = false) String segundoApellido,
            @RequestParam(required = false) String codigoCuenta
    ) {

        validarAcceso(PERMISO_APERTURA);

        Integer idAgencia =
                usuarioSesionService.agenciaPrincipal();

        return service.buscarCuentasAhorro(
                documento,
                nombres,
                primerApellido,
                segundoApellido,
                codigoCuenta,
                idAgencia
        );
    }

    @GetMapping("/{idCuentaCdat}")
    public CdatFormDTO obtenerPorId(
            @PathVariable Long idCuentaCdat
    ) {

        validarAcceso(PERMISO_VIEW);

        return service.obtenerPorId(idCuentaCdat);
    }

    @GetMapping("/proximo-comprobante/{idAgencia}/{tipoComprobante}")
    public Map<String, String> obtenerProximoComprobante(
            @PathVariable Integer idAgencia,
            @PathVariable String tipoComprobante
    ) {

        validarAcceso(PERMISO_APERTURA);

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

    @PostMapping("/apertura/preview-contable")
    public CdatAperturaPreviewDTO previewContable(
            @RequestBody CdatSaveDTO dto
    ) {

        validarAcceso(PERMISO_APERTURA);

        return contabilizacionService.previewApertura(dto);
    }

    @PostMapping
    public CdatSaveResponseDTO guardar(
            @RequestBody CdatSaveDTO dto
    ) {

        validarAcceso(PERMISO_APERTURA);

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        Integer idAgencia =
                usuarioSesionService.agenciaPrincipal();

        return service.guardar(
                dto,
                idUsuario,
                idAgencia
        );
    }

    private void validarAcceso(String permiso) {

        Usuario usuarioActual = new Usuario();
        usuarioActual.setIdUsuario(
                usuarioSesionService.idUsuario()
        );

        accessValidator.validarAcceso(
                usuarioActual,
                permiso
        );
    }
}