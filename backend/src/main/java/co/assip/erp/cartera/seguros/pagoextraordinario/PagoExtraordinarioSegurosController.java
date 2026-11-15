package co.assip.erp.cartera.seguros.pagoextraordinario;

import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Expone consulta, vista previa y confirmación de pagos y correcciones de seguros. */
@RestController
@RequestMapping("/cartera/seguros/pago-extraordinario")
@RequiredArgsConstructor
public class PagoExtraordinarioSegurosController {
    private static final String PERMISO_VIEW = "CARTERA_SEGUROS_ADMIN_VIEW";
    private static final String PERMISO_EXECUTE = "CARTERA_SEGUROS_ADMIN_EDIT";

    private final PagoExtraordinarioSegurosService service;
    private final PagoExtraordinarioSegurosRepository repository;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    /** Consulta el saldo actual del seguro del crédito autorizado. */
    @GetMapping("/creditos/{idCredito}/saldo")
    public ResponseEntity<java.math.BigDecimal> consultarSaldo(@PathVariable Integer idCredito) {
        validarPermiso(PERMISO_VIEW);
        return ResponseEntity.ok(service.consultarSaldo(idCredito));
    }

    /** Consulta las cuentas de ahorro utilizables por el asociado del crédito. */
    @GetMapping("/creditos/{idCredito}/cuentas-ahorro")
    public ResponseEntity<?> consultarCuentasAhorro(@PathVariable Integer idCredito) {
        validarPermiso(PERMISO_VIEW);
        service.consultarSaldo(idCredito);
        return ResponseEntity.ok(repository.listarCuentasAhorro(idCredito));
    }

    /** Construye el comprobante CC sin registrar movimientos ni consumir consecutivos. */
    @PostMapping("/vista-previa")
    public ResponseEntity<PagoExtraordinarioSegurosService.VistaPrevia> previsualizar(
            @RequestBody PagoExtraordinarioSegurosService.Solicitud solicitud) {
        validarPermiso(PERMISO_VIEW);
        return ResponseEntity.ok(service.previsualizar(solicitud));
    }

    /** Confirma el pago o corrección, registra el CC y publica las operaciones necesarias. */
    @PostMapping("/confirmar")
    public ResponseEntity<PagoExtraordinarioSegurosService.Resultado> confirmar(
            @RequestBody PagoExtraordinarioSegurosService.Solicitud solicitud) {
        validarPermiso(PERMISO_EXECUTE);
        return ResponseEntity.ok(service.confirmar(solicitud));
    }

    /** Comprueba el permiso funcional del usuario autenticado sin depender del nombre del rol. */
    private void validarPermiso(String permiso) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(usuarioSesionService.idUsuario());
        accessValidator.validarAcceso(usuario, permiso);
    }
}
