package co.assip.erp.cdat.informes.simulador_cdat;

import co.assip.erp.cdat.informes.simulador_cdat.dto.CdatSimuladorEntradaDTO;
import co.assip.erp.cdat.informes.simulador_cdat.dto.CdatSimuladorPreviewDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cdat/informes/simulador-cdat")
@RequiredArgsConstructor
public class CdatSimuladorController {

    private static final String PERMISO =
            "CDAT_INFORMES_VIEW";

    private final CdatSimuladorService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    @GetMapping("/cuentas-ahorro")
    public List<Map<String, Object>> buscarCuentasAhorro(
            @RequestParam(required = false) String documento,
            @RequestParam(required = false) String nombres,
            @RequestParam(required = false) String primerApellido,
            @RequestParam(required = false) String segundoApellido
    ) {

        validarAcceso();

        Integer idAgencia =
                usuarioSesionService.agenciaPrincipal();

        return service.buscarCuentasAhorro(
                documento,
                nombres,
                primerApellido,
                segundoApellido,
                idAgencia
        );
    }

    @PostMapping("/preview")
    public CdatSimuladorPreviewDTO preview(
            @RequestBody CdatSimuladorEntradaDTO input
    ) {

        validarAcceso();

        Integer idAgencia =
                usuarioSesionService.agenciaPrincipal();

        return service.generarPreview(
                input,
                idAgencia
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