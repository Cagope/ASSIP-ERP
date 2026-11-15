package co.assip.erp.cartera.seguros.causacionextraordinaria;

import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** API de consulta, vista previa y confirmación de causaciones extraordinarias de seguros. */
@RestController
@RequestMapping("/cartera/seguros/causacion-extraordinaria")
public class CausacionExtraordinariaSegurosController {
    private static final String PERMISO_VIEW = "CARTERA_SEGUROS_ADMIN_VIEW";
    private static final String PERMISO_EXECUTE = "CARTERA_SEGUROS_ADMIN_EDIT";

    private final CausacionExtraordinariaSegurosService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    public CausacionExtraordinariaSegurosController(CausacionExtraordinariaSegurosService service,
                                                    UsuarioSesionService usuarioSesionService,
                                                    AccessValidator accessValidator) {
        this.service = service;
        this.usuarioSesionService = usuarioSesionService;
        this.accessValidator = accessValidator;
    }

    /** Consulta la configuración vigente y el cálculo sugerido para el crédito seleccionado. */
    @GetMapping("/creditos/{idCredito}/sugerencia")
    public ResponseEntity<CausacionExtraordinariaSegurosService.Sugerencia> consultarSugerencia(
            @PathVariable Integer idCredito) {
        validarPermiso(PERMISO_VIEW);
        return ResponseEntity.ok(service.consultarSugerencia(idCredito));
    }

    /** Valida la solicitud y devuelve las partidas de la NC sin registrar la causación. */
    @PostMapping("/vista-previa")
    public ResponseEntity<CausacionExtraordinariaSegurosService.VistaPrevia> previsualizar(
            @RequestBody CausacionExtraordinariaSegurosService.Solicitud solicitud) {
        validarPermiso(PERMISO_VIEW);
        return ResponseEntity.ok(service.previsualizar(solicitud));
    }

    /** Confirma la causación extraordinaria y su comprobante contable en una transacción. */
    @PostMapping("/confirmar")
    public ResponseEntity<CausacionExtraordinariaSegurosService.Resultado> confirmar(
            @RequestBody CausacionExtraordinariaSegurosService.Solicitud solicitud) {
        validarPermiso(PERMISO_EXECUTE);
        return ResponseEntity.ok(service.confirmar(solicitud));
    }

    /** Verifica el permiso funcional del usuario autenticado, independientemente de sus roles. */
    private void validarPermiso(String permiso) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(usuarioSesionService.idUsuario());
        accessValidator.validarAcceso(usuario, permiso);
    }
}
