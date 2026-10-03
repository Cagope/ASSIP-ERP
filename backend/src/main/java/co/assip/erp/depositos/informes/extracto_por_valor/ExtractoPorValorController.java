package co.assip.erp.depositos.informes.extracto_por_valor;

import co.assip.erp.depositos.informes.extracto_por_valor.dto.ExtractoPorValorRequestDTO;
import co.assip.erp.depositos.informes.extracto_por_valor.dto.ExtractoPorValorResponseDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/depositos/informes/extracto-por-valor")
@RequiredArgsConstructor
public class ExtractoPorValorController {

    private static final String PERMISO_VIEW =
            "DEPOSITOS_INFORMES_VIEW";

    private final ExtractoPorValorService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    @PostMapping
    public ExtractoPorValorResponseDTO consultar(
            @RequestBody ExtractoPorValorRequestDTO request
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