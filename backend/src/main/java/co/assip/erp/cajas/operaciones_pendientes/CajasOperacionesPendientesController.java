package co.assip.erp.cajas.operaciones_pendientes;

import co.assip.erp.cajas.operaciones_pendientes.dto.CajaOperacionPendienteDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/cajas/operaciones-pendientes")
@RequiredArgsConstructor
public class CajasOperacionesPendientesController {

    private final CajasOperacionesPendientesService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    /** Consulta las operaciones pendientes de la caja autorizada en la fecha indicada. */
    @GetMapping
    public List<CajaOperacionPendienteDTO> listarPendientes(
            @RequestParam Long idCaja,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaContable,
            HttpServletRequest request) {
        validarAcceso(request, "CAJAS_OPERACIONES_PENDIENTES_VIEW");
        return service.listarPendientes(idCaja, fechaContable);
    }

    /** Confirma el movimiento físico de Caja; no genera comprobantes ni aplica Cartera. */
    @PostMapping("/{idMovimientoCaja}/confirmar")
    public ResponseEntity<Void> confirmar(
            @PathVariable Long idMovimientoCaja,
            @RequestParam Long idCaja,
            @RequestBody(required = false) ConfirmacionComprobante solicitud,
            HttpServletRequest request) {
        validarAcceso(request, "CAJAS_OPERACIONES_PENDIENTES_EXECUTE");
        service.confirmar(idMovimientoCaja, idCaja,
                solicitud == null ? null : solicitud.huella());
        return ResponseEntity.noContent().build();
    }

    /** Mantiene compatible el cuerpo opcional de confirmación del frontend actual. */
    public record ConfirmacionComprobante(String huella) { }

    /** Verifica el permiso funcional del usuario autenticado. */
    private void validarAcceso(HttpServletRequest request, String permiso) {
        Integer idUsuario = usuarioSesionService.idUsuario();
        Usuario usuarioActual = new Usuario();
        usuarioActual.setIdUsuario(idUsuario);
        accessValidator.validarAcceso(usuarioActual, permiso);
    }
}
