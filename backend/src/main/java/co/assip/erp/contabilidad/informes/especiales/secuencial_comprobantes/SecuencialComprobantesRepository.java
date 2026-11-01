
package co.assip.erp.contabilidad.informes.especiales.secuencial_comprobantes;

import co.assip.erp.contabilidad.informes.especiales.secuencial_comprobantes.dto.SecuencialComprobantesFilaDTO;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SecuencialComprobantesRepository {
    private final JdbcTemplate jdbcTemplate;

    public SecuencialComprobantesRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Consulta los comprobantes activos de una agencia y período.
     * Agrupa sus movimientos por tipo, número y fecha, consolida
     * débitos y créditos y ordena por tipo y consecutivo.
     */
    public List<SecuencialComprobantesFilaDTO> consultar(
            Integer idAgencia,
            LocalDate fechaInicial,
            LocalDate fechaFinal
    ) {
        String sql = """
            SELECT
                TRIM(ac.tipo_comprobante) AS tipo_comprobante,
                COALESCE(
                    MAX(tc.nombre_tipo_comprobante),
                    TRIM(ac.tipo_comprobante)
                ) AS nombre_tipo_comprobante,
                TRIM(ac.numero_comprobante) AS numero_comprobante,
                ac.fecha_auxiliar AS fecha,
                SUM(COALESCE(ac.valor_debito, 0)) AS total_debitos,
                SUM(COALESCE(ac.valor_credito, 0)) AS total_creditos
            FROM contabilidad.auxiliares_contables ac
            LEFT JOIN contabilidad.tipos_comprobantes tc
                ON tc.id_agencia = ac.id_agencia
                AND tc.tipo_comprobante = ac.tipo_comprobante
            WHERE ac.id_agencia = ?
                AND ac.fecha_auxiliar >= ?
                AND ac.fecha_auxiliar <= ?
                AND ac.estado_movimiento = 'A'
            GROUP BY
                ac.tipo_comprobante,
                ac.numero_comprobante,
                ac.fecha_auxiliar
            ORDER BY
                TRIM(ac.tipo_comprobante),
                TRIM(ac.numero_comprobante),
                ac.fecha_auxiliar
            """;
        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new SecuencialComprobantesFilaDTO(
                        rs.getString("tipo_comprobante"),
                        rs.getString("nombre_tipo_comprobante"),
                        rs.getString("numero_comprobante"),
                        rs.getObject("fecha", LocalDate.class),
                        rs.getBigDecimal("total_debitos"),
                        rs.getBigDecimal("total_creditos")
                ),
                idAgencia,
                Date.valueOf(fechaInicial),
                Date.valueOf(fechaFinal)
        );
    }
}
