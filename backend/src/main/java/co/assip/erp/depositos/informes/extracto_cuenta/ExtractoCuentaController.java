package co.assip.erp.depositos.informes.extracto_cuenta;

import co.assip.erp.depositos.informes.extracto_cuenta.dto.*;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/depositos/informes/extracto_cuenta")
@RequiredArgsConstructor
public class ExtractoCuentaController {

    private static final String PERMISO_VIEW =
            "DEPOSITOS_INFORMES_VIEW";

    private final ExtractoCuentaService service;
    private final ExtractoCuentaPdfService pdfService;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    @GetMapping("/buscar-cuentas")
    public List<ExtractoCuentaBusquedaDTO> buscarCuentas(
            @RequestParam(required = false) String documento,
            @RequestParam(required = false) String nombres,
            @RequestParam(required = false) String primerApellido,
            @RequestParam(required = false) String segundoApellido,
            @RequestParam(required = false) String codigoCuenta
    ) {

        validarAcceso();

        return service.buscarCuentas(
                documento,
                nombres,
                primerApellido,
                segundoApellido,
                codigoCuenta
        );
    }

    @PostMapping
    public ExtractoCuentaResponseDTO consultar(
            @RequestBody ExtractoCuentaRequestDTO request
    ) {

        validarAcceso();

        return service.consultar(request);
    }

    @PostMapping("/pdf")
    public ResponseEntity<byte[]> pdf(
            @RequestBody ExtractoCuentaRequestDTO request
    ) {

        validarAcceso();

        return pdfService.generarPdfResponse(
                service.consultar(request)
        );
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