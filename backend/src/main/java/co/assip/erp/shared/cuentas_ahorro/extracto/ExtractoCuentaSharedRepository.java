package co.assip.erp.shared.cuentas_ahorro.extracto;

import co.assip.erp.shared.cuentas_ahorro.extracto.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ExtractoCuentaSharedRepository {

    private final JdbcTemplate jdbc;

    public ExtractoCuentaSharedResumenDTO obtenerResumen(
            ExtractoCuentaSharedRequestDTO request
    ) {

        BigDecimal saldoInicial =
                obtenerSaldoInicial(
                        request.getIdCuentaAhorro(),
                        request.getFechaInicial()
                );

        String sql = """
            SELECT
                c.id_cuenta_ahorro,
                TRIM(c.codigo_cuenta) AS codigo_cuenta,

                hv.documento,

                CASE
                    WHEN hv.tipo_persona = '2'
                        THEN hv.nombres
                    ELSE TRIM(CONCAT(
                        COALESCE(hv.primer_apellido, ''),
                        ' ',
                        COALESCE(hv.segundo_apellido, ''),
                        ' ',
                        COALESCE(hv.nombres, '')
                    ))
                END AS nombre_completo,

                hv.direccion_residencia,

                f.codigo_forma,
                f.nombre_forma,

                a.nombre_agencia,

                COALESCE(SUM(e.valor_credito), 0) AS total_creditos,
                COALESCE(SUM(e.valor_debito), 0) AS total_debitos

            FROM depositos.cuentas_ahorro c

            INNER JOIN depositos.formas_ahorro f
                ON f.id_forma_ahorro = c.id_forma_ahorro

            INNER JOIN general.datos_agencias a
                ON a.id_agencia = c.id_agencia

            LEFT JOIN reporting.vw_hoja_vida_general_total_reciente hv
                ON hv.id_datos_personal = c.id_datos_personal

            LEFT JOIN depositos.extractos_cuentas_ahorros e
                ON e.id_cuenta_ahorro = c.id_cuenta_ahorro
                AND e.fecha_movimiento >= ?::date
                AND e.fecha_movimiento <= ?::date

            WHERE c.id_cuenta_ahorro = ?

            GROUP BY
                c.id_cuenta_ahorro,
                c.codigo_cuenta,
                hv.documento,
                hv.tipo_persona,
                hv.nombres,
                hv.primer_apellido,
                hv.segundo_apellido,
                hv.direccion_residencia,
                f.codigo_forma,
                f.nombre_forma,
                a.nombre_agencia
            """;

        return jdbc.queryForObject(
                sql,
                (rs, rowNum) -> {

                    BigDecimal totalCreditos =
                            rs.getBigDecimal("total_creditos");

                    BigDecimal totalDebitos =
                            rs.getBigDecimal("total_debitos");

                    return ExtractoCuentaSharedResumenDTO.builder()
                            .idCuentaAhorro(rs.getInt("id_cuenta_ahorro"))
                            .codigoCuenta(rs.getString("codigo_cuenta"))
                            .documento(rs.getString("documento"))
                            .nombreCompleto(rs.getString("nombre_completo"))
                            .direccion(rs.getString("direccion_residencia"))
                            .codigoForma(rs.getString("codigo_forma"))
                            .nombreForma(rs.getString("nombre_forma"))
                            .nombreAgencia(rs.getString("nombre_agencia"))
                            .saldoInicial(saldoInicial)
                            .totalCreditos(totalCreditos)
                            .totalDebitos(totalDebitos)
                            .saldoFinal(
                                    saldoInicial
                                            .add(totalCreditos)
                                            .subtract(totalDebitos)
                            )
                            .build();
                },
                request.getFechaInicial(),
                request.getFechaFinal(),
                request.getIdCuentaAhorro()
        );
    }

    public ExtractoCuentaSharedEstadisticaDTO obtenerEstadisticas(
            ExtractoCuentaSharedRequestDTO request
    ) {

        String sql = """
            SELECT
                COUNT(*) AS cantidad_movimientos,

                COALESCE(AVG(valor_credito), 0) AS promedio_creditos,
                COALESCE(AVG(valor_debito), 0) AS promedio_debitos,

                COALESCE(MAX(valor_credito), 0) AS credito_maximo,
                COALESCE(MAX(valor_debito), 0) AS debito_maximo,

                COALESCE(MIN(valor_credito), 0) AS credito_minimo,
                COALESCE(MIN(valor_debito), 0) AS debito_minimo,

                COALESCE(
                    AVG(
                        ABS(
                            COALESCE(valor_credito,0)
                            -
                            COALESCE(valor_debito,0)
                        )
                    ),
                    0
                ) AS media_movimientos,

                COALESCE(
                    SUM(
                        ABS(
                            COALESCE(valor_credito,0)
                            -
                            COALESCE(valor_debito,0)
                        )
                    ),
                    0
                ) AS valor_movilizado

            FROM depositos.extractos_cuentas_ahorros

            WHERE id_cuenta_ahorro = ?
              AND fecha_movimiento >= ?::date
              AND fecha_movimiento <= ?::date
            """;

        return jdbc.queryForObject(
                sql,
                (rs, rowNum) ->
                        ExtractoCuentaSharedEstadisticaDTO.builder()
                                .cantidadMovimientos(rs.getInt("cantidad_movimientos"))
                                .promedioCreditos(rs.getBigDecimal("promedio_creditos"))
                                .promedioDebitos(rs.getBigDecimal("promedio_debitos"))
                                .creditoMaximo(rs.getBigDecimal("credito_maximo"))
                                .debitoMaximo(rs.getBigDecimal("debito_maximo"))
                                .creditoMinimo(rs.getBigDecimal("credito_minimo"))
                                .debitoMinimo(rs.getBigDecimal("debito_minimo"))
                                .mediaMovimientos(rs.getBigDecimal("media_movimientos"))
                                .medianaMovimientos(BigDecimal.ZERO)
                                .valorMovilizado(rs.getBigDecimal("valor_movilizado"))
                                .build(),
                request.getIdCuentaAhorro(),
                request.getFechaInicial(),
                request.getFechaFinal()
        );
    }

    public List<ExtractoCuentaSharedMovimientoDTO> obtenerMovimientos(
            ExtractoCuentaSharedRequestDTO request,
            BigDecimal saldoInicial
    ) {

        String sql = """
            SELECT
                e.fecha_movimiento,
                e.hora_movimiento,

                e.tipo_movimiento,
                tm.descripcion AS descripcion_movimiento,

                TRIM(COALESCE(e.tipo_comprobante, '')) AS tipo_comprobante,

                LPAD(
                    TRIM(COALESCE(e.numero_comprobante, '')),
                    8,
                    '0'
                ) AS numero_comprobante,

                COALESCE(e.valor_debito, 0) AS debito,
                COALESCE(e.valor_credito, 0) AS credito,

                (
                    ? +
                    SUM(
                        COALESCE(e.valor_credito, 0)
                        -
                        COALESCE(e.valor_debito, 0)
                    ) OVER (
                        ORDER BY
                            e.fecha_movimiento,
                            e.hora_movimiento,
                            e.id_extracto_cuenta_ahorro
                    )
                ) AS saldo

            FROM depositos.extractos_cuentas_ahorros e

            LEFT JOIN depositos.tipo_movimiento tm
                ON tm.codigo_movimiento = e.tipo_movimiento

            WHERE e.id_cuenta_ahorro = ?
              AND e.fecha_movimiento >= ?::date
              AND e.fecha_movimiento <= ?::date

            ORDER BY
                e.fecha_movimiento,
                e.hora_movimiento,
                e.id_extracto_cuenta_ahorro
            """;

        return jdbc.query(
                sql,
                (rs, rowNum) ->
                        ExtractoCuentaSharedMovimientoDTO.builder()
                                .fechaMovimiento(
                                        rs.getDate("fecha_movimiento") != null
                                                ? rs.getDate("fecha_movimiento").toLocalDate()
                                                : null
                                )
                                .horaMovimiento(
                                        rs.getTime("hora_movimiento") != null
                                                ? rs.getTime("hora_movimiento").toLocalTime()
                                                : null
                                )
                                .tipoMovimiento(rs.getString("tipo_movimiento"))
                                .descripcionMovimiento(rs.getString("descripcion_movimiento"))
                                .tipoComprobante(rs.getString("tipo_comprobante"))
                                .numeroComprobante(rs.getString("numero_comprobante"))
                                .debito(rs.getBigDecimal("debito"))
                                .credito(rs.getBigDecimal("credito"))
                                .saldo(rs.getBigDecimal("saldo"))
                                .build(),

                saldoInicial,
                request.getIdCuentaAhorro(),
                request.getFechaInicial(),
                request.getFechaFinal()
        );
    }

    private BigDecimal obtenerSaldoInicial(
            Integer idCuenta,
            String fechaInicial
    ) {

        String sql = """
        SELECT
            COALESCE(c.saldo_inicial_cuenta, 0)
            +
            COALESCE((
                SELECT
                    SUM(COALESCE(e.valor_credito, 0))
                    -
                    SUM(COALESCE(e.valor_debito, 0))
                FROM depositos.extractos_cuentas_ahorros e
                WHERE e.id_cuenta_ahorro = c.id_cuenta_ahorro
                  AND e.fecha_movimiento < ?::date
            ), 0) AS saldo

        FROM depositos.cuentas_ahorro c

        WHERE c.id_cuenta_ahorro = ?
        """;

        BigDecimal saldo = jdbc.query(
                sql,
                rs -> rs.next()
                        ? rs.getBigDecimal("saldo")
                        : BigDecimal.ZERO,
                fechaInicial,
                idCuenta
        );

        return saldo != null
                ? saldo
                : BigDecimal.ZERO;
    }
}