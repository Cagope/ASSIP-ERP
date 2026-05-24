package co.assip.erp.depositos.informes.intereses_retencion;

import co.assip.erp.depositos.informes.intereses_retencion.dto.InteresesRetencionItemDTO;
import co.assip.erp.depositos.informes.intereses_retencion.dto.InteresesRetencionRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class InteresesRetencionRepository {

    private final JdbcTemplate jdbc;

    public List<InteresesRetencionItemDTO> consultar(
            InteresesRetencionRequestDTO request
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

                COALESCE(SUM(
                    CASE
                        WHEN e.tipo_movimiento IN ('INT', 'INTM', 'INTD')
                            THEN COALESCE(e.valor_credito, 0)
                        ELSE 0
                    END
                ), 0) AS intereses,

                COALESCE(SUM(
                    CASE
                        WHEN e.tipo_movimiento IN ('RTF', 'RET')
                            THEN COALESCE(e.valor_debito, 0)
                        ELSE 0
                    END
                ), 0) AS retencion

            FROM depositos.extractos_cuentas_ahorros e

            INNER JOIN depositos.cuentas_ahorro c
                ON c.id_cuenta_ahorro = e.id_cuenta_ahorro

            INNER JOIN general.datos_agencias a
                ON a.id_agencia = c.id_agencia

            INNER JOIN depositos.formas_ahorro f
                ON f.id_forma_ahorro = c.id_forma_ahorro

            LEFT JOIN hv_unica hv
                ON hv.id_datos_personal = c.id_datos_personal

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
                    ? = ''
                    OR TRIM(c.codigo_cuenta) = ?
                  )

            GROUP BY
                a.codigo_agencia,
                a.nombre_agencia,
                f.codigo_forma,
                f.nombre_forma,
                c.codigo_cuenta,
                hv.documento,
                hv.tipo_persona,
                hv.nombres,
                hv.primer_apellido,
                hv.segundo_apellido

            HAVING
                COALESCE(SUM(
                    CASE
                        WHEN e.tipo_movimiento = '005'
                            THEN COALESCE(e.valor_credito, 0)
                        ELSE 0
                    END
                ), 0) <> 0

                OR

                COALESCE(SUM(
                    CASE
                        WHEN e.tipo_movimiento = '556'
                            THEN COALESCE(e.valor_debito, 0)
                        ELSE 0
                    END
                ), 0) <> 0

            ORDER BY
                a.codigo_agencia,
                f.codigo_forma,
                c.codigo_cuenta
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

        String codigoCuenta =
                request.getCodigoCuenta() == null
                        ? ""
                        : request.getCodigoCuenta().trim();

        return jdbc.query(
                sql,
                (rs, rowNum) -> {

                    var intereses =
                            rs.getBigDecimal("intereses");

                    var retencion =
                            rs.getBigDecimal("retencion");

                    return InteresesRetencionItemDTO.builder()
                            .codigoAgencia(rs.getString("codigo_agencia"))
                            .nombreAgencia(rs.getString("nombre_agencia"))
                            .codigoForma(rs.getString("codigo_forma"))
                            .nombreForma(rs.getString("nombre_forma"))
                            .codigoCuenta(rs.getString("codigo_cuenta"))
                            .documento(rs.getString("documento"))
                            .nombreCompleto(rs.getString("nombre_completo"))
                            .intereses(intereses)
                            .retencion(retencion)
                            .neto(intereses.subtract(retencion))
                            .build();
                },

                request.getFechaInicial(),
                request.getFechaFinal(),

                idAgencia,
                idAgencia,

                codigoForma,
                codigoForma,

                codigoCuenta,
                codigoCuenta
        );
    }

}