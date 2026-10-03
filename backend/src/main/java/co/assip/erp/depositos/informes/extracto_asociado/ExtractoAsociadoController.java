package co.assip.erp.depositos.informes.extracto_asociado;

import co.assip.erp.depositos.informes.extracto_asociado.dto.*;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/depositos/informes/extracto_asociado")
@RequiredArgsConstructor
public class ExtractoAsociadoController {

    private static final String PERMISO_VIEW =
            "DEPOSITOS_INFORMES_VIEW";

    private final ExtractoAsociadoService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    @GetMapping("/buscar-asociados")
    public List<ExtractoAsociadoBusquedaDTO> buscarAsociados(
            @RequestParam(required = false) String documento,
            @RequestParam(required = false) String nombres,
            @RequestParam(required = false) String primerApellido,
            @RequestParam(required = false) String segundoApellido,
            @RequestParam(required = false) String codigoCuenta
    ) {

        validarAcceso();

        return service.buscarAsociados(
                documento,
                nombres,
                primerApellido,
                segundoApellido,
                codigoCuenta
        );
    }

    @PostMapping
    public ExtractoAsociadoResponseDTO consultar(
            @RequestBody ExtractoAsociadoRequestDTO request
    ) {

        validarAcceso();

        return service.consultar(request);
    }

    private void validarAcceso() {

        Usuario usuarioActual = new Usuario();
        usuarioActual.setIdUsuario(
                usuarioSesionService.idUsuario()
        );

        accessValidator.validarAcceso(
                usuarioActual,
                PERMISO_VIEW
        );
    }
}