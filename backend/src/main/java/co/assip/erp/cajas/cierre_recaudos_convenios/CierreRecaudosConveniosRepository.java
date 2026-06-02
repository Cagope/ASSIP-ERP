package co.assip.erp.cajas.cierre_recaudos_convenios;

import co.assip.erp.cajas.cierre_recaudos_convenios.dto.CierreRecaudosConveniosItemDTO;
import co.assip.erp.cajas.recaudos_convenios.dto.RecaudoConvenioConvenioDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CierreRecaudosConveniosRepository {

    private final JdbcTemplate jdbcTemplate;

    public Long obtenerProvisionAbierta(
            Long idCaja,
            LocalDate fechaContable
    ) {

        String sql = """
            SELECT id_provision
            FROM cajas.provisiones_diarias
            WHERE id_caja = ?
              AND fecha_contable = ?
              AND estado = 'ABIERTA'
            LIMIT 1
        """;

        List<Long> datos = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getLong("id_provision"),
                idCaja,
                fechaContable
        );

        return datos.isEmpty() ? null : datos.get(0);
    }

    public RecaudoConvenioConvenioDTO obtenerConvenioActivo(
            Long idConvenio
    ) {

        String sql = """
            SELECT
                cr.id_convenio,
                cr.id_agencia,
                ag.codigo_agencia,
                ag.nombre_agencia,

                cr.codigo_convenio,
                cr.nombre_convenio,

                cr.id_cuenta_ahorro,
                ca.codigo_cuenta,

                hv.documento AS documento_titular,

                CASE
                    WHEN hv.tipo_persona = '2' THEN COALESCE(hv.nombres, '')
                    ELSE TRIM(
                        COALESCE(hv.primer_apellido, '') || ' ' ||
                        COALESCE(hv.segundo_apellido, '') || ' ' ||
                        COALESCE(hv.nombres, '')
                    )
                END AS nombre_titular

            FROM cajas.convenios_recaudo cr

            INNER JOIN general.datos_agencias ag
                ON ag.id_agencia = cr.id_agencia

            INNER JOIN depositos.cuentas_ahorro ca
                ON ca.id_cuenta_ahorro = cr.id_cuenta_ahorro

            LEFT JOIN (
                SELECT DISTINCT ON (id_datos_personal)
                    id_datos_personal,
                    tipo_persona,
                    documento,
                    nombres,
                    primer_apellido,
                    segundo_apellido
                FROM reporting.vw_hoja_vida_general_total_reciente
                ORDER BY id_datos_personal
            ) hv
                ON hv.id_datos_personal = ca.id_datos_personal

            WHERE cr.id_convenio = ?
              AND cr.estado = 'A'
        """;

        List<RecaudoConvenioConvenioDTO> datos =
                jdbcTemplate.query(
                        sql,
                        (rs, rowNum) -> RecaudoConvenioConvenioDTO.builder()
                                .idConvenio(rs.getLong("id_convenio"))
                                .idAgencia(rs.getInt("id_agencia"))
                                .codigoAgencia(rs.getString("codigo_agencia"))
                                .nombreAgencia(rs.getString("nombre_agencia"))
                                .codigoConvenio(rs.getString("codigo_convenio"))
                                .nombreConvenio(rs.getString("nombre_convenio"))
                                .idCuentaAhorro(rs.getLong("id_cuenta_ahorro"))
                                .codigoCuenta(rs.getString("codigo_cuenta"))
                                .documentoTitular(rs.getString("documento_titular"))
                                .nombreTitular(rs.getString("nombre_titular"))
                                .build(),
                        idConvenio
                );

        return datos.isEmpty() ? null : datos.get(0);
    }

    public List<CierreRecaudosConveniosItemDTO> listarRecaudosPendientes(
            Long idProvision,
            Long idConvenio
    ) {

        String sql = """
            SELECT
                id_recaudo_convenio,
                fecha_recaudo,
                documento_soporte,
                valor_recaudo
            FROM cajas.recaudos_convenios
            WHERE id_provision = ?
              AND id_convenio = ?
              AND estado_recaudo = 'RECIBIDO'
            ORDER BY id_recaudo_convenio
        """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> CierreRecaudosConveniosItemDTO.builder()
                        .idRecaudoConvenio(rs.getLong("id_recaudo_convenio"))
                        .fechaRecaudo(rs.getDate("fecha_recaudo").toLocalDate())
                        .documentoSoporte(rs.getString("documento_soporte"))
                        .valorRecaudo(rs.getBigDecimal("valor_recaudo"))
                        .build(),
                idProvision,
                idConvenio
        );
    }

    public Long insertarMovimientoCaja(
            Long idProvision,
            Long idCaja,
            Long idDepartamentoOperativo,
            Long idOperacionCaja,
            String moduloOrigen,
            String procesoOrigen,
            String tablaOrigen,
            Long idOrigen,
            String tipoDocumento,
            String numeroDocumento,
            String codigoReferencia,
            String naturaleza,
            String medioPago,
            BigDecimal valor,
            String concepto,
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
                tipo_documento,
                numero_documento,
                codigo_referencia,
                naturaleza,
                medio_pago,
                valor,
                concepto,
                estado,
                fk_seguridad_creacion,
                fk_seguridad_edicion
            )
            SELECT
                ?,
                ?,
                p.fecha_contable,
                ?,
                ?,
                ?,
                ?,
                ?,
                ?,
                ?,
                ?,
                ?,
                ?,
                ?,
                ?,
                ?,
                'REGISTRADO',
                ?,
                ?
            FROM cajas.provisiones_diarias p
            WHERE p.id_provision = ?
            RETURNING id_movimiento_caja
        """;

        return jdbcTemplate.queryForObject(
                sql,
                Long.class,
                idProvision,
                idCaja,
                idDepartamentoOperativo,
                idOperacionCaja,
                moduloOrigen,
                procesoOrigen,
                tablaOrigen,
                idOrigen,
                tipoDocumento,
                numeroDocumento,
                codigoReferencia,
                naturaleza,
                medioPago,
                valor,
                concepto,
                idUsuario,
                idUsuario,
                idProvision
        );
    }

    public void insertarExtractoCuentaAhorro(
            Long idCuentaAhorro,
            LocalDate fechaMovimiento,
            String tipoComprobante,
            String numeroComprobante,
            String tipoMovimiento,
            BigDecimal valorCredito,
            String establecimiento,
            Integer idUsuario
    ) {

        String sql = """
            INSERT INTO depositos.extractos_cuentas_ahorros (
                id_cuenta_ahorro,
                fecha_movimiento,
                hora_movimiento,
                tipo_comprobante,
                numero_comprobante,
                tipo_movimiento,
                valor_debito,
                valor_credito,
                modulo,
                tarjeta,
                establecimiento,
                fk_seguridad_creacion,
                fk_seguridad_edicion
            )
            VALUES (
                ?,
                ?,
                CURRENT_TIME,
                ?,
                ?,
                ?,
                0,
                ?,
                '05',
                'N',
                ?,
                ?,
                ?
            )
        """;

        jdbcTemplate.update(
                sql,
                idCuentaAhorro,
                fechaMovimiento,
                tipoComprobante,
                numeroComprobante,
                tipoMovimiento,
                valorCredito,
                establecimiento,
                idUsuario,
                idUsuario
        );
    }

    public void actualizarSaldoCuenta(
            Long idCuentaAhorro,
            BigDecimal valorCredito,
            Integer idUsuario
    ) {

        String sql = """
            UPDATE depositos.cuentas_ahorro
               SET saldo_actual_cuenta = COALESCE(saldo_actual_cuenta, 0) + ?,
                   fk_seguridad_edicion = ?,
                   fecha_edicion = CURRENT_TIMESTAMP
             WHERE id_cuenta_ahorro = ?
        """;

        jdbcTemplate.update(
                sql,
                valorCredito,
                idUsuario,
                idCuentaAhorro
        );
    }

    public void marcarRecaudosProcesados(
            Long idProvision,
            Long idConvenio,
            Integer idUsuario
    ) {

        String sql = """
            UPDATE cajas.recaudos_convenios
               SET estado_recaudo = 'PROCESADO',
                   fk_seguridad_edicion = ?,
                   fecha_edicion = CURRENT_TIMESTAMP
             WHERE id_provision = ?
               AND id_convenio = ?
               AND estado_recaudo = 'RECIBIDO'
        """;

        jdbcTemplate.update(
                sql,
                idUsuario,
                idProvision,
                idConvenio
        );
    }

    public Long obtenerDepartamentoDepositos() {

        String sql = """
            SELECT id_departamento_operativo
            FROM cajas.departamentos_operativos
            WHERE codigo_departamento = '00'
              AND estado = 'A'
            LIMIT 1
        """;

        List<Long> datos = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getLong("id_departamento_operativo")
        );

        return datos.isEmpty() ? null : datos.get(0);
    }

    public Long obtenerOperacionCaja(
            String codigoOperacion
    ) {

        String sql = """
            SELECT id_operacion_caja
            FROM cajas.operaciones_caja
            WHERE codigo_operacion = ?
              AND estado = 'A'
            LIMIT 1
        """;

        List<Long> datos = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getLong("id_operacion_caja"),
                codigoOperacion
        );

        return datos.isEmpty() ? null : datos.get(0);
    }
}