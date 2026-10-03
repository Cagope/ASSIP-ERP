package co.assip.erp.cdat.catalogos;

import co.assip.erp.cdat.catalogos.dto.CdatAmortizacionDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cdat/catalogos")
@RequiredArgsConstructor
public class CdatCatalogosController {

    private static final String PERMISO =
            "CDAT_CUENTAS_VIEW";

    private final CdatCatalogosService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    @GetMapping("/amortizaciones")
    public List<CdatAmortizacionDTO> listarAmortizaciones() {

        validarAcceso();

        return service.listarAmortizaciones();
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