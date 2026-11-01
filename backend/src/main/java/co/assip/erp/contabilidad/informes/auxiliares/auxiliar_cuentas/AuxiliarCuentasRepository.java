
package co.assip.erp.contabilidad.informes.auxiliares.auxiliar_cuentas;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Consultas de solo lectura del auxiliar contable por cuentas.
 * Incluye identificación de terceros, usuarios y trazabilidad de origen.
 */
@Repository
@RequiredArgsConstructor
public class AuxiliarCuentasRepository {
    private final NamedParameterJdbcTemplate jdbc;

    /** Identificación y naturaleza de una cuenta auxiliar operable. */
    public record CuentaAuxiliar(
            Integer idCatalogoCuenta,
            String codigoCuenta,
            String nombreCuenta,
            String naturalezaCuenta
    ) {}

    /** Totales históricos anteriores al período consultado. */
    public record SaldoAnterior(
            BigDecimal debitos,
            BigDecimal creditos
    ) {}

    /** Movimiento contable con información de identificación y auditoría. */
    public record MovimientoAuxiliar(
            Integer idAuxiliarContable,
            LocalDate fechaAuxiliar,
            String tipoComprobante,
            String numeroComprobante,
            String detalleMovimiento,
            String estadoMovimiento,
            Integer idDatosPersonal,
            String documentoTercero,
            String nombreTercero,
            BigDecimal valorDebito,
            BigDecimal valorCredito,
            BigDecimal valorBase,
            Integer fkSeguridadCreacion,
            String usernameCreacion,
            String nombreUsuarioCreacion,
            LocalDateTime fechaCreacion,
            Integer fkSeguridadEdicion,
            String usernameEdicion,
            String nombreUsuarioEdicion,
            LocalDateTime fechaEdicion,
            String origenModulo,
            String documentoOrigen,
            String documentoSoporte
    ) {}

    /**
     * Recupera cuentas operables del rango solicitado.
     * Incluye descendientes cuando la cuenta final es agrupadora.
     */
    public List<CuentaAuxiliar> listarCuentas(
            Integer idAgencia,
            String cuentaInicial,
            String cuentaFinal
    ) {
        String sql = """
            SELECT
                id_catalogo_cuenta,
                codigo_cuenta,
                nombre_cuenta,
                naturaleza_cuenta
            FROM contabilidad.catalogo_cuentas
            WHERE id_agencia = :agencia
              AND cuenta_operable = TRUE
              AND codigo_cuenta >= :inicial
              AND (
                    codigo_cuenta <= :final
                    OR codigo_cuenta LIKE :prefijoFinal
                  )
            ORDER BY codigo_cuenta
            """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("agencia", idAgencia)
                .addValue("inicial", cuentaInicial)
                .addValue("final", cuentaFinal)
                .addValue("prefijoFinal", cuentaFinal + "%");
        return jdbc.query(sql, params, (rs, rowNum) ->
                new CuentaAuxiliar(
                        rs.getInt("id_catalogo_cuenta"),
                        rs.getString("codigo_cuenta"),
                        rs.getString("nombre_cuenta"),
                        rs.getString("naturaleza_cuenta")
                )
        );
    }

    /**
     * Obtiene débitos y créditos activos anteriores a la fecha inicial.
     * No incluye movimientos anulados o inactivos en los saldos.
     */
    public SaldoAnterior consultarSaldoAnterior(
            Integer idAgencia,
            Integer idCatalogoCuenta,
            LocalDate fechaInicial
    ) {
        String sql = """
            SELECT
                COALESCE(SUM(valor_debito), 0) AS debitos,
                COALESCE(SUM(valor_credito), 0) AS creditos
            FROM contabilidad.auxiliares_contables
            WHERE id_agencia = :agencia
              AND id_catalogo_cuenta = :cuenta
              AND estado_movimiento = 'A'
              AND fecha_auxiliar < :fechaInicial
            """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("agencia", idAgencia)
                .addValue("cuenta", idCatalogoCuenta)
                .addValue("fechaInicial", fechaInicial);
        return jdbc.queryForObject(sql, params, (rs, rowNum) ->
                new SaldoAnterior(
                        rs.getBigDecimal("debitos"),
                        rs.getBigDecimal("creditos")
                )
        );
    }

    /**
     * Recupera movimientos activos del período con terceros y auditoría.
     * Conserva los registros aunque no exista el tercero o usuario asociado.
     */
    public List<MovimientoAuxiliar> consultarMovimientos(
            Integer idAgencia,
            Integer idCatalogoCuenta,
            LocalDate fechaInicial,
            LocalDate fechaFinal
    ) {
        String sql = """
            SELECT
                ac.id_auxiliar_contable,
                ac.fecha_auxiliar,
                TRIM(ac.tipo_comprobante) AS tipo_comprobante,
                TRIM(ac.numero_comprobante) AS numero_comprobante,
                ac.detalle_movimiento,
                TRIM(ac.estado_movimiento) AS estado_movimiento,
                ac.id_datos_personal,
                dp.documento AS documento_tercero,
                CASE
                    WHEN dp.tipo_persona = '2' THEN
                        NULLIF(TRIM(dp.nombres), '')
                    ELSE
                        NULLIF(
                            TRIM(
                                CONCAT_WS(
                                    ' ',
                                    NULLIF(TRIM(dp.nombres), ''),
                                    NULLIF(TRIM(dp.primer_apellido), ''),
                                    NULLIF(TRIM(dp.segundo_apellido), '')
                                )
                            ),
                            ''
                        )
                END AS nombre_tercero,
                COALESCE(ac.valor_debito, 0) AS valor_debito,
                COALESCE(ac.valor_credito, 0) AS valor_credito,
                COALESCE(ac.valor_base, 0) AS valor_base,
                ac.fk_seguridad_creacion,
                uc.username AS username_creacion,
                uc.nombre_completo AS nombre_usuario_creacion,
                ac.fecha_creacion,
                ac.fk_seguridad_edicion,
                ue.username AS username_edicion,
                ue.nombre_completo AS nombre_usuario_edicion,
                ac.fecha_edicion,
                ac.origen_modulo,
                ac.documento_origen,
                ac.documento_soporte
            FROM contabilidad.auxiliares_contables ac
            LEFT JOIN hoja_vida.datos_personales dp
                ON dp.id_datos_personal = ac.id_datos_personal
            LEFT JOIN seguridad.usuarios uc
                ON uc.id_usuario = ac.fk_seguridad_creacion
            LEFT JOIN seguridad.usuarios ue
                ON ue.id_usuario = ac.fk_seguridad_edicion
            WHERE ac.id_agencia = :agencia
              AND ac.id_catalogo_cuenta = :cuenta
              AND ac.estado_movimiento = 'A'
              AND ac.fecha_auxiliar BETWEEN :fechaInicial AND :fechaFinal
            ORDER BY
                ac.fecha_auxiliar,
                ac.id_auxiliar_contable
            """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("agencia", idAgencia)
                .addValue("cuenta", idCatalogoCuenta)
                .addValue("fechaInicial", fechaInicial)
                .addValue("fechaFinal", fechaFinal);
        return jdbc.query(sql, params, (rs, rowNum) ->
                new MovimientoAuxiliar(
                        rs.getInt("id_auxiliar_contable"),
                        rs.getObject("fecha_auxiliar", LocalDate.class),
                        rs.getString("tipo_comprobante"),
                        rs.getString("numero_comprobante"),
                        rs.getString("detalle_movimiento"),
                        rs.getString("estado_movimiento"),
                        (Integer) rs.getObject("id_datos_personal"),
                        rs.getString("documento_tercero"),
                        rs.getString("nombre_tercero"),
                        rs.getBigDecimal("valor_debito"),
                        rs.getBigDecimal("valor_credito"),
                        rs.getBigDecimal("valor_base"),
                        rs.getInt("fk_seguridad_creacion"),
                        rs.getString("username_creacion"),
                        rs.getString("nombre_usuario_creacion"),
                        rs.getObject("fecha_creacion", LocalDateTime.class),
                        rs.getInt("fk_seguridad_edicion"),
                        rs.getString("username_edicion"),
                        rs.getString("nombre_usuario_edicion"),
                        rs.getObject("fecha_edicion", LocalDateTime.class),
                        rs.getString("origen_modulo"),
                        rs.getString("documento_origen"),
                        rs.getString("documento_soporte")
                )
        );
    }
}
