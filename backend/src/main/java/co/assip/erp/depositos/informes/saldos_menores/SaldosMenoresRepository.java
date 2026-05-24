package co.assip.erp.depositos.informes.saldos_menores;

import co.assip.erp.depositos.informes.saldos_menores.dto.SaldosMenoresItemDTO;
import co.assip.erp.depositos.informes.saldos_menores.dto.SaldosMenoresRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SaldosMenoresRepository {

    private final JdbcTemplate jdbc;

    public List<SaldosMenoresItemDTO> consultar(
            SaldosMenoresRequestDTO request
    ) {

        String sql = """
            WITH hv_unica AS (
                SELECT DISTINCT ON (id_datos_personal)
                    *
                FROM reporting.vw_hoja_vida_general_total_reciente
                ORDER BY
                    id_datos_personal
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

                WHERE (? = 0 OR c.id_agencia = ?)

                GROUP BY
                    c.id_cuenta_ahorro
            )

            SELECT
                c.id_agencia,
                COALESCE(a.nombre_agencia, '') AS nombre_agencia,

                LPAD(f.codigo_forma::text, 2, '0') AS codigo_forma,
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

                c.fecha_apertura_cuenta,
                COALESCE(c.estado_cuenta_cuenta, '') AS estado_cuenta,

                s.saldo_corte

            FROM depositos.cuentas_ahorro c

            INNER JOIN saldos s
                ON s.id_cuenta_ahorro = c.id_cuenta_ahorro

            INNER JOIN depositos.formas_ahorro f
                ON f.id_forma_ahorro = c.id_forma_ahorro

            LEFT JOIN general.datos_agencias a
                ON a.id_agencia = c.id_agencia

            LEFT JOIN hv_unica dp
                ON dp.id_datos_personal = c.id_datos_personal

            WHERE (? = 0 OR c.id_agencia = ?)

              AND (
                    ? = '0'
                    OR LPAD(f.codigo_forma::text, 2, '0') = LPAD(?::text, 2, '0')
                  )

              AND s.saldo_corte <> 0
              AND s.saldo_corte <= ?

            ORDER BY
                LPAD(f.codigo_forma::text, 2, '0'),
                s.saldo_corte,
                nombre_completo,
                c.codigo_cuenta
            """;

        String codigoForma =
                request.getCodigoForma() == null
                        || request.getCodigoForma().isBlank()
                        ? "0"
                        : request.getCodigoForma();

        Integer idAgencia =
                request.getIdAgencia() == null
                        ? 0
                        : request.getIdAgencia();

        return jdbc.query(
                sql,
                (rs, rowNum) ->
                        SaldosMenoresItemDTO.builder()
                                .idAgencia(rs.getInt("id_agencia"))
                                .nombreAgencia(rs.getString("nombre_agencia"))
                                .codigoForma(rs.getString("codigo_forma"))
                                .nombreForma(rs.getString("nombre_forma"))
                                .codigoCuenta(rs.getString("codigo_cuenta"))
                                .documento(rs.getString("documento"))
                                .nombreCompleto(rs.getString("nombre_completo"))
                                .fechaApertura(rs.getObject("fecha_apertura_cuenta", java.time.LocalDate.class))
                                .estadoCuenta(rs.getString("estado_cuenta"))
                                .saldoCorte(rs.getBigDecimal("saldo_corte"))
                                .build(),

                request.getFechaCorte(),

                idAgencia,
                idAgencia,

                idAgencia,
                idAgencia,

                codigoForma,
                codigoForma,

                request.getValorMaximo()
        );
    }

}