package co.assip.erp.cartera.reversion_pagos;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/** Consultas del pago original; no modifica el proceso Pago de Cuotas V1. */
@Repository
@RequiredArgsConstructor
public class ReversionPagosRepository {

    private final JdbcTemplate jdbc;
    private final NamedParameterJdbcTemplate namedJdbc;

    // ================================================================
    // CONSULTAS DEL PAGO ORIGINAL
    // ================================================================

    /** Recupera los pagos de V1 y su estado, ordenados por registro. */
    public List<PagoOriginal> listarPagos(Integer idCredito) {
        return jdbc.query("""
                SELECT id_pago, id_cartera_credito, id_agencia_receptora,
                       fecha_pago, fecha_contable, valor_pago, estado,
                       medios_recaudo::text AS medios_recaudo
                FROM cartera.pagos_cuotas
                WHERE id_cartera_credito = ?
                ORDER BY id_pago DESC
                """, (rs, i) -> new PagoOriginal(
                rs.getLong("id_pago"),
                rs.getInt("id_cartera_credito"),
                rs.getInt("id_agencia_receptora"),
                rs.getObject("fecha_pago", LocalDate.class),
                rs.getObject("fecha_contable", LocalDate.class),
                rs.getBigDecimal("valor_pago"),
                rs.getString("estado"),
                rs.getString("medios_recaudo")), idCredito);
    }

    /** Consulta el pago original y sus referencias contables sin modificar registros. */
    public PagoDetalle consultarPago(Long idPago) {
        List<PagoDetalle> pagos = jdbc.query("""
                SELECT pc.id_pago, pc.id_cartera_credito, pc.id_agencia_receptora,
                       pc.fecha_pago, pc.fecha_contable, pc.valor_pago, pc.estado,
                       pc.id_extracto_cartera, pc.tipo_comprobante,
                       pc.numero_comprobante, pc.id_movimiento_caja,
                       pc.medios_recaudo::text AS medios_recaudo
                FROM cartera.pagos_cuotas pc
                WHERE pc.id_pago = ?
                """, (rs, fila) -> new PagoDetalle(
                rs.getLong("id_pago"), rs.getInt("id_cartera_credito"),
                rs.getInt("id_agencia_receptora"),
                rs.getObject("fecha_pago", LocalDate.class),
                rs.getObject("fecha_contable", LocalDate.class),
                rs.getBigDecimal("valor_pago"), rs.getString("estado"),
                rs.getObject("id_extracto_cartera", Long.class),
                rs.getString("tipo_comprobante"),
                rs.getString("numero_comprobante"),
                rs.getObject("id_movimiento_caja") == null ? null : ((Number) rs.getObject("id_movimiento_caja")).longValue(),
                rs.getString("medios_recaudo")), idPago);
        if (pagos.size() != 1) {
            throw new IllegalArgumentException("No existe el pago seleccionado.");
        }
        return pagos.get(0);
    }

    /** Identifica el último pago aplicado, excluyendo registros no aplicados. */
    public Long ultimoPagoAplicado(Integer idCredito) {
        List<Long> ids = jdbc.queryForList("""
                SELECT id_pago FROM cartera.pagos_cuotas
                WHERE id_cartera_credito = ? AND estado = 'APLICADO'
                ORDER BY fecha_aplicacion DESC NULLS LAST, id_pago DESC
                LIMIT 1
                """, Long.class, idCredito);
        return ids.isEmpty() ? null : ids.get(0);
    }

    // ================================================================
    // CONTRAPARTIDA CONTABLE
    // ================================================================

    /** Consulta el comprobante original y sus descripciones sin recalcular las contrapartidas. */
    public List<LineaContrapartida> consultarContrapartidas(PagoDetalle pago) {
        return jdbc.query("""
                SELECT ac.id_catalogo_cuenta, cc.codigo_cuenta, cc.nombre_cuenta,
                       ac.id_datos_personal, dp.documento AS documento_tercero,
                       NULLIF(TRIM(CONCAT_WS(' ',
                           NULLIF(TRIM(dp.nombres), ''),
                           NULLIF(TRIM(dp.primer_apellido), ''),
                           NULLIF(TRIM(dp.segundo_apellido), '')
                       )), '') AS nombre_tercero,
                       ac.valor_credito AS debito_reversion,
                       ac.valor_debito AS credito_reversion,
                       ac.detalle_movimiento
                FROM contabilidad.auxiliares_contables ac
                LEFT JOIN contabilidad.catalogo_cuentas cc
                  ON cc.id_catalogo_cuenta = ac.id_catalogo_cuenta
                 AND cc.id_agencia = ac.id_agencia
                LEFT JOIN hoja_vida.datos_personales dp
                  ON dp.id_datos_personal = ac.id_datos_personal
                WHERE ac.id_agencia = ?
                  AND ac.tipo_comprobante = ?
                  AND ac.numero_comprobante = ?
                  AND ac.estado_movimiento = 'A'
                ORDER BY ac.id_auxiliar_contable
                """, (rs, i) -> new LineaContrapartida(
                        rs.getInt("id_catalogo_cuenta"),
                        rs.getObject("id_datos_personal") == null ? null : ((Number) rs.getObject("id_datos_personal")).intValue(),
                        rs.getBigDecimal("debito_reversion"),
                        rs.getBigDecimal("credito_reversion"),
                        rs.getString("detalle_movimiento"),
                        rs.getString("codigo_cuenta"),
                        rs.getString("nombre_cuenta"),
                        rs.getString("documento_tercero"),
                        rs.getString("nombre_tercero")),
                pago.idAgencia(), pago.tipoComprobante(), pago.numeroComprobante());
    }

    // ================================================================
    // RESTITUCIÓN DE AUXILIARES
    // ================================================================

    /**
     * Restituye los abonos auxiliares del CC original mediante débitos compensatorios.
     * Requiere una transacción coordinadora activa; no puede ejecutarse aisladamente.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public ResumenAuxiliares revertirAbonosAuxiliares(
            Integer idCredito,
            String comprobanteOriginal,
            String comprobanteReversion,
            LocalDate fechaReversion,
            Integer idUsuario
    ) {
        if (idCredito == null || idCredito <= 0 || idUsuario == null || idUsuario <= 0
                || comprobanteOriginal == null || comprobanteOriginal.isBlank()
                || comprobanteReversion == null || comprobanteReversion.isBlank()
                || comprobanteOriginal.equals(comprobanteReversion) || fechaReversion == null) {
            throw new IllegalArgumentException("Datos incompletos para restituir auxiliares.");
        }
        int cantidad = 0;
        BigDecimal valorTotal = BigDecimal.ZERO;

        for (var concepto : CONCEPTOS_AUXILIARES) {
            String tabla = "cartera." + concepto.detalle();
            String cabecera = "cartera." + concepto.cabecera();
            String clave = concepto.clave();
            var parametros = new MapSqlParameterSource()
                    .addValue("credito", idCredito)
                    .addValue("original", comprobanteOriginal)
                    .addValue("reversion", comprobanteReversion)
                    .addValue("fecha", fechaReversion)
                    .addValue("usuario", idUsuario);

            String origen = " FROM " + tabla + " d JOIN " + cabecera + " h ON h."
                    + clave + " = d." + clave + " WHERE h.id_cartera_credito = :credito";

            Integer existentes = namedJdbc.queryForObject("SELECT COUNT(*)" + origen
                            + " AND d.tipo_comprobante = 'CC' AND d.numero_comprobante = :reversion",
                    parametros, Integer.class);
            if (existentes != null && existentes > 0) {
                throw new IllegalStateException("El CC de reversión ya tiene movimientos auxiliares.");
            }

            var abonos = namedJdbc.query("SELECT d." + clave
                            + " AS id, d.valor_credito AS valor" + origen
                            + " AND d.estado = 'A' AND d.tipo_comprobante = 'CC'"
                            + " AND d.numero_comprobante = :original"
                            + " AND d.valor_debito = 0 AND d.valor_credito > 0"
                            + " ORDER BY d." + clave + " FOR UPDATE OF d",
                    parametros, (rs, fila) -> new AbonoAuxiliar(
                            rs.getInt("id"), rs.getBigDecimal("valor")));

            String extraColumna = concepto.costas() ? ", tipo_movimiento" : "";
            String extraValor = concepto.costas() ? ", 'A'" : "";
            String sql = "INSERT INTO " + tabla + " (" + clave
                    + ", fecha_movimiento, valor_debito, valor_credito, tipo_comprobante, "
                    + "numero_comprobante, " + concepto.observacion()
                    + ", estado, fk_seguridad_creacion, fk_seguridad_edicion"
                    + extraColumna + ") VALUES (:id, :fecha, :valor, 0, 'CC', :reversion, "
                    + ":observacion, 'A', :usuario, :usuario" + extraValor + ")";

            for (var abono : abonos) {
                parametros.addValue("id", abono.id()).addValue("valor", abono.valor())
                        .addValue("observacion", "Reversión del pago CC " + comprobanteOriginal);
                if (namedJdbc.update(sql, parametros) != 1) {
                    throw new IllegalStateException("No se registró la restitución auxiliar.");
                }
                cantidad++;
                valorTotal = valorTotal.add(abono.valor());
            }
        }

        return new ResumenAuxiliares(cantidad, valorTotal);
    }

    private static final List<ConceptoAuxiliar> CONCEPTOS_AUXILIARES = List.of(
            new ConceptoAuxiliar("creditos_seguros", "creditos_seguros_detalle",
                    "id_credito_seguro", "observacion_seguro", false),
            new ConceptoAuxiliar("creditos_alivios", "creditos_alivios_detalle",
                    "id_credito_alivio", "observacion_alivio", false),
            new ConceptoAuxiliar("creditos_costas_judiciales", "creditos_costas_judiciales_detalle",
                    "id_credito_costa_judicial", "observacion_costa", true));

    private record ConceptoAuxiliar(
            String cabecera,
            String detalle,
            String clave,
            String observacion,
            boolean costas
    ) {}

    private record AbonoAuxiliar(Integer id, BigDecimal valor) {}

    public record ResumenAuxiliares(int movimientos, BigDecimal valorRestituido) {}

    // ================================================================
    // VALIDACIÓN DEL ESTADO DEL CRÉDITO
    // ================================================================

    /**
     * Valida los siete valores posteriores persistidos en liquidacion.aplicacion.
     * No depende de un extracto anterior y no realiza modificaciones.
     */
    public EstadoRestitucion validarEstadoRestitucion(PagoDetalle pago) {
        List<EstadoRestitucion> estados = jdbc.query("""
                SELECT c.saldo_actual AS saldo_actual,
                       (p.liquidacion #>> '{aplicacion,saldoAnterior}')::numeric AS saldo_anterior,
                       (p.liquidacion #>> '{aplicacion,saldoNuevo}')::numeric AS saldo_nuevo,
                       (p.liquidacion #>> '{aplicacion,capitalAplicado}')::numeric AS capital,
                       c.codigo_estado_cartera AS estado_actual,
                       p.liquidacion #>> '{aplicacion,codigoEstadoCarteraProyectado}' AS estado_nuevo,
                       c.altura_cuota AS altura_actual,
                       (p.liquidacion #>> '{aplicacion,alturaCuotaActual}')::integer AS altura_anterior,
                       (p.liquidacion #>> '{aplicacion,alturaCuotaNueva}')::integer AS altura_nueva,
                       c.ultima_fecha_capital AS ultima_capital_actual,
                       (p.liquidacion #>> '{aplicacion,ultimaFechaCapitalActual}')::date AS ultima_capital_anterior,
                       (p.liquidacion #>> '{aplicacion,ultimaFechaCapitalNueva}')::date AS ultima_capital_nueva,
                       c.ultima_fecha_interes AS ultima_interes_actual,
                       (p.liquidacion #>> '{aplicacion,ultimaFechaInteresActual}')::date AS ultima_interes_anterior,
                       (p.liquidacion #>> '{aplicacion,ultimaFechaInteresNueva}')::date AS ultima_interes_nueva,
                       c.proxima_fecha_capital AS proxima_capital_actual,
                       (p.liquidacion #>> '{aplicacion,proximaFechaCapitalActual}')::date AS proxima_capital_anterior,
                       (p.liquidacion #>> '{aplicacion,proximaFechaCapitalNueva}')::date AS proxima_capital_nueva,
                       c.proxima_fecha_interes AS proxima_interes_actual,
                       (p.liquidacion #>> '{aplicacion,proximaFechaInteresActual}')::date AS proxima_interes_anterior,
                       (p.liquidacion #>> '{aplicacion,proximaFechaInteresNueva}')::date AS proxima_interes_nueva
                FROM cartera.pagos_cuotas p
                JOIN cartera.carteras_creditos c ON c.id_cartera_credito = p.id_cartera_credito
                WHERE p.id_pago = ? AND p.id_cartera_credito = ?
                """, (rs, i) -> {
            BigDecimal saldo = rs.getBigDecimal("saldo_actual");
            BigDecimal anterior = rs.getBigDecimal("saldo_anterior");
            BigDecimal nuevo = rs.getBigDecimal("saldo_nuevo");
            BigDecimal capital = rs.getBigDecimal("capital");
            boolean historicoCompleto = saldo != null && anterior != null && nuevo != null
                    && capital != null && rs.getString("estado_nuevo") != null
                    && rs.getObject("altura_anterior") != null
                    && rs.getObject("altura_nueva") != null;
            boolean saldosConsistentes = historicoCompleto
                    && anterior.subtract(capital).compareTo(nuevo) == 0;
            boolean coincide = saldosConsistentes
                    && saldo.compareTo(nuevo) == 0
                    && Objects.equals(rs.getString("estado_actual"), rs.getString("estado_nuevo"))
                    && Objects.equals(rs.getObject("altura_actual"), rs.getObject("altura_nueva"))
                    && Objects.equals(rs.getObject("ultima_capital_actual"), rs.getObject("ultima_capital_nueva"))
                    && Objects.equals(rs.getObject("ultima_interes_actual"), rs.getObject("ultima_interes_nueva"))
                    && Objects.equals(rs.getObject("proxima_capital_actual"), rs.getObject("proxima_capital_nueva"))
                    && Objects.equals(rs.getObject("proxima_interes_actual"), rs.getObject("proxima_interes_nueva"));
            return new EstadoRestitucion(coincide,
                    !historicoCompleto ? "Faltan datos históricos en liquidacion.aplicacion."
                            : !saldosConsistentes ? "El capital aplicado no concuerda con los saldos históricos."
                            : !coincide ? "El crédito difiere del estado posterior registrado en el pago."
                            : "Los siete campos coinciden con el estado posterior del pago.",
                    saldo, anterior);
        }, pago.idPago(), pago.idCredito());
        if (estados.size() != 1) {
            throw new IllegalStateException("No se encontró el pago con su crédito.");
        }
        return estados.get(0);
    }

    public record EstadoRestitucion(
            boolean consistente,
            String observacion,
            BigDecimal saldoActual,
            BigDecimal saldoRestituible
    ) {}

    /** Línea contraria con identificadores operativos y descripciones para consulta. */
    public record LineaContrapartida(
            Integer idCuenta,
            Integer idTercero,
            BigDecimal debito,
            BigDecimal credito,
            String detalleOriginal,
            String codigoCuenta,
            String nombreCuenta,
            String documentoTercero,
            String nombreTercero
    ) {}

    /** Datos originales para previsualizar la elegibilidad de una reversión. */
    public record PagoDetalle(
            Long idPago,
            Integer idCredito,
            Integer idAgencia,
            LocalDate fechaPago,
            LocalDate fechaContable,
            BigDecimal valor,
            String estado,
            Long idExtractoCartera,
            String tipoComprobante,
            String numeroComprobante,
            Long idMovimientoCaja,
            String mediosRecaudoJson
    ) {}

    /** Datos persistidos del pago, sin inferir valores financieros. */
    public record PagoOriginal(
            Long idPago,
            Integer idCredito,
            Integer idAgencia,
            LocalDate fechaPago,
            LocalDate fechaContable,
            BigDecimal valor,
            String estado,
            String mediosRecaudoJson
    ) {}

    // ================================================================
    // RESTITUCIÓN DEL CRÉDITO
    // ================================================================

    /**
     * Restituye el estado previo del crédito usando la aplicación histórica.
     * Exige transacción externa; el coordinador debe bloquear antes el pago,
     * contabilizar el CC contrario y completar las operaciones asociadas.
     * No se expone por controlador para evitar una reversión parcial.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public BigDecimal restituirCreditoDesdePago(Long idPago, Integer idUsuario) {
        if (idPago == null || idPago <= 0 || idUsuario == null || idUsuario <= 0) {
            throw new IllegalArgumentException("Pago y usuario obligatorios.");
        }

        // Bloqueo del pago y del crédito: serializa operaciones concurrentes.
        List<Integer> creditos = jdbc.query("""
                SELECT id_cartera_credito FROM cartera.pagos_cuotas
                WHERE id_pago = ? AND estado = 'APLICADO' FOR UPDATE
                """, (rs, n) -> rs.getInt(1), idPago);
        if (creditos.size() != 1) {
            throw new IllegalStateException("El pago no está aplicado o no existe.");
        }
        Integer idCredito = creditos.get(0);
        List<Integer> bloqueados = jdbc.query("""
                SELECT id_cartera_credito FROM cartera.carteras_creditos
                WHERE id_cartera_credito = ? FOR UPDATE
                """, (rs, n) -> rs.getInt(1), idCredito);
        if (bloqueados.size() != 1 || !Objects.equals(ultimoPagoAplicado(idCredito), idPago)) {
            throw new IllegalStateException("El pago no es el último aplicado del crédito.");
        }

        // Se exige historial completo: no se reemplazan ausencias por ceros o fechas inventadas.
        Integer historicoValido = jdbc.queryForObject("""
                SELECT CASE WHEN
                  p.liquidacion #>> '{aplicacion,saldoAnterior}' IS NOT NULL AND
                  p.liquidacion #>> '{aplicacion,saldoNuevo}' IS NOT NULL AND
                  p.liquidacion #>> '{aplicacion,capitalAplicado}' IS NOT NULL AND
                  p.liquidacion #>> '{aplicacion,alturaCuotaActual}' IS NOT NULL AND
                  p.liquidacion #>> '{aplicacion,alturaCuotaNueva}' IS NOT NULL AND
                  p.liquidacion #>> '{aplicacion,codigoEstadoCarteraProyectado}' IS NOT NULL AND
                  (p.liquidacion #>> '{aplicacion,saldoAnterior}')::numeric -
                  (p.liquidacion #>> '{aplicacion,capitalAplicado}')::numeric =
                  (p.liquidacion #>> '{aplicacion,saldoNuevo}')::numeric
                THEN 1 ELSE 0 END
                FROM cartera.pagos_cuotas p WHERE p.id_pago = ?
                """, Integer.class, idPago);
        if (!Integer.valueOf(1).equals(historicoValido)) {
            throw new IllegalStateException("El pago carece de historial financiero consistente.");
        }

        // Compara todos los campos posteriores antes de escribir los anteriores.
        int filas = jdbc.update("""
                UPDATE cartera.carteras_creditos c SET
                  saldo_actual = (p.liquidacion #>> '{aplicacion,saldoAnterior}')::numeric,
                  codigo_estado_cartera = 'A',
                  altura_cuota = (p.liquidacion #>> '{aplicacion,alturaCuotaActual}')::integer,
                  ultima_fecha_capital = (p.liquidacion #>> '{aplicacion,ultimaFechaCapitalActual}')::date,
                  ultima_fecha_interes = (p.liquidacion #>> '{aplicacion,ultimaFechaInteresActual}')::date,
                  proxima_fecha_capital = (p.liquidacion #>> '{aplicacion,proximaFechaCapitalActual}')::date,
                  proxima_fecha_interes = (p.liquidacion #>> '{aplicacion,proximaFechaInteresActual}')::date,
                  fk_seguridad_edicion = ?, fecha_edicion = CURRENT_TIMESTAMP
                FROM cartera.pagos_cuotas p
                WHERE p.id_pago = ? AND p.id_cartera_credito = c.id_cartera_credito
                  AND p.estado = 'APLICADO'
                  AND c.saldo_actual IS NOT DISTINCT FROM
                      (p.liquidacion #>> '{aplicacion,saldoNuevo}')::numeric
                  AND c.codigo_estado_cartera IS NOT DISTINCT FROM
                      (p.liquidacion #>> '{aplicacion,codigoEstadoCarteraProyectado}')
                  AND c.altura_cuota IS NOT DISTINCT FROM
                      (p.liquidacion #>> '{aplicacion,alturaCuotaNueva}')::integer
                  AND c.ultima_fecha_capital IS NOT DISTINCT FROM
                      (p.liquidacion #>> '{aplicacion,ultimaFechaCapitalNueva}')::date
                  AND c.ultima_fecha_interes IS NOT DISTINCT FROM
                      (p.liquidacion #>> '{aplicacion,ultimaFechaInteresNueva}')::date
                  AND c.proxima_fecha_capital IS NOT DISTINCT FROM
                      (p.liquidacion #>> '{aplicacion,proximaFechaCapitalNueva}')::date
                  AND c.proxima_fecha_interes IS NOT DISTINCT FROM
                      (p.liquidacion #>> '{aplicacion,proximaFechaInteresNueva}')::date
                """, idUsuario, idPago);
        if (filas != 1) {
            throw new IllegalStateException(
                    "El crédito cambió después del pago; se impide sobrescribir su estado.");
        }

        return jdbc.queryForObject("""
                SELECT saldo_actual FROM cartera.carteras_creditos
                WHERE id_cartera_credito = ?
                """, BigDecimal.class, idCredito);
    }


    /**
     * Bloquea el pago original y su crédito para ejecutar
     * una reversión atómica.
     *
     * La validación de agencia corresponde al Service.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public PagoDetalle bloquearPagoParaReversion(Long idPago) {

        if (idPago == null || idPago <= 0) {
            throw new IllegalArgumentException(
                    "Debe seleccionar un pago válido."
            );
        }

        List<Long> pagos = jdbc.query("""
            SELECT id_pago
            FROM cartera.pagos_cuotas
            WHERE id_pago = ?
              AND estado = 'APLICADO'
            FOR UPDATE
            """,
                (rs, fila) -> rs.getLong(1),
                idPago
        );

        if (pagos.size() != 1) {
            throw new IllegalStateException(
                    "El pago no existe o ya no está aplicado."
            );
        }

        PagoDetalle pago = consultarPago(idPago);

        List<Integer> creditos = jdbc.query("""
            SELECT id_cartera_credito
            FROM cartera.carteras_creditos
            WHERE id_cartera_credito = ?
            FOR UPDATE
            """,
                (rs, fila) -> rs.getInt(1),
                pago.idCredito()
        );

        if (creditos.size() != 1
                || !Objects.equals(
                ultimoPagoAplicado(pago.idCredito()),
                idPago
        )) {
            throw new IllegalStateException(
                    "Solo puede reversarse el último pago aplicado del crédito."
            );
        }

        return pago;
    }


    /**
     * Marca el pago original como reversado y conserva
     * la referencia del CC contrario, fecha, motivo y usuario.
     *
     * Debe ejecutarse dentro de la transacción financiera.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void marcarPagoReversado(
            Long idPago,
            String numeroComprobanteReversion,
            LocalDate fechaReversion,
            String motivo,
            Integer idUsuario
    ) {

        if (idPago == null || idPago <= 0
                || numeroComprobanteReversion == null
                || numeroComprobanteReversion.isBlank()
                || fechaReversion == null
                || motivo == null
                || motivo.isBlank()
                || idUsuario == null
                || idUsuario <= 0) {

            throw new IllegalArgumentException(
                    "Datos obligatorios de reversión incompletos."
            );
        }

        int actualizados = jdbc.update("""
            UPDATE cartera.pagos_cuotas
               SET estado = 'REVERSADO',
                   liquidacion = jsonb_set(
                       COALESCE(liquidacion, '{}'::jsonb),
                       '{reversion}',
                       jsonb_build_object(
                           'tipoComprobante', 'CC',
                           'numeroComprobante', ?,
                           'fechaReversion', CAST(? AS text),
                           'motivo', ?,
                           'idUsuario', ?,
                           'fechaRegistro', CURRENT_TIMESTAMP
                       ),
                       true
                   )
             WHERE id_pago = ?
               AND estado = 'APLICADO'
               AND NOT jsonb_exists(
                   COALESCE(liquidacion, '{}'::jsonb),
                   'reversion'
               )
            """,
                numeroComprobanteReversion,
                fechaReversion,
                motivo.trim(),
                idUsuario,
                idPago
        );

        if (actualizados != 1) {
            throw new IllegalStateException(
                    "El pago cambió de estado o ya tiene una reversión registrada."
            );
        }
    }


    /**
     * Conserva el pago original como R y registra su reversión como V.
     * Valida la referencia del pago, bloquea el extracto y exige transacción activa.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public Long registrarReversionExtracto(PagoDetalle pago, String comprobanteReversion,
                                           LocalDate fechaReversion, String motivo, Integer idUsuario) {
        if (pago == null || pago.idExtractoCartera() == null || pago.idExtractoCartera() <= 0
                || comprobanteReversion == null || comprobanteReversion.isBlank()
                || fechaReversion == null || motivo == null || motivo.isBlank()
                || idUsuario == null || idUsuario <= 0) {
            throw new IllegalArgumentException("Datos incompletos para reversar el extracto de Cartera.");
        }
        if ("CC".equals(pago.tipoComprobante()) && comprobanteReversion.equals(pago.numeroComprobante())) {
            throw new IllegalArgumentException("El comprobante de reversión debe ser diferente del original.");
        }
        List<Long> originales = jdbc.query("""
                SELECT id_extracto_cartera
                FROM cartera.extractos_cartera
                WHERE id_extracto_cartera = ? AND id_cartera_credito = ?
                  AND tipo_comprobante = ? AND numero_comprobante = ? AND estado = 'A'
                FOR UPDATE
                """, (rs, i) -> rs.getLong(1), pago.idExtractoCartera(),
                pago.idCredito(), pago.tipoComprobante(), pago.numeroComprobante());
        if (originales.size() != 1) {
            throw new IllegalStateException("El extracto original no existe, no coincide o ya fue reversado.");
        }
        String observacion = "Reversado mediante CC " + comprobanteReversion + ". Motivo: " + motivo.trim();
        int actualizados = jdbc.update("""
                UPDATE cartera.extractos_cartera
                SET estado = 'R', observacion_pago = ?,
                    fk_seguridad_edicion = ?, fecha_edicion = CURRENT_TIMESTAMP
                WHERE id_extracto_cartera = ? AND estado = 'A'
                """, observacion, idUsuario, pago.idExtractoCartera());
        if (actualizados != 1) {
            throw new IllegalStateException("No fue posible marcar el extracto original como reversado.");
        }
        List<Long> nuevos = jdbc.query("""
                INSERT INTO cartera.extractos_cartera (
                    id_cartera_credito, fecha_pago, hora, tipo_comprobante, numero_comprobante,
                    valor_capital, valor_interes_causado, valor_interes_ingreso,
                    valor_interes_anticipado, valor_interes_mora, valor_seguro, valor_aportes,
                    valor_papeleria, valor_fondo_garantia, otros_valores,
                    pago_efectivo, pago_cheques, pago_notas, id_cuenta_ahorro,
                    pago_consignacion, pago_pse, otro_pago, observacion_pago,
                    dias_capital_ingreso, dias_interes_ingreso, dias_mora, tarjeta, modulo,
                    altura_cuota, abonos_pendientes, fecha_contable,
                    ultima_fecha_capital, ultima_fecha_interes, ultima_fecha_seguro,
                    ultima_fecha_mora, proxima_fecha_capital, proxima_fecha_interes,
                    proxima_fecha_seguro, intereses_pagados_hasta,
                    estado, fk_seguridad_creacion, fecha_creacion,
                    fk_seguridad_edicion, fecha_edicion
                )
                SELECT id_cartera_credito, ?, CURRENT_TIMESTAMP, 'CC', ?,
                    valor_capital, valor_interes_causado, valor_interes_ingreso,
                    valor_interes_anticipado, valor_interes_mora, valor_seguro, valor_aportes,
                    valor_papeleria, valor_fondo_garantia, otros_valores,
                    pago_efectivo, pago_cheques, pago_notas, id_cuenta_ahorro,
                    pago_consignacion, pago_pse, otro_pago, ?,
                    dias_capital_ingreso, dias_interes_ingreso, dias_mora, tarjeta, modulo,
                    altura_cuota, abonos_pendientes, ?,
                    ultima_fecha_capital, ultima_fecha_interes, ultima_fecha_seguro,
                    ultima_fecha_mora, proxima_fecha_capital, proxima_fecha_interes,
                    proxima_fecha_seguro, intereses_pagados_hasta,
                    'V', ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP
                FROM cartera.extractos_cartera
                WHERE id_extracto_cartera = ? AND estado = 'R'
                RETURNING id_extracto_cartera
                """, (rs, i) -> rs.getLong(1), fechaReversion, comprobanteReversion,
                "Reversión del pago CC " + pago.numeroComprobante() + ". Motivo: " + motivo.trim(),
                fechaReversion, idUsuario, idUsuario, pago.idExtractoCartera());
        if (nuevos.size() != 1) {
            throw new IllegalStateException("No se registró el movimiento de reversión en el extracto.");
        }
        return nuevos.get(0);
    }
}
