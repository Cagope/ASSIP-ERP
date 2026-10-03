package co.assip.erp.depositos.informes.saldos_menores;

import co.assip.erp.depositos.informes.saldos_menores.dto.SaldosMenoresRequestDTO;
import co.assip.erp.depositos.informes.saldos_menores.dto.SaldosMenoresResponseDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/depositos/informes/saldos-menores")
@RequiredArgsConstructor
public class SaldosMenoresController {

    private static final String PERMISO_VIEW =
            "DEPOSITOS_INFORMES_VIEW";

    private final SaldosMenoresService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    @PostMapping
    public SaldosMenoresResponseDTO consultar(
            @RequestBody SaldosMenoresRequestDTO request
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