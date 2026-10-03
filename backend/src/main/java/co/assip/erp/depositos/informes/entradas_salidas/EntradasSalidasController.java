package co.assip.erp.depositos.informes.entradas_salidas;

import co.assip.erp.depositos.informes.entradas_salidas.dto.EntradasSalidasRequestDTO;
import co.assip.erp.depositos.informes.entradas_salidas.dto.EntradasSalidasResponseDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/depositos/informes/entradas-salidas")
@RequiredArgsConstructor
public class EntradasSalidasController {

    private static final String PERMISO_VIEW =
            "DEPOSITOS_INFORMES_VIEW";

    private final EntradasSalidasService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    @PostMapping
    public EntradasSalidasResponseDTO consultar(
            @RequestBody EntradasSalidasRequestDTO request
    ) {

        validarAcceso();

        return service.consultar(request);
    }

    @PostMapping("/resumen-forma")
    public EntradasSalidasResponseDTO resumenPorForma(
            @RequestBody EntradasSalidasRequestDTO request
    ) {

        validarAcceso();

        return service.resumenPorForma(request);
    }

    @PostMapping("/detalle")
    public EntradasSalidasResponseDTO detalleMovimientos(
            @RequestBody EntradasSalidasRequestDTO request
    ) {

        validarAcceso();

        return service.detalleMovimientos(request);
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