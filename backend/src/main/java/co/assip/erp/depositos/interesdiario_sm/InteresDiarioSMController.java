package co.assip.erp.depositos.interesdiario_sm;

import co.assip.erp.contabilidad.consecutivos_comprobantes.ConsecutivosComprobantesService;
import co.assip.erp.depositos.interesdiario_sm.dto.InteresDiarioSMEntradaDTO;
import co.assip.erp.depositos.interesdiario_sm.dto.InteresDiarioSMItemDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/depositos/interes-diario-sm")
@RequiredArgsConstructor
public class InteresDiarioSMController {

    private static final String PERMISO_EXECUTE =
            "DEPOSITOS_INTERESES_EXECUTE";

    private final InteresDiarioSMService service;
    private final ConsecutivosComprobantesService consecutivosComprobantesService;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    @PostMapping("/liquidar")
    public List<InteresDiarioSMItemDTO> liquidar(
            @RequestBody InteresDiarioSMEntradaDTO input
    ) {

        validarAcceso();

        return service.liquidar(input);
    }

    @PostMapping("/aplicar")
    public void aplicar(
            @RequestBody InteresDiarioSMEntradaDTO input
    ) {

        Integer idUsuario = usuarioSesionService.idUsuario();

        validarAcceso(idUsuario);

        service.aplicar(input, idUsuario);
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

    @GetMapping("/ultima-liquidacion/{idAgencia}/{idFormaAhorro}")
    public Map<String, Object> obtenerUltimaLiquidacion(
            @PathVariable Integer idAgencia,
            @PathVariable Integer idFormaAhorro
    ) {

        validarAcceso();

        return service.obtenerUltimaLiquidacion(
                idAgencia,
                idFormaAhorro
        );
    }

    private void validarAcceso() {

        validarAcceso(usuarioSesionService.idUsuario());
    }

    private void validarAcceso(Integer idUsuario) {

        Usuario usuarioActual = new Usuario();
        usuarioActual.setIdUsuario(idUsuario);

        accessValidator.validarAcceso(
                usuarioActual,
                PERMISO_EXECUTE
        );
    }
}