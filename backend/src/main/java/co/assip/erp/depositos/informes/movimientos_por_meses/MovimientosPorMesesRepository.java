package co.assip.erp.depositos.informes.movimientos_por_meses;

import co.assip.erp.depositos.informes.movimientos_por_meses.dto.MovimientosPorMesesAsociadoDTO;
import co.assip.erp.depositos.informes.movimientos_por_meses.dto.MovimientosPorMesesRequestDTO;
import co.assip.erp.depositos.informes.movimientos_por_meses.dto.MovimientosPorMesesResumenDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MovimientosPorMesesRepository {

    private final JdbcTemplate jdbc;

    public List<MovimientosPorMesesResumenDTO> resumenMensual(
            MovimientosPorMesesRequestDTO request
    ) {

        String sql = """
            SELECT
                TO_CHAR(
                    DATE_TRUNC('month', e.fecha_movimiento),
                    'YYYY-MM'
                ) AS mes,

                COUNT(*) AS cantidad_movimientos,

                COALESCE(SUM(e.valor_credito), 0) AS entradas,
                COALESCE(SUM(e.valor_debito), 0) AS salidas

            FROM depositos.extractos_cuentas_ahorros e

            INNER JOIN depositos.cuentas_ahorro c
                ON c.id_cuenta_ahorro = e.id_cuenta_ahorro

            INNER JOIN depositos.formas_ahorro f
                ON f.id_forma_ahorro = c.id_forma_ahorro

            WHERE e.fecha_movimiento >= ?
              AND e.fecha_movimiento <= ?

              AND (
                    ? = 0
                    OR c.id_agencia = ?
                  )

              AND (
                    ? = '0'
                    OR f.codigo_forma = ?
                  )

            GROUP BY
                DATE_TRUNC('month', e.fecha_movimiento)

            ORDER BY
                DATE_TRUNC('month', e.fecha_movimiento) DESC
            """;

        LocalDate fechaInicial = calcularFechaInicial(request);
        Integer idAgencia = normalizarAgencia(request);
        String codigoForma = normalizarForma(request);

        return jdbc.query(
                sql,
                (rs, rowNum) ->
                        MovimientosPorMesesResumenDTO.builder()
                                .mes(rs.getString("mes"))
                                .cantidadMovimientos(rs.getInt("cantidad_movimientos"))
                                .entradas(rs.getBigDecimal("entradas"))
                                .salidas(rs.getBigDecimal("salidas"))
                                .build(),
                fechaInicial,
                request.getFechaCorte(),
                idAgencia,
                idAgencia,
                codigoForma,
                codigoForma
        );
    }

    public List<MovimientosPorMesesAsociadoDTO> detallePorAsociado(
            MovimientosPorMesesRequestDTO request
    ) {

        String sql = """
            WITH hv_unica AS (
                SELECT DISTINCT ON (id_datos_personal)
                    *
                FROM reporting.vw_hoja_vida_general_total_reciente
                ORDER BY
                    id_datos_personal
            )

            SELECT
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

                TRIM(c.codigo_cuenta) AS codigo_cuenta,

                f.codigo_forma,
                f.nombre_forma,

                TO_CHAR(
                    DATE_TRUNC('month', e.fecha_movimiento),
                    'YYYY-MM'
                ) AS mes,

                COUNT(*) AS cantidad_movimientos,

                COALESCE(SUM(e.valor_credito), 0) AS entradas,
                COALESCE(SUM(e.valor_debito), 0) AS salidas

            FROM depositos.extractos_cuentas_ahorros e

            INNER JOIN depositos.cuentas_ahorro c
                ON c.id_cuenta_ahorro = e.id_cuenta_ahorro

            INNER JOIN depositos.formas_ahorro f
                ON f.id_forma_ahorro = c.id_forma_ahorro

            LEFT JOIN hv_unica dp
                ON dp.id_datos_personal = c.id_datos_personal

            WHERE e.fecha_movimiento >= ?
              AND e.fecha_movimiento <= ?

              AND (
                    ? = 0
                    OR c.id_agencia = ?
                  )

              AND (
                    ? = '0'
                    OR f.codigo_forma = ?
                  )

            GROUP BY
                dp.documento,
                dp.tipo_persona,
                dp.nombres,
                dp.primer_apellido,
                dp.segundo_apellido,
                c.codigo_cuenta,
                f.codigo_forma,
                f.nombre_forma,
                DATE_TRUNC('month', e.fecha_movimiento)

            ORDER BY
                nombre_completo,
                c.codigo_cuenta,
                DATE_TRUNC('month', e.fecha_movimiento) DESC
            """;

        LocalDate fechaInicial = calcularFechaInicial(request);
        Integer idAgencia = normalizarAgencia(request);
        String codigoForma = normalizarForma(request);

        return jdbc.query(
                sql,
                (rs, rowNum) ->
                        MovimientosPorMesesAsociadoDTO.builder()
                                .documento(rs.getString("documento"))
                                .nombreCompleto(rs.getString("nombre_completo"))
                                .codigoCuenta(rs.getString("codigo_cuenta"))
                                .codigoForma(rs.getString("codigo_forma"))
                                .nombreForma(rs.getString("nombre_forma"))
                                .mes(rs.getString("mes"))
                                .cantidadMovimientos(rs.getInt("cantidad_movimientos"))
                                .entradas(rs.getBigDecimal("entradas"))
                                .salidas(rs.getBigDecimal("salidas"))
                                .build(),
                fechaInicial,
                request.getFechaCorte(),
                idAgencia,
                idAgencia,
                codigoForma,
                codigoForma
        );
    }

    private LocalDate calcularFechaInicial(
            MovimientosPorMesesRequestDTO request
    ) {
        return request.getFechaCorte()
                .withDayOfMonth(1)
                .minusMonths(request.getMeses() - 1L);
    }

    private Integer normalizarAgencia(
            MovimientosPorMesesRequestDTO request
    ) {
        return request.getIdAgencia() == null
                ? 0
                : request.getIdAgencia();
    }

    private String normalizarForma(
            MovimientosPorMesesRequestDTO request
    ) {
        return request.getCodigoForma() == null
                || request.getCodigoForma().isBlank()
                ? "0"
                : request.getCodigoForma();
    }

}