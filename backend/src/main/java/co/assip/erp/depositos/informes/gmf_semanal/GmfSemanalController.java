package co.assip.erp.depositos.informes.gmf_semanal;

import co.assip.erp.depositos.informes.gmf_semanal.dto.GmfSemanalRequestDTO;
import co.assip.erp.depositos.informes.gmf_semanal.dto.GmfSemanalResponseDTO;
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
@RequestMapping("/depositos/informes/gmf-semanal")
@RequiredArgsConstructor
public class GmfSemanalController {

    private static final String PERMISO_VIEW =
            "DEPOSITOS_INFORMES_VIEW";

    private final GmfSemanalService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    @PostMapping
    public ResponseEntity<GmfSemanalResponseDTO> consultar(
            @RequestBody GmfSemanalRequestDTO request
    ) {

        validarAcceso();

        GmfSemanalResponseDTO response =
                service.consultar(request);

        return ResponseEntity.ok(response);
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