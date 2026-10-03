package co.assip.erp.cdat.informes.consulta_cdats;

import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cdat/informes/consulta-cdats")
@RequiredArgsConstructor
public class ConsultaCdatsController {

    private static final String PERMISO =
            "CDAT_INFORMES_VIEW";

    private final ConsultaCdatsService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    @GetMapping
    public List<Map<String, Object>> consultarCdats(
            @RequestParam(required = false) String documento,
            @RequestParam(required = false) String nombres,
            @RequestParam(required = false) String primerApellido,
            @RequestParam(required = false) String segundoApellido,
            @RequestParam(required = false) String codigoCdat
    ) {

        validarAcceso();

        return service.consultarCdats(
                documento,
                nombres,
                primerApellido,
                segundoApellido,
                codigoCdat
        );
    }

    @GetMapping("/{codigoCdat}/extracto")
    public List<Map<String, Object>> consultarExtracto(
            @PathVariable String codigoCdat,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaInicial,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaFinal
    ) {

        validarAcceso();

        return service.consultarExtracto(
                codigoCdat,
                fechaInicial,
                fechaFinal
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