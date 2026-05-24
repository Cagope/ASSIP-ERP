package co.assip.erp.depositos.informes.extracto_por_valor;

import co.assip.erp.depositos.informes.extracto_por_valor.dto.ExtractoPorValorItemDTO;
import co.assip.erp.depositos.informes.extracto_por_valor.dto.ExtractoPorValorRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ExtractoPorValorRepository {

    private final JdbcTemplate jdbc;

    public List<ExtractoPorValorItemDTO> consultar(
            ExtractoPorValorRequestDTO request
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

                COALESCE(tm.descripcion, 'SIN DESCRIPCIÓN')
                    AS nombre_movimiento,

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
                    (
                        ? = 'D'
                        AND COALESCE(e.valor_debito, 0) = ?
                    )

                    OR

                    (
                        ? = 'C'
                        AND COALESCE(e.valor_credito, 0) = ?
                    )

                    OR

                    (
                        ? = 'A'
                        AND (
                            COALESCE(e.valor_debito, 0) = ?
                            OR
                            COALESCE(e.valor_credito, 0) = ?
                        )
                    )
                  )

            ORDER BY
                e.fecha_movimiento,
                e.hora_movimiento,
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

        String tipoBusqueda =
                request.getTipoBusqueda() == null
                        || request.getTipoBusqueda().isBlank()
                        ? "A"
                        : request.getTipoBusqueda();

        return jdbc.query(
                sql,
                (rs, rowNum) ->
                        ExtractoPorValorItemDTO.builder()
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

                tipoBusqueda,
                request.getValor(),

                tipoBusqueda,
                request.getValor(),

                tipoBusqueda,
                request.getValor(),
                request.getValor()
        );
    }

}