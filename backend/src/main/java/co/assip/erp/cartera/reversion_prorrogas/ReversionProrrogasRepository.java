
package co.assip.erp.cartera.reversion_prorrogas;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Bloqueos y restitución contractual de prórrogas; no ejecuta recaudos ni comprobantes. */
@Repository
@RequiredArgsConstructor
public class ReversionProrrogasRepository {
    private final NamedParameterJdbcTemplate jdbc;

    /** Bloquea primero el crédito para conservar el orden usado al aplicar prórrogas. */
    @Transactional(propagation = Propagation.MANDATORY)
    public CreditoBloqueado bloquearCredito(Integer idCredito) {
        if (idCredito == null || idCredito <= 0) {
            throw new IllegalArgumentException("Crédito inválido.");
        }
        List<CreditoBloqueado> filas = jdbc.query("""
                SELECT id_cartera_credito, id_agencia, codigo_estado_cartera,
                       saldo_actual, proxima_fecha_capital, proxima_fecha_interes,
                       fecha_final, plazo
                FROM cartera.carteras_creditos
                WHERE id_cartera_credito = :credito
                FOR UPDATE
                """, new MapSqlParameterSource("credito", idCredito), (rs, i) ->
                new CreditoBloqueado(rs.getInt("id_cartera_credito"),
                        rs.getInt("id_agencia"), rs.getString("codigo_estado_cartera"),
                        rs.getBigDecimal("saldo_actual"),
                        rs.getObject("proxima_fecha_capital", LocalDate.class),
                        rs.getObject("proxima_fecha_interes", LocalDate.class),
                        rs.getObject("fecha_final", LocalDate.class),
                        rs.getObject("plazo", Integer.class)));
        if (filas.size() != 1) {
            throw new IllegalStateException("No existe el crédito seleccionado.");
        }
        return filas.get(0);
    }

    /** Bloquea la prórroga y obtiene las fechas originales y el comprobante aplicado. */
    @Transactional(propagation = Propagation.MANDATORY)
    public ProrrogaBloqueada bloquearProrroga(Long idProrroga, Integer idCredito) {
        if (idProrroga == null || idProrroga <= 0 || idCredito == null || idCredito <= 0) {
            throw new IllegalArgumentException("Identificación de prórroga inválida.");
        }
        var p = new MapSqlParameterSource().addValue("prorroga", idProrroga)
                .addValue("credito", idCredito);
        List<ProrrogaBloqueada> filas = jdbc.query("""
                SELECT id_credito_prorroga, id_cartera_credito, id_agencia,
                       numero_prorroga, estado_prorroga, fecha_aplicacion,
                       proxima_fecha_capital_anterior, proxima_fecha_interes_anterior,
                       fecha_vencimiento_anterior, plazo_anterior,
                       proxima_fecha_capital_nueva, proxima_fecha_interes_nueva,
                       fecha_vencimiento_nueva, plazo_nuevo,
                       valor_total_pagado, tipo_comprobante, numero_comprobante
                FROM cartera.creditos_prorrogas
                WHERE id_credito_prorroga = :prorroga
                  AND id_cartera_credito = :credito
                FOR UPDATE
                """, p, (rs, i) -> new ProrrogaBloqueada(
                rs.getLong("id_credito_prorroga"), rs.getInt("id_cartera_credito"),
                rs.getObject("id_agencia", Integer.class), rs.getInt("numero_prorroga"),
                rs.getString("estado_prorroga"),
                rs.getObject("proxima_fecha_capital_anterior", LocalDate.class),
                rs.getObject("proxima_fecha_interes_anterior", LocalDate.class),
                rs.getObject("fecha_vencimiento_anterior", LocalDate.class),
                rs.getObject("plazo_anterior", Integer.class),
                rs.getObject("proxima_fecha_capital_nueva", LocalDate.class),
                rs.getObject("proxima_fecha_interes_nueva", LocalDate.class),
                rs.getObject("fecha_vencimiento_nueva", LocalDate.class),
                rs.getObject("plazo_nuevo", Integer.class),
                rs.getBigDecimal("valor_total_pagado"),
                rs.getString("tipo_comprobante"), rs.getString("numero_comprobante")));
        if (filas.size() != 1) {
            throw new IllegalStateException("No existe la prórroga seleccionada para el crédito.");
        }
        return filas.get(0);
    }

    /** Impide reversar una prórroga cuando existe otra aplicada posteriormente. */
    @Transactional(propagation = Propagation.MANDATORY)
    public boolean existeProrrogaAplicadaPosterior(Integer idCredito, Integer numeroProrroga) {
        Boolean existe = jdbc.queryForObject("""
                SELECT EXISTS (
                    SELECT 1 FROM cartera.creditos_prorrogas
                    WHERE id_cartera_credito = :credito
                      AND numero_prorroga > :numero
                      AND estado_prorroga = 'A'
                )
                """, new MapSqlParameterSource().addValue("credito", idCredito)
                .addValue("numero", numeroProrroga), Boolean.class);
        return Boolean.TRUE.equals(existe);
    }

    /** Restituye fechas solo si siguen iguales a las aplicadas por esta prórroga. */
    @Transactional(propagation = Propagation.MANDATORY)
    public void restituirFechas(ProrrogaBloqueada prorroga, Integer idUsuario) {
        if (prorroga == null || idUsuario == null || idUsuario <= 0
                || prorroga.idAgencia() == null || prorroga.idAgencia() <= 0
                || prorroga.capitalAnterior() == null || prorroga.capitalNuevo() == null) {
            throw new IllegalArgumentException("Datos insuficientes para restituir fechas.");
        }
        var p = new MapSqlParameterSource()
                .addValue("credito", prorroga.idCredito())
                .addValue("agencia", prorroga.idAgencia())
                .addValue("capitalAnterior", prorroga.capitalAnterior())
                .addValue("interesAnterior", prorroga.interesAnterior())
                .addValue("vencimientoAnterior", prorroga.vencimientoAnterior())
                .addValue("capitalNuevo", prorroga.capitalNuevo())
                .addValue("interesNuevo", prorroga.interesNuevo())
                .addValue("vencimientoNuevo", prorroga.vencimientoNuevo())
                .addValue("usuario", idUsuario);
        int actualizados = jdbc.update("""
                UPDATE cartera.carteras_creditos SET
                    proxima_fecha_capital = :capitalAnterior,
                    proxima_fecha_interes = :interesAnterior,
                    fecha_final = :vencimientoAnterior,
                    fk_seguridad_edicion = :usuario,
                    fecha_edicion = CURRENT_TIMESTAMP
                WHERE id_cartera_credito = :credito
                  AND id_agencia = :agencia
                  AND codigo_estado_cartera = 'A'
                  AND proxima_fecha_capital IS NOT DISTINCT FROM :capitalNuevo
                  AND proxima_fecha_interes IS NOT DISTINCT FROM :interesNuevo
                  AND fecha_final IS NOT DISTINCT FROM :vencimientoNuevo
                """, p);
        if (actualizados != 1) {
            throw new IllegalStateException("Las fechas del crédito cambiaron; no es seguro reversar la prórroga.");
        }
    }

    /** Localiza y bloquea el pago y su extracto originales; valida el vínculo con la prórroga. */
    @Transactional(propagation = Propagation.MANDATORY)
    public PagoProrrogaBloqueado bloquearPagoOriginal(ProrrogaBloqueada prorroga) {
        var p = new MapSqlParameterSource()
                .addValue("prorroga", prorroga.idProrroga())
                .addValue("credito", prorroga.idCredito())
                .addValue("agencia", prorroga.idAgencia())
                .addValue("tipo", prorroga.tipoComprobante())
                .addValue("numero", prorroga.numeroComprobante());
        List<PagoProrrogaBloqueado> pagos = jdbc.query("""
                SELECT pc.id_pago, pc.id_extracto_cartera, pc.valor_pago,
                       pc.estado, pc.liquidacion ->> 'idCreditoProrroga' AS id_prorroga_json,
                       e.valor_capital, e.estado AS estado_extracto
                FROM cartera.pagos_cuotas pc
                JOIN cartera.extractos_cartera e
                  ON e.id_extracto_cartera = pc.id_extracto_cartera
                 AND e.id_cartera_credito = pc.id_cartera_credito
                WHERE pc.id_cartera_credito = :credito
                  AND pc.id_agencia_receptora = :agencia
                  AND pc.tipo_operacion = 'PRORROGA'
                  AND pc.tipo_comprobante = :tipo
                  AND TRIM(pc.numero_comprobante) = TRIM(:numero)
                  AND pc.liquidacion ->> 'idCreditoProrroga' = CAST(:prorroga AS text)
                  AND e.tipo_comprobante = :tipo
                  AND TRIM(e.numero_comprobante) = TRIM(:numero)
                FOR UPDATE OF pc, e
                """, p, (rs, i) -> new PagoProrrogaBloqueado(
                rs.getLong("id_pago"), rs.getInt("id_extracto_cartera"),
                rs.getBigDecimal("valor_pago"), rs.getBigDecimal("valor_capital"),
                rs.getString("estado"), rs.getString("estado_extracto")));
        if (pagos.size() != 1) {
            throw new IllegalStateException("No existe un único pago y extracto para la prórroga.");
        }
        PagoProrrogaBloqueado pago = pagos.get(0);
        if (!"APLICADO".equals(pago.estado()) || !"A".equals(pago.estadoExtracto())
                || pago.valor().compareTo(prorroga.totalPagado()) != 0
                || pago.capital().signum() < 0) {
            throw new IllegalStateException("El pago o extracto original no está vigente o no coincide.");
        }
        return pago;
    }

    /** Impide restituir el crédito si hay extractos activos posteriores al de la prórroga. */
    @Transactional(propagation = Propagation.MANDATORY)
    public void validarSinExtractosActivosPosteriores(Integer idCredito, Integer idExtracto) {
        Boolean existe = jdbc.queryForObject("""
                SELECT EXISTS (
                    SELECT 1 FROM cartera.extractos_cartera
                    WHERE id_cartera_credito = :credito
                      AND id_extracto_cartera > :extracto
                      AND estado = 'A'
                )
                """, new MapSqlParameterSource().addValue("credito", idCredito)
                .addValue("extracto", idExtracto), Boolean.class);
        if (Boolean.TRUE.equals(existe)) {
            throw new IllegalStateException("Existen movimientos activos posteriores a la prórroga.");
        }
    }


    /**
     * Impide reversar una prórroga si existen pagos aplicados posteriores.
     * Utiliza el identificador del pago para evitar depender únicamente
     * de fechas contables que pueden registrarse retroactivamente.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void validarSinPagosAplicadosPosteriores(
            Integer idCredito,
            Long idPagoOriginal) {

        if (idCredito == null || idCredito <= 0
                || idPagoOriginal == null || idPagoOriginal <= 0) {
            throw new IllegalArgumentException(
                    "Datos inválidos para validar pagos posteriores.");
        }

        Boolean existe = jdbc.queryForObject("""
            SELECT EXISTS (
                SELECT 1
                FROM cartera.pagos_cuotas
                WHERE id_cartera_credito = :credito
                  AND id_pago > :pagoOriginal
                  AND estado = 'APLICADO'
            )
            """,
                new MapSqlParameterSource()
                        .addValue("credito", idCredito)
                        .addValue("pagoOriginal", idPagoOriginal),
                Boolean.class);

        if (Boolean.TRUE.equals(existe)) {
            throw new IllegalStateException(
                    "El crédito tiene pagos aplicados posteriores "
                            + "a la prórroga. No es posible reversarla.");
        }
    }


    /** Restituye únicamente el capital efectivamente abonado por el extracto original. */
    @Transactional(propagation = Propagation.MANDATORY)
    public BigDecimal restituirCapital(ProrrogaBloqueada prorroga,
                                       PagoProrrogaBloqueado pago, Integer idUsuario) {
        if (prorroga == null || pago == null || idUsuario == null || idUsuario <= 0
                || pago.capital() == null || pago.capital().signum() < 0) {
            throw new IllegalArgumentException("Datos inválidos para restituir capital.");
        }
        if (pago.capital().signum() == 0) {
            return BigDecimal.ZERO;
        }
        var p = new MapSqlParameterSource()
                .addValue("credito", prorroga.idCredito())
                .addValue("agencia", prorroga.idAgencia())
                .addValue("capital", pago.capital())
                .addValue("usuario", idUsuario);
        int actualizados = jdbc.update("""
                UPDATE cartera.carteras_creditos SET
                    saldo_actual = saldo_actual + :capital,
                    fk_seguridad_edicion = :usuario,
                    fecha_edicion = CURRENT_TIMESTAMP
                WHERE id_cartera_credito = :credito
                  AND id_agencia = :agencia
                  AND codigo_estado_cartera = 'A'
                  AND saldo_actual IS NOT NULL
                """, p);
        if (actualizados != 1) {
            throw new IllegalStateException("No se pudo restituir el capital del crédito.");
        }
        return pago.capital();
    }

    public record PagoProrrogaBloqueado(Long idPago, Integer idExtracto,
                                        BigDecimal valor, BigDecimal capital,
                                        String estado, String estadoExtracto) {}

    /** Marca la prórroga reversada después de finalizar todas las restituciones. */
    @Transactional(propagation = Propagation.MANDATORY)
    public void marcarReversada(Long idProrroga, Integer idUsuario) {
        if (idProrroga == null || idProrroga <= 0 || idUsuario == null || idUsuario <= 0) {
            throw new IllegalArgumentException("Datos inválidos para marcar la reversión.");
        }
        int actualizados = jdbc.update("""
                UPDATE cartera.creditos_prorrogas SET
                    estado_prorroga = 'R',
                    fk_seguridad_edicion = :usuario,
                    fecha_edicion = CURRENT_TIMESTAMP
                WHERE id_credito_prorroga = :prorroga
                  AND estado_prorroga = 'A'
                """, new MapSqlParameterSource().addValue("prorroga", idProrroga)
                .addValue("usuario", idUsuario));
        if (actualizados != 1) {
            throw new IllegalStateException("La prórroga ya no está aplicada.");
        }
    }

    public record CreditoBloqueado(Integer idCredito, Integer idAgencia, String estado,
                                   BigDecimal saldoActual, LocalDate capital,
                                   LocalDate interes, LocalDate vencimiento, Integer plazo) {}

    public record ProrrogaBloqueada(Long idProrroga, Integer idCredito, Integer idAgencia,
                                    Integer numeroProrroga, String estado,
                                    LocalDate capitalAnterior, LocalDate interesAnterior,
                                    LocalDate vencimientoAnterior, Integer plazoAnterior,
                                    LocalDate capitalNuevo, LocalDate interesNuevo,
                                    LocalDate vencimientoNuevo, Integer plazoNuevo,
                                    BigDecimal totalPagado, String tipoComprobante,
                                    String numeroComprobante) {}
}
