package co.assip.erp.cartera.prorrogas_v1;

import co.assip.erp.cartera.prorrogas_v1.dto.ProrrogasV1HistorialDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Consultas y bloqueos de crédito e historial para Prórrogas V1. */
@Repository
@RequiredArgsConstructor
public class ProrrogasV1Repository {
    private final NamedParameterJdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    /** Bloquea el crédito dentro de la transacción que coordina la operación. */
    @Transactional(propagation = Propagation.MANDATORY)
    public CreditoProrroga bloquearCredito(Integer idCredito) {
        validarCredito(idCredito);
        String sql = """
                SELECT id_cartera_credito, id_agencia, codigo_estado_cartera,
                       saldo_actual, altura_cuota, ultima_fecha_capital,
                       ultima_fecha_interes, proxima_fecha_capital, proxima_fecha_interes,
                       fecha_final, plazo
                FROM cartera.carteras_creditos
                WHERE id_cartera_credito = :credito
                FOR UPDATE
                """;
        return jdbc.query(sql, new MapSqlParameterSource("credito", idCredito),
                        (rs, i) -> new CreditoProrroga(
                                rs.getInt("id_cartera_credito"),
                                rs.getInt("id_agencia"),
                                rs.getString("codigo_estado_cartera"),
                                rs.getBigDecimal("saldo_actual"),
                                rs.getObject("altura_cuota", Integer.class),
                                fecha(rs.getDate("ultima_fecha_capital")),
                                fecha(rs.getDate("ultima_fecha_interes")),
                                fecha(rs.getDate("proxima_fecha_capital")),
                                fecha(rs.getDate("proxima_fecha_interes")),
                                fecha(rs.getDate("fecha_final")),
                                rs.getObject("plazo", Integer.class)))
                .stream().findFirst().orElseThrow(() ->
                        new IllegalArgumentException("No existe el crédito seleccionado."));
    }

    /** Comprueba si hay una prórroga activa después del bloqueo del crédito. */
    @Transactional(propagation = Propagation.MANDATORY)
    public boolean existeProrrogaActiva(Integer idCredito) {
        validarCredito(idCredito);
        String sql = """
                SELECT EXISTS (
                    SELECT 1 FROM cartera.creditos_prorrogas
                    WHERE id_cartera_credito = :credito AND estado_prorroga = 'A'
                )
                """;
        return Boolean.TRUE.equals(jdbc.queryForObject(sql,
                new MapSqlParameterSource("credito", idCredito), Boolean.class));
    }

    /** Calcula el siguiente consecutivo con el crédito bloqueado en la misma transacción. */
    @Transactional(propagation = Propagation.MANDATORY)
    public int siguienteNumeroProrroga(Integer idCredito) {
        validarCredito(idCredito);
        String sql = """
                SELECT COALESCE(MAX(numero_prorroga), 0) + 1
                FROM cartera.creditos_prorrogas
                WHERE id_cartera_credito = :credito
                """;
        return jdbc.queryForObject(sql,
                new MapSqlParameterSource("credito", idCredito), Integer.class);
    }

    /** Recupera el historial completo sin alterar las prórrogas registradas. */
    public List<ProrrogasV1HistorialDTO> listarHistorial(Integer idCredito) {
        validarCredito(idCredito);
        String sql = """
                SELECT id_credito_prorroga, id_cartera_credito, numero_prorroga,
                       fecha_prorroga, dias_prorroga, meses_prorroga,
                       proxima_fecha_capital_anterior, proxima_fecha_interes_anterior,
                       fecha_vencimiento_anterior, plazo_anterior,
                       proxima_fecha_capital_nueva, proxima_fecha_interes_nueva,
                       fecha_vencimiento_nueva, plazo_nuevo,
                       valor_interes_corriente, valor_interes_mora, valor_seguro,
                       valor_aportes, valor_fondo_garantia, valor_otros_conceptos,
                       valor_total_pagado, tipo_comprobante, numero_comprobante,
                       fecha_comprobante, id_agencia, estado_prorroga,
                       fecha_aplicacion, observacion
                FROM cartera.creditos_prorrogas
                WHERE id_cartera_credito = :credito
                ORDER BY numero_prorroga DESC, id_credito_prorroga DESC
                """;
        return jdbc.query(sql, new MapSqlParameterSource("credito", idCredito),
                (rs, i) -> new ProrrogasV1HistorialDTO(
                        rs.getLong("id_credito_prorroga"),
                        rs.getInt("id_cartera_credito"),
                        rs.getInt("numero_prorroga"),
                        fecha(rs.getDate("fecha_prorroga")),
                        rs.getObject("dias_prorroga", Integer.class),
                        rs.getInt("meses_prorroga"),
                        fecha(rs.getDate("proxima_fecha_capital_anterior")),
                        fecha(rs.getDate("proxima_fecha_interes_anterior")),
                        fecha(rs.getDate("fecha_vencimiento_anterior")),
                        rs.getObject("plazo_anterior", Integer.class),
                        fecha(rs.getDate("proxima_fecha_capital_nueva")),
                        fecha(rs.getDate("proxima_fecha_interes_nueva")),
                        fecha(rs.getDate("fecha_vencimiento_nueva")),
                        rs.getObject("plazo_nuevo", Integer.class),
                        rs.getBigDecimal("valor_interes_corriente"),
                        rs.getBigDecimal("valor_interes_mora"),
                        rs.getBigDecimal("valor_seguro"),
                        rs.getBigDecimal("valor_aportes"),
                        rs.getBigDecimal("valor_fondo_garantia"),
                        rs.getBigDecimal("valor_otros_conceptos"),
                        rs.getBigDecimal("valor_total_pagado"),
                        rs.getString("tipo_comprobante"),
                        rs.getString("numero_comprobante"),
                        fecha(rs.getDate("fecha_comprobante")),
                        rs.getObject("id_agencia", Integer.class),
                        rs.getString("estado_prorroga"),
                        fechaHora(rs.getTimestamp("fecha_aplicacion")),
                        rs.getString("observacion")));
    }

    /**
     * Registra la prórroga ya liquidada y recaudada dentro de la transacción vigente.
     * El servicio debe bloquear previamente el crédito, validar agencia y comprobar
     * que no exista una prórroga activa. No realiza ningún recaudo por sí mismo.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public Long registrarProrroga(RegistroProrroga registro) {
        if (registro == null || registro.idCredito() == null || registro.idUsuario() == null
                || registro.idUsuario() <= 0 || registro.numeroProrroga() == null
                || registro.numeroProrroga() <= 0 || registro.idAgencia() == null
                || registro.idAgencia() <= 0 || registro.fechaProrroga() == null
                || registro.mesesProrroga() == null || registro.diasProrroga() == null
                || registro.diasProrroga() <= 0 || registro.proximaCapitalAnterior() == null
                || registro.proximaCapitalNueva() == null || registro.interesCorriente() == null
                || registro.interesCorriente().signum() < 0 || registro.totalPagado() == null
                || registro.totalPagado().signum() <= 0) {
            throw new IllegalArgumentException("Registro de prórroga incompleto o inválido.");
        }
        String sql = """
                INSERT INTO cartera.creditos_prorrogas (
                    id_cartera_credito, numero_prorroga, fecha_prorroga,
                    dias_prorroga, meses_prorroga,
                    proxima_fecha_capital_anterior, proxima_fecha_interes_anterior,
                    fecha_vencimiento_anterior, plazo_anterior,
                    proxima_fecha_capital_nueva, proxima_fecha_interes_nueva,
                    fecha_vencimiento_nueva, plazo_nuevo,
                    valor_interes_corriente, valor_interes_mora, valor_seguro,
                    valor_aportes, valor_fondo_garantia, valor_otros_conceptos,
                    valor_total_pagado, tipo_comprobante, numero_comprobante,
                    fecha_comprobante, id_agencia, estado_prorroga,
                    fecha_aplicacion, observacion, fk_seguridad_creacion
                ) VALUES (
                    :credito, :numero, :fecha, :dias, :meses,
                    :capitalAnterior, :interesAnterior, :vencimientoAnterior, :plazoAnterior,
                    :capitalNueva, :interesNueva, :vencimientoNuevo, :plazoNuevo,
                    :interesCorriente, :interesMora, :seguro, :aportes, :fondo,
                    :otros, :total, :tipoComprobante, :numeroComprobante,
                    :fechaComprobante, :agencia, 'A', CURRENT_TIMESTAMP,
                    :observacion, :usuario
                ) RETURNING id_credito_prorroga
                """;
        var p = new MapSqlParameterSource()
                .addValue("credito", registro.idCredito())
                .addValue("numero", registro.numeroProrroga())
                .addValue("fecha", registro.fechaProrroga())
                .addValue("dias", registro.diasProrroga())
                .addValue("meses", registro.mesesProrroga())
                .addValue("capitalAnterior", registro.proximaCapitalAnterior())
                .addValue("interesAnterior", registro.proximaInteresAnterior())
                .addValue("vencimientoAnterior", registro.vencimientoAnterior())
                .addValue("plazoAnterior", registro.plazoAnterior())
                .addValue("capitalNueva", registro.proximaCapitalNueva())
                .addValue("interesNueva", registro.proximaInteresNueva())
                .addValue("vencimientoNuevo", registro.vencimientoNuevo())
                .addValue("plazoNuevo", registro.plazoNuevo())
                .addValue("interesCorriente", registro.interesCorriente())
                .addValue("interesMora", cero(registro.interesMora()))
                .addValue("seguro", cero(registro.seguro()))
                .addValue("aportes", cero(registro.aportes()))
                .addValue("fondo", cero(registro.fondoGarantia()))
                .addValue("otros", cero(registro.otrosConceptos()))
                .addValue("total", registro.totalPagado())
                .addValue("tipoComprobante", registro.tipoComprobante())
                .addValue("numeroComprobante", registro.numeroComprobante())
                .addValue("fechaComprobante", registro.fechaComprobante())
                .addValue("agencia", registro.idAgencia())
                .addValue("observacion", registro.observacion())
                .addValue("usuario", registro.idUsuario());
        return jdbc.queryForObject(sql, p, Long.class);
    }


    /**
     * Aplica exclusivamente el abono opcional a capital de una prórroga.
     * Conserva altura de cuota, fechas y estado del crédito.
     * Debe ejecutarse dentro de la transacción financiera del otorgamiento.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void aplicarAbonoCapitalProrroga(
            Integer idCredito,
            Integer idAgencia,
            BigDecimal saldoAnterior,
            BigDecimal valorCapital,
            Integer idUsuario) {

        validarCredito(idCredito);

        if (idAgencia == null || idAgencia <= 0
                || idUsuario == null || idUsuario <= 0
                || saldoAnterior == null || saldoAnterior.signum() <= 0
                || valorCapital == null || valorCapital.signum() <= 0
                || valorCapital.stripTrailingZeros().scale() > 0
                || valorCapital.compareTo(saldoAnterior) >= 0) {
            throw new IllegalArgumentException(
                    "El abono a capital debe ser positivo e inferior al saldo del crédito.");
        }

        String sql = """
                UPDATE cartera.carteras_creditos
                   SET saldo_actual = saldo_actual - :capital,
                       fk_seguridad_edicion = :usuario,
                       fecha_edicion = CURRENT_TIMESTAMP
                 WHERE id_cartera_credito = :credito
                   AND id_agencia = :agencia
                   AND codigo_estado_cartera = 'A'
                   AND saldo_actual = :saldoAnterior
                """;

        var parametros = new MapSqlParameterSource()
                .addValue("credito", idCredito)
                .addValue("agencia", idAgencia)
                .addValue("saldoAnterior", saldoAnterior)
                .addValue("capital", valorCapital)
                .addValue("usuario", idUsuario);

        if (jdbc.update(sql, parametros) != 1) {
            throw new IllegalStateException(
                    "El saldo del crédito cambió; no fue posible aplicar el abono a capital.");
        }
    }


    /**
     * Actualiza próximas fechas y vencimiento, sin modificar plazo, saldo ni altura de cuota.
     * El crédito debe estar bloqueado y el recaudo aplicado en la misma transacción.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void actualizarFechasProrroga(Integer idCredito, Integer idAgencia,
                                         LocalDate capitalAnterior, LocalDate interesAnterior,
                                         LocalDate capitalNueva, LocalDate interesNueva,
                                         LocalDate vencimientoAnterior, LocalDate vencimientoNuevo,
                                         Integer idUsuario) {
        validarCredito(idCredito);
        if (idAgencia == null || idUsuario == null || idUsuario <= 0
                || capitalAnterior == null || capitalNueva == null
                || vencimientoAnterior == null || vencimientoNuevo == null
                || !vencimientoNuevo.isAfter(vencimientoAnterior)) {
            throw new IllegalArgumentException("Datos insuficientes para actualizar fechas.");
        }
        String sql = """
                UPDATE cartera.carteras_creditos SET
                    proxima_fecha_capital = :capitalNueva,
                    proxima_fecha_interes = :interesNueva,
                    fecha_final = :vencimientoNuevo,
                    fk_seguridad_edicion = :usuario,
                    fecha_edicion = CURRENT_TIMESTAMP
                WHERE id_cartera_credito = :credito
                  AND id_agencia = :agencia
                  AND codigo_estado_cartera = 'A'
                  AND proxima_fecha_capital = :capitalAnterior
                  AND proxima_fecha_interes IS NOT DISTINCT FROM :interesAnterior
                  AND fecha_final = :vencimientoAnterior
                """;
        var p = new MapSqlParameterSource()
                .addValue("credito", idCredito)
                .addValue("agencia", idAgencia)
                .addValue("capitalAnterior", capitalAnterior)
                .addValue("interesAnterior", interesAnterior)
                .addValue("capitalNueva", capitalNueva)
                .addValue("interesNueva", interesNueva)
                .addValue("vencimientoAnterior", vencimientoAnterior)
                .addValue("vencimientoNuevo", vencimientoNuevo)
                .addValue("usuario", idUsuario);
        if (jdbc.update(sql, p) != 1) {
            throw new IllegalStateException("El crédito cambió o no está activo; no se aplicó la prórroga.");
        }
    }

    /**
     * Registra la trazabilidad financiera de una prórroga ya aplicada.
     * No genera recaudos, extractos ni comprobantes adicionales; exige la
     * coincidencia del extracto y comprobante existentes con el crédito.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public Long registrarPagoProrroga(
            Integer idCredito, Integer idAgencia, LocalDate fechaPago,
            LocalDate fechaContable, Integer mesesProrroga, BigDecimal valorPago,
            Object liquidacion, List<?> mediosRecaudo, Integer idExtractoCartera,
            String tipoComprobante, String numeroComprobante, Integer idUsuario) {
        validarCredito(idCredito);
        if (idAgencia == null || idAgencia <= 0 || fechaPago == null
                || fechaContable == null || mesesProrroga == null || mesesProrroga <= 0
                || valorPago == null || valorPago.signum() <= 0
                || liquidacion == null || mediosRecaudo == null || mediosRecaudo.isEmpty()
                || idExtractoCartera == null || idExtractoCartera <= 0
                || !"CC".equals(tipoComprobante) || numeroComprobante == null
                || numeroComprobante.isBlank() || idUsuario == null || idUsuario <= 0) {
            throw new IllegalArgumentException("Datos incompletos para registrar el recaudo de prórroga.");
        }
        String liquidacionJson;
        String mediosJson;
        try {
            liquidacionJson = objectMapper.writeValueAsString(liquidacion);
            mediosJson = objectMapper.writeValueAsString(mediosRecaudo);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("No fue posible serializar el recaudo de prórroga.", ex);
        }
        String sql = """
                INSERT INTO cartera.pagos_cuotas (
                    id_cartera_credito, id_agencia_receptora, fecha_pago,
                    fecha_contable, periodo_interes_meses, cuotas_a_cancelar,
                    valor_pago, liquidacion, medios_recaudo,
                    requiere_confirmacion_caja, id_caja, id_movimiento_caja,
                    estado, id_extracto_cartera, tipo_comprobante,
                    numero_comprobante, fk_seguridad_creacion,
                    fk_seguridad_aplicacion, fecha_aplicacion, tipo_operacion
                )
                SELECT :credito, :agencia, :fechaPago, :fechaContable,
                       :meses, 0, :valor, CAST(:liquidacion AS jsonb),
                       CAST(:medios AS jsonb), FALSE, NULL, NULL,
                       'APLICADO', e.id_extracto_cartera, :tipo,
                       :numero, :usuario, :usuario, CURRENT_TIMESTAMP, 'PRORROGA'
                  FROM cartera.extractos_cartera e
                 WHERE e.id_extracto_cartera = :extracto
                   AND e.id_cartera_credito = :credito
                   AND e.tipo_comprobante = :tipo
                   AND TRIM(e.numero_comprobante) = TRIM(:numero)
                   AND e.estado = 'A'
                   AND EXISTS (
                       SELECT 1 FROM cartera.creditos_prorrogas p
                        WHERE p.id_cartera_credito = :credito
                          AND p.id_agencia = :agencia
                          AND p.tipo_comprobante = :tipo
                          AND TRIM(p.numero_comprobante) = TRIM(:numero)
                          AND p.estado_prorroga = 'A'
                   )
                RETURNING id_pago
                """;
        var parametros = new MapSqlParameterSource()
                .addValue("credito", idCredito)
                .addValue("agencia", idAgencia)
                .addValue("fechaPago", fechaPago)
                .addValue("fechaContable", fechaContable)
                .addValue("meses", mesesProrroga)
                .addValue("valor", valorPago)
                .addValue("liquidacion", liquidacionJson)
                .addValue("medios", mediosJson)
                .addValue("extracto", idExtractoCartera)
                .addValue("tipo", tipoComprobante)
                .addValue("numero", numeroComprobante)
                .addValue("usuario", idUsuario);
        var ids = jdbc.query(sql, parametros, (rs, rowNum) -> rs.getLong("id_pago"));
        if (ids.size() != 1 || ids.get(0) <= 0) {
            throw new IllegalStateException("No coincide el extracto y comprobante de la prórroga.");
        }
        return ids.get(0);
    }

    /** Normaliza los importes opcionales para las columnas financieras del historial. */
    private static BigDecimal cero(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }

    /** Datos ya validados por el servicio financiero para registrar una prórroga. */
    public record RegistroProrroga(
            Integer idCredito, Integer numeroProrroga, Integer idAgencia,
            Integer idUsuario, LocalDate fechaProrroga, Integer diasProrroga,
            Integer mesesProrroga, LocalDate proximaCapitalAnterior,
            LocalDate proximaInteresAnterior, LocalDate vencimientoAnterior,
            Integer plazoAnterior, LocalDate proximaCapitalNueva,
            LocalDate proximaInteresNueva, LocalDate vencimientoNuevo,
            Integer plazoNuevo, BigDecimal interesCorriente, BigDecimal interesMora,
            BigDecimal seguro, BigDecimal aportes, BigDecimal fondoGarantia,
            BigDecimal otrosConceptos, BigDecimal totalPagado,
            String tipoComprobante, String numeroComprobante,
            LocalDate fechaComprobante, String observacion) {
    }


    /**
     * Obtiene la periodicidad y modalidad contractual del crédito.
     * No utiliza fechas proyectadas ni modifica registros financieros.
     */
    public ModalidadInteresCredito consultarModalidadInteres(Integer idCredito) {
        validarCredito(idCredito);

        String sql = """
                SELECT mi.periodo_meses,
                       mi.tipo_modalidad,
                       mi.descripcion_modalidad_interes
                FROM cartera.carteras_creditos cc
                INNER JOIN cartera.modalidades_intereses mi
                    ON mi.periodo_codigo = cc.periodo_codigo_interes
                   AND mi.tipo_modalidad = cc.tipo_modalidad_interes
                WHERE cc.id_cartera_credito = :credito
                  AND mi.activo = TRUE
                """;

        var resultados = jdbc.query(
                sql,
                new MapSqlParameterSource("credito", idCredito),
                (rs, fila) -> new ModalidadInteresCredito(
                        rs.getInt("periodo_meses"),
                        rs.getString("tipo_modalidad"),
                        rs.getString("descripcion_modalidad_interes")
                )
        );

        if (resultados.size() != 1) {
            throw new IllegalStateException(
                    "No existe una modalidad de intereses contractual "
                            + "única y activa para el crédito.");
        }

        ModalidadInteresCredito modalidad = resultados.get(0);

        if (modalidad.periodoMeses() <= 0
                || (!"A".equals(modalidad.tipoModalidad())
                && !"V".equals(modalidad.tipoModalidad()))) {
            throw new IllegalStateException(
                    "La modalidad contractual de intereses es inválida.");
        }

        return modalidad;
    }

    /** Periodicidad contractual y forma de liquidación de intereses. */
    public record ModalidadInteresCredito(
            Integer periodoMeses,
            String tipoModalidad,
            String descripcion
    ) {}


    /** Rechaza identificadores de crédito inexistentes o inválidos antes de consultar. */
    private static void validarCredito(Integer idCredito) {
        if (idCredito == null || idCredito <= 0) {
            throw new IllegalArgumentException("Debe seleccionar un crédito válido.");
        }
    }

    /** Convierte fechas SQL preservando los valores nulos. */
    private static LocalDate fecha(Date valor) {
        return valor == null ? null : valor.toLocalDate();
    }

    /** Convierte marcas de tiempo SQL preservando los valores nulos. */
    private static LocalDateTime fechaHora(Timestamp valor) {
        return valor == null ? null : valor.toLocalDateTime();
    }

    public record CreditoProrroga(
            Integer idCredito,
            Integer idAgencia,
            String estado,
            BigDecimal saldoActual,
            Integer alturaCuota,
            LocalDate ultimaFechaCapital,
            LocalDate ultimaFechaInteres,
            LocalDate proximaFechaCapital,
            LocalDate proximaFechaInteres,
            LocalDate fechaFinal, Integer plazo) {
    }
}
