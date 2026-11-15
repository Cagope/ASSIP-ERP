package co.assip.erp.contabilidad.informes.balances.balance_comparado_mensual;

import co.assip.erp.contabilidad.informes.balances.balance_comparado_mensual.dto.BalanceComparadoMensualRespuestaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/** Consultas de solo lectura sobre cierres contables históricos y firmas por agencia. */
@Repository
@RequiredArgsConstructor
public class BalanceComparadoMensualRepository {
    private final NamedParameterJdbcTemplate jdbc;

    public record Agencia(Integer idAgencia, String codigo, String nombre) {}
    public record EstadoPeriodo(Integer idAgencia, String codigoAgencia, String nombreAgencia,
                                String estado, long cantidadSaldos) {}
    public record CuentaPeriodo(String codigoCuenta, String nombreCuenta, Integer nivelCuenta,
                                String naturalezaCuenta, boolean cuentaOperable,
                                BigDecimal debitoAnterior, BigDecimal creditoAnterior,
                                BigDecimal debitoAcumulado, BigDecimal creditoAcumulado) {}

    /** Recupera las agencias reales; el servicio controla visibilidad y autorización. */
    public List<Agencia> listarAgencias() {
        return jdbc.query("""
                SELECT id_agencia, TRIM(codigo_agencia) AS codigo,
                       TRIM(nombre_agencia) AS nombre
                FROM general.datos_agencias
                ORDER BY codigo_agencia
                """, (rs, n) -> new Agencia(rs.getInt("id_agencia"),
                rs.getString("codigo"), rs.getString("nombre")));
    }

    /** Identifica las agencias operativas mediante su catálogo, sin asumir IDs fijos. */
    public List<Agencia> listarAgenciasOperativas() {
        return jdbc.query("""
                SELECT a.id_agencia, TRIM(a.codigo_agencia) AS codigo,
                       TRIM(a.nombre_agencia) AS nombre
                FROM general.datos_agencias a
                WHERE TRIM(a.codigo_agencia) <> '00'
                  AND EXISTS (
                      SELECT 1 FROM contabilidad.catalogo_cuentas c
                      WHERE c.id_agencia = a.id_agencia
                  )
                ORDER BY a.codigo_agencia
                """, (rs, n) -> new Agencia(rs.getInt("id_agencia"),
                rs.getString("codigo"), rs.getString("nombre")));
    }

    /** Comprueba estado y existencia de saldos de cada agencia en el mes solicitado. */
    public List<EstadoPeriodo> consultarEstados(List<Integer> agencias, int ano, int mes) {
        if (agencias == null || agencias.isEmpty()) return List.of();
        String sql = """
                SELECT a.id_agencia, TRIM(a.codigo_agencia) AS codigo,
                       TRIM(a.nombre_agencia) AS nombre,
                       COALESCE(p.estado, 'SIN PERIODO') AS estado,
                       (SELECT COUNT(*) FROM contabilidad.periodos_contables_saldos s
                        WHERE s.id_agencia = a.id_agencia
                          AND s.ano = :ano AND s.mes = :mes) AS cantidad_saldos
                FROM general.datos_agencias a
                LEFT JOIN contabilidad.periodos_contables p
                  ON p.id_agencia = a.id_agencia AND p.ano = :ano AND p.mes = :mes
                WHERE a.id_agencia IN (:agencias)
                ORDER BY a.codigo_agencia
                """;
        var params = new MapSqlParameterSource().addValue("agencias", agencias)
                .addValue("ano", ano).addValue("mes", mes);
        return jdbc.query(sql, params, (rs, n) -> new EstadoPeriodo(
                rs.getInt("id_agencia"), rs.getString("codigo"), rs.getString("nombre"),
                rs.getString("estado"), rs.getLong("cantidad_saldos")));
    }

    /**
     * Lee el cierre mensual y su apertura: enero usa el cierre anual previo;
     * otros meses usan el cierre mensual inmediatamente anterior.
     * Las cuentas agrupadoras ya contienen acumulados y no deben sumar sus hijas.
     * Los registros se mantienen separados por agencia para consolidarlos por código en servicio.
     */
    public List<CuentaPeriodo> consultarCuentas(List<Integer> agencias, int ano, int mes, int nivel,
                                                boolean soloBalance) {
        if (agencias == null || agencias.isEmpty()) return List.of();
        String sql = """
                WITH anterior AS (
                    SELECT id_agencia, id_catalogo_cuenta,
                           COALESCE(valor_debito, 0) AS debito,
                           COALESCE(valor_credito, 0) AS credito
                    FROM contabilidad.cierres_anuales
                    WHERE id_agencia IN (:agencias) AND ano = :anoAnterior AND :mes = 1
                    UNION ALL
                    SELECT id_agencia, id_catalogo_cuenta,
                           COALESCE(valor_debito, 0) AS debito,
                           COALESCE(valor_credito, 0) AS credito
                    FROM contabilidad.periodos_contables_saldos
                    WHERE id_agencia IN (:agencias) AND ano = :ano
                      AND mes = :mesAnterior AND :mes > 1
                )
                SELECT TRIM(c.codigo_cuenta) AS codigo_cuenta,
                       TRIM(c.nombre_cuenta) AS nombre_cuenta,
                       c.nivel_cuenta,
                       TRIM(c.naturaleza_cuenta) AS naturaleza_cuenta,
                       COALESCE(c.cuenta_operable, FALSE) AS cuenta_operable,
                       COALESCE(a.debito, 0) AS debito_anterior,
                       COALESCE(a.credito, 0) AS credito_anterior,
                       COALESCE(s.valor_debito, 0) AS debito_acumulado,
                       COALESCE(s.valor_credito, 0) AS credito_acumulado
                FROM contabilidad.periodos_contables_saldos s
                JOIN contabilidad.catalogo_cuentas c
                  ON c.id_catalogo_cuenta = s.id_catalogo_cuenta
                 AND c.id_agencia = s.id_agencia
                LEFT JOIN anterior a
                  ON a.id_catalogo_cuenta = s.id_catalogo_cuenta
                 AND a.id_agencia = s.id_agencia
                WHERE s.id_agencia IN (:agencias)
                  AND s.ano = :ano AND s.mes = :mes
                  AND c.nivel_cuenta <= :nivel
                  AND (:soloBalance = FALSE OR LEFT(TRIM(c.codigo_cuenta), 1) IN ('1','2','3'))
                ORDER BY TRIM(c.codigo_cuenta), s.id_agencia
                """;
        var params = new MapSqlParameterSource().addValue("agencias", agencias)
                .addValue("ano", ano).addValue("mes", mes)
                .addValue("anoAnterior", ano - 1).addValue("mesAnterior", mes - 1)
                .addValue("nivel", nivel).addValue("soloBalance", soloBalance);
        return jdbc.query(sql, params, (rs, n) -> new CuentaPeriodo(
                rs.getString("codigo_cuenta"), rs.getString("nombre_cuenta"),
                rs.getInt("nivel_cuenta"), rs.getString("naturaleza_cuenta"),
                rs.getBoolean("cuenta_operable"), rs.getBigDecimal("debito_anterior"),
                rs.getBigDecimal("credito_anterior"), rs.getBigDecimal("debito_acumulado"),
                rs.getBigDecimal("credito_acumulado")));
    }

    /** Recupera hasta tres firmantes de la agencia solicitada, sin sustituciones implícitas. */
    public BalanceComparadoMensualRespuestaDTO.FirmasDTO consultarFirmas(Integer idAgencia) {
        String sql = """
                SELECT titulo_1, nombre_titulo_1, tarjeta_titulo_1,
                       titulo_2, nombre_titulo_2, tarjeta_titulo_2,
                       titulo_3, nombre_titulo_3, tarjeta_titulo_3
                FROM contabilidad.firmas_contables
                WHERE id_agencia = :agencia
                """;
        var params = new MapSqlParameterSource("agencia", idAgencia);
        List<BalanceComparadoMensualRespuestaDTO.FirmasDTO> firmas = jdbc.query(sql, params,
                (rs, n) -> new BalanceComparadoMensualRespuestaDTO.FirmasDTO(
                        firmante(rs.getString("titulo_1"), rs.getString("nombre_titulo_1"), rs.getString("tarjeta_titulo_1")),
                        firmante(rs.getString("titulo_2"), rs.getString("nombre_titulo_2"), rs.getString("tarjeta_titulo_2")),
                        firmante(rs.getString("titulo_3"), rs.getString("nombre_titulo_3"), rs.getString("tarjeta_titulo_3"))));
        return firmas.isEmpty() ? null : firmas.get(0);
    }

    private BalanceComparadoMensualRespuestaDTO.FirmanteDTO firmante(String titulo, String nombre,
                                                                     String tarjeta) {
        if (nombre == null || nombre.isBlank()) return null;
        return new BalanceComparadoMensualRespuestaDTO.FirmanteDTO(
                titulo == null ? null : titulo.trim(), nombre.trim(),
                tarjeta == null ? null : tarjeta.trim());
    }
}
