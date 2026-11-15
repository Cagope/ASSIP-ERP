package co.assip.erp.cartera.seguros.pagoextraordinario;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Persistencia del pago y la corrección de saldos de seguros de Cartera. */
@Repository
public class PagoExtraordinarioSegurosRepository {
    private final NamedParameterJdbcTemplate jdbc;

    public PagoExtraordinarioSegurosRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Consulta el seguro activo y sus terceros, sin modificar registros. */
    public Optional<DatosSeguro> consultarSeguro(Integer idCredito) {
        String sql = """
            SELECT c.id_cartera_credito, c.id_agencia,
                   c.id_datos_personal AS id_asociado,
                   s.id_credito_seguro, s.id_tipo_seguro,
                   s.porcentaje_base_seguro, s.porcentaje_extraprima,
                   s.porcentaje_aplicar, s.sobre_saldo_actual,
                   s.incluye_intereses_causados,
                   t.id_datos_personal_aseguradora AS id_aseguradora
            FROM cartera.carteras_creditos c
            JOIN cartera.creditos_seguros s
              ON s.id_cartera_credito = c.id_cartera_credito
             AND s.activo = TRUE
            JOIN cartera.tipos_seguros t
              ON t.id_tipo_seguro = s.id_tipo_seguro
             AND t.activo = TRUE
            WHERE c.id_cartera_credito = :credito
            ORDER BY s.id_credito_seguro DESC
            """;
        List<DatosSeguro> datos = jdbc.query(sql,
                new MapSqlParameterSource("credito", idCredito),
                (rs, fila) -> new DatosSeguro(
                        rs.getInt("id_cartera_credito"),
                        rs.getInt("id_agencia"),
                        rs.getInt("id_asociado"),
                        rs.getInt("id_credito_seguro"),
                        rs.getInt("id_tipo_seguro"),
                        rs.getBigDecimal("porcentaje_base_seguro"),
                        rs.getBigDecimal("porcentaje_extraprima"),
                        rs.getBigDecimal("porcentaje_aplicar"),
                        rs.getBoolean("sobre_saldo_actual"),
                        rs.getBoolean("incluye_intereses_causados"),
                        rs.getInt("id_aseguradora")));
        if (datos.size() > 1) {
            throw new IllegalStateException("El crédito tiene más de un seguro activo.");
        }
        return datos.stream().findFirst();
    }

    /** Bloquea primero el crédito y después el seguro, para revalidar el saldo al confirmar. */
    @Transactional(propagation = Propagation.MANDATORY)
    public DatosSeguro bloquearSeguro(Integer idCredito) {
        if (idCredito == null || idCredito <= 0) {
            throw new IllegalArgumentException("Seleccione un crédito válido.");
        }
        List<Integer> creditos = jdbc.query("""
                SELECT id_cartera_credito
                FROM cartera.carteras_creditos
                WHERE id_cartera_credito = :credito
                FOR UPDATE
                """, new MapSqlParameterSource("credito", idCredito),
                (rs, fila) -> rs.getInt(1));
        if (creditos.isEmpty()) {
            throw new IllegalArgumentException("El crédito seleccionado no existe.");
        }
        jdbc.query("""
                SELECT id_credito_seguro
                FROM cartera.creditos_seguros
                WHERE id_cartera_credito = :credito AND activo = TRUE
                ORDER BY id_credito_seguro
                FOR UPDATE
                """, new MapSqlParameterSource("credito", idCredito),
                (rs, fila) -> rs.getInt(1));
        return consultarSeguro(idCredito).orElseThrow(() ->
                new IllegalArgumentException("El crédito no tiene un seguro activo configurado."));
    }

    /** Suma únicamente movimientos activos: cargos menos pagos y correcciones. */
    public BigDecimal consultarSaldoSeguro(Integer idCreditoSeguro) {
        return jdbc.queryForObject("""
                SELECT COALESCE(SUM(valor_debito - valor_credito), 0)
                FROM cartera.creditos_seguros_detalle
                WHERE id_credito_seguro = :seguro AND estado = 'A'
                """, new MapSqlParameterSource("seguro", idCreditoSeguro), BigDecimal.class);
    }

    /** Resuelve una cuenta contable general activa y operable para la agencia. */
    public CuentaContable resolverCuenta(Integer idAgencia, String concepto) {
        String sql = """
            SELECT p.id_catalogo_cuenta, p.codigo_cuenta,
                   c.nombre_cuenta, c.naturaleza_cuenta
            FROM cartera.parametros_contables p
            JOIN contabilidad.catalogo_cuentas c
              ON c.id_catalogo_cuenta = p.id_catalogo_cuenta
             AND c.id_agencia = p.id_agencia
            WHERE p.id_agencia = :agencia
              AND p.concepto = :concepto
              AND p.id_linea_credito IS NULL
              AND p.activo = TRUE
              AND c.cuenta_operable = TRUE
            """;
        List<CuentaContable> cuentas = jdbc.query(sql,
                new MapSqlParameterSource("agencia", idAgencia)
                        .addValue("concepto", concepto),
                (rs, fila) -> new CuentaContable(
                        rs.getInt("id_catalogo_cuenta"),
                        rs.getString("codigo_cuenta"),
                        rs.getString("nombre_cuenta"),
                        rs.getString("naturaleza_cuenta")));
        if (cuentas.size() != 1) {
            throw new IllegalStateException("Se requiere exactamente una cuenta activa para "
                    + concepto + " en la agencia " + idAgencia + ".");
        }
        return cuentas.get(0);
    }

    /** Lista las cuentas de ahorros operativas del asociado en la agencia del crédito. */
    public List<CuentaAhorro> listarCuentasAhorro(Integer idCredito) {
        String sql = """
            SELECT a.id_cuenta_ahorro, a.id_agencia,
                   TRIM(a.codigo_cuenta) AS codigo_cuenta,
                   f.nombre_forma, a.saldo_actual_cuenta
            FROM cartera.carteras_creditos cr
            JOIN depositos.cuentas_ahorro a
              ON a.id_datos_personal = cr.id_datos_personal
             AND a.id_agencia = cr.id_agencia
            JOIN depositos.formas_ahorro f
              ON f.id_forma_ahorro = a.id_forma_ahorro
             AND f.id_agencia = a.id_agencia
            WHERE cr.id_cartera_credito = :credito
              AND TRIM(f.tipo_captacion_forma) = '2'
              AND TRIM(a.estado_cuenta_cuenta) = 'A'
              AND EXISTS (
                  SELECT 1 FROM depositos.estados_ahorros ea
                  WHERE TRIM(ea.codigo_estado_ahorro) = TRIM(a.cuenta_activa)
                    AND ea.operativo = TRUE
              )
            ORDER BY f.codigo_forma, a.codigo_cuenta, a.id_cuenta_ahorro
            """;
        return jdbc.query(sql, new MapSqlParameterSource("credito", idCredito),
                (rs, fila) -> new CuentaAhorro(
                        rs.getInt("id_cuenta_ahorro"),
                        rs.getInt("id_agencia"),
                        rs.getString("codigo_cuenta"),
                        rs.getString("nombre_forma"),
                        rs.getBigDecimal("saldo_actual_cuenta")));
    }

    /** Registra una disminución del seguro; exige saldo suficiente bajo bloqueo previo. */
    @Transactional(propagation = Propagation.MANDATORY)
    public Integer registrarDisminucion(DatosSeguro seguro, LocalDate fecha,
                                        BigDecimal valor, String numeroComprobante,
                                        String observacion, Integer idUsuario) {
        if (seguro == null || fecha == null || valor == null || valor.signum() <= 0
                || numeroComprobante == null || numeroComprobante.isBlank()
                || observacion == null || observacion.isBlank()
                || observacion.length() > 500
                || idUsuario == null || idUsuario <= 0) {
            throw new IllegalArgumentException("Los datos de disminución del seguro son inválidos.");
        }
        BigDecimal saldo = consultarSaldoSeguro(seguro.idCreditoSeguro());
        if (valor.compareTo(saldo) > 0) {
            throw new IllegalArgumentException("El valor excede el saldo pendiente del seguro.");
        }
        String sql = """
            INSERT INTO cartera.creditos_seguros_detalle (
                id_credito_seguro, fecha_movimiento,
                saldo_base, intereses_causados_base, otros_base, base_calculo,
                porcentaje_base_seguro, porcentaje_extraprima, porcentaje_aplicar,
                sobre_saldo_actual, incluye_intereses_causados,
                valor_debito, valor_credito,
                tipo_comprobante, numero_comprobante,
                observacion_seguro, estado,
                fk_seguridad_creacion, fk_seguridad_edicion
            ) VALUES (
                :seguro, :fecha,
                0, 0, 0, 0,
                :porcentajeBase, :extraprima, :porcentajeAplicar,
                :sobreSaldoActual, FALSE,
                0, :valor,
                'CC', :numero,
                :observacion, 'A',
                :usuario, :usuario
            )
            RETURNING id_credito_seguro_detalle
            """;
        return jdbc.queryForObject(sql, new MapSqlParameterSource()
                .addValue("seguro", seguro.idCreditoSeguro())
                .addValue("fecha", fecha)
                .addValue("porcentajeBase", seguro.porcentajeBaseSeguro())
                .addValue("extraprima", seguro.porcentajeExtraprima())
                .addValue("porcentajeAplicar", seguro.porcentajeAplicar())
                .addValue("sobreSaldoActual", seguro.sobreSaldoActual())
                .addValue("valor", valor)
                .addValue("numero", numeroComprobante.trim())
                .addValue("observacion", observacion.trim())
                .addValue("usuario", idUsuario), Integer.class);
    }

    public record DatosSeguro(Integer idCredito, Integer idAgencia,
                              Integer idAsociado, Integer idCreditoSeguro,
                              Integer idTipoSeguro, BigDecimal porcentajeBaseSeguro,
                              BigDecimal porcentajeExtraprima, BigDecimal porcentajeAplicar,
                              boolean sobreSaldoActual, boolean incluyeInteresesCausados,
                              Integer idAseguradora) { }

    public record CuentaContable(Integer idCatalogoCuenta, String codigoCuenta,
                                 String nombreCuenta, String naturalezaCuenta) { }

    public record CuentaAhorro(Integer idCuentaAhorro, Integer idAgencia,
                               String codigoCuenta, String nombreForma,
                               BigDecimal saldoDisponible) { }
}
