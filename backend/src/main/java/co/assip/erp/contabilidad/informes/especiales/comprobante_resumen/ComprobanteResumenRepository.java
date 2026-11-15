package co.assip.erp.contabilidad.informes.especiales.comprobante_resumen;

import co.assip.erp.contabilidad.informes.especiales.comprobante_resumen.dto.ComprobanteResumenFilaDTO;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Repositorio del informe Comprobante Resumen.
 *
 * Consolida los movimientos contables por cuenta,
 * dentro de una agencia y un período determinado.
 *
 * Incluye movimientos en estados A y C.
 * Excluye los movimientos anulados en estado N.
 */
@Repository
@RequiredArgsConstructor
public class ComprobanteResumenRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Consulta los movimientos contables agrupados por cuenta.
     *
     * Acumula los débitos y créditos de cada cuenta contable,
     * sin modificar los valores registrados en los auxiliares.
     *
     * Conserva el orden por código de cuenta.
     */
    public List<ComprobanteResumenFilaDTO> consultar(
            Integer idAgencia,
            LocalDate fechaInicial,
            LocalDate fechaFinal
    ) {

        String sql = """
                SELECT
                    cc.codigo_cuenta,
                    cc.nombre_cuenta,
                    SUM(COALESCE(ac.valor_debito, 0)) AS total_debitos,
                    SUM(COALESCE(ac.valor_credito, 0)) AS total_creditos
                FROM contabilidad.auxiliares_contables ac
                INNER JOIN contabilidad.catalogo_cuentas cc
                    ON cc.id_catalogo_cuenta = ac.id_catalogo_cuenta
                   AND cc.id_agencia = ac.id_agencia
                WHERE ac.id_agencia = ?
                  AND ac.fecha_auxiliar >= ?
                  AND ac.fecha_auxiliar <= ?
                  AND TRIM(ac.estado_movimiento) IN ('A', 'C')
                GROUP BY
                    cc.id_catalogo_cuenta,
                    cc.codigo_cuenta,
                    cc.nombre_cuenta
                ORDER BY
                    cc.codigo_cuenta
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new ComprobanteResumenFilaDTO(
                        rs.getString("codigo_cuenta"),
                        rs.getString("nombre_cuenta"),
                        rs.getBigDecimal("total_debitos"),
                        rs.getBigDecimal("total_creditos")
                ),
                idAgencia,
                Date.valueOf(fechaInicial),
                Date.valueOf(fechaFinal)
        );
    }
}