package co.assip.erp.contabilidad.informes.especiales.verificar_partida_doble;

import co.assip.erp.contabilidad.informes.especiales.verificar_partida_doble.dto.VerificarPartidaDobleFilaDTO;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class VerificarPartidaDobleRepository {
    private final JdbcTemplate jdbcTemplate;

    public VerificarPartidaDobleRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** Agrupa cada comprobante activo por agencia, fecha, tipo y número; no omite el último. */
    public List<VerificarPartidaDobleFilaDTO> consultar(
            Integer idAgencia, LocalDate fechaInicial, LocalDate fechaFinal
    ) {
        String sql = """
                SELECT
                    TRIM(ac.tipo_comprobante) AS tipo_comprobante,
                    COALESCE(MAX(tc.nombre_tipo_comprobante), TRIM(ac.tipo_comprobante))
                        AS nombre_tipo_comprobante,
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
                GROUP BY ac.tipo_comprobante, ac.numero_comprobante, ac.fecha_auxiliar
                ORDER BY ac.fecha_auxiliar, TRIM(ac.tipo_comprobante),
                         TRIM(ac.numero_comprobante)
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            var debitos = rs.getBigDecimal("total_debitos");
            var creditos = rs.getBigDecimal("total_creditos");
            var diferencia = debitos.subtract(creditos);
            return new VerificarPartidaDobleFilaDTO(
                    rs.getString("tipo_comprobante"),
                    rs.getString("nombre_tipo_comprobante"),
                    rs.getString("numero_comprobante"),
                    rs.getObject("fecha", LocalDate.class),
                    debitos, creditos, diferencia, diferencia.signum() != 0
            );
        }, idAgencia, Date.valueOf(fechaInicial), Date.valueOf(fechaFinal));
    }
}
