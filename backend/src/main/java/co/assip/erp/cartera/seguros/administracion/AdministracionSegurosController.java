package co.assip.erp.cartera.seguros.administracion;

import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.SecurityUtils;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** API de consulta, creación y edición de seguros asociados a créditos desembolsados. */
@RestController
@RequestMapping("/cartera/administracion-seguros")
public class AdministracionSegurosController {
    private static final String PERMISO_VIEW = "CARTERA_SEGUROS_ADMIN_VIEW";
    private static final String PERMISO_EDIT = "CARTERA_SEGUROS_ADMIN_EDIT";

    private final AdministracionSegurosService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    public AdministracionSegurosController(
            AdministracionSegurosService service,
            UsuarioSesionService usuarioSesionService,
            AccessValidator accessValidator) {
        this.service = service;
        this.usuarioSesionService = usuarioSesionService;
        this.accessValidator = accessValidator;
    }

    /** Consulta todas las configuraciones de un crédito, incluso las inactivas. */
    @GetMapping("/creditos/{idCredito}")
    public ResponseEntity<List<AdministracionSegurosRepository.SeguroCredito>> consultar(
            @PathVariable Integer idCredito,
            @RequestParam Integer idAgencia) {
        validarPermiso(PERMISO_VIEW);
        return ResponseEntity.ok(service.consultar(idCredito, idAgencia));
    }

    /** Consulta el porcentaje base parametrizado para la agencia autorizada. */
    @GetMapping("/parametros/porcentaje-base")
    public ResponseEntity<BigDecimal> consultarPorcentajeBase(
            @RequestParam Integer idAgencia) {
        validarPermiso(PERMISO_VIEW);
        if (idAgencia == null || idAgencia <= 0) {
            throw new IllegalArgumentException("La agencia indicada no es válida.");
        }
        SecurityUtils.validarAgencia(idAgencia);
        return ResponseEntity.ok(service.consultarPorcentajeBaseSeguro(idAgencia));
    }

    /** Crea una configuración únicamente cuando el crédito todavía no tiene seguro. */
    @PostMapping("/creditos/{idCredito}")
    public ResponseEntity<Integer> crear(
            @PathVariable Integer idCredito,
            @RequestParam Integer idAgencia,
            @RequestBody AdministracionSegurosRepository.ConfiguracionSeguro configuracion) {
        validarPermiso(PERMISO_EDIT);
        return ResponseEntity.ok(service.crear(idCredito, idAgencia, configuracion));
    }

    /** Actualiza el seguro existente y registra su configuración anterior para auditoría. */
    @PutMapping("/creditos/{idCredito}/seguros/{idCreditoSeguro}")
    public ResponseEntity<Void> editar(
            @PathVariable Integer idCredito,
            @PathVariable Integer idCreditoSeguro,
            @RequestParam Integer idAgencia,
            @RequestBody EdicionSeguroRequest solicitud) {
        validarPermiso(PERMISO_EDIT);
        if (solicitud == null || solicitud.configuracion() == null || solicitud.activo() == null) {
            throw new IllegalArgumentException(
                    "Debe indicar las condiciones del seguro y su estado activo.");
        }
        service.editar(
                idCredito,
                idAgencia,
                idCreditoSeguro,
                solicitud.configuracion(),
                solicitud.activo());
        return ResponseEntity.noContent().build();
    }

    /** Valida autorización funcional del usuario de sesión, sin depender del nombre de su rol. */
    private void validarPermiso(String permiso) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(usuarioSesionService.idUsuario());
        accessValidator.validarAcceso(usuario, permiso);
    }

    /** Datos de edición; el identificador del seguro se obtiene de la ruta. */
    public record EdicionSeguroRequest(
            AdministracionSegurosRepository.ConfiguracionSeguro configuracion,
            Boolean activo) {
    }
}