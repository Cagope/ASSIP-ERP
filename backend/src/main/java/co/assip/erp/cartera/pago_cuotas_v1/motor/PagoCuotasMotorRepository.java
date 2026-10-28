package co.assip.erp.cartera.pago_cuotas_v1.motor;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import co.assip.erp.cartera.pago_cuotas_v1.motor.dto.PagoCuotasMedioRecaudoDTO;

import java.math.BigDecimal;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Persistencia del motor de pagos.
 * Las escrituras deben invocarse en una única transacción.
 */
@Repository
@RequiredArgsConstructor
public class PagoCuotasMotorRepository {

    private final NamedParameterJdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    public List<CajaRecaudo> listarCajasRecaudo(Integer idAgencia) {
        if (idAgencia == null || idAgencia <= 0) {
            throw new IllegalArgumentException("Debe indicar una agencia válida.");
        }

        String sql = """
                SELECT c.id_caja, c.id_agencia, c.codigo_caja, c.descripcion
                FROM cajas.cajas c
                WHERE c.id_agencia = :idAgencia
                  AND c.activa = TRUE
                  AND c.permite_efectivo = TRUE
                  AND c.tipo_caja = 'OPERATIVA'
                  AND EXISTS (
                      SELECT 1
                      FROM cajas.provisiones_diarias p
                      WHERE p.id_caja = c.id_caja
                        AND p.fecha_contable = CURRENT_DATE
                        AND p.estado = 'ABIERTA'
                  )
                ORDER BY c.codigo_caja, c.id_caja
                """;

        return jdbc.query(sql,
                new MapSqlParameterSource("idAgencia", idAgencia),
                (rs, fila) -> new CajaRecaudo(
                        rs.getLong("id_caja"),
                        rs.getInt("id_agencia"),
                        rs.getString("codigo_caja"),
                        rs.getString("descripcion")));
    }

    /**
     * Lista las cuentas de ahorro operativas del titular y agencia del crédito.
     * Excluye aportes y TACS mediante tipo de captación 2.
     * Admite estados A e I cuando estén habilitados como operativos.
     * La disponibilidad definitiva se valida al ejecutar el débito.
     */
    public List<CuentaAhorroRecaudo> listarCuentasAhorroRecaudo(Integer idCredito) {
        if (idCredito == null || idCredito <= 0) {
            throw new IllegalArgumentException("Debe indicar un crédito válido.");
        }

        String sql = """
            SELECT
                a.id_cuenta_ahorro,
                a.id_agencia,
                TRIM(a.codigo_cuenta) AS codigo_cuenta,
                f.nombre_forma,
                a.saldo_actual_cuenta
            FROM cartera.carteras_creditos cr
            JOIN depositos.cuentas_ahorro a
              ON a.id_datos_personal = cr.id_datos_personal
             AND a.id_agencia = cr.id_agencia
            JOIN depositos.formas_ahorro f
              ON f.id_forma_ahorro = a.id_forma_ahorro
             AND f.id_agencia = a.id_agencia
            WHERE cr.id_cartera_credito = :idCredito
              AND TRIM(f.tipo_captacion_forma) = '2'
              AND TRIM(a.estado_cuenta_cuenta) = 'A'
              AND EXISTS (
                  SELECT 1
                  FROM depositos.estados_ahorros ea
                  WHERE TRIM(ea.codigo_estado_ahorro) = TRIM(a.cuenta_activa)
                    AND ea.operativo = TRUE
              )
            ORDER BY f.codigo_forma, a.codigo_cuenta, a.id_cuenta_ahorro
            """;

        return jdbc.query(sql,
                new MapSqlParameterSource("idCredito", idCredito),
                (rs, fila) -> new CuentaAhorroRecaudo(
                        rs.getInt("id_cuenta_ahorro"),
                        rs.getInt("id_agencia"),
                        rs.getString("codigo_cuenta"),
                        rs.getString("nombre_forma"),
                        rs.getBigDecimal("saldo_actual_cuenta")));
    }

    /** Datos de consulta; el saldo mostrado no reserva fondos. */
    public record CuentaAhorroRecaudo(
            Integer idCuentaAhorro,
            Integer idAgencia,
            String codigoCuenta,
            String nombreForma,
            BigDecimal saldoActual) { }

    public Optional<CajaRecaudo> obtenerCajaRecaudo(
            Long idCaja, Integer idAgencia) {

        if (idCaja == null || idCaja <= 0
                || idAgencia == null || idAgencia <= 0) {
            return Optional.empty();
        }

        String sql = """
                SELECT c.id_caja, c.id_agencia, c.codigo_caja, c.descripcion
                FROM cajas.cajas c
                WHERE c.id_caja = :idCaja
                  AND c.id_agencia = :idAgencia
                  AND c.activa = TRUE
                  AND c.permite_efectivo = TRUE
                  AND c.tipo_caja = 'OPERATIVA'
                  AND EXISTS (
                      SELECT 1
                      FROM cajas.provisiones_diarias p
                      WHERE p.id_caja = c.id_caja
                        AND p.fecha_contable = CURRENT_DATE
                        AND p.estado = 'ABIERTA'
                  )
                """;

        var params = new MapSqlParameterSource()
                .addValue("idCaja", idCaja)
                .addValue("idAgencia", idAgencia);

        return jdbc.query(sql, params,
                        (rs, fila) -> new CajaRecaudo(
                                rs.getLong("id_caja"),
                                rs.getInt("id_agencia"),
                                rs.getString("codigo_caja"),
                                rs.getString("descripcion")))
                .stream()
                .findFirst();
    }

    /**
     * Bloquea el crédito hasta finalizar la transacción
     * que confirma el pago.
     */
    public CreditoBloqueado bloquearCredito(Integer idCredito) {

        String sql = """
                SELECT
                    id_cartera_credito,
                    id_agencia,
                    codigo_estado_cartera,
                    saldo_actual,
                    altura_cuota,
                    ultima_fecha_capital,
                    ultima_fecha_interes,
                    proxima_fecha_capital,
                    proxima_fecha_interes
                FROM cartera.carteras_creditos
                WHERE id_cartera_credito = :idCredito
                FOR UPDATE
                """;

        return jdbc.query(
                        sql,
                        new MapSqlParameterSource("idCredito", idCredito),
                        (rs, fila) -> new CreditoBloqueado(
                                rs.getInt("id_cartera_credito"),
                                rs.getInt("id_agencia"),
                                rs.getString("codigo_estado_cartera"),
                                rs.getBigDecimal("saldo_actual"),
                                rs.getObject("altura_cuota", Integer.class),
                                fecha(rs.getDate("ultima_fecha_capital")),
                                fecha(rs.getDate("ultima_fecha_interes")),
                                fecha(rs.getDate("proxima_fecha_capital")),
                                fecha(rs.getDate("proxima_fecha_interes"))))
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe el crédito seleccionado."));
    }

    /**
     * Actualiza los campos proyectados después
     * de validar el crédito bloqueado.
     */
    public void actualizarCredito(
            PagoCuotasMotorService.ResultadoAplicacion resultado,
            Integer idUsuario) {

        exigirUsuario(idUsuario);

        String sql = """
                UPDATE cartera.carteras_creditos SET
                    saldo_actual = :saldo,
                    codigo_estado_cartera = :estado,
                    altura_cuota = :altura,
                    ultima_fecha_capital = :ultimaCapital,
                    ultima_fecha_interes = :ultimaInteres,
                    proxima_fecha_capital = :proximaCapital,
                    proxima_fecha_interes = :proximaInteres,
                    fk_seguridad_edicion = :usuario,
                    fecha_edicion = CURRENT_TIMESTAMP
                WHERE id_cartera_credito = :credito
                  AND codigo_estado_cartera = 'A'
                  AND saldo_actual = :saldoAnterior
                """;

        var p = new MapSqlParameterSource()
                .addValue("credito", resultado.idCarteraCredito())
                .addValue("saldo", resultado.saldoNuevo())
                .addValue("saldoAnterior", resultado.saldoAnterior())
                .addValue("estado", resultado.codigoEstadoCarteraProyectado())
                .addValue("altura", resultado.alturaCuotaNueva())
                .addValue("ultimaCapital", resultado.ultimaFechaCapitalNueva())
                .addValue("ultimaInteres", resultado.ultimaFechaInteresNueva())
                .addValue("proximaCapital", resultado.proximaFechaCapitalNueva())
                .addValue("proximaInteres", resultado.proximaFechaInteresNueva())
                .addValue("usuario", idUsuario);

        if (resultado.cancelaCredito()) {
            // La cancelación exige que los abonos auxiliares ya se hayan
            // registrado en esta misma transacción.
            var saldos = bloquearYValidar(resultado.idCarteraCredito(),
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
            if (saldos.seguros().signum() != 0
                    || saldos.alivios().signum() != 0
                    || saldos.costasJudiciales().signum() != 0) {
                throw new IllegalStateException(
                        "No se puede cancelar el crédito con seguros, alivios o costas pendientes.");
            }
        }
        if (jdbc.update(sql, p) != 1) {
            throw new IllegalStateException(
                    "El crédito cambió; debe recalcular el pago.");
        }
    }

    /**
     * Cierra únicamente la última prórroga aplicada.
     * Conserva las anteriores y sus datos históricos.
     */
    public boolean cerrarUltimaProrrogaAplicada(
            Integer idCredito, Integer idUsuario) {

        exigirUsuario(idUsuario);

        String sql = """
                UPDATE cartera.creditos_prorrogas SET
                    estado_prorroga = 'C',
                    fk_seguridad_edicion = :usuario,
                    fecha_edicion = CURRENT_TIMESTAMP
                WHERE id_credito_prorroga = (
                    SELECT p.id_credito_prorroga
                    FROM cartera.creditos_prorrogas p
                    WHERE p.id_cartera_credito = :credito
                      AND p.estado_prorroga = 'A'
                    ORDER BY
                        p.numero_prorroga DESC,
                        p.id_credito_prorroga DESC
                    LIMIT 1
                    FOR UPDATE
                )
                """;

        return jdbc.update(
                sql,
                new MapSqlParameterSource()
                        .addValue("credito", idCredito)
                        .addValue("usuario", idUsuario)) == 1;
    }

    /**
     * Registra importes contables previamente determinados
     * por el motor.
     *
     * No decide la distribución de intereses ni los medios
     * que corresponden a cada importe.
     */
    public Integer registrarExtracto(ExtractoPago e) {

        exigirUsuario(e.idUsuario());

        String sql = """
                INSERT INTO cartera.extractos_cartera (
                    id_cartera_credito,
                    fecha_pago,
                    tipo_comprobante,
                    numero_comprobante,
                    valor_capital,
                    valor_interes_causado,
                    valor_interes_ingreso,
                    valor_interes_anticipado,
                    valor_interes_mora,
                    dias_mora,
                    valor_seguro,
                    otros_valores,
                    pago_efectivo,
                    pago_cheques,
                    pago_notas,
                    pago_consignacion,
                    pago_pse,
                    otro_pago,
                    id_cuenta_ahorro,
                    observacion_pago,
                    altura_cuota,
                    abonos_pendientes,
                    fecha_contable,
                    ultima_fecha_capital,
                    ultima_fecha_interes,
                    proxima_fecha_capital,
                    proxima_fecha_interes,
                    fk_seguridad_creacion,
                    fk_seguridad_edicion
                ) VALUES (
                    :credito,
                    :fecha,
                    :tipo,
                    :numero,
                    :capital,
                    :interesCausado,
                    :interesIngreso,
                    :interesAnticipado,
                    :mora,
                    :diasMora,
                    :seguro,
                    :otros,
                    :efectivo,
                    :cheques,
                    :notas,
                    :consignacion,
                    :pse,
                    :otroPago,
                    :cuentaAhorro,
                    :observacion,
                    :altura,
                    :abonos,
                    :fechaContable,
                    :ultimaCapital,
                    :ultimaInteres,
                    :proximaCapital,
                    :proximaInteres,
                    :usuario,
                    :usuario
                )
                RETURNING id_extracto_cartera
                """;

        var p = new MapSqlParameterSource()
                .addValue("credito", e.idCredito())
                .addValue("fecha", e.fechaPago())
                .addValue("tipo", e.tipoComprobante())
                .addValue("numero", e.numeroComprobante())
                .addValue("capital", e.capital())
                .addValue("interesCausado", e.interesCausado())
                .addValue("interesIngreso", e.interesIngreso())
                .addValue("interesAnticipado", e.interesAnticipado())
                .addValue("mora", e.mora())
                .addValue("diasMora", e.diasMora())
                .addValue("seguro", e.seguro())
                .addValue("otros", e.otros())
                .addValue("efectivo", e.efectivo())
                .addValue("cheques", e.cheques())
                .addValue("notas", e.notas())
                .addValue("consignacion", e.consignacion())
                .addValue("pse", e.pse())
                .addValue("otroPago", e.otroPago())
                .addValue("cuentaAhorro", e.idCuentaAhorro())
                .addValue("observacion", e.observacion())
                .addValue("altura", e.alturaCuota())
                .addValue("abonos", e.abonosPendientes())
                .addValue("fechaContable", e.fechaContable())
                .addValue("ultimaCapital", e.ultimaFechaCapital())
                .addValue("ultimaInteres", e.ultimaFechaInteres())
                .addValue("proximaCapital", e.proximaFechaCapital())
                .addValue("proximaInteres", e.proximaFechaInteres())
                .addValue("usuario", e.idUsuario());

        return jdbc.queryForObject(sql, p, Integer.class);
    }

    private static LocalDate fecha(Date d) {
        return d == null ? null : d.toLocalDate();
    }

    private static void exigirUsuario(Integer idUsuario) {
        if (idUsuario == null || idUsuario <= 0) {
            throw new IllegalArgumentException(
                    "El usuario de sesión es obligatorio.");
        }
    }

    /**
     * Conserva la liquidación y todos los medios de recaudo
     * de una operación que será confirmada desde Caja.
     *
     * Debe ejecutarse en la misma transacción que registra
     * el movimiento pendiente en cajas.movimientos_caja.
     */
    public Long registrarPagoPendienteCaja(
            Integer idCarteraCredito,
            Integer idAgenciaReceptora,
            LocalDate fechaPago,
            LocalDate fechaContable,
            Integer periodoInteresMeses,
            Integer cuotasACancelar,
            BigDecimal valorPago,
            Object liquidacion,
            List<PagoCuotasMedioRecaudoDTO> mediosRecaudo,
            Long idCaja,
            Integer idUsuario
    ) {
        exigirUsuario(idUsuario);

        if (idCarteraCredito == null || idCarteraCredito <= 0
                || idAgenciaReceptora == null || idAgenciaReceptora <= 0
                || fechaPago == null || fechaContable == null
                || periodoInteresMeses == null || periodoInteresMeses <= 0
                || valorPago == null || valorPago.signum() <= 0
                || liquidacion == null
                || mediosRecaudo == null || mediosRecaudo.isEmpty()
                || idCaja == null || idCaja <= 0) {
            throw new IllegalArgumentException(
                    "Datos incompletos para registrar el pago pendiente de Caja.");
        }

        String liquidacionJson;
        String mediosJson;

        try {
            liquidacionJson = objectMapper.writeValueAsString(liquidacion);
            mediosJson = objectMapper.writeValueAsString(mediosRecaudo);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(
                    "No fue posible serializar la información del pago.", ex);
        }

        String sql = """
            INSERT INTO cartera.pagos_cuotas (
                id_cartera_credito,
                id_agencia_receptora,
                fecha_pago,
                fecha_contable,
                periodo_interes_meses,
                cuotas_a_cancelar,
                valor_pago,
                liquidacion,
                medios_recaudo,
                requiere_confirmacion_caja,
                id_caja,
                estado,
                fk_seguridad_creacion
            ) VALUES (
                :credito,
                :agencia,
                :fechaPago,
                :fechaContable,
                :periodo,
                :cuotas,
                :valor,
                CAST(:liquidacion AS jsonb),
                CAST(:medios AS jsonb),
                TRUE,
                :caja,
                'PENDIENTE_CAJA',
                :usuario
            )
            RETURNING id_pago
            """;

        var p = new MapSqlParameterSource()
                .addValue("credito", idCarteraCredito)
                .addValue("agencia", idAgenciaReceptora)
                .addValue("fechaPago", fechaPago)
                .addValue("fechaContable", fechaContable)
                .addValue("periodo", periodoInteresMeses)
                .addValue("cuotas", cuotasACancelar)
                .addValue("valor", valorPago)
                .addValue("liquidacion", liquidacionJson)
                .addValue("medios", mediosJson)
                .addValue("caja", idCaja)
                .addValue("usuario", idUsuario);

        Long idPago = jdbc.queryForObject(sql, p, Long.class);

        if (idPago == null) {
            throw new IllegalStateException(
                    "No se generó el identificador de la operación de pago.");
        }

        return idPago;
    }

    /**
     * Registra un pago sin Caja con uno o varios medios AHORROS/CXP.
     * El servicio debe validar y bloquear los recursos antes de invocarlo;
     * el pago se completa en la misma transacción con extracto y comprobante.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public Long registrarPagoSinCaja(
            Integer idCarteraCredito,
            Integer idAgenciaReceptora,
            LocalDate fechaPago,
            LocalDate fechaContable,
            Integer periodoInteresMeses,
            Integer cuotasACancelar,
            BigDecimal valorPago,
            Object liquidacion,
            List<PagoCuotasMedioRecaudoDTO> mediosRecaudo,
            Integer idUsuario) {

        exigirUsuario(idUsuario);
        if (idCarteraCredito == null || idCarteraCredito <= 0
                || idAgenciaReceptora == null || idAgenciaReceptora <= 0
                || fechaPago == null || fechaContable == null
                || periodoInteresMeses == null || periodoInteresMeses <= 0
                || valorPago == null || valorPago.signum() <= 0
                || liquidacion == null || mediosRecaudo == null || mediosRecaudo.isEmpty()) {
            throw new IllegalArgumentException("Datos incompletos para registrar el pago sin Caja.");
        }

        var identificadores = new java.util.HashSet<String>();
        BigDecimal total = BigDecimal.ZERO;
        for (var medio : mediosRecaudo) {
            if (medio == null || medio.tipoMedio() == null || medio.valor() == null
                    || medio.valor().signum() <= 0
                    || medio.valor().stripTrailingZeros().scale() > 0) {
                throw new IllegalArgumentException("El pago contiene un medio de recaudo inválido.");
            }
            String identificador;
            switch (medio.tipoMedio()) {
                case AHORROS -> {
                    if (medio.idCuentaAhorro() == null || medio.idCuentaAhorro() <= 0
                            || medio.idCuentaAhorro() > Integer.MAX_VALUE
                            || medio.idCaja() != null || medio.idCuentaPorPagar() != null) {
                        throw new IllegalArgumentException("Cuenta de ahorro inválida para el recaudo.");
                    }
                    identificador = "AHORROS:" + medio.idCuentaAhorro();
                }
                case CXP -> {
                    if (medio.idCuentaPorPagar() == null || medio.idCuentaPorPagar() <= 0
                            || medio.idCaja() != null || medio.idCuentaAhorro() != null) {
                        throw new IllegalArgumentException("Referencia CXP inválida para el recaudo.");
                    }
                    identificador = "CXP:" + medio.idCuentaPorPagar();
                }
                default -> throw new IllegalArgumentException(
                        "Los pagos sin Caja solo admiten AHORROS y CXP.");
            }
            if (!identificadores.add(identificador)) {
                throw new IllegalArgumentException("No puede repetirse el mismo medio de recaudo.");
            }
            total = total.add(medio.valor());
        }
        if (total.compareTo(valorPago) != 0) {
            throw new IllegalArgumentException("La suma de los medios no coincide con el valor del pago.");
        }

        String liquidacionJson;
        String mediosJson;
        try {
            liquidacionJson = objectMapper.writeValueAsString(liquidacion);
            mediosJson = objectMapper.writeValueAsString(mediosRecaudo);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("No fue posible serializar el pago sin Caja.", ex);
        }

        String sql = """
                INSERT INTO cartera.pagos_cuotas (
                    id_cartera_credito, id_agencia_receptora, fecha_pago, fecha_contable,
                    periodo_interes_meses, cuotas_a_cancelar, valor_pago, liquidacion,
                    medios_recaudo, requiere_confirmacion_caja, id_caja,
                    id_movimiento_caja, estado, fk_seguridad_creacion
                ) VALUES (
                    :credito, :agencia, :fechaPago, :fechaContable,
                    :periodo, :cuotas, :valor, CAST(:liquidacion AS jsonb),
                    CAST(:medios AS jsonb), FALSE, NULL,
                    NULL, 'APLICADO', :usuario
                ) RETURNING id_pago
                """;
        Long idPago = jdbc.queryForObject(sql, new MapSqlParameterSource()
                .addValue("credito", idCarteraCredito)
                .addValue("agencia", idAgenciaReceptora)
                .addValue("fechaPago", fechaPago)
                .addValue("fechaContable", fechaContable)
                .addValue("periodo", periodoInteresMeses)
                .addValue("cuotas", cuotasACancelar)
                .addValue("valor", valorPago)
                .addValue("liquidacion", liquidacionJson)
                .addValue("medios", mediosJson)
                .addValue("usuario", idUsuario), Long.class);
        if (idPago == null || idPago <= 0) {
            throw new IllegalStateException("No se generó el identificador del pago sin Caja.");
        }
        return idPago;
    }

    /** Mantiene la compatibilidad del flujo de pago exclusivamente por AHORROS. */
    @Transactional(propagation = Propagation.MANDATORY)
    public Long registrarPagoAhorros(
            Integer idCarteraCredito,
            Integer idAgenciaReceptora,
            LocalDate fechaPago,
            LocalDate fechaContable,
            Integer periodoInteresMeses,
            Integer cuotasACancelar,
            BigDecimal valorPago,
            Object liquidacion,
            List<PagoCuotasMedioRecaudoDTO> mediosRecaudo,
            Integer idUsuario) {
        if (mediosRecaudo == null || mediosRecaudo.isEmpty()
                || mediosRecaudo.stream().anyMatch(m -> m == null
                || m.tipoMedio() != PagoCuotasMedioRecaudoDTO.TipoMedioRecaudo.AHORROS)) {
            throw new IllegalArgumentException("Este método admite únicamente pagos por AHORROS.");
        }
        return registrarPagoSinCaja(idCarteraCredito, idAgenciaReceptora,
                fechaPago, fechaContable, periodoInteresMeses, cuotasACancelar,
                valorPago, liquidacion, mediosRecaudo, idUsuario);
    }

    /**
     * Completa el pago por AHORROS con su extracto y comprobante CC.
     * Solo puede ejecutarse una vez, en la misma transacción del débito.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void marcarPagoAhorrosAplicado(
            Long idPago, Integer idExtractoCartera,
            String tipoComprobante, String numeroComprobante, Integer idUsuario) {

        exigirUsuario(idUsuario);
        if (idPago == null || idPago <= 0
                || idExtractoCartera == null || idExtractoCartera <= 0
                || !"CC".equals(tipoComprobante)
                || numeroComprobante == null || numeroComprobante.isBlank()) {
            throw new IllegalArgumentException("Pago, extracto y comprobante CC son obligatorios.");
        }
        String sql = """
                UPDATE cartera.pagos_cuotas pc
                   SET id_extracto_cartera = :extracto,
                       tipo_comprobante = :tipo,
                       numero_comprobante = :numero,
                       fk_seguridad_aplicacion = :usuario,
                       fecha_aplicacion = CURRENT_TIMESTAMP
                 WHERE pc.id_pago = :pago
                   AND pc.estado = 'APLICADO'
                   AND pc.requiere_confirmacion_caja = FALSE
                   AND pc.id_caja IS NULL
                   AND pc.id_movimiento_caja IS NULL
                   AND pc.id_extracto_cartera IS NULL
                   AND pc.fecha_aplicacion IS NULL
                   AND EXISTS (
                       SELECT 1 FROM cartera.extractos_cartera e
                        WHERE e.id_extracto_cartera = :extracto
                          AND e.id_cartera_credito = pc.id_cartera_credito
                          AND e.tipo_comprobante = :tipo
                          AND e.numero_comprobante = :numero
                   )
                """;
        int actualizados = jdbc.update(sql, new MapSqlParameterSource()
                .addValue("pago", idPago)
                .addValue("extracto", idExtractoCartera)
                .addValue("tipo", tipoComprobante)
                .addValue("numero", numeroComprobante)
                .addValue("usuario", idUsuario));
        if (actualizados != 1) {
            throw new IllegalStateException("No fue posible finalizar el pago por ahorros con su comprobante.");
        }
    }

    /**
     * Valida bajo bloqueo que la cuenta de ahorro pertenece al titular y agencia
     * del crédito, es captación tipo 2 y tiene un estado operativo habilitado.
     * No descuenta el saldo; únicamente lo consulta bajo bloqueo transaccional.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public BigDecimal validarCuentaAhorroRecaudoConLock(Integer idCredito, Integer idCuentaAhorro) {
        if (idCredito == null || idCredito <= 0 || idCuentaAhorro == null || idCuentaAhorro <= 0) {
            throw new IllegalArgumentException("Crédito y cuenta de ahorro son obligatorios.");
        }

        String sql = """
            SELECT a.saldo_actual_cuenta
              FROM depositos.cuentas_ahorro a
              JOIN cartera.carteras_creditos cr
                ON cr.id_datos_personal = a.id_datos_personal
               AND cr.id_agencia = a.id_agencia
              JOIN depositos.formas_ahorro f
                ON f.id_forma_ahorro = a.id_forma_ahorro
               AND f.id_agencia = a.id_agencia
             WHERE cr.id_cartera_credito = :credito
               AND a.id_cuenta_ahorro = :cuenta
               AND TRIM(f.tipo_captacion_forma) = '2'
               AND TRIM(a.estado_cuenta_cuenta) = 'A'
               AND EXISTS (
                   SELECT 1
                   FROM depositos.estados_ahorros ea
                   WHERE TRIM(ea.codigo_estado_ahorro) = TRIM(a.cuenta_activa)
                     AND ea.operativo = TRUE
               )
             FOR UPDATE OF a
            """;

        return jdbc.query(sql, new MapSqlParameterSource()
                                .addValue("credito", idCredito)
                                .addValue("cuenta", idCuentaAhorro),
                        (rs, fila) -> rs.getBigDecimal("saldo_actual_cuenta"))
                .stream().findFirst().orElseThrow(() -> new IllegalStateException(
                        "La cuenta de ahorro no está habilitada para este crédito."));
    }


    /**
     * Resuelve la cuenta contable de la forma de ahorro para el recaudo.
     * Valida titularidad, agencia, captación tipo 2 y estado operativo,
     * utilizando la parametrización de depositos.estados_ahorros.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public Integer resolverCuentaContableAhorroRecaudo(Integer idCredito, Integer idCuentaAhorro) {
        String sql = """
            SELECT f.cuenta_forma_corto
              FROM cartera.carteras_creditos cr
              JOIN depositos.cuentas_ahorro a
                ON a.id_datos_personal = cr.id_datos_personal
               AND a.id_agencia = cr.id_agencia
              JOIN depositos.formas_ahorro f
                ON f.id_forma_ahorro = a.id_forma_ahorro
               AND f.id_agencia = a.id_agencia
             WHERE cr.id_cartera_credito = :credito
               AND a.id_cuenta_ahorro = :cuenta
               AND TRIM(f.tipo_captacion_forma) = '2'
               AND TRIM(a.estado_cuenta_cuenta) = 'A'
               AND EXISTS (
                   SELECT 1
                   FROM depositos.estados_ahorros ea
                   WHERE TRIM(ea.codigo_estado_ahorro) = TRIM(a.cuenta_activa)
                     AND ea.operativo = TRUE
               )
            """;

        Integer cuenta = jdbc.query(sql, new MapSqlParameterSource()
                                .addValue("credito", idCredito)
                                .addValue("cuenta", idCuentaAhorro),
                        (rs, i) -> rs.getObject("cuenta_forma_corto", Integer.class))
                .stream().findFirst().orElseThrow(() -> new IllegalStateException(
                        "La cuenta de ahorro no tiene una forma válida para el recaudo."));

        if (cuenta == null || cuenta <= 0) {
            throw new IllegalStateException(
                    "La forma de ahorro no tiene cuenta contable configurada.");
        }

        return cuenta;
    }

    /**
     * Vincula el pago de cuotas con el movimiento pendiente
     * generado en Caja.
     *
     * Debe ejecutarse dentro de la misma transacción que
     * registra el pago y el movimiento de Caja.
     */
    public void vincularMovimientoCaja(
            Long idPago,
            Long idMovimientoCaja
    ) {
        if (idPago == null || idPago <= 0
                || idMovimientoCaja == null || idMovimientoCaja <= 0) {
            throw new IllegalArgumentException(
                    "Debe indicar el pago y el movimiento de Caja.");
        }

        String sql = """
            UPDATE cartera.pagos_cuotas
            SET id_movimiento_caja = :movimiento
            WHERE id_pago = :pago
              AND estado = 'PENDIENTE_CAJA'
              AND requiere_confirmacion_caja = TRUE
              AND id_movimiento_caja IS NULL
              AND EXISTS (
                  SELECT 1
                  FROM cajas.movimientos_caja mc
                  WHERE mc.id_movimiento_caja = :movimiento
                    AND mc.modulo_origen = 'CARTERA'
                    AND mc.proceso_origen = 'PAGO_CUOTAS'
                    AND mc.tabla_origen = 'cartera.pagos_cuotas'
                    AND mc.id_origen = :pago
                    AND mc.estado = 'PENDIENTE'
              )
            """;

        var params = new MapSqlParameterSource()
                .addValue("pago", idPago)
                .addValue("movimiento", idMovimientoCaja);

        if (jdbc.update(sql, params) != 1) {
            throw new IllegalStateException(
                    "No fue posible vincular el pago con su movimiento pendiente de Caja.");
        }
    }

    /**
     * Bloquea el pago asociado al movimiento de Caja.
     * Debe llamarse dentro de la transacción de confirmación, después
     * de bloquear el movimiento de Caja y antes de bloquear el crédito.
     */
    public PagoPendienteBloqueado bloquearPagoPendienteCaja(
            Long idPago, Long idMovimientoCaja, Long idCaja) {

        if (idPago == null || idPago <= 0
                || idMovimientoCaja == null || idMovimientoCaja <= 0
                || idCaja == null || idCaja <= 0) {
            throw new IllegalArgumentException("Identificadores inválidos para confirmar el pago.");
        }

        String sql = """
                SELECT id_pago, id_cartera_credito, id_agencia_receptora,
                       fecha_pago, fecha_contable, periodo_interes_meses,
                       cuotas_a_cancelar, valor_pago,
                       liquidacion::text AS liquidacion_json,
                       medios_recaudo::text AS medios_recaudo_json,
                       id_caja, id_movimiento_caja
                FROM cartera.pagos_cuotas
                WHERE id_pago = :pago
                  AND id_movimiento_caja = :movimiento
                  AND id_caja = :caja
                  AND estado = 'PENDIENTE_CAJA'
                  AND requiere_confirmacion_caja = TRUE
                  AND EXISTS (
                      SELECT 1
                      FROM cajas.movimientos_caja mc
                      WHERE mc.id_movimiento_caja = :movimiento
                        AND mc.id_caja = :caja
                        AND mc.modulo_origen = 'CARTERA'
                        AND mc.proceso_origen = 'PAGO_CUOTAS'
                        AND mc.tabla_origen = 'cartera.pagos_cuotas'
                        AND mc.id_origen = :pago
                        AND mc.estado = 'PENDIENTE'
                  )
                FOR UPDATE
                """;

        var params = new MapSqlParameterSource()
                .addValue("pago", idPago)
                .addValue("movimiento", idMovimientoCaja)
                .addValue("caja", idCaja);

        return jdbc.query(sql, params, (rs, fila) -> new PagoPendienteBloqueado(
                        rs.getLong("id_pago"),
                        rs.getInt("id_cartera_credito"),
                        rs.getInt("id_agencia_receptora"),
                        fecha(rs.getDate("fecha_pago")),
                        fecha(rs.getDate("fecha_contable")),
                        rs.getInt("periodo_interes_meses"),
                        rs.getObject("cuotas_a_cancelar", Integer.class),
                        rs.getBigDecimal("valor_pago"),
                        rs.getString("liquidacion_json"),
                        rs.getString("medios_recaudo_json"),
                        rs.getLong("id_caja"),
                        rs.getLong("id_movimiento_caja")))
                .stream().findFirst().orElseThrow(() -> new IllegalStateException(
                        "El pago no está pendiente o no corresponde al movimiento y caja seleccionados."));
    }

    public record PagoPendienteBloqueado(
            Long idPago,
            Integer idCarteraCredito,
            Integer idAgenciaReceptora,
            LocalDate fechaPago,
            LocalDate fechaContable,
            Integer periodoInteresMeses,
            Integer cuotasACancelar,
            BigDecimal valorPago,
            String liquidacionJson,
            String mediosRecaudoJson,
            Long idCaja,
            Long idMovimientoCaja) {
    }

    /**
     * Finaliza la aplicación financiera y vincula su extracto y comprobante.
     * Exige el movimiento de Caja pendiente y un extracto del mismo crédito;
     * la confirmación física de Caja se realiza posteriormente.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void marcarPagoAplicado(
            Long idPago,
            Long idMovimientoCaja,
            Integer idExtractoCartera,
            String tipoComprobante,
            String numeroComprobante,
            Integer idUsuario) {

        exigirUsuario(idUsuario);
        if (idPago == null || idPago <= 0
                || idMovimientoCaja == null || idMovimientoCaja <= 0
                || idExtractoCartera == null || idExtractoCartera <= 0
                || !"CC".equals(tipoComprobante)
                || numeroComprobante == null || numeroComprobante.isBlank()) {
            throw new IllegalArgumentException(
                    "El pago, extracto y comprobante CC son obligatorios.");
        }

        String sql = """
                UPDATE cartera.pagos_cuotas AS pc
                SET estado = 'APLICADO',
                    id_extracto_cartera = :extracto,
                    tipo_comprobante = :tipo,
                    numero_comprobante = :numero,
                    fk_seguridad_aplicacion = :usuario,
                    fecha_aplicacion = CURRENT_TIMESTAMP
                WHERE pc.id_pago = :pago
                  AND pc.id_movimiento_caja = :movimiento
                  AND pc.estado = 'PENDIENTE_CAJA'
                  AND pc.requiere_confirmacion_caja = TRUE
                  AND pc.id_extracto_cartera IS NULL
                  AND pc.fecha_aplicacion IS NULL
                  AND EXISTS (
                      SELECT 1
                      FROM cartera.extractos_cartera e
                      WHERE e.id_extracto_cartera = :extracto
                        AND e.id_cartera_credito = pc.id_cartera_credito
                        AND e.tipo_comprobante = :tipo
                        AND e.numero_comprobante = :numero
                  )
                  AND EXISTS (
                      SELECT 1
                      FROM cajas.movimientos_caja m
                      WHERE m.id_movimiento_caja = :movimiento
                        AND m.id_caja = pc.id_caja
                        AND m.modulo_origen = 'CARTERA'
                        AND m.proceso_origen = 'PAGO_CUOTAS'
                        AND m.tabla_origen = 'cartera.pagos_cuotas'
                        AND m.id_origen = pc.id_pago
                        AND m.estado = 'PENDIENTE'
                  )
                """;

        var parametros = new MapSqlParameterSource()
                .addValue("pago", idPago)
                .addValue("movimiento", idMovimientoCaja)
                .addValue("extracto", idExtractoCartera)
                .addValue("tipo", tipoComprobante)
                .addValue("numero", numeroComprobante)
                .addValue("usuario", idUsuario);

        if (jdbc.update(sql, parametros) != 1) {
            throw new IllegalStateException(
                    "No fue posible aplicar el pago: cambió de estado o no coincide con su extracto y movimiento de Caja.");
        }
    }

    /**
     * Verifica si el crédito tiene un pago pendiente
     * de confirmación en Caja.
     *
     * Debe invocarse después de bloquear el crédito,
     * dentro de la misma transacción.
     */
    public boolean existePagoPendienteCaja(Integer idCarteraCredito) {

        if (idCarteraCredito == null || idCarteraCredito <= 0) {
            throw new IllegalArgumentException(
                    "Debe indicar un crédito válido.");
        }

        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM cartera.pagos_cuotas
                WHERE id_cartera_credito = :credito
                  AND estado = 'PENDIENTE_CAJA'
            )
            """;

        Boolean existe = jdbc.queryForObject(
                sql,
                new MapSqlParameterSource()
                        .addValue("credito", idCarteraCredito),
                Boolean.class
        );

        return Boolean.TRUE.equals(existe);
    }

    public record CajaRecaudo(
            Long idCaja,
            Integer idAgencia,
            String codigoCaja,
            String descripcion) {
    }

    public record CreditoBloqueado(
            Integer idCredito,
            Integer idAgencia,
            String estado,
            BigDecimal saldo,
            Integer alturaCuota,
            LocalDate ultimaFechaCapital,
            LocalDate ultimaFechaInteres,
            LocalDate proximaFechaCapital,
            LocalDate proximaFechaInteres) {
    }

    /**
     * Datos del extracto de pago de Cartera.
     * Conserva los días de mora calculados en la liquidación,
     * sin modificar los valores financieros del pago.
     */
    public record ExtractoPago(
            Integer idCredito,
            LocalDate fechaPago,
            String tipoComprobante,
            String numeroComprobante,
            BigDecimal capital,
            BigDecimal interesCausado,
            BigDecimal interesIngreso,
            BigDecimal interesAnticipado,
            BigDecimal mora,
            Integer diasMora,
            BigDecimal seguro,
            BigDecimal otros,
            BigDecimal efectivo,
            BigDecimal cheques,
            BigDecimal notas,
            BigDecimal consignacion,
            BigDecimal pse,
            BigDecimal otroPago,
            Integer idCuentaAhorro,
            String observacion,
            Integer alturaCuota,
            BigDecimal abonosPendientes,
            LocalDate fechaContable,
            LocalDate ultimaFechaCapital,
            LocalDate ultimaFechaInteres,
            LocalDate proximaFechaCapital,
            LocalDate proximaFechaInteres,
            Integer idUsuario) {
    }

    /**
     * Consulta si el crédito tiene una prórroga vigente.
     * Las prórrogas históricas cerradas no cuentan.
     */
    public boolean tieneProrrogaVigente(Integer idCarteraCredito) {

        if (idCarteraCredito == null || idCarteraCredito <= 0) {
            throw new IllegalArgumentException(
                    "Debe indicar un crédito válido.");
        }

        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM cartera.creditos_prorrogas
                WHERE id_cartera_credito = :idCredito
                  AND estado_prorroga = 'A'
            )
            """;

        Boolean resultado = jdbc.queryForObject(
                sql,
                new MapSqlParameterSource()
                        .addValue("idCredito", idCarteraCredito),
                Boolean.class
        );

        return Boolean.TRUE.equals(resultado);
    }

    /** Consulta el nombre del titular para describir comprobantes, sin modificar datos. */
    public String consultarNombreTitular(Integer idCredito) {
        String sql = """
                SELECT TRIM(CONCAT_WS(' ', NULLIF(TRIM(p.nombres), ''),
                    NULLIF(TRIM(p.primer_apellido), ''),
                    NULLIF(TRIM(p.segundo_apellido), ''))) AS nombre
                FROM cartera.carteras_creditos c
                JOIN hoja_vida.datos_personales p
                  ON p.id_datos_personal = c.id_datos_personal
                WHERE c.id_cartera_credito = :credito
                """;
        return jdbc.query(sql, new MapSqlParameterSource("credito", idCredito),
                (rs, row) -> rs.getString("nombre"))
                .stream().findFirst().orElseThrow(() -> new IllegalStateException(
                        "No fue posible identificar al titular del crédito."));
    }

    /** Consulta documento y nombre del titular para identificar los textos contables. */
    public IdentificacionTitular consultarIdentificacionTitular(Integer idCredito) {
        String sql = """
                SELECT TRIM(p.documento) AS documento,
                       TRIM(CONCAT_WS(' ', NULLIF(TRIM(p.nombres), ''),
                           NULLIF(TRIM(p.primer_apellido), ''),
                           NULLIF(TRIM(p.segundo_apellido), ''))) AS nombre
                FROM cartera.carteras_creditos c
                JOIN hoja_vida.datos_personales p
                  ON p.id_datos_personal = c.id_datos_personal
                WHERE c.id_cartera_credito = :credito
                """;
        var titular = jdbc.query(sql, new MapSqlParameterSource("credito", idCredito),
                (rs, row) -> new IdentificacionTitular(
                        rs.getString("documento"), rs.getString("nombre")))
                .stream().findFirst().orElseThrow(() -> new IllegalStateException(
                        "No fue posible identificar al titular del crédito."));
        if (titular.documento() == null || titular.documento().isBlank()
                || titular.nombreCompleto() == null || titular.nombreCompleto().isBlank()) {
            throw new IllegalStateException("El titular no tiene documento o nombre completo.");
        }
        return titular;
    }

    public record IdentificacionTitular(String documento, String nombreCompleto) { }

    // Resolución contable consolidada
    public DatosContablesCredito consultarDatosCredito(Integer idCarteraCredito) {
        if (idCarteraCredito == null || idCarteraCredito <= 0) {
            throw new IllegalArgumentException("El crédito es obligatorio.");
        }
        String sql = """
                SELECT c.id_cartera_credito, c.id_agencia,
                       c.codigo_clasificacion_credito, c.codigo_forma_pago,
                       g.tipo_garantia, c.edad_contable
                FROM cartera.carteras_creditos c
                JOIN cartera.garantias_creditos g
                  ON g.codigo_garantia_credito = c.codigo_garantia_credito
                WHERE c.id_cartera_credito = :credito
                """;
        return jdbc.query(sql, new MapSqlParameterSource("credito", idCarteraCredito),
                        (rs, row) -> new DatosContablesCredito(
                                rs.getInt("id_cartera_credito"),
                                rs.getInt("id_agencia"),
                                rs.getString("codigo_clasificacion_credito"),
                                rs.getString("codigo_forma_pago"),
                                rs.getString("tipo_garantia"),
                                rs.getString("edad_contable")))
                .stream().findFirst().orElseThrow(() -> new IllegalStateException(
                        "No fue posible resolver la clasificación contable del crédito "
                                + idCarteraCredito + "."));
    }

    /**
     * Resuelve la cuenta contable de Cartera según agencia, clasificación,
     * edad contable, forma de pago, garantía y segmento productivo.
     * Admite ingresos por intereses de mora y rechaza parametrizaciones
     * inexistentes o ambiguas.
     */
    public CuentaCartera resolverCuenta(
            DatosContablesCredito credito,
            String concepto,
            String segmentoProductivo) {

        if (credito == null
                || credito.edadContable() == null
                || credito.edadContable().isBlank()) {

            throw new IllegalStateException(
                    "El crédito no tiene edad contable válida.");
        }

        if (!java.util.Set.of(
                "CAPITAL",
                "INTERES",
                "INGRESO_INTERES_MORA",
                "ALIVIOS",
                "COSTAS_JUDICIALES"
        ).contains(concepto)) {

            throw new IllegalArgumentException(
                    "Concepto contable no admitido: " + concepto);
        }

        String sql = """
            SELECT
                ccc.id_catalogo_cuenta,
                ccc.codigo_cuenta,
                CASE
                    WHEN ccc.codigo_forma_pago = :forma THEN 1
                    ELSE 0
                END AS especificidad_forma,
                CASE
                    WHEN ccc.tipo_garantia = :garantia THEN 1
                    ELSE 0
                END AS especificidad_garantia,
                CASE
                    WHEN ccc.codigo_segmento_productivo = :segmento THEN 1
                    ELSE 0
                END AS especificidad_segmento
            FROM cartera.cuentas_contables_cartera ccc
            WHERE ccc.id_agencia = :agencia
              AND ccc.concepto = :concepto
              AND ccc.codigo_clasificacion_credito = :clasificacion
              AND ccc.codigo_edad_riesgo = :edadContable
              AND (
                  ccc.codigo_forma_pago = :forma
                  OR ccc.codigo_forma_pago IS NULL
              )
              AND (
                  ccc.tipo_garantia = :garantia
                  OR ccc.tipo_garantia IS NULL
              )
              AND (
                  ccc.codigo_segmento_productivo = :segmento
                  OR ccc.codigo_segmento_productivo IS NULL
              )
              AND ccc.activo = TRUE
            ORDER BY
                especificidad_forma DESC,
                especificidad_garantia DESC,
                especificidad_segmento DESC
            LIMIT 2
            """;

        var parametros = new MapSqlParameterSource()
                .addValue("agencia", credito.idAgencia())
                .addValue("concepto", concepto)
                .addValue("clasificacion", credito.clasificacion())
                .addValue("edadContable", credito.edadContable())
                .addValue("forma", credito.formaPago())
                .addValue("garantia", credito.tipoGarantia())
                .addValue("segmento", segmentoProductivo);

        List<Candidato> candidatos = jdbc.query(
                sql,
                parametros,
                (rs, row) -> new Candidato(
                        new CuentaCartera(
                                rs.getInt("id_catalogo_cuenta"),
                                rs.getString("codigo_cuenta")
                        ),
                        rs.getInt("especificidad_forma"),
                        rs.getInt("especificidad_garantia"),
                        rs.getInt("especificidad_segmento")
                )
        );

        if (candidatos.isEmpty()) {

            throw new IllegalStateException(
                    "No existe cuenta de " + concepto
                            + " para crédito " + credito.idCredito()
                            + ", agencia " + credito.idAgencia()
                            + ", edad contable " + credito.edadContable() + ".");
        }

        if (candidatos.size() > 1) {

            Candidato a = candidatos.get(0);
            Candidato b = candidatos.get(1);

            if (a.forma() == b.forma()
                    && a.garantia() == b.garantia()
                    && a.segmento() == b.segmento()) {

                throw new IllegalStateException(
                        "Parametrización contable ambigua para "
                                + concepto
                                + " del crédito "
                                + credito.idCredito() + ".");
            }
        }

        return candidatos.get(0).cuenta();
    }

    /** Resuelve un parámetro contable de agencia sin seleccionar cuentas arbitrarias. */
    public CuentaCartera resolverParametroContable(Integer idAgencia, String concepto) {
        if (idAgencia == null || idAgencia <= 0
                || concepto == null || concepto.isBlank()) {
            throw new IllegalArgumentException("Agencia y concepto contable son obligatorios.");
        }
        String sql = """
                SELECT id_catalogo_cuenta, codigo_cuenta
                FROM cartera.parametros_contables
                WHERE id_agencia = :agencia
                  AND concepto = :concepto
                  AND activo = TRUE
                  AND id_linea_credito IS NULL
                LIMIT 2
                """;
        var p = new MapSqlParameterSource()
                .addValue("agencia", idAgencia)
                .addValue("concepto", concepto);
        List<CuentaCartera> cuentas = jdbc.query(sql, p,
                (rs, fila) -> new CuentaCartera(
                        rs.getInt("id_catalogo_cuenta"), rs.getString("codigo_cuenta")));
        if (cuentas.isEmpty()) {
            throw new IllegalStateException("No existe parámetro contable general activo de "
                    + concepto + " para la agencia " + idAgencia + ".");
        }
        if (cuentas.size() > 1) {
            throw new IllegalStateException("Parametrización contable ambigua de "
                    + concepto + " para la agencia " + idAgencia + ".");
        }
        return cuentas.get(0);
    }

    public record DatosContablesCredito(Integer idCredito, Integer idAgencia,
                                        String clasificacion, String formaPago,
                                        String tipoGarantia, String edadContable) { }

    public record CuentaCartera(Integer idCatalogoCuenta, String codigoCuenta) { }

    private record Candidato(CuentaCartera cuenta, int forma, int garantia, int segmento) { }

    // Bloqueo y validación de saldos auxiliares
    @Transactional(propagation = Propagation.MANDATORY)
    public SaldosBloqueados bloquearYValidar(
            Integer idCredito,
            BigDecimal seguroAplicado,
            BigDecimal alivioAplicado,
            BigDecimal costasJudicialesAplicadas) {

        if (idCredito == null || idCredito <= 0) {
            throw new IllegalArgumentException("El crédito es obligatorio.");
        }
        validarImporte(seguroAplicado, "seguros");
        validarImporte(alivioAplicado, "alivios");
        validarImporte(costasJudicialesAplicadas, "costas judiciales");

        var p = new MapSqlParameterSource("credito", idCredito);

        // Orden estable de bloqueo. Los procesos de causación y abono deben
        // respetar este mismo protocolo para evitar carreras y bloqueos cruzados.
        jdbc.query("""
                SELECT id_credito_seguro
                FROM cartera.creditos_seguros
                WHERE id_cartera_credito = :credito AND activo = TRUE
                ORDER BY id_credito_seguro FOR UPDATE
                """, p, (rs, i) -> rs.getLong(1));

        jdbc.query("""
                SELECT id_credito_alivio
                FROM cartera.creditos_alivios
                WHERE id_cartera_credito = :credito
                ORDER BY id_credito_alivio FOR UPDATE
                """, p, (rs, i) -> rs.getLong(1));

        jdbc.query("""
                SELECT id_credito_costa_judicial
                FROM cartera.creditos_costas_judiciales
                WHERE id_cartera_credito = :credito AND activo = TRUE
                ORDER BY id_credito_costa_judicial FOR UPDATE
                """, p, (rs, i) -> rs.getLong(1));

        BigDecimal saldoSeguro = consultarSaldo("""
                SELECT COALESCE(SUM(COALESCE(d.valor_debito, 0)
                                    - COALESCE(d.valor_credito, 0)), 0)
                FROM cartera.creditos_seguros s
                JOIN cartera.creditos_seguros_detalle d
                  ON d.id_credito_seguro = s.id_credito_seguro
                WHERE s.id_cartera_credito = :credito
                  AND s.activo = TRUE AND d.estado = 'A'
                """, p);

        BigDecimal saldoAlivio = consultarSaldo("""
                SELECT COALESCE(SUM(COALESCE(d.valor_debito, 0)
                                    - COALESCE(d.valor_credito, 0)), 0)
                FROM cartera.creditos_alivios a
                JOIN cartera.creditos_alivios_detalle d
                  ON d.id_credito_alivio = a.id_credito_alivio
                WHERE a.id_cartera_credito = :credito AND d.estado = 'A'
                """, p);

        BigDecimal saldoCostasJudiciales = consultarSaldo("""
                SELECT COALESCE(SUM(COALESCE(d.valor_debito, 0)
                                    - COALESCE(d.valor_credito, 0)), 0)
                FROM cartera.creditos_costas_judiciales c
                JOIN cartera.creditos_costas_judiciales_detalle d
                  ON d.id_credito_costa_judicial = c.id_credito_costa_judicial
                WHERE c.id_cartera_credito = :credito
                  AND c.activo = TRUE AND d.estado = 'A'
                """, p);

        if (saldoSeguro.signum() < 0 || saldoAlivio.signum() < 0
                || saldoCostasJudiciales.signum() < 0) {
            throw new IllegalStateException("El crédito tiene saldos auxiliares negativos.");
        }
        if (seguroAplicado.compareTo(saldoSeguro) > 0) {
            throw new IllegalStateException("El recaudo de seguros supera el saldo pendiente.");
        }
        if (alivioAplicado.compareTo(saldoAlivio) > 0) {
            throw new IllegalStateException("El recaudo de alivios supera el saldo pendiente.");
        }
        if (costasJudicialesAplicadas.compareTo(saldoCostasJudiciales) > 0) {
            throw new IllegalStateException(
                    "El recaudo de costas judiciales supera el saldo pendiente.");
        }

        return new SaldosBloqueados(saldoSeguro, saldoAlivio, saldoCostasJudiciales);
    }

    private BigDecimal consultarSaldo(String sql, MapSqlParameterSource parametros) {
        BigDecimal valor = jdbc.queryForObject(sql, parametros, BigDecimal.class);
        return valor == null ? BigDecimal.ZERO : valor;
    }

    private static void validarImporte(BigDecimal importe, String concepto) {
        if (importe == null || importe.signum() < 0) {
            throw new IllegalArgumentException("Importe inválido de " + concepto + ".");
        }
    }

    public record SaldosBloqueados(
            BigDecimal seguros,
            BigDecimal alivios,
            BigDecimal costasJudiciales) { }

    /** Registra créditos contra saldos causados; invocar en la misma transacción del pago. */
    @Transactional(propagation = Propagation.MANDATORY)
    public void registrarAbonosAuxiliares(Integer idCredito, BigDecimal seguros,
                                          BigDecimal alivios, BigDecimal costas, String tipoComprobante,
                                          String numeroComprobante, Integer idUsuario) {
        exigirUsuario(idUsuario);
        if (tipoComprobante == null || tipoComprobante.isBlank()
                || numeroComprobante == null || numeroComprobante.isBlank()) {
            throw new IllegalArgumentException("El comprobante del pago es obligatorio.");
        }
        bloquearYValidar(idCredito, seguros, alivios, costas);
        registrarAbonosConcepto("seguros", idCredito, seguros, tipoComprobante, numeroComprobante, idUsuario);
        registrarAbonosConcepto("alivios", idCredito, alivios, tipoComprobante, numeroComprobante, idUsuario);
        registrarAbonosConcepto("costas", idCredito, costas, tipoComprobante, numeroComprobante, idUsuario);
    }

    private void registrarAbonosConcepto(String concepto, Integer idCredito, BigDecimal importe,
                                         String tipoComprobante, String numeroComprobante, Integer idUsuario) {
        validarImporte(importe, concepto);
        if (importe.signum() == 0) return;
        String cabecera;
        String detalle;
        String clave;
        String observacion;
        String filtro;
        switch (concepto) {
            case "seguros" -> {
                cabecera = "creditos_seguros"; detalle = "creditos_seguros_detalle";
                clave = "id_credito_seguro"; observacion = "observacion_seguro";
                filtro = " AND h.activo = TRUE";
            }
            case "alivios" -> {
                cabecera = "creditos_alivios"; detalle = "creditos_alivios_detalle";
                clave = "id_credito_alivio"; observacion = "observacion_alivio";
                filtro = "";
            }
            case "costas" -> {
                cabecera = "creditos_costas_judiciales"; detalle = "creditos_costas_judiciales_detalle";
                clave = "id_credito_costa_judicial"; observacion = "observacion_costa";
                filtro = " AND h.activo = TRUE";
            }
            default -> throw new IllegalArgumentException("Concepto no admitido.");
        }
        String consulta = "SELECT h." + clave + " AS id, "
                + "COALESCE(SUM(CASE WHEN d.estado = 'A' THEN d.valor_debito - d.valor_credito ELSE 0 END), 0) AS saldo "
                + "FROM cartera." + cabecera + " h LEFT JOIN cartera." + detalle
                + " d ON d." + clave + " = h." + clave
                + " WHERE h.id_cartera_credito = :credito" + filtro
                + " GROUP BY h." + clave + " ORDER BY h." + clave;
        var saldos = jdbc.query(consulta, new MapSqlParameterSource("credito", idCredito),
                (rs, i) -> new SaldoAuxiliar(rs.getInt("id"), rs.getBigDecimal("saldo")));
        BigDecimal restante = importe;
        for (SaldoAuxiliar saldo : saldos) {
            if (saldo.saldo().signum() < 0) {
                throw new IllegalStateException("Saldo auxiliar negativo en " + concepto);
            }
            BigDecimal aplicar = restante.min(saldo.saldo());
            if (aplicar.signum() <= 0) continue;
            String columnasExtra = "";
            String valoresExtra = "";
            if ("costas".equals(concepto)) {
                columnasExtra = ", tipo_movimiento";
                valoresExtra = ", 'A'";
            }
            String sql = "INSERT INTO cartera." + detalle + " (" + clave
                    + ", fecha_movimiento, valor_debito, valor_credito, tipo_comprobante, "
                    + "numero_comprobante, " + observacion
                    + ", estado, fk_seguridad_creacion, fk_seguridad_edicion"
                    + columnasExtra + ") VALUES (:id, CURRENT_DATE, 0, :valor, :tipo, :numero, "
                    + ":observacion, 'A', :usuario, :usuario" + valoresExtra + ")";
            var params = new MapSqlParameterSource()
                    .addValue("id", saldo.id()).addValue("valor", aplicar)
                    .addValue("tipo", tipoComprobante).addValue("numero", numeroComprobante)
                    .addValue("observacion", "Abono por pago de cuotas")
                    .addValue("usuario", idUsuario);
            if (jdbc.update(sql, params) != 1) {
                throw new IllegalStateException("No se registró el abono de " + concepto);
            }
            restante = restante.subtract(aplicar);
            if (restante.signum() == 0) break;
        }
        if (restante.signum() != 0) {
            throw new IllegalStateException("Saldo insuficiente para abonar " + concepto);
        }
    }

    private record SaldoAuxiliar(Integer id, BigDecimal saldo) { }

}
