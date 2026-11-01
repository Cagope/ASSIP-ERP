package co.assip.erp.cartera.informes.reversiones;

import co.assip.erp.cartera.informes.reversiones.dto.InformeReversionDTO;
import co.assip.erp.cartera.informes.reversiones.dto.InformeReversionFiltroDTO;
import co.assip.erp.cartera.informes.reversiones.dto.InformeReversionAsociadoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.OffsetDateTime;
import java.util.List;

/** Consulta histórica de pagos reversados; no altera información financiera. */
@Repository
@RequiredArgsConstructor
public class InformeReversionRepository {
    private final NamedParameterJdbcTemplate jdbc;

    private static final String SQL = """
        SELECT p.id_pago, c.id_agencia AS id_agencia_credito,
               c.codigo_agencia, c.nombre_agencia, p.id_agencia_receptora,
               ar.codigo_agencia AS codigo_agencia_receptora,
               ar.nombre_agencia AS nombre_agencia_receptora,
               lc.codigo_linea_credito, lc.nombre_linea_credito,
               c.pagare_cartera AS pagare, dp.documento,
               TRIM(CONCAT_WS(' ', dp.nombres, dp.primer_apellido, dp.segundo_apellido)) AS asociado,
               p.fecha_pago AS fecha_pago_original,
               (p.liquidacion #>> '{reversion,fechaReversion}')::date AS fecha_reversion,
               (p.liquidacion #>> '{reversion,fechaRegistro}')::timestamptz AS fecha_registro_reversion,
               p.liquidacion #>> '{reversion,motivo}' AS motivo,
               u.username AS usuario, u.nombre_completo AS funcionario,
               CONCAT_WS('-', p.tipo_comprobante, p.numero_comprobante) AS comprobante_original,
               CONCAT_WS('-', p.liquidacion #>> '{reversion,tipoComprobante}',
                              p.liquidacion #>> '{reversion,numeroComprobante}') AS comprobante_reversion,
               COALESCE(e.valor_capital, 0) AS capital,
               COALESCE(e.valor_interes_causado, 0) AS interes_causado,
               COALESCE(e.valor_interes_ingreso, 0) AS interes_ingreso,
               COALESCE(e.valor_interes_anticipado, 0) AS interes_anticipado,
               COALESCE(e.valor_interes_mora, 0) AS mora,
               COALESCE(e.valor_seguro, 0) AS seguros,
               COALESCE(e.valor_aportes, 0) AS aportes,
               COALESCE(e.valor_papeleria, 0) AS papeleria,
               COALESCE(e.valor_fondo_garantia, 0) AS fondo_garantia,
               COALESCE(e.otros_valores, 0) AS otros,
               p.valor_pago AS valor_reversado
        FROM cartera.pagos_cuotas p
        JOIN cartera.vw_cartera_creditos_total c ON c.id_cartera_credito = p.id_cartera_credito
        LEFT JOIN general.datos_agencias ar ON ar.id_agencia = p.id_agencia_receptora
        LEFT JOIN cartera.lineas_creditos lc ON lc.id_linea_credito = c.id_linea_credito
        JOIN hoja_vida.datos_personales dp ON dp.id_datos_personal = c.id_datos_personal
        JOIN cartera.extractos_cartera e ON e.id_extracto_cartera = p.id_extracto_cartera AND e.estado = 'R'
        LEFT JOIN seguridad.usuarios u
          ON u.id_usuario = (p.liquidacion #>> '{reversion,idUsuario}')::integer
        WHERE p.estado = 'REVERSADO'
          AND (p.liquidacion #>> '{reversion,fechaReversion}')::date BETWEEN :fechaInicial AND :fechaFinal
          AND (CAST(:idAgencia AS integer) IS NULL OR c.id_agencia = :idAgencia)
          AND (:accesoTotal = true OR c.id_agencia IN (:agencias))
          AND (CAST(:asociado AS text) IS NULL OR dp.documento ILIKE :asociado
               OR CONCAT_WS(' ', dp.nombres, dp.primer_apellido, dp.segundo_apellido) ILIKE :asociado)
          AND (CAST(:pagare AS text) IS NULL OR c.pagare_cartera ILIKE :pagare)
          AND (CAST(:funcionario AS text) IS NULL OR u.username ILIKE :funcionario OR u.nombre_completo ILIKE :funcionario)
        ORDER BY fecha_registro_reversion DESC NULLS LAST, p.id_pago DESC
        """;

    /** Obtiene las reversiones dentro de las agencias efectivamente autorizadas. */
    public List<InformeReversionDTO> consultar(InformeReversionFiltroDTO filtro,
                                               List<Integer> agencias, boolean accesoTotal) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("fechaInicial", filtro.fechaInicial())
                .addValue("fechaFinal", filtro.fechaFinal())
                .addValue("idAgencia", filtro.idAgencia(), Types.INTEGER)
                .addValue("accesoTotal", accesoTotal)
                .addValue("agencias", agencias.isEmpty() ? List.of(-1) : agencias)
                .addValue("asociado", patron(filtro.asociado()), Types.VARCHAR)
                .addValue("pagare", patron(filtro.pagare()), Types.VARCHAR)
                .addValue("funcionario", patron(filtro.funcionario()), Types.VARCHAR);
        return jdbc.query(SQL, params, (rs, rowNum) -> mapear(rs));
    }


    /** Sugiere asociados con reversiones dentro de las agencias autorizadas. */
    public List<InformeReversionAsociadoDTO> buscarAsociados(String texto,
                                                             List<Integer> agencias, boolean accesoTotal) {
        String sql = """
            SELECT DISTINCT dp.documento,
                   TRIM(CONCAT_WS(' ', dp.nombres, dp.primer_apellido, dp.segundo_apellido)) AS nombre_completo
            FROM cartera.pagos_cuotas p
            JOIN cartera.vw_cartera_creditos_total c ON c.id_cartera_credito = p.id_cartera_credito
            JOIN hoja_vida.datos_personales dp ON dp.id_datos_personal = c.id_datos_personal
            WHERE p.estado = 'REVERSADO'
              AND (:accesoTotal = true OR c.id_agencia IN (:agencias))
              AND (dp.documento ILIKE :texto OR
                   CONCAT_WS(' ', dp.nombres, dp.primer_apellido, dp.segundo_apellido) ILIKE :texto)
            ORDER BY nombre_completo, dp.documento
            LIMIT 20
            """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("accesoTotal", accesoTotal)
                .addValue("agencias", agencias.isEmpty() ? List.of(-1) : agencias)
                .addValue("texto", "%" + texto.trim() + "%", Types.VARCHAR);
        return jdbc.query(sql, params, (rs, rowNum) -> new InformeReversionAsociadoDTO(
                rs.getString("documento"), rs.getString("nombre_completo")));
    }

    /** Prepara filtros parciales para búsqueda sin concatenar SQL. */
    private static String patron(String valor) {
        return valor == null || valor.isBlank() ? null : "%" + valor.trim() + "%";
    }

    /** Convierte cada pago original en una única fila de informe. */
    private static InformeReversionDTO mapear(ResultSet r) throws SQLException {
        return new InformeReversionDTO(
                r.getLong("id_pago"), r.getObject("id_agencia_credito", Integer.class),
                r.getString("codigo_agencia"), r.getString("nombre_agencia"),
                r.getObject("id_agencia_receptora", Integer.class),
                r.getString("codigo_agencia_receptora"), r.getString("nombre_agencia_receptora"),
                r.getString("codigo_linea_credito"), r.getString("nombre_linea_credito"), r.getString("pagare"),
                r.getString("documento"), r.getString("asociado"),
                r.getDate("fecha_pago_original").toLocalDate(),
                r.getDate("fecha_reversion").toLocalDate(),
                r.getObject("fecha_registro_reversion", OffsetDateTime.class),
                r.getString("motivo"), r.getString("usuario"), r.getString("funcionario"),
                r.getString("comprobante_original"), r.getString("comprobante_reversion"),
                r.getBigDecimal("capital"), r.getBigDecimal("interes_causado"),
                r.getBigDecimal("interes_ingreso"), r.getBigDecimal("interes_anticipado"),
                r.getBigDecimal("mora"), r.getBigDecimal("seguros"),
                r.getBigDecimal("aportes"), r.getBigDecimal("papeleria"),
                r.getBigDecimal("fondo_garantia"), r.getBigDecimal("otros"),
                r.getBigDecimal("valor_reversado"));
    }
}
