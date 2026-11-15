package co.assip.erp.cajas.operaciones_pendientes;

import co.assip.erp.cajas.operaciones_pendientes.dto.CajaOperacionPendienteDTO;
import co.assip.erp.cartera.pago_cuotas_v1.motor.PagoCuotasMotorService;
import co.assip.erp.cajas.provisiones.CajasUsuariosService;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
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
    private final NamedParameterJdbcTemplate jdbc;

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

        if (esReversaPagoCuotasCartera(operacion)) {
            confirmarDevolucionCartera(idMovimientoCaja, idCaja, operacion, idUsuario);
            return;
        }

        if (esReversaProrrogaCartera(operacion)) {
            confirmarDevolucionProrroga(idMovimientoCaja, idCaja, operacion, idUsuario);
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

    /**
     * Ejecuta la devolución física en la provisión abierta de la jornada actual.
     * No genera comprobantes ni vuelve a modificar los saldos de Cartera.
     */
    private void confirmarDevolucionCartera(
            Long idMovimientoCaja,
            Long idCaja,
            CajasOperacionesPendientesRepository.OperacionPendienteBloqueada operacion,
            Integer idUsuario) {
        if (!"cartera.pagos_cuotas".equalsIgnoreCase(operacion.tablaOrigen())
                || operacion.idOrigen() == null || operacion.idOrigen() <= 0) {
            throw new IllegalStateException(
                    "La devolución no está vinculada a un pago de Cartera válido.");
        }

        var parametros = new MapSqlParameterSource()
                .addValue("idMovimientoCaja", idMovimientoCaja)
                .addValue("idCaja", idCaja)
                .addValue("idPago", operacion.idOrigen())
                .addValue("idUsuario", idUsuario);

        // Exigir una sola provisión abierta para la caja y la fecha del servidor.
        List<Long> provisiones = jdbc.query("""
                SELECT id_provision
                  FROM cajas.provisiones_diarias
                 WHERE id_caja = :idCaja
                   AND fecha_contable = CURRENT_DATE
                   AND estado = 'ABIERTA'
                 FOR UPDATE
                """, parametros, (rs, fila) -> rs.getLong("id_provision"));
        if (provisiones.size() != 1) {
            throw new IllegalStateException(
                    "La caja debe tener una única provisión abierta para la jornada actual "
                            + "antes de entregar la devolución.");
        }
        parametros.addValue("idProvision", provisiones.get(0));

        // El pago original debe estar reversado y su ingreso físico registrado.
        // La fila pendiente ya está bloqueada por bloquearOperacionPendiente().
        int actualizados = jdbc.update("""
                UPDATE cajas.movimientos_caja AS m
                   SET estado = 'REGISTRADO',
                       id_provision = :idProvision,
                       fecha_contable = CURRENT_DATE,
                       hora_movimiento = CURRENT_TIME,
                       fk_seguridad_edicion = :idUsuario,
                       fecha_edicion = CURRENT_TIMESTAMP
                 WHERE m.id_movimiento_caja = :idMovimientoCaja
                   AND m.id_caja = :idCaja
                   AND m.estado = 'PENDIENTE'
                   AND m.modulo_origen = 'CARTERA'
                   AND m.proceso_origen = 'REVERSA_PAGO_CUOTAS'
                   AND m.tabla_origen = 'cartera.pagos_cuotas'
                   AND m.id_origen = :idPago
                   AND m.naturaleza = 'EGRESO'
                   AND m.medio_pago = 'EFECTIVO'
                   AND EXISTS (
                       SELECT 1
                         FROM cajas.operaciones_caja o
                        WHERE o.id_operacion_caja = m.id_operacion_caja
                          AND o.codigo_operacion = 'REVERSA_PAGO_CUOTAS'
                          AND o.naturaleza = 'EGRESO'
                          AND o.afecta_efectivo = TRUE
                          AND o.estado = 'A'
                   )
                   AND EXISTS (
                       SELECT 1
                         FROM cartera.pagos_cuotas pc
                         JOIN cajas.movimientos_caja ingreso
                           ON ingreso.id_movimiento_caja = pc.id_movimiento_caja
                          AND ingreso.id_caja = pc.id_caja
                        WHERE pc.id_pago = :idPago
                          AND pc.id_caja = m.id_caja
                          AND pc.estado = 'REVERSADO'
                          AND ingreso.estado = 'REGISTRADO'
                          AND ingreso.naturaleza = 'INGRESO'
                          AND ingreso.medio_pago = 'EFECTIVO'
                          AND ingreso.valor = m.valor
                          AND ingreso.modulo_origen = 'CARTERA'
                          AND ingreso.proceso_origen = 'PAGO_CUOTAS'
                   )
                """, parametros);
        if (actualizados != 1) {
            throw new IllegalStateException(
                    "No se pudo confirmar la devolución: verifique la reversión "
                            + "de Cartera y el ingreso original de Caja.");
        }
    }

    /** Identifica exclusivamente las devoluciones de pagos de cuotas. */
    private boolean esReversaPagoCuotasCartera(
            CajasOperacionesPendientesRepository.OperacionPendienteBloqueada operacion) {
        return "CARTERA".equalsIgnoreCase(operacion.moduloOrigen())
                && "REVERSA_PAGO_CUOTAS".equalsIgnoreCase(operacion.procesoOrigen());
    }


    /** Identifica las devoluciones de efectivo originadas por una prórroga reversada. */
    private boolean esReversaProrrogaCartera(
            CajasOperacionesPendientesRepository.OperacionPendienteBloqueada operacion) {
        return "CARTERA".equalsIgnoreCase(operacion.moduloOrigen())
                && "REVERSA_PRORROGA".equalsIgnoreCase(operacion.procesoOrigen());
    }

    /**
     * Confirma la entrega física de una devolución de prórroga previamente reversada.
     * Exige el ingreso original registrado, el pago reversado y la provisión abierta.
     */
    private void confirmarDevolucionProrroga(
            Long idMovimientoCaja,
            Long idCaja,
            CajasOperacionesPendientesRepository.OperacionPendienteBloqueada operacion,
            Integer idUsuario) {
        if (!"cartera.creditos_prorrogas".equalsIgnoreCase(operacion.tablaOrigen())
                || operacion.idOrigen() == null || operacion.idOrigen() <= 0) {
            throw new IllegalStateException(
                    "La devolución no está vinculada a una prórroga válida.");
        }

        var parametros = new MapSqlParameterSource()
                .addValue("idMovimientoCaja", idMovimientoCaja)
                .addValue("idCaja", idCaja)
                .addValue("idProrroga", operacion.idOrigen())
                .addValue("idUsuario", idUsuario);

        List<Long> provisiones = jdbc.query("""
                SELECT id_provision
                  FROM cajas.provisiones_diarias
                 WHERE id_caja = :idCaja
                   AND fecha_contable = CURRENT_DATE
                   AND estado = 'ABIERTA'
                 FOR UPDATE
                """, parametros, (rs, fila) -> rs.getLong("id_provision"));
        if (provisiones.size() != 1) {
            throw new IllegalStateException(
                    "La caja debe tener una única provisión abierta para la jornada actual "
                            + "antes de entregar la devolución de prórroga.");
        }
        parametros.addValue("idProvision", provisiones.get(0));

        int actualizados = jdbc.update("""
                UPDATE cajas.movimientos_caja AS m
                   SET estado = 'REGISTRADO',
                       id_provision = :idProvision,
                       fecha_contable = CURRENT_DATE,
                       hora_movimiento = CURRENT_TIME,
                       fk_seguridad_edicion = :idUsuario,
                       fecha_edicion = CURRENT_TIMESTAMP
                 WHERE m.id_movimiento_caja = :idMovimientoCaja
                   AND m.id_caja = :idCaja
                   AND m.estado = 'PENDIENTE'
                   AND m.modulo_origen = 'CARTERA'
                   AND m.proceso_origen = 'REVERSA_PRORROGA'
                   AND m.tabla_origen = 'cartera.creditos_prorrogas'
                   AND m.id_origen = :idProrroga
                   AND m.naturaleza = 'EGRESO'
                   AND m.medio_pago = 'EFECTIVO'
                   AND m.valor > 0
                   AND EXISTS (
                       SELECT 1
                         FROM cajas.operaciones_caja o
                        WHERE o.id_operacion_caja = m.id_operacion_caja
                          AND o.codigo_operacion = 'REVERSA_PRORROGA'
                          AND o.naturaleza = 'EGRESO'
                          AND o.afecta_efectivo = TRUE
                          AND o.estado = 'A'
                   )
                   AND EXISTS (
                       SELECT 1
                         FROM cartera.creditos_prorrogas p
                         JOIN cartera.pagos_cuotas pc
                           ON pc.id_cartera_credito = p.id_cartera_credito
                          AND pc.tipo_operacion = 'PRORROGA'
                          AND pc.liquidacion ->> 'idCreditoProrroga' = CAST(p.id_credito_prorroga AS text)
                          AND pc.tipo_comprobante = 'CC'
                          AND TRIM(pc.numero_comprobante) = TRIM(p.numero_comprobante)
                         JOIN cajas.movimientos_caja ingreso
                           ON ingreso.modulo_origen = 'CARTERA'
                          AND ingreso.proceso_origen = 'PRORROGA'
                          AND ingreso.tabla_origen = 'cartera.creditos_prorrogas'
                          AND ingreso.id_origen = p.id_credito_prorroga
                          AND ingreso.id_caja = m.id_caja
                          AND ingreso.naturaleza = 'INGRESO'
                          AND ingreso.medio_pago = 'EFECTIVO'
                          AND ingreso.estado = 'REGISTRADO'
                          AND ingreso.valor = m.valor
                          AND ingreso.tipo_comprobante = 'CC'
                          AND TRIM(ingreso.numero_comprobante) = TRIM(p.numero_comprobante)
                        WHERE p.id_credito_prorroga = :idProrroga
                          AND p.estado_prorroga = 'R'
                          AND pc.estado = 'REVERSADO'
                   )
                """, parametros);
        if (actualizados != 1) {
            throw new IllegalStateException(
                    "No se pudo confirmar la devolución: verifique la prórroga reversada, "
                            + "el pago y el ingreso original registrado en Caja.");
        }
    }

    /** Rechaza identificadores de caja nulos o inválidos. */
    private void validarIdCaja(Long idCaja) {
        if (idCaja == null || idCaja <= 0) {
            throw new IllegalArgumentException("Debe indicar una caja válida.");
        }
    }
}
