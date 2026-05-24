package co.assip.erp.depositos.informes.promedios;

import co.assip.erp.depositos.informes.promedios.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PromediosRepository {

    private final JdbcTemplate jdbc;

    public List<PromediosItemDTO> consultarSaldos(
            PromediosRequestDTO request
    ) {

        String sql = """
            WITH hv_unica AS (
                SELECT DISTINCT ON (id_datos_personal)
                    *
                FROM reporting.vw_hoja_vida_general_total_reciente
                ORDER BY id_datos_personal
            ),

            saldos AS (
                SELECT
                    c.id_cuenta_ahorro,

                    COALESCE(SUM(e.valor_credito), 0)
                    - COALESCE(SUM(e.valor_debito), 0) AS saldo_corte

                FROM depositos.cuentas_ahorro c

                LEFT JOIN depositos.extractos_cuentas_ahorros e
                    ON e.id_cuenta_ahorro = c.id_cuenta_ahorro
                   AND e.fecha_movimiento <= ?

                WHERE c.id_agencia = ?

                GROUP BY c.id_cuenta_ahorro
            )

            SELECT
                f.codigo_forma,
                f.nombre_forma,

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

                s.saldo_corte

            FROM depositos.cuentas_ahorro c

            INNER JOIN saldos s
                ON s.id_cuenta_ahorro = c.id_cuenta_ahorro

            INNER JOIN depositos.formas_ahorro f
                ON f.id_forma_ahorro = c.id_forma_ahorro

            LEFT JOIN hv_unica dp
                ON dp.id_datos_personal = c.id_datos_personal

            WHERE c.id_agencia = ?

              AND (
                    ? = '0'
                    OR f.codigo_forma = ?
                  )

            ORDER BY
                s.saldo_corte DESC,
                nombre_completo
            """;

        String codigoForma =
                request.getCodigoForma() == null
                        || request.getCodigoForma().isBlank()
                        ? "0"
                        : request.getCodigoForma();

        return jdbc.query(
                sql,
                (rs, rowNum) ->
                        PromediosItemDTO.builder()
                                .codigoForma(rs.getString("codigo_forma"))
                                .nombreForma(rs.getString("nombre_forma"))
                                .codigoCuenta(rs.getString("codigo_cuenta"))
                                .documento(rs.getString("documento"))
                                .nombreCompleto(rs.getString("nombre_completo"))
                                .saldoCorte(rs.getBigDecimal("saldo_corte"))
                                .build(),
                request.getFechaCorte(),
                request.getIdAgencia(),
                request.getIdAgencia(),
                codigoForma,
                codigoForma
        );
    }

    public List<PromediosFormaDTO> resumenPorForma(
            PromediosRequestDTO request
    ) {

        String sql = """
            WITH saldos AS (
                SELECT
                    c.id_cuenta_ahorro,
                    c.id_forma_ahorro,

                    COALESCE(SUM(e.valor_credito), 0)
                    - COALESCE(SUM(e.valor_debito), 0) AS saldo_corte

                FROM depositos.cuentas_ahorro c

                LEFT JOIN depositos.extractos_cuentas_ahorros e
                    ON e.id_cuenta_ahorro = c.id_cuenta_ahorro
                   AND e.fecha_movimiento <= ?

                WHERE c.id_agencia = ?

                GROUP BY
                    c.id_cuenta_ahorro,
                    c.id_forma_ahorro
            )

            SELECT
                f.codigo_forma,
                f.nombre_forma,

                COUNT(*) AS total_cuentas,

                COALESCE(SUM(s.saldo_corte), 0) AS total_saldos,

                COALESCE(AVG(s.saldo_corte), 0) AS saldo_promedio,

                COALESCE(MAX(s.saldo_corte), 0) AS saldo_mayor,

                COALESCE(MIN(s.saldo_corte), 0) AS saldo_menor

            FROM saldos s

            INNER JOIN depositos.formas_ahorro f
                ON f.id_forma_ahorro = s.id_forma_ahorro

            WHERE (
                    ? = '0'
                    OR f.codigo_forma = ?
                  )

            GROUP BY
                f.codigo_forma,
                f.nombre_forma

            ORDER BY
                total_saldos DESC
            """;

        String codigoForma =
                request.getCodigoForma() == null
                        || request.getCodigoForma().isBlank()
                        ? "0"
                        : request.getCodigoForma();

        return jdbc.query(
                sql,
                (rs, rowNum) ->
                        PromediosFormaDTO.builder()
                                .codigoForma(rs.getString("codigo_forma"))
                                .nombreForma(rs.getString("nombre_forma"))
                                .totalCuentas(rs.getInt("total_cuentas"))
                                .totalSaldos(rs.getBigDecimal("total_saldos"))
                                .saldoPromedio(rs.getBigDecimal("saldo_promedio"))
                                .saldoMayor(rs.getBigDecimal("saldo_mayor"))
                                .saldoMenor(rs.getBigDecimal("saldo_menor"))
                                .build(),
                request.getFechaCorte(),
                request.getIdAgencia(),
                codigoForma,
                codigoForma
        );
    }

}