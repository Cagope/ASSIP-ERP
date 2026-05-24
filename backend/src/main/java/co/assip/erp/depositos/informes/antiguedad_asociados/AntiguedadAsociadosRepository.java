package co.assip.erp.depositos.informes.antiguedad_asociados;

import co.assip.erp.depositos.informes.antiguedad_asociados.dto.AntiguedadAsociadosItemDTO;
import co.assip.erp.depositos.informes.antiguedad_asociados.dto.AntiguedadAsociadosRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AntiguedadAsociadosRepository {

    private final JdbcTemplate jdbc;

    public List<AntiguedadAsociadosItemDTO> consultar(
            AntiguedadAsociadosRequestDTO request
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
                    - COALESCE(SUM(e.valor_debito), 0) AS saldo_aportes

                FROM depositos.cuentas_ahorro c

                INNER JOIN depositos.formas_ahorro f
                    ON f.id_forma_ahorro = c.id_forma_ahorro

                LEFT JOIN depositos.extractos_cuentas_ahorros e
                    ON e.id_cuenta_ahorro = c.id_cuenta_ahorro
                   AND e.fecha_movimiento <= ?

                WHERE c.id_agencia = ?
                  AND f.codigo_forma = '01'

                GROUP BY
                    c.id_cuenta_ahorro
            )

            SELECT
                c.id_agencia,
                COALESCE(a.nombre_agencia, '') AS nombre_agencia,

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

                c.fecha_apertura_cuenta AS fecha_vinculacion,

                EXTRACT(
                    YEAR FROM AGE(?::date, c.fecha_apertura_cuenta)
                )::int AS anios_asociado,

                EXTRACT(
                    YEAR FROM AGE(?::date, dp.fecha_nacimiento)
                )::int AS edad,

                s.saldo_aportes,

                COALESCE(dp.ciudad_residencia, '') AS ciudad,
                COALESCE(dp.departamento_residencia, '') AS departamento,
                COALESCE(dp.celular_uno, '') AS celular,
                COALESCE(dp.correo_personal, '') AS correo,

                CASE
                    WHEN EXTRACT(YEAR FROM AGE(?::date, c.fecha_apertura_cuenta)) < 1
                        THEN '0-1 años'

                    WHEN EXTRACT(YEAR FROM AGE(?::date, c.fecha_apertura_cuenta)) < 3
                        THEN '1-3 años'

                    WHEN EXTRACT(YEAR FROM AGE(?::date, c.fecha_apertura_cuenta)) < 5
                        THEN '3-5 años'

                    WHEN EXTRACT(YEAR FROM AGE(?::date, c.fecha_apertura_cuenta)) < 10
                        THEN '5-10 años'

                    WHEN EXTRACT(YEAR FROM AGE(?::date, c.fecha_apertura_cuenta)) < 15
                        THEN '10-15 años'

                    WHEN EXTRACT(YEAR FROM AGE(?::date, c.fecha_apertura_cuenta)) < 20
                        THEN '15-20 años'

                    ELSE '20+ años'
                END AS rango_antiguedad

            FROM depositos.cuentas_ahorro c

            INNER JOIN saldos s
                ON s.id_cuenta_ahorro = c.id_cuenta_ahorro

            INNER JOIN depositos.formas_ahorro f
                ON f.id_forma_ahorro = c.id_forma_ahorro

            LEFT JOIN general.datos_agencias a
                ON a.id_agencia = c.id_agencia

            LEFT JOIN hv_unica dp
                ON dp.id_datos_personal = c.id_datos_personal

            WHERE c.id_agencia = ?
              AND f.codigo_forma = '01'
              AND s.saldo_aportes > 0

            ORDER BY
                anios_asociado DESC,
                nombre_completo,
                c.codigo_cuenta
            """;

        return jdbc.query(
                sql,
                (rs, rowNum) ->
                        AntiguedadAsociadosItemDTO.builder()
                                .idAgencia(rs.getInt("id_agencia"))
                                .nombreAgencia(rs.getString("nombre_agencia"))
                                .codigoCuenta(rs.getString("codigo_cuenta"))
                                .documento(rs.getString("documento"))
                                .nombreCompleto(rs.getString("nombre_completo"))
                                .fechaVinculacion(rs.getObject("fecha_vinculacion", java.time.LocalDate.class))
                                .aniosAsociado(rs.getObject("anios_asociado", Integer.class))
                                .edad(rs.getObject("edad", Integer.class))
                                .saldoAportes(rs.getBigDecimal("saldo_aportes"))
                                .ciudad(rs.getString("ciudad"))
                                .departamento(rs.getString("departamento"))
                                .celular(rs.getString("celular"))
                                .correo(rs.getString("correo"))
                                .rangoAntiguedad(rs.getString("rango_antiguedad"))
                                .build(),
                request.getFechaCorte(),
                request.getIdAgencia(),
                request.getFechaCorte(),
                request.getFechaCorte(),
                request.getFechaCorte(),
                request.getFechaCorte(),
                request.getFechaCorte(),
                request.getFechaCorte(),
                request.getFechaCorte(),
                request.getFechaCorte(),
                request.getIdAgencia()
        );
    }

}