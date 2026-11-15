package co.assip.erp.cartera.reversion_prorrogas;

import co.assip.erp.cartera.reversion_pagos.ReversionPagosRepository;
import co.assip.erp.cartera.reversion_pagos.ReversionPagosService;
import co.assip.erp.tesoreria.operaciones_pendientes.TesoreriaOperacionesPendientesService;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Coordina la reversión contractual, contable y financiera de Prórrogas V1. */
@Service
@RequiredArgsConstructor
public class ReversionProrrogasService {
    private final ReversionProrrogasRepository repository;
    private final ReversionPagosRepository pagosRepository;
    private final ReversionPagosService pagosService;
    private final UsuarioSesionService usuarioSesionService;
    private final ObjectMapper objectMapper;
    private final JdbcTemplate jdbcTemplate;
    private final TesoreriaOperacionesPendientesService operacionesPendientesService;

    /**
     * Revierte una prórroga aplicada en una sola transacción.
     * Caja y Tesorería conservan sus movimientos ejecutados para regularización propia.
     */
    @Transactional(rollbackFor = Exception.class)
    public ResultadoReversion confirmarReversion(
            Integer idCredito, Long idProrroga, LocalDate fechaReversion, String motivo) {
        if (idCredito == null || idCredito <= 0 || idProrroga == null || idProrroga <= 0
                || fechaReversion == null || motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("Crédito, prórroga, fecha y motivo son obligatorios.");
        }
        Integer idUsuario = usuarioSesionService.idUsuario();
        var credito = repository.bloquearCredito(idCredito);
        usuarioSesionService.validarAgencia(credito.idAgencia());
        if (!"A".equals(credito.estado())) {
            throw new IllegalStateException("El crédito debe encontrarse activo.");
        }
        var prorroga = repository.bloquearProrroga(idProrroga, idCredito);
        if (!credito.idAgencia().equals(prorroga.idAgencia()) || !"A".equals(prorroga.estado())) {
            throw new IllegalStateException("La prórroga no está aplicada en la agencia del crédito.");
        }
        if (!"CC".equals(prorroga.tipoComprobante())
                || prorroga.numeroComprobante() == null || prorroga.numeroComprobante().isBlank()
                || prorroga.totalPagado() == null || prorroga.totalPagado().signum() <= 0) {
            throw new IllegalStateException("La prórroga no tiene comprobante o valor original válido.");
        }
        if (repository.existeProrrogaAplicadaPosterior(idCredito, prorroga.numeroProrroga())) {
            throw new IllegalStateException("Existe una prórroga aplicada posteriormente.");
        }
        var pagoBloqueado = repository.bloquearPagoOriginal(prorroga);
        repository.validarSinExtractosActivosPosteriores(idCredito, pagoBloqueado.idExtracto());
        repository.validarSinPagosAplicadosPosteriores(idCredito, pagoBloqueado.idPago());
        var pago = pagosRepository.consultarPago(pagoBloqueado.idPago());
        if (!"APLICADO".equals(pago.estado()) || !idCredito.equals(pago.idCredito())
                || !credito.idAgencia().equals(pago.idAgencia())
                || !prorroga.numeroComprobante().trim().equals(pago.numeroComprobante().trim())
                || pago.valor().compareTo(prorroga.totalPagado()) != 0
                || pago.idExtractoCartera() == null
                || pago.idExtractoCartera().longValue() != pagoBloqueado.idExtracto().longValue()) {
            throw new IllegalStateException("El pago original no coincide con la prórroga bloqueada.");
        }
        JsonNode medios = leerYValidarMedios(pago);
        BigDecimal totalMedios = BigDecimal.ZERO;
        BigDecimal recaudoBancario = BigDecimal.ZERO;
        BigDecimal recaudoCaja = BigDecimal.ZERO;
        for (JsonNode medio : medios) {
            String tipo = medio.path("tipoMedio").asText();
            BigDecimal valor = medio.path("valor").decimalValue();
            if (!"CAJA".equals(tipo) && !"AHORROS".equals(tipo) && !"CXP".equals(tipo)) {
                throw new IllegalStateException("Medio de recaudo no soportado: " + tipo);
            }
            totalMedios = totalMedios.add(valor);
            if ("CXP".equals(tipo)) {
                recaudoBancario = recaudoBancario.add(valor);
            } else if ("CAJA".equals(tipo)) {
                recaudoCaja = recaudoCaja.add(valor);
            }
        }
        if (totalMedios.compareTo(pago.valor()) != 0) {
            throw new IllegalStateException("Los medios no suman el pago original de la prórroga.");
        }
        String ccReversion = pagosService.registrarComprobanteReversionProrroga(
                idProrroga, pago.idPago(), credito.idAgencia(),
                prorroga.tipoComprobante(), prorroga.numeroComprobante(),
                prorroga.totalPagado(), fechaReversion, idUsuario);
        pagosRepository.revertirAbonosAuxiliares(idCredito, prorroga.numeroComprobante(),
                ccReversion, fechaReversion, idUsuario);
        BigDecimal capitalRestituido = repository.restituirCapital(prorroga, pagoBloqueado, idUsuario);
        repository.restituirFechas(prorroga, idUsuario);
        BigDecimal ahorrosDevueltos = pagosService.devolverAhorros(
                pago, ccReversion, fechaReversion, idUsuario, "REVERSION_PRORROGA", idProrroga);
        pagosRepository.registrarReversionExtracto(
                pago, ccReversion, fechaReversion, motivo, idUsuario);
        pagosRepository.marcarPagoReversado(
                pago.idPago(), ccReversion, fechaReversion, motivo, idUsuario);
        registrarReversionCaja(idProrroga, prorroga.numeroComprobante(),
                recaudoCaja, idUsuario);
        registrarReversionTesoreria(idProrroga, credito.idAgencia(),
                recaudoBancario, motivo, idUsuario);
        repository.marcarReversada(idProrroga, idUsuario);
        return new ResultadoReversion(idProrroga, pago.idPago(), ccReversion,
                capitalRestituido, ahorrosDevueltos);
    }

    /**
     * Consulta los efectos financieros estimados sin bloquear ni modificar registros.
     * La confirmación vuelve a validar todo dentro de su propia transacción.
     */
    @Transactional(readOnly = true)
    public VistaPrevia consultarVistaPrevia(Integer idCredito, Long idProrroga) {
        if (idCredito == null || idCredito <= 0 || idProrroga == null || idProrroga <= 0) {
            throw new IllegalArgumentException("Crédito y prórroga son obligatorios.");
        }
        List<Map<String, Object>> filas = jdbcTemplate.queryForList("""
                SELECT p.id_credito_prorroga, p.numero_prorroga, p.estado_prorroga,
                       p.id_agencia, p.tipo_comprobante, p.numero_comprobante,
                       p.valor_total_pagado, c.codigo_estado_cartera,
                       c.proxima_fecha_capital, c.proxima_fecha_interes, c.fecha_final,
                       p.proxima_fecha_capital_nueva, p.proxima_fecha_interes_nueva,
                       p.fecha_vencimiento_nueva
                FROM cartera.creditos_prorrogas p
                JOIN cartera.carteras_creditos c
                  ON c.id_cartera_credito = p.id_cartera_credito
                WHERE p.id_credito_prorroga = ? AND p.id_cartera_credito = ?
                  AND p.id_agencia = c.id_agencia
                """, idProrroga, idCredito);
        if (filas.size() != 1) {
            throw new IllegalStateException("No existe la prórroga del crédito seleccionado.");
        }
        Map<String, Object> p = filas.get(0);
        Integer idAgencia = ((Number) p.get("id_agencia")).intValue();
        usuarioSesionService.validarAgencia(idAgencia);
        String ccOriginal = (String) p.get("numero_comprobante");
        BigDecimal total = (BigDecimal) p.get("valor_total_pagado");
        List<String> impedimentos = new java.util.ArrayList<>();
        if (!"A".equals(p.get("estado_prorroga"))) {
            impedimentos.add("La prórroga no está aplicada.");
        }
        if (!"A".equals(p.get("codigo_estado_cartera"))) {
            impedimentos.add("El crédito no está activo.");
        }
        if (!"CC".equals(p.get("tipo_comprobante")) || ccOriginal == null
                || ccOriginal.isBlank() || total == null || total.signum() <= 0) {
            impedimentos.add("Comprobante o valor original inválido.");
        }
        if (!java.util.Objects.equals(p.get("proxima_fecha_capital"), p.get("proxima_fecha_capital_nueva"))
                || !java.util.Objects.equals(p.get("proxima_fecha_interes"), p.get("proxima_fecha_interes_nueva"))
                || !java.util.Objects.equals(p.get("fecha_final"), p.get("fecha_vencimiento_nueva"))) {
            impedimentos.add("Las fechas actuales del crédito no coinciden con la prórroga.");
        }
        Boolean posterior = jdbcTemplate.queryForObject("""
                SELECT EXISTS (SELECT 1 FROM cartera.creditos_prorrogas
                    WHERE id_cartera_credito = ? AND numero_prorroga > ?
                      AND estado_prorroga = 'A')
                """, Boolean.class, idCredito, p.get("numero_prorroga"));
        if (Boolean.TRUE.equals(posterior)) {
            impedimentos.add("Existe una prórroga aplicada posteriormente.");
        }
        List<Map<String, Object>> pagos = jdbcTemplate.queryForList("""
                SELECT pc.id_pago, pc.id_extracto_cartera, pc.valor_pago,
                       pc.medios_recaudo::text AS medios_recaudo, pc.estado
                FROM cartera.pagos_cuotas pc
                JOIN cartera.extractos_cartera e
                  ON e.id_extracto_cartera = pc.id_extracto_cartera
                 AND e.id_cartera_credito = pc.id_cartera_credito
                WHERE pc.id_cartera_credito = ?
                  AND pc.id_agencia_receptora = ?
                  AND pc.tipo_operacion = 'PRORROGA'
                  AND pc.liquidacion ->> 'idCreditoProrroga' = ?
                  AND pc.tipo_comprobante = 'CC'
                  AND TRIM(pc.numero_comprobante) = TRIM(?)
                  AND e.tipo_comprobante = 'CC'
                  AND TRIM(e.numero_comprobante) = TRIM(?)
                """, idCredito, idAgencia, String.valueOf(idProrroga), ccOriginal, ccOriginal);
        BigDecimal capital = BigDecimal.ZERO;
        BigDecimal ahorros = BigDecimal.ZERO;
        BigDecimal caja = BigDecimal.ZERO;
        BigDecimal banco = BigDecimal.ZERO;
        Long idPago = null;
        if (pagos.size() != 1) {
            impedimentos.add("No se encontró un único pago vinculado a la prórroga.");
        } else {
            Map<String, Object> pago = pagos.get(0);
            idPago = ((Number) pago.get("id_pago")).longValue();
            if (total != null && (pago.get("valor_pago") == null
                    || total.compareTo((BigDecimal) pago.get("valor_pago")) != 0)) {
                impedimentos.add("El valor del pago no coincide con la prórroga.");
            }
            if (!"APLICADO".equals(pago.get("estado"))) {
                impedimentos.add("El pago original no está aplicado.");
            }
            Number idExtracto = (Number) pago.get("id_extracto_cartera");
            if (idExtracto == null) {
                impedimentos.add("El pago no tiene extracto de Cartera.");
            } else {
                List<BigDecimal> capitales = jdbcTemplate.query("""
                        SELECT valor_capital FROM cartera.extractos_cartera
                        WHERE id_extracto_cartera = ? AND id_cartera_credito = ?
                          AND estado = 'A'
                        """, (rs, i) -> rs.getBigDecimal(1), idExtracto.longValue(), idCredito);
                if (capitales.size() != 1) {
                    impedimentos.add("No se encontró un extracto activo único.");
                } else {
                    capital = capitales.get(0) == null ? BigDecimal.ZERO : capitales.get(0);
                }
                Boolean posteriores = jdbcTemplate.queryForObject("""
                        SELECT EXISTS (SELECT 1 FROM cartera.extractos_cartera
                            WHERE id_cartera_credito = ? AND estado = 'A'
                              AND id_extracto_cartera > ?)
                        """, Boolean.class, idCredito, idExtracto.longValue());
                if (Boolean.TRUE.equals(posteriores)) {
                    impedimentos.add("Existen extractos activos posteriores.");
                }
            }
            Boolean pagosPosteriores = jdbcTemplate.queryForObject("""
                    SELECT EXISTS (SELECT 1 FROM cartera.pagos_cuotas
                        WHERE id_cartera_credito = ? AND estado = 'APLICADO'
                          AND id_pago > ?)
                    """, Boolean.class, idCredito, idPago);
            if (Boolean.TRUE.equals(pagosPosteriores)) {
                impedimentos.add("Existen pagos aplicados posteriores.");
            }
            try {
                JsonNode medios = objectMapper.readTree(String.valueOf(pago.get("medios_recaudo")));
                if (medios == null || !medios.isArray() || medios.isEmpty()) {
                    impedimentos.add("Los medios de recaudo no son válidos.");
                } else {
                    for (JsonNode medio : medios) {
                        if (!medio.path("valor").isNumber() || medio.path("valor").decimalValue().signum() <= 0) {
                            impedimentos.add("Existe un medio de recaudo con valor inválido.");
                            continue;
                        }
                        BigDecimal valor = medio.path("valor").decimalValue();
                        switch (medio.path("tipoMedio").asText()) {
                            case "AHORROS" -> ahorros = ahorros.add(valor);
                            case "CAJA" -> caja = caja.add(valor);
                            case "CXP" -> banco = banco.add(valor);
                            default -> impedimentos.add("Existe un medio de recaudo no reconocido.");
                        }
                    }
                    if (total != null && total.compareTo(ahorros.add(caja).add(banco)) != 0) {
                        impedimentos.add("Los medios de recaudo no suman el total de la prórroga.");
                    }
                }
            } catch (com.fasterxml.jackson.core.JsonProcessingException ex) {
                impedimentos.add("El JSON de medios de recaudo no es válido.");
            }
        }
        return new VistaPrevia(idProrroga, idPago, ccOriginal, total,
                capital, ahorros, caja, banco, impedimentos.isEmpty(), List.copyOf(impedimentos));
    }

    /** Importes consultados; no representa autorización definitiva de reversión. */
    public record VistaPrevia(Long idProrroga, Long idPago, String comprobanteOriginal,
            BigDecimal valorTotal, BigDecimal capitalARestituir, BigDecimal ahorrosADevolver,
            BigDecimal cajaPorRegularizar, BigDecimal tesoreriaPorRegularizar,
            boolean elegiblePreliminar, List<String> impedimentos) {}

    /** Valida los medios persistidos sin recalcular ni modificar el pago original. */
    private JsonNode leerYValidarMedios(ReversionPagosRepository.PagoDetalle pago) {
        try {
            JsonNode medios = objectMapper.readTree(pago.mediosRecaudoJson());
            if (medios == null || !medios.isArray() || medios.isEmpty()) {
                throw new IllegalStateException("La prórroga no contiene medios de recaudo válidos.");
            }
            for (JsonNode medio : medios) {
                if (!medio.isObject() || !medio.path("tipoMedio").isTextual()
                        || !medio.path("valor").isNumber()
                        || medio.path("valor").decimalValue().signum() <= 0) {
                    throw new IllegalStateException("La prórroga contiene un medio de recaudo inválido.");
                }
            }
            return medios;
        } catch (com.fasterxml.jackson.core.JsonProcessingException ex) {
            throw new IllegalStateException("El JSON de medios de recaudo es inválido.", ex);
        }
    }

    /**
     * Anula el recaudo de efectivo pendiente o publica su devolución para Caja.
     * El movimiento original se bloquea y conserva si ya fue registrado.
     */
    private void registrarReversionCaja(Long idProrroga, String ccOriginal,
            BigDecimal efectivo, Integer idUsuario) {
        if (efectivo.signum() == 0) {
            return;
        }
        List<Map<String, Object>> movimientos = jdbcTemplate.queryForList("""
                SELECT m.id_movimiento_caja, m.id_provision, m.id_caja,
                       m.id_departamento_operativo, m.estado, m.valor,
                       m.codigo_referencia
                  FROM cajas.movimientos_caja m
                 WHERE m.modulo_origen = 'CARTERA'
                   AND m.proceso_origen = 'PRORROGA'
                   AND m.tabla_origen = 'cartera.creditos_prorrogas'
                   AND m.id_origen = ?
                   AND m.naturaleza = 'INGRESO'
                   AND m.medio_pago = 'EFECTIVO'
                   AND m.tipo_comprobante = 'CC'
                   AND TRIM(m.numero_comprobante) = TRIM(?)
                 FOR UPDATE OF m
                """, idProrroga, ccOriginal);
        if (movimientos.size() != 1) {
            throw new IllegalStateException(
                    "La prórroga con efectivo no tiene un único ingreso de Caja asociado.");
        }
        Map<String, Object> movimiento = movimientos.get(0);
        Long idMovimiento = ((Number) movimiento.get("id_movimiento_caja")).longValue();
        BigDecimal valor = (BigDecimal) movimiento.get("valor");
        if (valor == null || valor.compareTo(efectivo) != 0) {
            throw new IllegalStateException(
                    "El valor de Caja no coincide con el efectivo de la prórroga.");
        }
        String estado = String.valueOf(movimiento.get("estado"));
        if ("PENDIENTE".equals(estado)) {
            int actualizados = jdbcTemplate.update("""
                    UPDATE cajas.movimientos_caja
                       SET estado = 'ANULADO',
                           fk_seguridad_edicion = ?,
                           fecha_edicion = CURRENT_TIMESTAMP
                     WHERE id_movimiento_caja = ?
                       AND estado = 'PENDIENTE'
                    """, idUsuario, idMovimiento);
            if (actualizados != 1) {
                throw new IllegalStateException(
                        "No fue posible anular el ingreso pendiente de la prórroga.");
            }
            return;
        }
        if (!"REGISTRADO".equals(estado)) {
            throw new IllegalStateException(
                    "Estado del recaudo de Caja incompatible: " + estado);
        }

        Integer duplicados = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                  FROM cajas.movimientos_caja
                 WHERE modulo_origen = 'CARTERA'
                   AND proceso_origen = 'REVERSA_PRORROGA'
                   AND tabla_origen = 'cartera.creditos_prorrogas'
                   AND id_origen = ?
                   AND naturaleza = 'EGRESO'
                """, Integer.class, idProrroga);
        if (duplicados == null || duplicados != 0) {
            throw new IllegalStateException(
                    "Ya existe una devolución de Caja para esta prórroga.");
        }

        int creados = jdbcTemplate.update("""
                INSERT INTO cajas.movimientos_caja (
                    id_provision, id_caja, fecha_contable,
                    id_departamento_operativo, id_operacion_caja,
                    modulo_origen, proceso_origen, tabla_origen, id_origen,
                    codigo_referencia, naturaleza, medio_pago, valor, concepto,
                    estado, fk_seguridad_creacion, fk_seguridad_edicion
                )
                SELECT ?, ?, CURRENT_DATE, ?, o.id_operacion_caja,
                       'CARTERA', 'REVERSA_PRORROGA',
                       'cartera.creditos_prorrogas', ?, ?,
                       'EGRESO', 'EFECTIVO', ?,
                       'Devolución de efectivo por reversión de prórroga. Movimiento original ' || ?,
                       'PENDIENTE', ?, ?
                  FROM cajas.operaciones_caja o
                 WHERE o.codigo_operacion = 'REVERSA_PRORROGA'
                   AND o.naturaleza = 'EGRESO'
                   AND o.afecta_efectivo = TRUE
                   AND o.estado = 'A'
                """,
                movimiento.get("id_provision"),
                movimiento.get("id_caja"),
                movimiento.get("id_departamento_operativo"),
                idProrroga,
                movimiento.get("codigo_referencia"),
                efectivo,
                idMovimiento,
                idUsuario,
                idUsuario);
        if (creados != 1) {
            throw new IllegalStateException(
                    "No existe una única operación activa REVERSA_PRORROGA para Caja.");
        }
    }

    /** Anula recaudos bancarios no ejecutados o solicita regularización de los ya recibidos. */
    private void registrarReversionTesoreria(Long idProrroga, Integer idAgencia,
            BigDecimal valorBancario, String motivo, Integer idUsuario) {
        if (valorBancario.signum() == 0) {
            return;
        }
        List<Map<String, Object>> operaciones = jdbcTemplate.queryForList("""
                SELECT id_operacion_pendiente, id_datos_personal, valor_original, valor_ejecutado, estado
                FROM tesoreria.operaciones_pendientes
                WHERE modulo_origen = 'CARTERA'
                  AND proceso_origen = 'PRORROGA'
                  AND tabla_origen = 'cartera.creditos_prorrogas'
                  AND id_origen = ? AND id_agencia = ?
                FOR UPDATE
                """, idProrroga, idAgencia);
        if (operaciones.size() != 1) {
            throw new IllegalStateException("No existe una única operación bancaria de la prórroga.");
        }
        Map<String, Object> operacion = operaciones.get(0);
        Long idOperacion = ((Number) operacion.get("id_operacion_pendiente")).longValue();
        BigDecimal original = (BigDecimal) operacion.get("valor_original");
        BigDecimal ejecutado = (BigDecimal) operacion.get("valor_ejecutado");
        String estado = String.valueOf(operacion.get("estado"));
        if (original == null || ejecutado == null || original.compareTo(valorBancario) != 0
                || ejecutado.signum() < 0 || ejecutado.compareTo(original) > 0
                || "ANULADA".equals(estado)) {
            throw new IllegalStateException("La operación de Tesorería no es consistente con el recaudo.");
        }
        if (ejecutado.signum() == 0) {
            if (!"PENDIENTE".equals(estado)) {
                throw new IllegalStateException("La operación bancaria sin recaudo no está pendiente.");
            }
            int filas = jdbcTemplate.update("""
                    UPDATE tesoreria.operaciones_pendientes
                    SET estado = 'ANULADA', fk_seguridad_edicion = ?,
                        fecha_edicion = CURRENT_TIMESTAMP
                    WHERE id_operacion_pendiente = ? AND estado = 'PENDIENTE'
                    """, idUsuario, idOperacion);
            if (filas != 1) {
                throw new IllegalStateException("No se pudo anular la operación bancaria pendiente.");
            }
        } else if (!"PARCIAL".equals(estado) && !"COMPLETADA".equals(estado)) {
            throw new IllegalStateException("Estado de recaudo bancario incompatible: " + estado);
        }
        int insertados = jdbcTemplate.update("""
                INSERT INTO tesoreria.operaciones_reversiones_origen (
                    id_operacion_pendiente, modulo_origen, proceso_origen,
                    tabla_origen, id_origen, motivo, estado_regularizacion,
                    fk_seguridad_creacion)
                VALUES (?, 'CARTERA', 'PRORROGA', 'cartera.creditos_prorrogas',
                        ?, ?, ?, ?)
                """, idOperacion, idProrroga, motivo.trim(),
                ejecutado.signum() == 0 ? "NO_REQUIERE" : "PENDIENTE", idUsuario);
        if (insertados != 1) {
            throw new IllegalStateException("No se registró la regularización de Tesorería.");
        }
        if (ejecutado.signum() > 0) {
            publicarDevolucionBancaria(idProrroga, idAgencia, idOperacion,
                    operacion, ejecutado, idUsuario);
        }
    }

    /**
     * Publica la devolución de lo efectivamente recaudado, sin alterar el CI original.
     * Admite varios bancos de recaudo; la devolución se publica como una sola obligación.
     * Tesorería debe validar y ejecutar cada importe contra su banco original.
     */
    private void publicarDevolucionBancaria(Long idProrroga, Integer idAgencia,
            Long idOperacionOriginal, Map<String, Object> operacionOriginal,
            BigDecimal valorRecaudado, Integer idUsuario) {
        List<Map<String, Object>> ejecuciones = jdbcTemplate.queryForList("""
                SELECT id_operacion_ejecucion, id_cuenta_bancaria, valor
                FROM tesoreria.operaciones_ejecuciones
                WHERE id_operacion_pendiente = ? AND estado = 'CONFIRMADA'
                ORDER BY id_operacion_ejecucion
                FOR UPDATE
                """, idOperacionOriginal);
        if (ejecuciones.isEmpty()) {
            throw new IllegalStateException(
                    "El recaudo de la prórroga no tiene ejecuciones bancarias confirmadas.");
        }
        BigDecimal totalConfirmado = BigDecimal.ZERO;
        Map<Long, BigDecimal> importesPorBanco = new java.util.TreeMap<>();
        for (Map<String, Object> ejecucion : ejecuciones) {
            if (!(ejecucion.get("id_cuenta_bancaria") instanceof Number numeroCuenta)) {
                throw new IllegalStateException(
                        "Una ejecución de la prórroga no identifica cuenta bancaria.");
            }
            Long cuenta = numeroCuenta.longValue();
            if (!(ejecucion.get("valor") instanceof BigDecimal valor)
                    || valor.signum() <= 0) {
                throw new IllegalStateException("Una ejecución bancaria no tiene valor positivo válido.");
            }
            importesPorBanco.merge(cuenta, valor, BigDecimal::add);
            totalConfirmado = totalConfirmado.add(valor);
        }
        if (totalConfirmado.compareTo(valorRecaudado) != 0) {
            throw new IllegalStateException(
                    "Las ejecuciones confirmadas no coinciden con el recaudo de la prórroga.");
        }

        operacionesPendientesService.publicarEstricto(
                idAgencia, "CARTERA", "REVERSA_PRORROGA",
                "cartera.creditos_prorrogas", idProrroga,
                (Integer) operacionOriginal.get("id_datos_personal"),
                "SALIDA", LocalDate.now(),
                "Devolución bancaria por reversión de prórroga. Recaudo original "
                        + idOperacionOriginal + ". Bancos e importes originales: "
                        + importesPorBanco,
                valorRecaudado, idUsuario,
                "DEVOLUCION_REVERSA_PRORROGA", "DEVOLUCION_CXC_CARTERA", true);

    }

    public record ResultadoReversion(Long idProrroga, Long idPago,
            String numeroComprobante, BigDecimal capitalRestituido,
            BigDecimal ahorrosDevueltos) {}
}
