package co.assip.erp.cajas.operaciones_pendientes;

import co.assip.erp.cajas.operaciones_pendientes.dto.CajaOperacionPendienteDTO;
import co.assip.erp.cartera.pago_cuotas_v1.motor.PagoCuotasMotorService;
import co.assip.erp.cajas.provisiones.CajasUsuariosService;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CajasOperacionesPendientesService {

    private final CajasOperacionesPendientesRepository repository;
    private final CajasUsuariosService cajasUsuariosService;
    private final UsuarioSesionService usuarioSesionService;

    /** Lista los movimientos pendientes de la caja autorizada para la fecha indicada. */
    @Transactional(readOnly = true)
    public List<CajaOperacionPendienteDTO> listarPendientes(Long idCaja, LocalDate fechaContable) {
        validarIdCaja(idCaja);
        if (fechaContable == null) {
            throw new IllegalArgumentException(
                    "Debe indicar la fecha contable para consultar las operaciones pendientes.");
        }
        cajasUsuariosService.validarCajaUsuario(idCaja);
        return repository.listarPendientes(idCaja, fechaContable);
    }

    /** Conserva temporalmente el contrato del controlador; la vista previa se realiza en Cartera. */
    @Deprecated
    @Transactional(readOnly = true)
    public PagoCuotasMotorService.ComprobantePrevio previsualizarComprobante(
            Long idMovimientoCaja, Long idCaja) {
        throw new UnsupportedOperationException(
                "La vista previa del comprobante CC se realiza en Cartera antes de enviar el pago a Caja.");
    }

    /** Confirma exclusivamente la recepción física; nunca contabiliza ni aplica pagos de Cartera. */
    @Transactional
    public void confirmar(Long idMovimientoCaja, Long idCaja, String huellaRevisada) {
        if (idMovimientoCaja == null || idMovimientoCaja <= 0) {
            throw new IllegalArgumentException("Debe indicar la operación pendiente.");
        }
        validarIdCaja(idCaja);
        cajasUsuariosService.validarCajaUsuario(idCaja);
        Integer idUsuario = usuarioSesionService.idUsuario();

        var operacion = repository.bloquearOperacionPendiente(idMovimientoCaja, idCaja);
        if (esPagoCuotasCartera(operacion)) {
            if (operacion.idOrigen() == null || operacion.idOrigen() <= 0) {
                throw new IllegalStateException("El movimiento no tiene un pago de Cartera válido.");
            }
            // El repositorio exige que Cartera ya haya aplicado el pago y vinculado el movimiento.
            int confirmados = repository.confirmarPagoCuotasCartera(
                    idMovimientoCaja, idCaja, operacion.idOrigen(), idUsuario);
            if (confirmados != 1) {
                throw new IllegalStateException(
                        "El pago aún no está aplicado en Cartera o no corresponde a esta caja; no se confirmó el recaudo.");
            }
            return;
        }

        // Las demás operaciones conservan su procedimiento de confirmación existente.
        int actualizados = repository.confirmar(idMovimientoCaja, idCaja, idUsuario);
        if (actualizados != 1) {
            throw new IllegalStateException(
                    "La operación ya fue atendida, no se encuentra pendiente o no pertenece a la caja actual.");
        }
    }

    /** Identifica los movimientos de pago de cuotas provenientes de Cartera. */
    private boolean esPagoCuotasCartera(
            CajasOperacionesPendientesRepository.OperacionPendienteBloqueada operacion) {
        return "CARTERA".equalsIgnoreCase(operacion.moduloOrigen())
                && "PAGO_CUOTAS".equalsIgnoreCase(operacion.procesoOrigen());
    }

    /** Rechaza identificadores de caja nulos o inválidos. */
    private void validarIdCaja(Long idCaja) {
        if (idCaja == null || idCaja <= 0) {
            throw new IllegalArgumentException("Debe indicar una caja válida.");
        }
    }
}
