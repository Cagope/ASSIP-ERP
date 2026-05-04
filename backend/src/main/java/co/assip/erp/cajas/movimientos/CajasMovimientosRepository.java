package co.assip.erp.cajas.movimientos;

import co.assip.erp.cajas.medios_pago.dto.MedioPagoChequeDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;

@Repository
@RequiredArgsConstructor
public class CajasMovimientosRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public Long obtenerProvisionAbierta(Long idCaja, LocalDate fechaContable) {
        String sql = """
            SELECT p.id_provision
            FROM cajas.provisiones_diarias p
            WHERE p.id_caja = :idCaja
              AND p.fecha_contable = :fechaContable
              AND p.estado = 'ABIERTA'
        """;

        return jdbc.query(
                sql,
                new MapSqlParameterSource()
                        .addValue("idCaja", idCaja)
                        .addValue("fechaContable", fechaContable),
                rs -> rs.next() ? rs.getLong("id_provision") : null
        );
    }

    public Long obtenerIdDepartamento(String codigoDepartamento) {
        String sql = """
            SELECT id_departamento_operativo
            FROM cajas.departamentos_operativos
            WHERE codigo_departamento = :codigo
              AND estado = 'A'
        """;

        return jdbc.query(
                sql,
                new MapSqlParameterSource("codigo", codigoDepartamento),
                rs -> rs.next() ? rs.getLong("id_departamento_operativo") : null
        );
    }

    public Long obtenerIdOperacion(String codigoOperacion) {
        String sql = """
            SELECT id_operacion_caja
            FROM cajas.operaciones_caja
            WHERE codigo_operacion = :codigo
              AND estado = 'A'
        """;

        return jdbc.query(
                sql,
                new MapSqlParameterSource("codigo", codigoOperacion),
                rs -> rs.next() ? rs.getLong("id_operacion_caja") : null
        );
    }

    public Long insertarMovimientoCaja(
            Long idProvision,
            Long idCaja,
            LocalDate fechaContable,
            Long idDepartamentoOperativo,
            Long idOperacionCaja,
            String moduloOrigen,
            String procesoOrigen,
            String tablaOrigen,
            Long idOrigen,
            String naturaleza,
            String medioPago,
            BigDecimal valor,
            String concepto,
            String tipoComprobante,
            String numeroComprobante,
            Integer idUsuario
    ) {
        String sql = """
            INSERT INTO cajas.movimientos_caja (
                id_provision,
                id_caja,
                fecha_contable,
                id_departamento_operativo,
                id_operacion_caja,
                modulo_origen,
                proceso_origen,
                tabla_origen,
                id_origen,
                naturaleza,
                medio_pago,
                valor,
                concepto,
                tipo_comprobante,
                numero_comprobante,
                fk_seguridad_creacion,
                fk_seguridad_edicion
            )
            VALUES (
                :idProvision,
                :idCaja,
                :fechaContable,
                :idDepartamentoOperativo,
                :idOperacionCaja,
                :moduloOrigen,
                :procesoOrigen,
                :tablaOrigen,
                :idOrigen,
                :naturaleza,
                :medioPago,
                :valor,
                :concepto,
                :tipoComprobante,
                :numeroComprobante,
                :idUsuario,
                :idUsuario
            )
            RETURNING id_movimiento_caja
        """;

        return jdbc.queryForObject(sql, new MapSqlParameterSource()
                .addValue("idProvision", idProvision)
                .addValue("idCaja", idCaja)
                .addValue("fechaContable", fechaContable)
                .addValue("idDepartamentoOperativo", idDepartamentoOperativo)
                .addValue("idOperacionCaja", idOperacionCaja)
                .addValue("moduloOrigen", moduloOrigen)
                .addValue("procesoOrigen", procesoOrigen)
                .addValue("tablaOrigen", tablaOrigen)
                .addValue("idOrigen", idOrigen)
                .addValue("naturaleza", naturaleza)
                .addValue("medioPago", medioPago)
                .addValue("valor", valor)
                .addValue("concepto", concepto)
                .addValue("tipoComprobante", tipoComprobante)
                .addValue("numeroComprobante", numeroComprobante)
                .addValue("idUsuario", idUsuario), Long.class);
    }

    public void insertarChequeRecibido(
            Long idMovimientoCaja,
            Long idCaja,
            LocalDate fechaRecibido,
            MedioPagoChequeDTO cheque,
            String numeroDocumento,
            String codigoReferencia,
            String moduloOrigen,
            String procesoOrigen,
            String tablaOrigen,
            Long idOrigen,
            Integer idUsuario
    ) {
        String sql = """
            INSERT INTO cajas.cheques_recibidos (
                id_movimiento_caja,
                id_caja,
                fecha_recibido,
                codigo_banco,
                numero_cheque,
                valor_cheque,
                estado_cheque,
                fecha_estado,
                numero_documento,
                codigo_referencia,
                modulo_origen,
                proceso_origen,
                tabla_origen,
                id_origen,
                fk_seguridad_creacion,
                fk_seguridad_edicion
            )
            VALUES (
                :idMovimientoCaja,
                :idCaja,
                :fechaRecibido,
                :codigoBanco,
                :numeroCheque,
                :valorCheque,
                'RECIBIDO',
                :fechaRecibido,
                :numeroDocumento,
                :codigoReferencia,
                :moduloOrigen,
                :procesoOrigen,
                :tablaOrigen,
                :idOrigen,
                :idUsuario,
                :idUsuario
            )
        """;

        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("idMovimientoCaja", idMovimientoCaja)
                .addValue("idCaja", idCaja)
                .addValue("fechaRecibido", fechaRecibido)
                .addValue("codigoBanco", cheque.getCodigoBanco())
                .addValue("numeroCheque", cheque.getNumeroCheque())
                .addValue("valorCheque", cheque.getValorCheque())
                .addValue("numeroDocumento", numeroDocumento)
                .addValue("codigoReferencia", codigoReferencia)
                .addValue("moduloOrigen", moduloOrigen)
                .addValue("procesoOrigen", procesoOrigen)
                .addValue("tablaOrigen", tablaOrigen)
                .addValue("idOrigen", idOrigen)
                .addValue("idUsuario", idUsuario));
    }
}