package co.assip.erp.cartera.seguros.administracion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/** Acceso a créditos desembolsados y a sus configuraciones de seguros. */
@Repository
public class AdministracionSegurosRepository {
    private final NamedParameterJdbcTemplate jdbc;

    public AdministracionSegurosRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Bloquea el crédito durante el alta para serializar registros concurrentes. */
    public boolean bloquearCreditoDesembolsado(Integer idCredito, Integer idAgencia) {
        String sql = """
            SELECT COUNT(*)
            FROM (
                SELECT 1
                FROM cartera.carteras_creditos c
                WHERE c.id_cartera_credito = :idCredito
                  AND c.id_agencia = :idAgencia
                  AND c.codigo_estado_cartera = 'A'
                  AND c.fecha_desembolso IS NOT NULL
                FOR UPDATE
            ) credito
            """;
        Integer cantidad = jdbc.queryForObject(sql, new MapSqlParameterSource()
                .addValue("idCredito", idCredito)
                .addValue("idAgencia", idAgencia), Integer.class);
        return cantidad != null && cantidad == 1;
    }

    /** Obtiene el porcentaje base predeterminado de seguros para la agencia (parámetro 641). */
    public Optional<BigDecimal> consultarPorcentajeBaseSeguro(Integer idAgencia) {
        String sql = """
            SELECT p.valor_parametro
            FROM general.parametros p
            WHERE p.id_agencia = :idAgencia
              AND p.codigo_parametro = 641
            """;
        List<BigDecimal> valores = jdbc.query(sql,
                new MapSqlParameterSource("idAgencia", idAgencia),
                (rs, rowNum) -> rs.getBigDecimal("valor_parametro"));
        return valores.stream().filter(v -> v != null).findFirst();
    }

    /** Consulta el seguro activo del crédito, si existe. */
    public Optional<SeguroCredito> consultarSeguroActivo(Integer idCredito) {
        String sql = """
            SELECT s.id_credito_seguro, s.id_cartera_credito,
                   s.porcentaje_base_seguro, s.tipo_cobro_seguro,
                   s.fecha_inicial_cobro, s.porcentaje_auxilio_entidad,
                   s.porcentaje_extraprima, s.porcentaje_aplicar,
                   s.sobre_saldo_actual, s.incluye_intereses_causados,
                   s.entidad_asume_seguro_base, s.activo
            FROM cartera.creditos_seguros s
            WHERE s.id_cartera_credito = :idCredito
              AND s.activo = TRUE
            ORDER BY s.id_credito_seguro DESC
            """;
        List<SeguroCredito> seguros = jdbc.query(sql,
                new MapSqlParameterSource("idCredito", idCredito),
                (rs, rowNum) -> new SeguroCredito(
                        rs.getInt("id_credito_seguro"),
                        rs.getInt("id_cartera_credito"),
                        rs.getBigDecimal("porcentaje_base_seguro"),
                        rs.getString("tipo_cobro_seguro"),
                        rs.getObject("fecha_inicial_cobro", LocalDate.class),
                        rs.getBigDecimal("porcentaje_auxilio_entidad"),
                        rs.getBigDecimal("porcentaje_extraprima"),
                        rs.getBigDecimal("porcentaje_aplicar"),
                        rs.getBoolean("sobre_saldo_actual"),
                        rs.getBoolean("incluye_intereses_causados"),
                        rs.getBoolean("entidad_asume_seguro_base"),
                        rs.getBoolean("activo")));
        return seguros.stream().findFirst();
    }

    /** Consulta todas las configuraciones, incluidas las inactivas, para mantenimiento. */
    public List<SeguroCredito> consultarSeguros(Integer idCredito) {
        String sql = """
            SELECT s.id_credito_seguro, s.id_cartera_credito,
                   s.porcentaje_base_seguro, s.tipo_cobro_seguro,
                   s.fecha_inicial_cobro, s.porcentaje_auxilio_entidad,
                   s.porcentaje_extraprima, s.porcentaje_aplicar,
                   s.sobre_saldo_actual, s.incluye_intereses_causados,
                   s.entidad_asume_seguro_base, s.activo
            FROM cartera.creditos_seguros s
            WHERE s.id_cartera_credito = :idCredito
            ORDER BY s.id_credito_seguro DESC
            """;
        return jdbc.query(sql, new MapSqlParameterSource("idCredito", idCredito),
                (rs, rowNum) -> new SeguroCredito(
                        rs.getInt("id_credito_seguro"),
                        rs.getInt("id_cartera_credito"),
                        rs.getBigDecimal("porcentaje_base_seguro"),
                        rs.getString("tipo_cobro_seguro"),
                        rs.getObject("fecha_inicial_cobro", LocalDate.class),
                        rs.getBigDecimal("porcentaje_auxilio_entidad"),
                        rs.getBigDecimal("porcentaje_extraprima"),
                        rs.getBigDecimal("porcentaje_aplicar"),
                        rs.getBoolean("sobre_saldo_actual"),
                        rs.getBoolean("incluye_intereses_causados"),
                        rs.getBoolean("entidad_asume_seguro_base"),
                        rs.getBoolean("activo")));
    }

    /** Consulta el saldo vigente del seguro para impedir desactivaciones con deuda. */
    public BigDecimal consultarSaldoSeguro(Integer idCreditoSeguro) {
        String sql = """
            SELECT COALESCE(SUM(d.valor_debito - d.valor_credito), 0)
            FROM cartera.creditos_seguros_detalle d
            WHERE d.id_credito_seguro = :idSeguro AND d.estado = 'A'
            """;
        return jdbc.queryForObject(sql,
                new MapSqlParameterSource("idSeguro", idCreditoSeguro), BigDecimal.class);
    }

    /** Cambia únicamente el estado, conservando identificador y auditoría. */
    public boolean actualizarEstado(Integer idCreditoSeguro, Integer idCredito,
                                    boolean activo, Integer idUsuario) {
        String sql = """
            UPDATE cartera.creditos_seguros
            SET activo = :activo,
                fk_seguridad_edicion = :usuario,
                fecha_edicion = CURRENT_TIMESTAMP
            WHERE id_credito_seguro = :idSeguro
              AND id_cartera_credito = :idCredito
            """;
        return jdbc.update(sql, new MapSqlParameterSource()
                .addValue("activo", activo)
                .addValue("usuario", idUsuario)
                .addValue("idSeguro", idCreditoSeguro)
                .addValue("idCredito", idCredito)) == 1;
    }

    /** Bloquea y recupera la configuración existente para una edición transaccional. */
    public Optional<SeguroMantenimiento> bloquearSeguroParaEdicion(Integer idCreditoSeguro, Integer idCredito) {
        String sql = """
            SELECT s.id_credito_seguro, s.id_cartera_credito,
                   s.porcentaje_base_seguro, s.tipo_cobro_seguro,
                   s.fecha_inicial_cobro, s.porcentaje_auxilio_entidad,
                   s.porcentaje_extraprima, s.porcentaje_aplicar,
                   s.sobre_saldo_actual, s.incluye_intereses_causados,
                   s.entidad_asume_seguro_base, s.activo,
                   s.fecha_creacion::date AS fecha_creacion
            FROM cartera.creditos_seguros s
            WHERE s.id_credito_seguro = :idSeguro
              AND s.id_cartera_credito = :idCredito
            FOR UPDATE
            """;
        List<SeguroMantenimiento> datos = jdbc.query(sql, new MapSqlParameterSource()
                .addValue("idSeguro", idCreditoSeguro)
                .addValue("idCredito", idCredito), (rs, n) -> new SeguroMantenimiento(
                rs.getInt("id_credito_seguro"), rs.getInt("id_cartera_credito"),
                rs.getBigDecimal("porcentaje_base_seguro"), rs.getString("tipo_cobro_seguro"),
                rs.getObject("fecha_inicial_cobro", LocalDate.class),
                rs.getBigDecimal("porcentaje_auxilio_entidad"),
                rs.getBigDecimal("porcentaje_extraprima"), rs.getBigDecimal("porcentaje_aplicar"),
                rs.getBoolean("sobre_saldo_actual"), rs.getBoolean("incluye_intereses_causados"),
                rs.getBoolean("entidad_asume_seguro_base"), rs.getBoolean("activo"),
                rs.getObject("fecha_creacion", LocalDate.class)));
        return datos.stream().findFirst();
    }

    /** Obtiene el inicio del tramo histórico aún no archivado. */
    public LocalDate consultarInicioVigenciaActual(Integer idCreditoSeguro, LocalDate fechaOrigen) {
        String sql = """
            SELECT COALESCE(MAX(h.fecha_fin_vigencia) + 1, :fechaOrigen)
            FROM cartera.creditos_seguros_historial h
            WHERE h.id_credito_seguro = :idSeguro
            """;
        return jdbc.queryForObject(sql, new MapSqlParameterSource()
                .addValue("idSeguro", idCreditoSeguro)
                .addValue("fechaOrigen", fechaOrigen), LocalDate.class);
    }

    /** Archiva las condiciones anteriores con el intervalo de vigencia indicado. */
    public void registrarHistorial(SeguroMantenimiento anterior, LocalDate desde,
                                   LocalDate hasta, Integer idUsuario) {
        String sql = """
            INSERT INTO cartera.creditos_seguros_historial (
                id_credito_seguro, fecha_inicio_vigencia, fecha_fin_vigencia,
                porcentaje_base_seguro, tipo_cobro_seguro, porcentaje_auxilio_entidad,
                porcentaje_extraprima, porcentaje_aplicar, sobre_saldo_actual,
                incluye_intereses_causados, entidad_asume_seguro_base, activo,
                fk_seguridad_creacion
            ) VALUES (
                :idSeguro, :desde, :hasta, :base, :tipo, :auxilio,
                :extraprima, :aplicar, :saldoActual, :intereses, :asumeBase,
                :activo, :usuario
            )
            """;
        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("idSeguro", anterior.idCreditoSeguro())
                .addValue("desde", desde).addValue("hasta", hasta)
                .addValue("base", anterior.porcentajeBaseSeguro())
                .addValue("tipo", anterior.tipoCobroSeguro())
                .addValue("auxilio", anterior.porcentajeAuxilioEntidad())
                .addValue("extraprima", anterior.porcentajeExtraprima())
                .addValue("aplicar", anterior.porcentajeAplicar())
                .addValue("saldoActual", anterior.sobreSaldoActual())
                .addValue("intereses", anterior.incluyeInteresesCausados())
                .addValue("asumeBase", anterior.entidadAsumeSeguroBase())
                .addValue("activo", anterior.activo())
                .addValue("usuario", idUsuario));
    }

    /** Modifica la configuración original sin crear otra cabecera. */
    public boolean actualizarSeguro(Integer idCreditoSeguro, Integer idCredito,
                                    ConfiguracionSeguro seguro, boolean activo, Integer idUsuario) {
        String sql = """
            UPDATE cartera.creditos_seguros SET
                porcentaje_base_seguro = :base,
                tipo_cobro_seguro = :tipo,
                fecha_inicial_cobro = :fechaInicial,
                porcentaje_auxilio_entidad = :auxilio,
                porcentaje_extraprima = :extraprima,
                porcentaje_aplicar = :aplicar,
                sobre_saldo_actual = :saldoActual,
                incluye_intereses_causados = :intereses,
                entidad_asume_seguro_base = :asumeBase,
                activo = :activo,
                fk_seguridad_edicion = :usuario,
                fecha_edicion = CURRENT_TIMESTAMP
            WHERE id_credito_seguro = :idSeguro
              AND id_cartera_credito = :idCredito
            """;
        return jdbc.update(sql, new MapSqlParameterSource()
                .addValue("idSeguro", idCreditoSeguro).addValue("idCredito", idCredito)
                .addValue("base", seguro.porcentajeBaseSeguro())
                .addValue("tipo", seguro.tipoCobroSeguro())
                .addValue("fechaInicial", seguro.fechaInicialCobro())
                .addValue("auxilio", seguro.porcentajeAuxilioEntidad())
                .addValue("extraprima", seguro.porcentajeExtraprima())
                .addValue("aplicar", seguro.porcentajeAplicar())
                .addValue("saldoActual", seguro.sobreSaldoActual())
                .addValue("intereses", seguro.incluyeInteresesCausados())
                .addValue("asumeBase", seguro.entidadAsumeSeguroBase())
                .addValue("activo", activo).addValue("usuario", idUsuario)) == 1;
    }

    /** Estado y condiciones previas del seguro, incluidos datos para calcular vigencia. */
    public record SeguroMantenimiento(
            Integer idCreditoSeguro, Integer idCarteraCredito,
            BigDecimal porcentajeBaseSeguro, String tipoCobroSeguro,
            LocalDate fechaInicialCobro, BigDecimal porcentajeAuxilioEntidad,
            BigDecimal porcentajeExtraprima, BigDecimal porcentajeAplicar,
            boolean sobreSaldoActual, boolean incluyeInteresesCausados,
            boolean entidadAsumeSeguroBase, boolean activo, LocalDate fechaCreacion) {
    }


    /** Crea el seguro de Vida Deudores y asigna automáticamente el tipo de seguro 1. */
    public Integer crearSeguro(Integer idCredito, ConfiguracionSeguro seguro, Integer idUsuario) {
        String sql = """
        INSERT INTO cartera.creditos_seguros (
            id_cartera_credito, id_tipo_seguro,
            porcentaje_base_seguro, tipo_cobro_seguro,
            activo, fk_seguridad_creacion, fk_seguridad_edicion,
            fecha_inicial_cobro, porcentaje_auxilio_entidad,
            porcentaje_extraprima, porcentaje_aplicar,
            sobre_saldo_actual, incluye_intereses_causados,
            entidad_asume_seguro_base
        )
        SELECT :idCredito, 1,
               :porcentajeBase, :tipoCobro,
               TRUE, :idUsuario, :idUsuario,
               :fechaInicial, :porcentajeAuxilio,
               :porcentajeExtraprima, :porcentajeAplicar,
               :sobreSaldoActual, :incluyeIntereses,
               :entidadAsumeBase
        WHERE NOT EXISTS (
            SELECT 1
            FROM cartera.creditos_seguros
            WHERE id_cartera_credito = :idCredito
        )
        RETURNING id_credito_seguro
        """;
        List<Integer> ids = jdbc.query(sql, new MapSqlParameterSource()
                        .addValue("idCredito", idCredito)
                        .addValue("porcentajeBase", seguro.porcentajeBaseSeguro())
                        .addValue("tipoCobro", seguro.tipoCobroSeguro())
                        .addValue("idUsuario", idUsuario)
                        .addValue("fechaInicial", seguro.fechaInicialCobro())
                        .addValue("porcentajeAuxilio", seguro.porcentajeAuxilioEntidad())
                        .addValue("porcentajeExtraprima", seguro.porcentajeExtraprima())
                        .addValue("porcentajeAplicar", seguro.porcentajeAplicar())
                        .addValue("sobreSaldoActual", seguro.sobreSaldoActual())
                        .addValue("incluyeIntereses", seguro.incluyeInteresesCausados())
                        .addValue("entidadAsumeBase", seguro.entidadAsumeSeguroBase()),
                (rs, rowNum) -> rs.getInt("id_credito_seguro"));
        return ids.isEmpty() ? null : ids.get(0);
    }


    /** Datos persistidos de una configuración activa. */
    public record SeguroCredito(
            Integer idCreditoSeguro,
            Integer idCarteraCredito,
            BigDecimal porcentajeBaseSeguro,
            String tipoCobroSeguro,
            LocalDate fechaInicialCobro,
            BigDecimal porcentajeAuxilioEntidad,
            BigDecimal porcentajeExtraprima,
            BigDecimal porcentajeAplicar,
            boolean sobreSaldoActual,
            boolean incluyeInteresesCausados,
            boolean entidadAsumeSeguroBase,
            boolean activo) {
    }

    /** Condiciones validadas por el servicio antes de crear la cabecera. */
    public record ConfiguracionSeguro(
            BigDecimal porcentajeBaseSeguro,
            String tipoCobroSeguro,
            LocalDate fechaInicialCobro,
            BigDecimal porcentajeAuxilioEntidad,
            BigDecimal porcentajeExtraprima,
            BigDecimal porcentajeAplicar,
            boolean sobreSaldoActual,
            boolean incluyeInteresesCausados,
            boolean entidadAsumeSeguroBase) {
    }
}
