package co.assip.erp.depositos.informes.saldos_rangos_edad;

import co.assip.erp.depositos.informes.saldos_rangos_edad.dto.SaldosRangosEdadItemDTO;
import co.assip.erp.depositos.informes.saldos_rangos_edad.dto.SaldosRangosEdadRangoDTO;
import co.assip.erp.depositos.informes.saldos_rangos_edad.dto.SaldosRangosEdadRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class SaldosRangosEdadRepository {

    private final JdbcTemplate jdbc;

    public List<SaldosRangosEdadItemDTO> consultar(
            SaldosRangosEdadRequestDTO request
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

                EXTRACT(
                    YEAR FROM AGE(?::date, dp.fecha_nacimiento)
                )::int AS edad,

                COALESCE(dp.nombre_ocupacion, '') AS ocupacion,
                COALESCE(dp.nombre_sector_economico, '') AS sector_economico,

                COALESCE(dp.valor_salario, 0) AS salario,

                COALESCE(dp.ingresos_arriendo, 0)
                + COALESCE(dp.ingresos_comisiones, 0)
                + COALESCE(dp.otros_ingresos, 0) AS otros_ingresos,

                COALESCE(dp.valor_salario, 0)
                + COALESCE(dp.valor_pension, 0)
                + COALESCE(dp.ingresos_arriendo, 0)
                + COALESCE(dp.ingresos_comisiones, 0)
                + COALESCE(dp.otros_ingresos, 0) AS total_ingresos,

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

            ORDER BY
                edad,
                nombre_completo
            """;

        List<SaldosRangosEdadItemDTO> base =
                jdbc.query(
                        sql,
                        (rs, rowNum) ->
                                SaldosRangosEdadItemDTO.builder()
                                        .idAgencia(rs.getInt("id_agencia"))
                                        .nombreAgencia(rs.getString("nombre_agencia"))
                                        .documento(rs.getString("documento"))
                                        .nombreCompleto(rs.getString("nombre_completo"))
                                        .edad(rs.getObject("edad", Integer.class))
                                        .ocupacion(rs.getString("ocupacion"))
                                        .sectorEconomico(rs.getString("sector_economico"))
                                        .salario(rs.getBigDecimal("salario"))
                                        .otrosIngresos(rs.getBigDecimal("otros_ingresos"))
                                        .totalIngresos(rs.getBigDecimal("total_ingresos"))
                                        .ciudad(rs.getString("ciudad"))
                                        .celular(rs.getString("celular"))
                                        .correo(rs.getString("correo"))
                                        .saldoAportes(rs.getBigDecimal("saldo_aportes"))
                                        .build(),
                        request.getFechaCorte(),
                        request.getIdAgencia(),
                        request.getFechaCorte(),
                        request.getIdAgencia()
                );

        return asignarRangos(
                base,
                request.getRangos()
        );
    }

    private List<SaldosRangosEdadItemDTO> asignarRangos(
            List<SaldosRangosEdadItemDTO> base,
            List<SaldosRangosEdadRangoDTO> rangos
    ) {
        List<SaldosRangosEdadItemDTO> resultado =
                new ArrayList<>();

        for (SaldosRangosEdadItemDTO item : base) {

            if (item.getEdad() == null) {
                continue;
            }

            SaldosRangosEdadRangoDTO rangoEncontrado =
                    rangos.stream()
                            .filter(r ->
                                    item.getEdad() >= r.getEdadInicial()
                                            && item.getEdad() <= r.getEdadFinal()
                            )
                            .findFirst()
                            .orElse(null);

            if (rangoEncontrado == null) {
                continue;
            }

            resultado.add(
                    SaldosRangosEdadItemDTO.builder()
                            .idAgencia(item.getIdAgencia())
                            .nombreAgencia(item.getNombreAgencia())
                            .documento(item.getDocumento())
                            .nombreCompleto(item.getNombreCompleto())
                            .edad(item.getEdad())
                            .nombreRango(rangoEncontrado.getNombreRango())
                            .ocupacion(item.getOcupacion())
                            .sectorEconomico(item.getSectorEconomico())
                            .salario(
                                    item.getSalario() == null
                                            ? BigDecimal.ZERO
                                            : item.getSalario()
                            )
                            .otrosIngresos(
                                    item.getOtrosIngresos() == null
                                            ? BigDecimal.ZERO
                                            : item.getOtrosIngresos()
                            )
                            .totalIngresos(
                                    item.getTotalIngresos() == null
                                            ? BigDecimal.ZERO
                                            : item.getTotalIngresos()
                            )
                            .ciudad(item.getCiudad())
                            .celular(item.getCelular())
                            .correo(item.getCorreo())
                            .saldoAportes(
                                    item.getSaldoAportes() == null
                                            ? BigDecimal.ZERO
                                            : item.getSaldoAportes()
                            )
                            .build()
            );
        }

        return resultado;
    }

}