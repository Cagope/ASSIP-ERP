package co.assip.erp.depositos.interesmensual_tac;

import co.assip.erp.contabilidad.consecutivos_comprobantes.ConsecutivosComprobantesService;
import co.assip.erp.depositos.interesmensual_tac.dto.InteresMensualTACEntradaDTO;
import co.assip.erp.depositos.interesmensual_tac.dto.InteresMensualTACItemDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/depositos/interes-mensual-tac")
@RequiredArgsConstructor
public class InteresMensualTACController {

    private static final String PERMISO_EXECUTE =
            "DEPOSITOS_INTERESES_EXECUTE";

    private final InteresMensualTACService service;
    private final ConsecutivosComprobantesService consecutivosComprobantesService;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    @PostMapping("/liquidar")
    public List<InteresMensualTACItemDTO> liquidar(
            @RequestBody InteresMensualTACEntradaDTO input
    ) {

        validarAcceso();

        return service.liquidar(input);
    }

    @PostMapping("/aplicar")
    public void aplicar(
            @RequestBody InteresMensualTACEntradaDTO input
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