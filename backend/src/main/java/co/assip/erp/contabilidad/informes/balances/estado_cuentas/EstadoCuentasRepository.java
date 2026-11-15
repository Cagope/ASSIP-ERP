package co.assip.erp.contabilidad.informes.balances.estado_cuentas;

import co.assip.erp.contabilidad.informes.balances.estado_cuentas.dto.EstadoCuentasRespuestaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Consultas de solo lectura sobre cierres contables históricos y firmas por agencia. */
@Repository
@RequiredArgsConstructor
public class EstadoCuentasRepository {
    private final NamedParameterJdbcTemplate jdbc;

    public record Agencia(Integer idAgencia, String codigo, String nombre) {}
    public record EstadoPeriodo(Integer idAgencia, String codigoAgencia, String nombreAgencia,
                                String estado, long cantidadSaldos) {}
    public record CuentaPeriodo(String codigoCuenta, String nombreCuenta, Integer nivelCuenta,
                                String naturalezaCuenta, boolean cuentaOperable,
                                BigDecimal debitoAnterior, BigDecimal creditoAnterior,
                                BigDecimal debitoAcumulado, BigDecimal creditoAcumulado) {}

    /** Movimiento individual de una cuenta operable, con referencia a su comprobante. */
    public record MovimientoCuenta(
            Integer idAuxiliarContable,
            Integer idAgencia,
            String codigoAgencia,
            String nombreAgencia,
            Integer idCatalogoCuenta,
            LocalDate fechaAuxiliar,
            String tipoComprobante,
            String numeroComprobante,
            Integer idDatosPersonal,
            String detalleMovimiento,
            BigDecimal valorDebito,
            BigDecimal valorCredito
    ) {}

    /**
     * Consulta movimientos que participaron en el cierre del mes.
     * Resuelve la cuenta por código y agencia, incluso para balances consolidados.
     * La autorización de agencias y la validación del período corresponden al servicio.
     */
    /** Consulta movimientos efectivos del primer día del mes hasta el corte inclusive. */
    public List<MovimientoCuenta> consultarMovimientosCuenta(
            List<Integer> agencias, String codigoCuenta, LocalDate fechaCorte,
            int limite, int desplazamiento) {
        if (agencias == null || agencias.isEmpty()) return List.of();
        LocalDate inicio = fechaCorte.withDayOfMonth(1);
        String sql = """
                SELECT ac.id_auxiliar_contable, ac.id_agencia,
                       TRIM(ag.codigo_agencia) AS codigo_agencia,
                       TRIM(ag.nombre_agencia) AS nombre_agencia,
                       ac.id_catalogo_cuenta, ac.fecha_auxiliar,
                       TRIM(ac.tipo_comprobante) AS tipo_comprobante,
                       TRIM(ac.numero_comprobante) AS numero_comprobante,
                       ac.id_datos_personal, ac.detalle_movimiento,
                       COALESCE(ac.valor_debito, 0) AS valor_debito,
                       COALESCE(ac.valor_credito, 0) AS valor_credito
                FROM contabilidad.auxiliares_contables ac
                JOIN contabilidad.catalogo_cuentas cc
                  ON cc.id_catalogo_cuenta = ac.id_catalogo_cuenta
                 AND cc.id_agencia = ac.id_agencia
                JOIN general.datos_agencias ag ON ag.id_agencia = ac.id_agencia
                WHERE ac.id_agencia IN (:agencias)
                  AND TRIM(cc.codigo_cuenta) = :codigoCuenta
                  AND cc.cuenta_operable = TRUE
                  AND ac.fecha_auxiliar >= :inicio
                  AND ac.fecha_auxiliar <= :fechaCorte
                  AND TRIM(ac.estado_movimiento) IN ('A', 'C')
                ORDER BY ac.fecha_auxiliar, ac.id_agencia,
                         TRIM(ac.tipo_comprobante), TRIM(ac.numero_comprobante),
                         ac.id_auxiliar_contable
                LIMIT :limite OFFSET :desplazamiento
                """;
        var params = new MapSqlParameterSource()
                .addValue("agencias", agencias)
                .addValue("codigoCuenta", codigoCuenta)
                .addValue("inicio", inicio)
                .addValue("fechaCorte", fechaCorte)
                .addValue("limite", limite)
                .addValue("desplazamiento", desplazamiento);
        return jdbc.query(sql, params, (rs, n) -> new MovimientoCuenta(
                rs.getInt("id_auxiliar_contable"), rs.getInt("id_agencia"),
                rs.getString("codigo_agencia"), rs.getString("nombre_agencia"),
                rs.getInt("id_catalogo_cuenta"),
                rs.getObject("fecha_auxiliar", LocalDate.class),
                rs.getString("tipo_comprobante"), rs.getString("numero_comprobante"),
                (Integer) rs.getObject("id_datos_personal"),
                rs.getString("detalle_movimiento"), rs.getBigDecimal("valor_debito"),
                rs.getBigDecimal("valor_credito")));
    }

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
     * Obtiene el saldo de apertura del mes y movimientos efectivos hasta el corte.
     * La agregación por prefijo incluye subcuentas y evita depender de un cierre futuro.
     * Para enero, toma el cierre anual anterior; para otros meses, el cierre mensual previo.
     */
    public List<CuentaPeriodo> consultarCuentas(List<Integer> agencias, LocalDate fechaCorte,
                                                int nivel, String codigoPrincipal) {
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
                ), movimientos AS (
                    SELECT ac.id_agencia, ac.id_catalogo_cuenta,
                           SUM(COALESCE(ac.valor_debito, 0)) AS debito,
                           SUM(COALESCE(ac.valor_credito, 0)) AS credito
                    FROM contabilidad.auxiliares_contables ac
                    WHERE ac.id_agencia IN (:agencias)
                      AND ac.fecha_auxiliar >= :inicio
                      AND ac.fecha_auxiliar <= :fechaCorte
                      AND TRIM(ac.estado_movimiento) IN ('A', 'C')
                    GROUP BY ac.id_agencia, ac.id_catalogo_cuenta
                ), operables AS (
                    SELECT c.id_agencia, TRIM(c.codigo_cuenta) AS codigo,
                           TRIM(c.naturaleza_cuenta) AS naturaleza,
                           COALESCE(a.debito, 0) AS anterior_debito,
                           COALESCE(a.credito, 0) AS anterior_credito,
                           COALESCE(m.debito, 0) AS movimiento_debito,
                           COALESCE(m.credito, 0) AS movimiento_credito
                    FROM contabilidad.catalogo_cuentas c
                    LEFT JOIN anterior a ON a.id_agencia = c.id_agencia
                        AND a.id_catalogo_cuenta = c.id_catalogo_cuenta
                    LEFT JOIN movimientos m ON m.id_agencia = c.id_agencia
                        AND m.id_catalogo_cuenta = c.id_catalogo_cuenta
                    WHERE c.id_agencia IN (:agencias)
                      AND c.cuenta_operable = TRUE
                )
                SELECT TRIM(c.codigo_cuenta) AS codigo_cuenta,
                       TRIM(c.nombre_cuenta) AS nombre_cuenta,
                       c.nivel_cuenta,
                       TRIM(c.naturaleza_cuenta) AS naturaleza_cuenta,
                       COALESCE(c.cuenta_operable, FALSE) AS cuenta_operable,
                       COALESCE(SUM(o.anterior_debito), 0) AS debito_anterior,
                       COALESCE(SUM(o.anterior_credito), 0) AS credito_anterior,
                       COALESCE(SUM(o.anterior_debito + o.movimiento_debito), 0) AS debito_acumulado,
                       COALESCE(SUM(o.anterior_credito + o.movimiento_credito), 0) AS credito_acumulado
                FROM contabilidad.catalogo_cuentas c
                LEFT JOIN operables o ON o.id_agencia = c.id_agencia
                    AND LEFT(o.codigo, LENGTH(TRIM(c.codigo_cuenta))) = TRIM(c.codigo_cuenta)
                WHERE c.id_agencia IN (:agencias)
                  AND c.nivel_cuenta <= :nivel
                  AND (:codigoPrincipal = '' OR
                       LEFT(TRIM(c.codigo_cuenta), LENGTH(:codigoPrincipal)) = :codigoPrincipal)
                GROUP BY c.id_agencia, c.id_catalogo_cuenta, c.codigo_cuenta,
                         c.nombre_cuenta, c.nivel_cuenta, c.naturaleza_cuenta, c.cuenta_operable
                ORDER BY TRIM(c.codigo_cuenta), c.id_agencia
                """;
        var params = new MapSqlParameterSource()
                .addValue("agencias", agencias)
                .addValue("ano", fechaCorte.getYear())
                .addValue("anoAnterior", fechaCorte.getYear() - 1)
                .addValue("mes", fechaCorte.getMonthValue())
                .addValue("mesAnterior", fechaCorte.getMonthValue() - 1)
                .addValue("inicio", fechaCorte.withDayOfMonth(1))
                .addValue("fechaCorte", fechaCorte)
                .addValue("nivel", nivel)
                .addValue("codigoPrincipal", codigoPrincipal == null ? "" : codigoPrincipal);
        return jdbc.query(sql, params, (rs, n) -> new CuentaPeriodo(
                rs.getString("codigo_cuenta"), rs.getString("nombre_cuenta"),
                rs.getInt("nivel_cuenta"), rs.getString("naturaleza_cuenta"),
                rs.getBoolean("cuenta_operable"), rs.getBigDecimal("debito_anterior"),
                rs.getBigDecimal("credito_anterior"), rs.getBigDecimal("debito_acumulado"),
                rs.getBigDecimal("credito_acumulado")));
    }

    /** Cuentas principales seleccionables; no expone identificadores internos. */
    public List<CuentaPrincipal> listarCuentasPrincipales(List<Integer> agencias) {
        if (agencias == null || agencias.isEmpty()) return List.of();
        return jdbc.query("""
                SELECT DISTINCT TRIM(codigo_cuenta) AS codigo,
                       TRIM(nombre_cuenta) AS nombre
                FROM contabilidad.catalogo_cuentas
                WHERE id_agencia IN (:agencias) AND cuenta_operable = FALSE
                ORDER BY codigo, nombre
                """, new MapSqlParameterSource("agencias", agencias),
                (rs, n) -> new CuentaPrincipal(rs.getString("codigo"), rs.getString("nombre")));
    }

    public record CuentaPrincipal(String codigo, String nombre) {}

    public record Firmas(Firmante firmante1, Firmante firmante2, Firmante firmante3) {}
    public record Firmante(String titulo, String nombre, String tarjetaProfesional) {}

    /** Recupera hasta tres firmantes de la agencia solicitada, sin sustituciones implícitas. */
    public Firmas consultarFirmas(Integer idAgencia) {
        String sql = """
                SELECT titulo_1, nombre_titulo_1, tarjeta_titulo_1,
                       titulo_2, nombre_titulo_2, tarjeta_titulo_2,
                       titulo_3, nombre_titulo_3, tarjeta_titulo_3
                FROM contabilidad.firmas_contables
                WHERE id_agencia = :agencia
                """;
        var params = new MapSqlParameterSource("agencia", idAgencia);
        List<Firmas> firmas = jdbc.query(sql, params,
                (rs, n) -> new Firmas(
                        firmante(rs.getString("titulo_1"), rs.getString("nombre_titulo_1"), rs.getString("tarjeta_titulo_1")),
                        firmante(rs.getString("titulo_2"), rs.getString("nombre_titulo_2"), rs.getString("tarjeta_titulo_2")),
                        firmante(rs.getString("titulo_3"), rs.getString("nombre_titulo_3"), rs.getString("tarjeta_titulo_3"))));
        return firmas.isEmpty() ? null : firmas.get(0);
    }

    private Firmante firmante(String titulo, String nombre,
                                                                     String tarjeta) {
        if (nombre == null || nombre.isBlank()) return null;
        return new Firmante(
                titulo == null ? null : titulo.trim(), nombre.trim(),
                tarjeta == null ? null : tarjeta.trim());
    }
}
