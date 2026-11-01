
package co.assip.erp.contabilidad.informes.especiales.libro_diario_resumido;

import co.assip.erp.contabilidad.informes.especiales.libro_diario_resumido.dto.LibroDiarioResumidoFilaDTO;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Consultas del informe Libro diario resumido.
 *
 * Agrupa movimientos contables activos por fecha y cuenta
 * o por fecha y tipo de comprobante, según la modalidad.
 */
@Repository
public class LibroDiarioResumidoRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public LibroDiarioResumidoRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = jdbc;
    }

    /**
     * Convierte cada resultado SQL en una fila del informe.
     * Conserva la precisión de los valores contables.
     */
    private static final RowMapper<LibroDiarioResumidoFilaDTO> FILA_MAPPER =
            (rs, rowNum) -> new LibroDiarioResumidoFilaDTO(
                    rs.getDate("fecha").toLocalDate(),
                    rs.getString("codigo_agrupacion"),
                    rs.getString("nombre_agrupacion"),
                    rs.getBigDecimal("total_debitos"),
                    rs.getBigDecimal("total_creditos")
            );

    /**
     * Obtiene los movimientos activos agrupados por fecha y cuenta.
     * Restringe la consulta a la agencia y período solicitados.
     */
    public List<LibroDiarioResumidoFilaDTO> consultarPorCuenta(
            Integer idAgencia,
            LocalDate fechaInicial,
            LocalDate fechaFinal
    ) {
        String sql = """
                SELECT
                    a.fecha_auxiliar AS fecha,
                    c.codigo_cuenta AS codigo_agrupacion,
                    c.nombre_cuenta AS nombre_agrupacion,
                    COALESCE(SUM(a.valor_debito), 0) AS total_debitos,
                    COALESCE(SUM(a.valor_credito), 0) AS total_creditos
                FROM contabilidad.auxiliares_contables a
                INNER JOIN contabilidad.catalogo_cuentas c
                    ON c.id_catalogo_cuenta = a.id_catalogo_cuenta
                   AND c.id_agencia = a.id_agencia
                WHERE a.id_agencia = :idAgencia
                  AND a.fecha_auxiliar BETWEEN :fechaInicial AND :fechaFinal
                  AND a.estado_movimiento = 'A'
                GROUP BY
                    a.fecha_auxiliar,
                    c.codigo_cuenta,
                    c.nombre_cuenta
                ORDER BY
                    a.fecha_auxiliar,
                    c.codigo_cuenta
                """;

        return jdbc.query(
                sql,
                parametros(idAgencia, fechaInicial, fechaFinal),
                FILA_MAPPER
        );
    }

    /**
     * Obtiene los movimientos activos agrupados por fecha y tipo
     * de comprobante, respetando la agencia y período solicitados.
     */
    public List<LibroDiarioResumidoFilaDTO> consultarPorTipoComprobante(
            Integer idAgencia,
            LocalDate fechaInicial,
            LocalDate fechaFinal
    ) {
        String sql = """
                SELECT
                    a.fecha_auxiliar AS fecha,
                    TRIM(a.tipo_comprobante) AS codigo_agrupacion,
                    COALESCE(
                        t.nombre_tipo_comprobante,
                        TRIM(a.tipo_comprobante)
                    ) AS nombre_agrupacion,
                    COALESCE(SUM(a.valor_debito), 0) AS total_debitos,
                    COALESCE(SUM(a.valor_credito), 0) AS total_creditos
                FROM contabilidad.auxiliares_contables a
                LEFT JOIN contabilidad.tipos_comprobantes t
                    ON t.tipo_comprobante = a.tipo_comprobante
                   AND t.id_agencia = a.id_agencia
                WHERE a.id_agencia = :idAgencia
                  AND a.fecha_auxiliar BETWEEN :fechaInicial AND :fechaFinal
                  AND a.estado_movimiento = 'A'
                GROUP BY
                    a.fecha_auxiliar,
                    a.tipo_comprobante,
                    t.nombre_tipo_comprobante
                ORDER BY
                    a.fecha_auxiliar,
                    a.tipo_comprobante
                """;

        return jdbc.query(
                sql,
                parametros(idAgencia, fechaInicial, fechaFinal),
                FILA_MAPPER
        );
    }

    /**
     * Construye los parámetros comunes de ambas consultas.
     * Evita concatenar valores de usuario dentro del SQL.
     */
    private MapSqlParameterSource parametros(
            Integer idAgencia,
            LocalDate fechaInicial,
            LocalDate fechaFinal
    ) {
        return new MapSqlParameterSource()
                .addValue("idAgencia", idAgencia)
                .addValue("fechaInicial", fechaInicial)
                .addValue("fechaFinal", fechaFinal);
    }
}
