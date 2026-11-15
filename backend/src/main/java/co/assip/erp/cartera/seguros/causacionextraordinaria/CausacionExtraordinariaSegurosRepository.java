package co.assip.erp.cartera.seguros.causacionextraordinaria;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Consultas y persistencia de causaciones extraordinarias de seguros de Cartera. */
@Repository
public class CausacionExtraordinariaSegurosRepository {
    private static final String OBSERVACION_EXTRAORDINARIA = "CAUSACION EXTRAORDINARIA SEGURO";
    private final NamedParameterJdbcTemplate jdbc;

    public CausacionExtraordinariaSegurosRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Consulta el crédito y la configuración activa sin modificar saldos ni porcentajes. */
    public Optional<DatosSeguro> consultarSeguro(Integer idCredito) {
        String sql = """
            SELECT c.id_cartera_credito, c.id_agencia,
                   c.id_datos_personal AS id_asociado,
                   c.codigo_estado_cartera, c.saldo_actual,
                   c.valor_desembolsado,
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
            WHERE c.id_cartera_credito = :idCredito
            ORDER BY s.id_credito_seguro DESC
            """;
        List<DatosSeguro> datos = jdbc.query(sql,
                new MapSqlParameterSource("idCredito", idCredito),
                (rs, fila) -> new DatosSeguro(
                        rs.getInt("id_cartera_credito"),
                        rs.getInt("id_agencia"),
                        rs.getInt("id_asociado"),
                        rs.getString("codigo_estado_cartera"),
                        rs.getBigDecimal("saldo_actual"),
                        rs.getBigDecimal("valor_desembolsado"),
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

    /** Bloquea crédito y seguro en orden estable para serializar la causación con otros movimientos. */
    @Transactional(propagation = Propagation.MANDATORY)
    public DatosSeguro bloquearSeguro(Integer idCredito) {
        if (idCredito == null || idCredito <= 0) {
            throw new IllegalArgumentException("Seleccione un crédito válido.");
        }
        String bloqueoCredito = """
            SELECT id_cartera_credito
            FROM cartera.carteras_creditos
            WHERE id_cartera_credito = :idCredito
            FOR UPDATE
            """;
        List<Integer> creditos = jdbc.query(bloqueoCredito,
                new MapSqlParameterSource("idCredito", idCredito),
                (rs, fila) -> rs.getInt(1));
        if (creditos.isEmpty()) {
            throw new IllegalArgumentException("El crédito seleccionado no existe.");
        }
        String bloqueoSeguro = """
            SELECT id_credito_seguro
            FROM cartera.creditos_seguros
            WHERE id_cartera_credito = :idCredito AND activo = TRUE
            ORDER BY id_credito_seguro
            FOR UPDATE
            """;
        jdbc.query(bloqueoSeguro, new MapSqlParameterSource("idCredito", idCredito),
                (rs, fila) -> rs.getInt(1));
        return consultarSeguro(idCredito).orElseThrow(() ->
                new IllegalArgumentException("El crédito no tiene un seguro activo con aseguradora configurada."));
    }

    /** Resuelve una cuenta operable de la agencia mediante el concepto contable parametrizado. */
    public CuentaContable resolverCuenta(Integer idAgencia, String concepto) {
        String sql = """
            SELECT p.id_catalogo_cuenta, p.codigo_cuenta, c.nombre_cuenta,
                   c.naturaleza_cuenta
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
        List<CuentaContable> cuentas = jdbc.query(sql, new MapSqlParameterSource()
                        .addValue("agencia", idAgencia)
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

    /** Consulta los documentos de asociado y aseguradora para la vista previa de la NC. */
    public String consultarDocumentoTercero(Integer idDatosPersonal) {
        String sql = """
            SELECT documento
            FROM hoja_vida.datos_personales
            WHERE id_datos_personal = :tercero
            """;
        List<String> documentos = jdbc.query(sql,
                new MapSqlParameterSource("tercero", idDatosPersonal),
                (rs, fila) -> rs.getString("documento"));
        if (documentos.size() != 1) {
            throw new IllegalStateException("No existe el tercero contable " + idDatosPersonal + ".");
        }
        return documentos.get(0);
    }

    /** Calcula el saldo activo de seguros, incluyendo causaciones extraordinarias y pagos. */
    public BigDecimal consultarSaldoSeguro(Integer idCreditoSeguro) {
        String sql = """
            SELECT COALESCE(SUM(d.valor_debito - d.valor_credito), 0)
            FROM cartera.creditos_seguros_detalle d
            WHERE d.id_credito_seguro = :seguro
              AND d.estado = 'A'
            """;
        return jdbc.queryForObject(sql,
                new MapSqlParameterSource("seguro", idCreditoSeguro), BigDecimal.class);
    }

    /** Registra el cargo extraordinario con la base y tasas efectivamente usadas y la NC generada. */
    @Transactional(propagation = Propagation.MANDATORY)
    public Integer registrarCausacion(DatosSeguro seguro, LocalDate fecha,
                                      BigDecimal saldoBase, BigDecimal valor,
                                      String numeroComprobante, String justificacion,
                                      Integer idUsuario) {
        if (seguro == null || fecha == null || saldoBase == null
                || saldoBase.signum() < 0 || valor == null || valor.signum() <= 0
                || numeroComprobante == null || numeroComprobante.isBlank()
                || justificacion == null || justificacion.isBlank()
                || idUsuario == null || idUsuario <= 0) {
            throw new IllegalArgumentException("La causación extraordinaria está incompleta.");
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
                :saldoBase, 0, 0, :saldoBase,
                :porcentajeBase, :extraprima, :porcentajeAplicar,
                :sobreSaldoActual, FALSE,
                :valor, 0,
                'NC', :numero,
                :observacion, 'A',
                :usuario, :usuario
            )
            RETURNING id_credito_seguro_detalle
            """;
        return jdbc.queryForObject(sql, new MapSqlParameterSource()
                .addValue("seguro", seguro.idCreditoSeguro())
                .addValue("fecha", fecha)
                .addValue("saldoBase", saldoBase)
                .addValue("porcentajeBase", seguro.porcentajeBaseSeguro())
                .addValue("extraprima", seguro.porcentajeExtraprima())
                .addValue("porcentajeAplicar", seguro.porcentajeAplicar())
                .addValue("sobreSaldoActual", seguro.sobreSaldoActual())
                .addValue("valor", valor)
                .addValue("numero", numeroComprobante.trim())
                .addValue("observacion", OBSERVACION_EXTRAORDINARIA + " / " + justificacion.trim())
                .addValue("usuario", idUsuario), Integer.class);
    }

    public record DatosSeguro(
            Integer idCredito, Integer idAgencia, Integer idAsociado,
            String estadoCredito, BigDecimal saldoActual, BigDecimal valorDesembolsado,
            Integer idCreditoSeguro, Integer idTipoSeguro,
            BigDecimal porcentajeBaseSeguro, BigDecimal porcentajeExtraprima,
            BigDecimal porcentajeAplicar, boolean sobreSaldoActual,
            boolean incluyeInteresesCausados, Integer idAseguradora) { }

    public record CuentaContable(
            Integer idCatalogoCuenta, String codigoCuenta,
            String nombreCuenta, String naturalezaCuenta) { }
}
