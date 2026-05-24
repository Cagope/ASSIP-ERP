package co.assip.erp.depositos.informes.entradas_salidas;

import co.assip.erp.depositos.informes.entradas_salidas.dto.EntradasSalidasItemDTO;
import co.assip.erp.depositos.informes.entradas_salidas.dto.EntradasSalidasRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class EntradasSalidasRepository {

    private final JdbcTemplate jdbc;

    public List<EntradasSalidasItemDTO> consultar(
            EntradasSalidasRequestDTO request
    ) {

        String sql = """
            SELECT
                e.fecha_movimiento::date AS fecha_movimiento,

                COUNT(*) AS cantidad_movimientos,

                COALESCE(SUM(e.valor_credito), 0) AS entradas,
                COALESCE(SUM(e.valor_debito), 0) AS salidas,

                COALESCE(SUM(e.valor_credito), 0)
                - COALESCE(SUM(e.valor_debito), 0) AS neto

            FROM depositos.extractos_cuentas_ahorros e

            INNER JOIN depositos.cuentas_ahorro c
                ON c.id_cuenta_ahorro = e.id_cuenta_ahorro

            INNER JOIN depositos.formas_ahorro f
                ON f.id_forma_ahorro = c.id_forma_ahorro

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
                e.fecha_movimiento::date

            ORDER BY
                e.fecha_movimiento::date
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
                        EntradasSalidasItemDTO.builder()
                                .fechaMovimiento(rs.getObject("fecha_movimiento", java.time.LocalDate.class))
                                .cantidadMovimientos(rs.getInt("cantidad_movimientos"))
                                .entradas(rs.getBigDecimal("entradas"))
                                .salidas(rs.getBigDecimal("salidas"))
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

    public List<EntradasSalidasItemDTO> resumenPorForma(
            EntradasSalidasRequestDTO request
    ) {

        String sql = """
        SELECT
            NULL::date AS fecha_movimiento,
            f.codigo_forma,
            f.nombre_forma,

            COUNT(*) AS cantidad_movimientos,

            COALESCE(SUM(e.valor_credito), 0) AS entradas,
            COALESCE(SUM(e.valor_debito), 0) AS salidas,

            COALESCE(SUM(e.valor_credito), 0)
            - COALESCE(SUM(e.valor_debito), 0) AS neto

        FROM depositos.extractos_cuentas_ahorros e

        INNER JOIN depositos.cuentas_ahorro c
            ON c.id_cuenta_ahorro = e.id_cuenta_ahorro

        INNER JOIN depositos.formas_ahorro f
            ON f.id_forma_ahorro = c.id_forma_ahorro

        WHERE e.fecha_movimiento::date >= ?::date
          AND e.fecha_movimiento::date <= ?::date

          AND (
                ? = 0
                OR c.id_agencia = ?
              )

        GROUP BY
            f.codigo_forma,
            f.nombre_forma

        ORDER BY
            f.codigo_forma
        """;

        Integer idAgencia =
                request.getIdAgencia() == null
                        ? 0
                        : request.getIdAgencia();

        return jdbc.query(
                sql,
                (rs, rowNum) ->
                        EntradasSalidasItemDTO.builder()
                                .codigoForma(rs.getString("codigo_forma"))
                                .nombreForma(rs.getString("nombre_forma"))
                                .cantidadMovimientos(rs.getInt("cantidad_movimientos"))
                                .entradas(rs.getBigDecimal("entradas"))
                                .salidas(rs.getBigDecimal("salidas"))
                                .neto(rs.getBigDecimal("neto"))
                                .build(),
                request.getFechaInicial(),
                request.getFechaFinal(),
                idAgencia,
                idAgencia
        );
    }

    public List<EntradasSalidasItemDTO> detalleMovimientos(
            EntradasSalidasRequestDTO request
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
            e.fecha_movimiento::date AS fecha_movimiento,

            TRIM(c.codigo_cuenta) AS codigo_cuenta,

            dp.documento,

            CASE
                WHEN dp.tipo_persona = '2'
                    THEN dp.nombres

                ELSE TRIM(
                    CONCAT(
                        COALESCE(dp.primer_apellido, ''),
                        ' ',
                        COALESCE(dp.segundo_apellido, ''),
                        ' ',
                        COALESCE(dp.nombres, '')
                    )
                )
            END AS nombre_completo,

            f.codigo_forma,
            f.nombre_forma,

            tm.codigo_movimiento,
            tm.descripcion AS nombre_movimiento,

            CAST(e.id_extracto_cuenta_ahorro AS VARCHAR)
                AS numero_movimiento,

            COALESCE(e.valor_debito, 0) AS debito,
            COALESCE(e.valor_credito, 0) AS credito

        FROM depositos.extractos_cuentas_ahorros e

        INNER JOIN depositos.cuentas_ahorro c
            ON c.id_cuenta_ahorro = e.id_cuenta_ahorro

        INNER JOIN depositos.formas_ahorro f
            ON f.id_forma_ahorro = c.id_forma_ahorro

        LEFT JOIN hv_unica dp
            ON dp.id_datos_personal = c.id_datos_personal

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

        ORDER BY
            e.fecha_movimiento,
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

        return jdbc.query(
                sql,
                (rs, rowNum) ->
                        EntradasSalidasItemDTO.builder()
                                .fechaMovimiento(rs.getObject("fecha_movimiento", java.time.LocalDate.class))
                                .codigoCuenta(rs.getString("codigo_cuenta"))
                                .documento(rs.getString("documento"))
                                .nombreCompleto(rs.getString("nombre_completo"))
                                .codigoForma(rs.getString("codigo_forma"))
                                .nombreForma(rs.getString("nombre_forma"))
                                .codigoMovimiento(rs.getString("codigo_movimiento"))
                                .nombreMovimiento(rs.getString("nombre_movimiento"))
                                .numeroMovimiento(rs.getString("numero_movimiento"))
                                .debito(rs.getBigDecimal("debito"))
                                .credito(rs.getBigDecimal("credito"))
                                .build(),
                request.getFechaInicial(),
                request.getFechaFinal(),
                idAgencia,
                idAgencia,
                codigoForma,
                codigoForma
        );
    }

}