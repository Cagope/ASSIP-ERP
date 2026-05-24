package co.assip.erp.depositos.informes.cumpleanios_asociados;

import co.assip.erp.depositos.informes.cumpleanios_asociados.dto.CumpleaniosAsociadosItemDTO;
import co.assip.erp.depositos.informes.cumpleanios_asociados.dto.CumpleaniosAsociadosRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CumpleaniosAsociadosRepository {

    private final JdbcTemplate jdbc;

    public List<CumpleaniosAsociadosItemDTO> consultar(
            CumpleaniosAsociadosRequestDTO request
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

                WHERE c.id_agencia = ?
                  AND f.codigo_forma = '01'

                GROUP BY
                    c.id_cuenta_ahorro

                HAVING
                    COALESCE(SUM(e.valor_credito), 0)
                    - COALESCE(SUM(e.valor_debito), 0) > 0
            )

            SELECT
                c.id_agencia,

                COALESCE(a.nombre_agencia, '') AS nombre_agencia,

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

                dp.fecha_nacimiento,

                EXTRACT(
                    DAY FROM dp.fecha_nacimiento
                )::int AS dia_cumpleanios,

                EXTRACT(
                    YEAR FROM AGE(CURRENT_DATE, dp.fecha_nacimiento)
                )::int AS edad,

                COALESCE(dp.ciudad_residencia, '') AS ciudad,

                COALESCE(dp.celular_uno, '') AS celular,

                COALESCE(dp.correo_personal, '') AS correo,

                s.saldo_aportes

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
              AND dp.fecha_nacimiento IS NOT NULL

              AND TO_CHAR(dp.fecha_nacimiento, 'MMDD')
                  BETWEEN TO_CHAR(?::date, 'MMDD')
                      AND TO_CHAR(?::date, 'MMDD')

            ORDER BY
                TO_CHAR(dp.fecha_nacimiento, 'MMDD'),
                nombre_completo
            """;

        return jdbc.query(
                sql,
                (rs, rowNum) ->
                        CumpleaniosAsociadosItemDTO.builder()
                                .idAgencia(rs.getInt("id_agencia"))
                                .nombreAgencia(rs.getString("nombre_agencia"))
                                .documento(rs.getString("documento"))
                                .nombreCompleto(rs.getString("nombre_completo"))
                                .fechaNacimiento(rs.getObject("fecha_nacimiento", java.time.LocalDate.class))
                                .diaCumpleanios(rs.getObject("dia_cumpleanios", Integer.class))
                                .edad(rs.getObject("edad", Integer.class))
                                .ciudad(rs.getString("ciudad"))
                                .celular(rs.getString("celular"))
                                .correo(rs.getString("correo"))
                                .saldoAportes(rs.getBigDecimal("saldo_aportes"))
                                .build(),
                request.getIdAgencia(),
                request.getIdAgencia(),
                request.getFechaInicial(),
                request.getFechaFinal()
        );
    }

}