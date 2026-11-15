package co.assip.erp.contabilidad.cierres.diario;

import co.assip.erp.contabilidad.cierres.diario.dto.CierreDiarioDetalleCuentaDTO;
import co.assip.erp.contabilidad.cierres.diario.dto.CierreDiarioDiaDTO;
import co.assip.erp.contabilidad.cierres.diario.dto.CierreDiarioResumenDTO;
import co.assip.erp.contabilidad.cierres.diario.dto.CierreDiarioInformeDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Consulta los movimientos contables y administra los registros de cierre diario.
 * Considera movimientos A y C; excluye los anulados N.
 */
@Repository
@RequiredArgsConstructor
public class CierreDiarioRepository {

    private final NamedParameterJdbcTemplate jdbc;

    /**
     * Calcula el resumen contable de una fecha sin modificar auxiliares.
     */
    public CierreDiarioResumenDTO consultarResumen(
            Integer idAgencia,
            LocalDate fecha
    ) {
        String sql = """
            SELECT COUNT(*) AS cantidad_movimientos,
                   COUNT(DISTINCT (tipo_comprobante, numero_comprobante))
                       AS cantidad_comprobantes,
                   COALESCE(SUM(valor_debito), 0) AS total_debito,
                   COALESCE(SUM(valor_credito), 0) AS total_credito
            FROM contabilidad.auxiliares_contables
            WHERE id_agencia = :idAgencia
              AND fecha_auxiliar = :fecha
              AND estado_movimiento IN ('A', 'C')
            """;

        return jdbc.queryForObject(
                sql,
                parametros(idAgencia, fecha),
                (rs, rowNum) -> {
                    BigDecimal debitos = rs.getBigDecimal("total_debito");
                    BigDecimal creditos = rs.getBigDecimal("total_credito");

                    return new CierreDiarioResumenDTO(
                            idAgencia,
                            fecha,
                            null,
                            null,
                            debitos,
                            creditos,
                            debitos.subtract(creditos),
                            rs.getLong("cantidad_comprobantes"),
                            rs.getLong("cantidad_movimientos"),
                            List.of()
                    );
                }
        );
    }

    /**
     * Consulta los movimientos agrupados por cuenta contable.
     */
    public List<CierreDiarioDetalleCuentaDTO> consultarDetallePorCuenta(
            Integer idAgencia,
            LocalDate fecha
    ) {
        String sql = """
            SELECT a.id_catalogo_cuenta,
                   c.codigo_cuenta,
                   c.nombre_cuenta,
                   COUNT(*) AS cantidad_movimientos,
                   COALESCE(SUM(a.valor_debito), 0) AS total_debito,
                   COALESCE(SUM(a.valor_credito), 0) AS total_credito
            FROM contabilidad.auxiliares_contables a
            JOIN contabilidad.catalogo_cuentas c
              ON c.id_catalogo_cuenta = a.id_catalogo_cuenta
             AND c.id_agencia = a.id_agencia
            WHERE a.id_agencia = :idAgencia
              AND a.fecha_auxiliar = :fecha
              AND a.estado_movimiento IN ('A', 'C')
            GROUP BY
                a.id_catalogo_cuenta,
                c.codigo_cuenta,
                c.nombre_cuenta
            ORDER BY c.codigo_cuenta
            """;

        return jdbc.query(
                sql,
                parametros(idAgencia, fecha),
                (rs, rowNum) -> new CierreDiarioDetalleCuentaDTO(
                        rs.getInt("id_catalogo_cuenta"),
                        rs.getString("codigo_cuenta").trim(),
                        rs.getString("nombre_cuenta").trim(),
                        rs.getBigDecimal("total_debito"),
                        rs.getBigDecimal("total_credito"),
                        rs.getLong("cantidad_movimientos")
                )
        );
    }

    /**
     * Recupera los auxiliares de una fecha para el informe de cierre diario.
     * Incluye movimientos A y C y excluye los anulados N.
     */
    public List<CierreDiarioInformeDTO.ResumenMovimientoDTO> consultarMovimientosInforme(
            Integer idAgencia,
            LocalDate fecha
    ) {
        String sql = """
                SELECT
                    ac.id_auxiliar_contable,
                    TRIM(ac.tipo_comprobante) AS tipo_comprobante,
                    TRIM(ac.numero_comprobante) AS numero_comprobante,
                    TRIM(cc.codigo_cuenta) AS codigo_cuenta,
                    TRIM(cc.nombre_cuenta) AS nombre_cuenta,
                    dp.documento AS documento_tercero,
                    CASE
                        WHEN dp.tipo_persona = '2' THEN
                            NULLIF(TRIM(dp.nombres), '')
                        ELSE
                            NULLIF(
                                TRIM(CONCAT_WS(' ',
                                    NULLIF(TRIM(dp.nombres), ''),
                                    NULLIF(TRIM(dp.primer_apellido), ''),
                                    NULLIF(TRIM(dp.segundo_apellido), '')
                                )), '')
                    END AS nombre_tercero,
                    ac.detalle_movimiento,
                    COALESCE(ac.valor_debito, 0) AS valor_debito,
                    COALESCE(ac.valor_credito, 0) AS valor_credito,
                    ac.origen_modulo,
                    ac.documento_origen,
                    ac.documento_soporte
                FROM contabilidad.auxiliares_contables ac
                INNER JOIN contabilidad.catalogo_cuentas cc
                    ON cc.id_catalogo_cuenta = ac.id_catalogo_cuenta
                   AND cc.id_agencia = ac.id_agencia
                LEFT JOIN hoja_vida.datos_personales dp
                    ON dp.id_datos_personal = ac.id_datos_personal
                WHERE ac.id_agencia = :idAgencia
                  AND ac.fecha_auxiliar = :fecha
                  AND ac.estado_movimiento IN ('A', 'C')
                ORDER BY
                    cc.codigo_cuenta,
                    ac.tipo_comprobante,
                    ac.numero_comprobante,
                    ac.id_auxiliar_contable
                """;

        return jdbc.query(
                sql,
                parametros(idAgencia, fecha),
                (rs, rowNum) -> new CierreDiarioInformeDTO.ResumenMovimientoDTO(
                        rs.getLong("id_auxiliar_contable"),
                        rs.getString("tipo_comprobante"),
                        rs.getString("numero_comprobante"),
                        rs.getString("codigo_cuenta"),
                        rs.getString("nombre_cuenta"),
                        rs.getString("documento_tercero"),
                        rs.getString("nombre_tercero"),
                        rs.getString("detalle_movimiento"),
                        rs.getBigDecimal("valor_debito"),
                        rs.getBigDecimal("valor_credito"),
                        rs.getString("origen_modulo"),
                        rs.getString("documento_origen"),
                        rs.getString("documento_soporte")
                )
        );
    }

    /**
     * Consulta todos los días del mes y sus movimientos contables actuales.
     * El estado del día procede de periodos_diarios.
     */
    public List<CierreDiarioDiaDTO> consultarDiasMes(
            Integer idAgencia,
            int ano,
            int mes
    ) {
        LocalDate inicio = LocalDate.of(ano, mes, 1);
        LocalDate finExclusivo = inicio.plusMonths(1);

        String sql = """
            WITH fechas AS (
                SELECT fecha::date AS fecha
                FROM generate_series(
                    CAST(:inicio AS date),
                    CAST(:fin AS date) - INTERVAL '1 day',
                    INTERVAL '1 day'
                ) AS serie(fecha)
            ),
            movimientos AS (
                SELECT a.fecha_auxiliar AS fecha,
                       COUNT(*) AS cantidad_movimientos,
                       COUNT(DISTINCT (
                           a.tipo_comprobante,
                           a.numero_comprobante
                       )) AS cantidad_comprobantes,
                       COALESCE(SUM(a.valor_debito), 0) AS total_debito,
                       COALESCE(SUM(a.valor_credito), 0) AS total_credito
                FROM contabilidad.auxiliares_contables a
                WHERE a.id_agencia = :idAgencia
                  AND a.fecha_auxiliar >= :inicio
                  AND a.fecha_auxiliar < :fin
                  AND a.estado_movimiento IN ('A', 'C')
                GROUP BY a.fecha_auxiliar
            )
            SELECT f.fecha,
                   COALESCE(p.estado, 'A') AS estado,
                   COALESCE(m.cantidad_comprobantes, 0)
                       AS cantidad_comprobantes,
                   COALESCE(m.cantidad_movimientos, 0)
                       AS cantidad_movimientos,
                   COALESCE(m.total_debito, 0) AS total_debito,
                   COALESCE(m.total_credito, 0) AS total_credito
            FROM fechas f
            LEFT JOIN movimientos m
              ON m.fecha = f.fecha
            LEFT JOIN contabilidad.periodos_diarios p
              ON p.id_agencia = :idAgencia
             AND p.fecha = f.fecha
            ORDER BY f.fecha
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("idAgencia", idAgencia)
                .addValue("inicio", inicio)
                .addValue("fin", finExclusivo);

        return jdbc.query(
                sql,
                params,
                (rs, rowNum) -> {
                    BigDecimal debitos = rs.getBigDecimal("total_debito");
                    BigDecimal creditos = rs.getBigDecimal("total_credito");
                    BigDecimal diferencia = debitos.subtract(creditos);

                    long movimientos = rs.getLong("cantidad_movimientos");
                    boolean tieneMovimientos = movimientos > 0;
                    boolean cuadrado =
                            diferencia.compareTo(BigDecimal.ZERO) == 0;

                    String advertencia = null;

                    if (!cuadrado) {
                        advertencia =
                                "Existen diferencias entre débitos y créditos.";
                    } else if (!tieneMovimientos) {
                        advertencia =
                                "La fecha no registra movimientos contables.";
                    }

                    return new CierreDiarioDiaDTO(
                            rs.getObject("fecha", LocalDate.class),
                            rs.getString("estado").trim(),
                            rs.getLong("cantidad_comprobantes"),
                            movimientos,
                            debitos,
                            creditos,
                            diferencia,
                            tieneMovimientos,
                            cuadrado,
                            advertencia
                    );
                }
        );
    }

    /**
     * Consulta el estado diario sin bloquear el registro.
     * Devuelve null cuando no existe.
     */
    public String consultarEstadoDiario(Integer idAgencia, LocalDate fecha) {
        String sql = """
            SELECT estado
            FROM contabilidad.periodos_diarios
            WHERE id_agencia = :idAgencia
              AND fecha = :fecha
            """;
        List<String> estados = jdbc.query(
                sql,
                parametros(idAgencia, fecha),
                (rs, rowNum) -> rs.getString("estado").trim()
        );
        return estados.isEmpty() ? null : estados.get(0);
    }

    /**
     * Bloquea el registro diario para serializar cierres y reaperturas.
     * Devuelve null cuando todavía no existe.
     */
    public String bloquearEstadoDiario(
            Integer idAgencia,
            LocalDate fecha
    ) {
        String sql = """
            SELECT estado
            FROM contabilidad.periodos_diarios
            WHERE id_agencia = :idAgencia
              AND fecha = :fecha
            FOR UPDATE
            """;

        List<String> estados = jdbc.query(
                sql,
                parametros(idAgencia, fecha),
                (rs, rowNum) -> rs.getString("estado").trim()
        );

        return estados.isEmpty() ? null : estados.get(0);
    }

    /**
     * Recupera la fotografía del último cierre, sin recalcular auxiliares.
     * Se utiliza para registrar los valores históricos de una reapertura.
     */
    public CierreDiarioResumenDTO consultarUltimoCierre(
            Integer idAgencia,
            LocalDate fecha
    ) {
        String sql = """
            SELECT cantidad_comprobantes,
                   cantidad_movimientos,
                   total_debitos,
                   total_creditos,
                   diferencia
            FROM contabilidad.periodos_diarios
            WHERE id_agencia = :idAgencia
              AND fecha = :fecha
              AND estado = 'C'
            """;
        List<CierreDiarioResumenDTO> resultados = jdbc.query(
                sql,
                parametros(idAgencia, fecha),
                (rs, rowNum) -> new CierreDiarioResumenDTO(
                        idAgencia,
                        fecha,
                        null,
                        "C",
                        rs.getBigDecimal("total_debitos"),
                        rs.getBigDecimal("total_creditos"),
                        rs.getBigDecimal("diferencia"),
                        rs.getLong("cantidad_comprobantes"),
                        rs.getLong("cantidad_movimientos"),
                        List.of()
                )
        );
        if (resultados.size() != 1) {
            throw new IllegalStateException(
                    "No se encontró el resumen del último cierre diario."
            );
        }
        return resultados.get(0);
    }

    /**
     * Registra el cierre diario y conserva los valores calculados.
     * Solo inserta una fecha que todavía no tenga registro.
     */
    public int insertarCierre(
            Integer idAgencia,
            LocalDate fecha,
            CierreDiarioResumenDTO resumen,
            Integer idUsuario
    ) {
        String sql = """
            INSERT INTO contabilidad.periodos_diarios (
                id_agencia,
                fecha,
                estado,
                cantidad_comprobantes,
                cantidad_movimientos,
                total_debitos,
                total_creditos,
                diferencia,
                fk_seguridad_creacion
            )
            VALUES (
                :idAgencia,
                :fecha,
                'C',
                :cantidadComprobantes,
                :cantidadMovimientos,
                :totalDebitos,
                :totalCreditos,
                :diferencia,
                :idUsuario
            )
            ON CONFLICT (id_agencia, fecha) DO NOTHING
            """;

        return jdbc.update(
                sql,
                parametrosCierre(idAgencia, fecha, resumen, idUsuario)
        );
    }

    /**
     * Cierra nuevamente un día abierto y actualiza su último resumen.
     */
    public int actualizarCierre(
            Integer idAgencia,
            LocalDate fecha,
            CierreDiarioResumenDTO resumen,
            Integer idUsuario
    ) {
        String sql = """
            UPDATE contabilidad.periodos_diarios
            SET estado = 'C',
                cantidad_comprobantes = :cantidadComprobantes,
                cantidad_movimientos = :cantidadMovimientos,
                total_debitos = :totalDebitos,
                total_creditos = :totalCreditos,
                diferencia = :diferencia,
                fecha_modificacion = CURRENT_TIMESTAMP,
                fk_seguridad_modificacion = :idUsuario
            WHERE id_agencia = :idAgencia
              AND fecha = :fecha
              AND estado = 'A'
            """;

        return jdbc.update(
                sql,
                parametrosCierre(idAgencia, fecha, resumen, idUsuario)
        );
    }

    /**
     * Reabre un día cerrado sin borrar los valores de su último cierre.
     */
    public int reabrirDia(
            Integer idAgencia,
            LocalDate fecha,
            Integer idUsuario
    ) {
        String sql = """
            UPDATE contabilidad.periodos_diarios
            SET estado = 'A',
                fecha_modificacion = CURRENT_TIMESTAMP,
                fk_seguridad_modificacion = :idUsuario
            WHERE id_agencia = :idAgencia
              AND fecha = :fecha
              AND estado = 'C'
            """;

        return jdbc.update(
                sql,
                parametros(idAgencia, fecha)
                        .addValue("idUsuario", idUsuario)
        );
    }

    /**
     * Registra el historial de cierre o reapertura con la fotografía
     * contable de la operación. No modifica registros anteriores.
     */
    public int registrarHistorial(
            Integer idAgencia,
            LocalDate fecha,
            String operacion,
            String motivo,
            CierreDiarioResumenDTO resumen,
            Integer idUsuario
    ) {
        String sql = """
            INSERT INTO contabilidad.periodos_diarios_historial (
                id_periodo_diario,
                id_agencia,
                fecha,
                operacion,
                motivo,
                cantidad_comprobantes,
                cantidad_movimientos,
                total_debitos,
                total_creditos,
                diferencia,
                fk_seguridad_creacion
            )
            SELECT
                p.id_periodo_diario,
                p.id_agencia,
                p.fecha,
                :operacion,
                :motivo,
                :cantidadComprobantes,
                :cantidadMovimientos,
                :totalDebitos,
                :totalCreditos,
                :diferencia,
                :idUsuario
            FROM contabilidad.periodos_diarios p
            WHERE p.id_agencia = :idAgencia
              AND p.fecha = :fecha
            """;

        return jdbc.update(
                sql,
                parametrosCierre(idAgencia, fecha, resumen, idUsuario)
                        .addValue("operacion", operacion)
                        .addValue("motivo", motivo)
        );
    }

    /**
     * Comprueba si el año contable está cerrado para la agencia.
     */
    public boolean estaAnoCerrado(
            Integer idAgencia,
            int ano
    ) {
        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM contabilidad.anos_cerrados
                WHERE id_agencia = :idAgencia
                  AND ano = :ano
                  AND estado = 'C'
            )
            """;

        Boolean resultado = jdbc.queryForObject(
                sql,
                new MapSqlParameterSource()
                        .addValue("idAgencia", idAgencia)
                        .addValue("ano", ano),
                Boolean.class
        );

        return Boolean.TRUE.equals(resultado);
    }

    /**
     * Consulta el estado mensual. Devuelve NO_CREADO cuando no existe
     * un registro de control para el mes solicitado.
     */
    public String consultarEstadoMensual(
            Integer idAgencia,
            int ano,
            int mes
    ) {
        String sql = """
            SELECT estado
            FROM contabilidad.periodos_contables
            WHERE id_agencia = :idAgencia
              AND ano = :ano
              AND mes = :mes
            """;

        List<String> estados = jdbc.query(
                sql,
                new MapSqlParameterSource()
                        .addValue("idAgencia", idAgencia)
                        .addValue("ano", ano)
                        .addValue("mes", mes),
                (rs, rowNum) -> rs.getString("estado")
        );

        return estados.isEmpty() ? "NO_CREADO" : estados.get(0);
    }

    /**
     * Construye los parámetros comunes para una fecha contable.
     */
    private MapSqlParameterSource parametros(
            Integer idAgencia,
            LocalDate fecha
    ) {
        return new MapSqlParameterSource()
                .addValue("idAgencia", idAgencia)
                .addValue("fecha", fecha);
    }

    /**
     * Construye los parámetros para guardar resultados de cierre e historial.
     */
    private MapSqlParameterSource parametrosCierre(
            Integer idAgencia,
            LocalDate fecha,
            CierreDiarioResumenDTO resumen,
            Integer idUsuario
    ) {
        return parametros(idAgencia, fecha)
                .addValue("cantidadComprobantes", resumen.cantidadComprobantes())
                .addValue("cantidadMovimientos", resumen.cantidadMovimientos())
                .addValue("totalDebitos", resumen.totalDebitos())
                .addValue("totalCreditos", resumen.totalCreditos())
                .addValue("diferencia", resumen.diferencia())
                .addValue("idUsuario", idUsuario);
    }
}
