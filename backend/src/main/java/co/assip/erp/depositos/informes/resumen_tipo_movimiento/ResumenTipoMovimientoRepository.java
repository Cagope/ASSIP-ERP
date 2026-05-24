package co.assip.erp.depositos.informes.resumen_tipo_movimiento;

import co.assip.erp.depositos.informes.resumen_tipo_movimiento.dto.ResumenTipoMovimientoItemDTO;
import co.assip.erp.depositos.informes.resumen_tipo_movimiento.dto.ResumenTipoMovimientoRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class ResumenTipoMovimientoRepository {

    private final JdbcTemplate jdbc;

    public List<ResumenTipoMovimientoItemDTO> consultar(
            ResumenTipoMovimientoRequestDTO request
    ) {

        String sql = """
            WITH tm_unico AS (
                SELECT DISTINCT ON (codigo_movimiento)
                    codigo_movimiento,
                    descripcion
                FROM depositos.tipo_movimiento
                ORDER BY
                    codigo_movimiento
            )

            SELECT
                e.tipo_movimiento AS codigo_movimiento,
                COALESCE(tm.descripcion, 'SIN DESCRIPCIÓN') AS nombre_movimiento,

                COUNT(*) AS cantidad_movimientos,

                COALESCE(SUM(e.valor_debito), 0) AS total_debitos,
                COALESCE(SUM(e.valor_credito), 0) AS total_creditos,

                COALESCE(SUM(e.valor_credito), 0)
                - COALESCE(SUM(e.valor_debito), 0) AS neto

            FROM depositos.extractos_cuentas_ahorros e

            INNER JOIN depositos.cuentas_ahorro c
                ON c.id_cuenta_ahorro = e.id_cuenta_ahorro

            INNER JOIN depositos.formas_ahorro f
                ON f.id_forma_ahorro = c.id_forma_ahorro

            LEFT JOIN tm_unico tm
                ON tm.codigo_movimiento = e.tipo_movimiento

            WHERE e.fecha_movimiento::date >= ?::date
              AND e.fecha_movimiento::date <= ?::date

              AND (
                    ? = 0
                    OR c.id_agencia = ?
                  )

              AND (
                    ? = '0'
                    OR f.codigo_forma = ?
                  )

            GROUP BY
                e.tipo_movimiento,
                tm.descripcion

            ORDER BY
                e.tipo_movimiento
            """;

        Integer idAgencia =
                request.getIdAgencia() == null
                        ? 0
                        : request.getIdAgencia();

        String codigoForma =
                request.getCodigoForma() == null
                        || request.getCodigoForma().isBlank()
                        ? "0"
                        : request.getCodigoForma();

        return jdbc.query(
                sql,
                (rs, rowNum) ->
                        ResumenTipoMovimientoItemDTO.builder()
                                .codigoMovimiento(rs.getString("codigo_movimiento"))
                                .nombreMovimiento(rs.getString("nombre_movimiento"))
                                .cantidadMovimientos(rs.getInt("cantidad_movimientos"))
                                .totalDebitos(rs.getBigDecimal("total_debitos"))
                                .totalCreditos(rs.getBigDecimal("total_creditos"))
                                .neto(rs.getBigDecimal("neto"))
                                .build(),
                request.getFechaInicial(),
                request.getFechaFinal(),
                idAgencia,
                idAgencia,
                codigoForma,
                codigoForma
        );
    }

    public List<ResumenTipoMovimientoItemDTO> detalle(
            ResumenTipoMovimientoRequestDTO request
    ) {

        String sql = """
            WITH hv_unica AS (
                SELECT DISTINCT ON (id_datos_personal)
                    *
                FROM reporting.vw_hoja_vida_general_total_reciente
                ORDER BY
                    id_datos_personal
            ),

            tm_unico AS (
                SELECT DISTINCT ON (codigo_movimiento)
                    codigo_movimiento,
                    descripcion
                FROM depositos.tipo_movimiento
                ORDER BY
                    codigo_movimiento
            )

            SELECT
                e.id_extracto_cuenta_ahorro,

                e.fecha_movimiento,
                e.hora_movimiento,

                LPAD(a.codigo_agencia, 2, '0') AS codigo_agencia,
                a.nombre_agencia,

                f.codigo_forma,
                f.nombre_forma,

                TRIM(c.codigo_cuenta) AS codigo_cuenta,

                hv.documento,

                CASE
                    WHEN hv.tipo_persona = '2'
                        THEN hv.nombres
                    ELSE TRIM(
                        CONCAT(
                            COALESCE(hv.primer_apellido, ''),
                            ' ',
                            COALESCE(hv.segundo_apellido, ''),
                            ' ',
                            COALESCE(hv.nombres, '')
                        )
                    )
                END AS nombre_completo,

                e.tipo_movimiento AS codigo_movimiento,
                COALESCE(tm.descripcion, 'SIN DESCRIPCIÓN') AS nombre_movimiento,

                COALESCE(e.valor_debito, 0) AS debito,
                COALESCE(e.valor_credito, 0) AS credito

            FROM depositos.extractos_cuentas_ahorros e

            INNER JOIN depositos.cuentas_ahorro c
                ON c.id_cuenta_ahorro = e.id_cuenta_ahorro

            INNER JOIN general.datos_agencias a
                ON a.id_agencia = c.id_agencia

            INNER JOIN depositos.formas_ahorro f
                ON f.id_forma_ahorro = c.id_forma_ahorro

            LEFT JOIN hv_unica hv
                ON hv.id_datos_personal = c.id_datos_personal

            LEFT JOIN tm_unico tm
                ON tm.codigo_movimiento = e.tipo_movimiento

            WHERE e.fecha_movimiento::date >= ?::date
              AND e.fecha_movimiento::date <= ?::date

              AND (
                    ? = 0
                    OR c.id_agencia = ?
                  )

              AND (
                    ? = '0'
                    OR f.codigo_forma = ?
                  )

              AND (
                    ? = '0'
                    OR e.tipo_movimiento = ?
                  )

            ORDER BY
                e.fecha_movimiento,
                e.hora_movimiento,
                f.codigo_forma,
                c.codigo_cuenta,
                e.id_extracto_cuenta_ahorro
            """;

        Integer idAgencia =
                request.getIdAgencia() == null
                        ? 0
                        : request.getIdAgencia();

        String codigoForma =
                request.getCodigoForma() == null
                        || request.getCodigoForma().isBlank()
                        ? "0"
                        : request.getCodigoForma();

        String codigoMovimiento =
                request.getCodigoMovimiento() == null
                        || request.getCodigoMovimiento().isBlank()
                        ? "0"
                        : request.getCodigoMovimiento();

        return jdbc.query(
                sql,
                (rs, rowNum) ->
                        ResumenTipoMovimientoItemDTO.builder()
                                .idExtractoCuentaAhorro(rs.getLong("id_extracto_cuenta_ahorro"))
                                .fechaMovimiento(rs.getObject("fecha_movimiento", java.time.LocalDate.class))
                                .horaMovimiento(rs.getObject("hora_movimiento", java.time.LocalTime.class))
                                .codigoAgencia(rs.getString("codigo_agencia"))
                                .nombreAgencia(rs.getString("nombre_agencia"))
                                .codigoForma(rs.getString("codigo_forma"))
                                .nombreForma(rs.getString("nombre_forma"))
                                .codigoCuenta(rs.getString("codigo_cuenta"))
                                .documento(rs.getString("documento"))
                                .nombreCompleto(rs.getString("nombre_completo"))
                                .codigoMovimiento(rs.getString("codigo_movimiento"))
                                .nombreMovimiento(rs.getString("nombre_movimiento"))
                                .debito(rs.getBigDecimal("debito"))
                                .credito(rs.getBigDecimal("credito"))
                                .build(),
                request.getFechaInicial(),
                request.getFechaFinal(),
                idAgencia,
                idAgencia,
                codigoForma,
                codigoForma,
                codigoMovimiento,
                codigoMovimiento
        );
    }

    public List<Map<String, Object>> listarTiposMovimiento() {

        String sql = """
            SELECT
                TRIM(codigo_movimiento) AS codigoMovimiento,
                descripcion
            FROM depositos.tipo_movimiento
            ORDER BY
                codigo_movimiento
            """;

        return jdbc.queryForList(sql);
    }

}