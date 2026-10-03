package co.assip.erp.depositos.informes.documentos_soporte;

import co.assip.erp.depositos.informes.documentos_soporte.dto.DocumentoSoporteResumenAgenciaFormaDTO;
import co.assip.erp.depositos.informes.documentos_soporte.dto.DocumentosSoporteInformeRequestDTO;
import co.assip.erp.depositos.informes.documentos_soporte.dto.DocumentosSoporteInformeResponseDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/depositos/informes/documentos-soporte")
@RequiredArgsConstructor
public class DocumentosSoporteInformeController {

    private static final String PERMISO_VIEW =
            "DEPOSITOS_INFORMES_VIEW";

    private final DocumentosSoporteInformeService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    @PostMapping
    public DocumentosSoporteInformeResponseDTO consultar(
            @RequestBody DocumentosSoporteInformeRequestDTO request
    ) {

        validarAcceso();

        return service.consultar(request);
    }

    @PostMapping("/resumen-agencia-forma")
    public List<DocumentoSoporteResumenAgenciaFormaDTO> resumenPorAgenciaForma(
            @RequestBody DocumentosSoporteInformeRequestDTO request
    ) {

        validarAcceso();

        return service.resumenPorAgenciaForma(request);
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