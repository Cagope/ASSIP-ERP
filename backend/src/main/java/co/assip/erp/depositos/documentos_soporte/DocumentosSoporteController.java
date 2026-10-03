package co.assip.erp.depositos.documentos_soporte;

import co.assip.erp.depositos.documentos_soporte.dto.DocumentosSoporteBusquedaDTO;
import co.assip.erp.depositos.documentos_soporte.dto.DocumentosSoporteCuentaDTO;
import co.assip.erp.depositos.documentos_soporte.dto.DocumentosSoporteFormDTO;
import co.assip.erp.depositos.documentos_soporte.dto.DocumentosSoporteGuardarDTO;
import co.assip.erp.depositos.documentos_soporte.dto.DocumentosSoporteHistoricoDTO;
import co.assip.erp.depositos.documentos_soporte.dto.DocumentosSoporteResponseDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/depositos/documentos-soporte")
@RequiredArgsConstructor
public class DocumentosSoporteController {

    private static final String PERMISO_VIEW =
            "DEPOSITOS_DOCUMENTOS_SOPORTE_VIEW";

    private static final String PERMISO_EDIT =
            "DEPOSITOS_DOCUMENTOS_SOPORTE_EDIT";

    private final DocumentosSoporteService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    @PostMapping("/buscar-cuentas")
    public List<DocumentosSoporteCuentaDTO> buscarCuentas(
            @RequestBody DocumentosSoporteBusquedaDTO filtros
    ) {

        validarAcceso(PERMISO_VIEW);

        return service.buscarCuentas(filtros);
    }

    @GetMapping("/activo/{idCuentaAhorro}")
    public DocumentosSoporteFormDTO obtenerDocumentoActivo(
            @PathVariable Integer idCuentaAhorro
    ) {

        validarAcceso(PERMISO_VIEW);

        return service.obtenerDocumentoActivo(idCuentaAhorro);
    }

    @GetMapping("/historico/{idCuentaAhorro}")
    public List<DocumentosSoporteHistoricoDTO> obtenerHistorico(
            @PathVariable Integer idCuentaAhorro
    ) {

        validarAcceso(PERMISO_VIEW);

        return service.obtenerHistorico(idCuentaAhorro);
    }

    @PostMapping("/guardar")
    public DocumentosSoporteResponseDTO guardar(
            @RequestBody DocumentosSoporteGuardarDTO dto
    ) {

        validarAcceso(PERMISO_EDIT);

        return service.guardar(dto);
    }

    private void validarAcceso(String permiso) {

        Usuario usuarioActual = new Usuario();
        usuarioActual.setIdUsuario(usuarioSesionService.idUsuario());

        accessValidator.validarAcceso(usuarioActual, permiso);
    }
}