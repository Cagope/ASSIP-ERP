package co.assip.erp.depositos.analisis.concentracion;

import co.assip.erp.depositos.analisis.concentracion.dto.ConcentracionDepositosRequestDTO;
import co.assip.erp.depositos.analisis.concentracion.dto.ConcentracionDepositosResponseDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/depositos/analisis/concentracion")
@RequiredArgsConstructor
public class ConcentracionDepositosController {

    private static final String PERMISO_VIEW =
            "DEPOSITOS_ANALISIS_VIEW";

    private final ConcentracionDepositosService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    @PostMapping
    public ResponseEntity<ConcentracionDepositosResponseDTO> consultar(
            @RequestBody ConcentracionDepositosRequestDTO request
    ) {

        validarAcceso();

        return ResponseEntity.ok(
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