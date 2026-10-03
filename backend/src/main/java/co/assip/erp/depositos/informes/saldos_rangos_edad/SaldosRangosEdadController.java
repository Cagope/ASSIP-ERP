package co.assip.erp.depositos.informes.saldos_rangos_edad;

import co.assip.erp.depositos.informes.saldos_rangos_edad.dto.SaldosRangosEdadRequestDTO;
import co.assip.erp.depositos.informes.saldos_rangos_edad.dto.SaldosRangosEdadResponseDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/depositos/informes/saldos-rangos-edad")
@RequiredArgsConstructor
public class SaldosRangosEdadController {

    private static final String PERMISO_VIEW =
            "DEPOSITOS_INFORMES_VIEW";

    private final SaldosRangosEdadService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    @PostMapping
    public SaldosRangosEdadResponseDTO consultar(
            @RequestBody SaldosRangosEdadRequestDTO request
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