package co.assip.erp.tesoreria.operaciones_pendientes;

import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.general.operaciones_financieras.TiposOperacionesFinancierasService;
import co.assip.erp.general.operaciones_financieras.dto.TipoOperacionFinancieraDTO;
import co.assip.erp.contabilidad.auxiliares_contables.ContabilidadRegistroService;
import co.assip.erp.contabilidad.consecutivos_comprobantes.ConsecutivosComprobantesService;
import co.assip.erp.contabilidad.origen_comprobantes.dto.OrigenComprobanteDTO;
import co.assip.erp.tesoreria.operaciones_pendientes.dto.OperacionPagoResponseDTO;
import co.assip.erp.contabilidad.auxiliares_contables.dto.MovimientoContableDTO;
import co.assip.erp.tesoreria.operaciones_pendientes.dto.OperacionPagoRequestDTO;
import co.assip.erp.tesoreria.operaciones_pendientes.dto.OperacionPagoLineaDTO;
import co.assip.erp.tesoreria.operaciones_pendientes.dto.OperacionComprobantePreviewDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TesoreriaOperacionesEjecucionesService {

    private static final String PENDIENTE = "PENDIENTE";
    private static final String PARCIAL = "PARCIAL";
    private static final String TRANSFERENCIA = "TRANSFERENCIA";

    private final TesoreriaOperacionesEjecucionesRepository repository;
    private final UsuarioSesionService usuarioSesionService;
    private final TiposOperacionesFinancierasService tiposOperacionesFinancierasService;
    private final List<TesoreriaOrigenFinancieroValidator> validadoresOrigen;
    private final JdbcTemplate jdbcTemplate;
    private final ContabilidadRegistroService contabilidadRegistroService;
    private final ConsecutivosComprobantesService consecutivosComprobantesService;

    private static final String CHEQUE = "CHEQUE";

    // =========================================================
    // CONSULTAS
    // =========================================================

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarEjecuciones(
            Long idOperacion
    ) {
        validarId(idOperacion, "La operación");

        Map<String, Object> operacion =
                obtenerOperacion(idOperacion);

        validarAgencia(operacion);

        return repository.listarEjecuciones(idOperacion);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> consultarEjecucion(
            Long idEjecucion
    ) {
        validarId(idEjecucion, "La ejecución");

        Map<String, Object> ejecucion =
                repository.buscarEjecucion(idEjecucion);

        if (ejecucion == null) {
            throw new IllegalArgumentException(
                    "La ejecución no existe."
            );
        }

        Long idOperacion = numeroLong(
                ejecucion,
                "id_operacion_pendiente"
        );

        validarAgencia(obtenerOperacion(idOperacion));

        return ejecucion;
    }

    @Transactional(readOnly = true)
    public BigDecimal consultarSaldoDisponible(
            Long idOperacion
    ) {
        validarId(idOperacion, "La operación");

        Map<String, Object> operacion =
                obtenerOperacion(idOperacion);

        validarAgencia(operacion);

        return calcularSaldoDisponible(idOperacion, operacion);
    }


    // =========================================================
// VISTA PREVIA CONTABLE - NO PERSISTE
// =========================================================

    @Transactional(readOnly = true)
    public OperacionComprobantePreviewDTO generarVistaPrevia(
            OperacionPagoRequestDTO request
    ) {
        if (request == null) {
            throw new IllegalArgumentException("La solicitud es obligatoria.");
        }

        Long idOperacion = request.idOperacion();
        validarId(idOperacion, "La operación");

        if (request.lineas() == null || request.lineas().isEmpty()) {
            throw new IllegalArgumentException("Debe agregar líneas de pago.");
        }

        Map<String, Object> operacion = obtenerOperacion(idOperacion);
        validarAgencia(operacion);
        validarEstadoPreparacion(operacion);

        if (esDevolucionCartera(operacion)) {
            return generarVistaPreviaDevolucion(request, operacion);
        }

        Integer idAgencia = numeroEntero(operacion, "id_agencia");
        Integer idBeneficiario = numeroEntero(operacion, "id_datos_personal");

        BigDecimal original = numeroDecimal(operacion, "valor_original");
        BigDecimal ejecutado = numeroDecimal(operacion, "valor_ejecutado");
        BigDecimal disponible = calcularSaldoDisponible(idOperacion, operacion);

        if (disponible.signum() <= 0) {
            throw new IllegalStateException(
                    "La obligación no tiene saldo disponible."
            );
        }

        // =========================================================
        // IDENTIFICACIÓN DEL ORIGEN FINANCIERO
        // =========================================================

        String modulo = Objects.toString(
                operacion.get("modulo_origen"), ""
        ).trim();

        String proceso = Objects.toString(
                operacion.get("proceso_origen"), ""
        ).trim();

        String tabla = Objects.toString(
                operacion.get("tabla_origen"), ""
        ).trim();

        boolean esCdat = "CDAT".equals(modulo)
                && "CANCELACION_CDAT".equals(proceso)
                && "cdat.cuentas_cdats".equals(tabla);

        boolean esRetiroAsociado = "DEPOSITOS".equals(modulo)
                && "RETIRO_ASOCIADO".equals(proceso)
                && "depositos.cuentas_ahorro".equals(tabla);

        boolean esDesembolsoCredito =
                "CARTERA".equals(modulo)
                        && "DESEMBOLSO_CREDITO".equals(proceso)
                        && "cartera.carteras_creditos".equals(tabla);

        validarPerfilTesoreria(operacion, "PAGO_CXP", false);

        if (!esCdat && !esRetiroAsociado && !esDesembolsoCredito) {
            throw new IllegalStateException(
                    "No existe contabilización de pagos para "
                            + modulo + " / " + proceso
            );
        }

        // =========================================================
        // RESOLUCIÓN DE LA CUENTA POR PAGAR
        // =========================================================

        Integer cuentaPorPagar;
        if (esRetiroAsociado) {
            // El NC consolidado puede respaldar varias obligaciones individuales.
            cuentaPorPagar = obtenerCuentaPorPagarRetiroAsociado(
                    operacion, idAgencia, idBeneficiario);
        } else if (operacion.get("id_origen_comprobante") instanceof Number
                && operacion.get("id_catalogo_cuenta_cxp") instanceof Number) {
            // Para obligaciones con referencia histórica explícita se valida el respaldo
            // sin depender del detalle descriptivo del movimiento contable.
            cuentaPorPagar = obtenerCuentaPorPagarHistorica(
                    operacion, idAgencia, idBeneficiario, original);
        } else if (esCdat) {
            cuentaPorPagar = obtenerCuentaPorPagarCancelacionCdat(
                    numeroLong(operacion, "id_origen"), idAgencia,
                    idBeneficiario, original);
        } else {
            cuentaPorPagar = obtenerCuentaPorPagarDesembolsoCredito(
                    numeroLong(operacion, "id_origen"), idAgencia,
                    idBeneficiario, original);
        }

        // =========================================================
        // CONSTRUCCIÓN DE MOVIMIENTOS CONTABLES
        // =========================================================

        BigDecimal total = BigDecimal.ZERO;
        List<MovimientoContableDTO> movimientos = new ArrayList<>();

        String conceptoPago = esRetiroAsociado
                ? "retiro integral de asociado"
                : esDesembolsoCredito
                ? "desembolso de crédito"
                : "CDAT";

        for (OperacionPagoLineaDTO linea : request.lineas()) {

            if (linea == null) {
                throw new IllegalArgumentException(
                        "Hay una línea de pago vacía."
                );
            }

            validarValor(linea.valor());
            validarBeneficiario(operacion, linea.idBeneficiario());

            String medio = normalizarMedioPago(linea.medioPago());

            if (!TRANSFERENCIA.equals(medio) && !CHEQUE.equals(medio)) {
                throw new IllegalArgumentException(
                        "Medio de pago no permitido: " + medio
                );
            }

            String referencia = limpiar(linea.numeroDocumento());

            if (CHEQUE.equals(medio) && referencia == null) {
                throw new IllegalArgumentException(
                        "El número de cheque es obligatorio."
                );
            }

            validarId(linea.idCuentaBancaria(), "La cuenta bancaria");

            // =====================================================
            // VALIDACIÓN DE LA CUENTA BANCARIA
            // =====================================================

            List<Integer> cuentas = jdbcTemplate.queryForList("""
                SELECT cba.id_cuenta_contable
                FROM tesoreria.cuentas_bancarias_agencias cba
                JOIN tesoreria.cuentas_bancarias cb
                  ON cb.id_cuenta_bancaria = cba.id_cuenta_bancaria
                JOIN tesoreria.bancos b
                  ON b.codigo_banco = cb.codigo_banco
                JOIN contabilidad.catalogo_cuentas cc
                  ON cc.id_catalogo_cuenta = cba.id_cuenta_contable
                WHERE cba.id_agencia = ?
                  AND cba.id_cuenta_bancaria = ?
                  AND cba.activo = TRUE
                  AND cba.permite_pagar = TRUE
                  AND cb.activo = TRUE
                  AND cb.estado_cuenta = 'ACTIVA'
                  AND b.activo = TRUE
                  AND cc.id_agencia = cba.id_agencia
                  AND cc.cuenta_operable = TRUE
                """,
                    Integer.class,
                    idAgencia,
                    linea.idCuentaBancaria()
            );

            if (cuentas.size() != 1 || cuentas.get(0) == null) {
                throw new IllegalArgumentException(
                        "La cuenta bancaria no está habilitada "
                                + "para pagos en esta agencia."
                );
            }

            // =====================================================
            // CRÉDITO A LA CUENTA BANCARIA
            // =====================================================

            MovimientoContableDTO credito = movimiento(
                    idAgencia,
                    idBeneficiario,
                    cuentas.get(0),
                    BigDecimal.ZERO,
                    linea.valor(),
                    detallePago(conceptoPago, medio, referencia),
                    referencia,
                    idOperacion
            );

            movimientos.add(credito);
            total = total.add(linea.valor());
        }

        // =========================================================
        // VALIDACIÓN DEL TOTAL A PAGAR
        // =========================================================

        if (total.compareTo(disponible) > 0) {
            throw new IllegalArgumentException(
                    "El pago supera el saldo disponible: "
                            + disponible.toPlainString()
            );
        }

        validarPerfilTesoreria(operacion, "PAGO_CXP", total.compareTo(disponible) < 0);

        // =========================================================
        // DÉBITO A LA CUENTA POR PAGAR
        // =========================================================

        String detalleObligacion = esRetiroAsociado
                ? "Pago de cuenta por pagar - Retiro integral de asociado"
                : esDesembolsoCredito
                ? "Pago de cuenta por pagar - Desembolso de crédito"
                : "Cancelación de obligación CDAT - Pago al beneficiario";

        movimientos.add(0, movimiento(
                idAgencia,
                idBeneficiario,
                cuentaPorPagar,
                total,
                BigDecimal.ZERO,
                detalleObligacion,
                null,
                idOperacion
        ));

        // =========================================================
        // RESULTADO DE LA VISTA PREVIA
        // =========================================================

        return new OperacionComprobantePreviewDTO(
                idOperacion,
                "CE",
                idAgencia,
                idBeneficiario,
                original,
                ejecutado,
                disponible,
                total,
                disponible.subtract(total),
                total,
                total,
                true,
                List.copyOf(movimientos),
                consultarCuentasContables(idAgencia, movimientos)
        );
    }

    /** Identifica devoluciones bancarias de Pago de Cuotas y Prórrogas. */
    private boolean esDevolucionCartera(Map<String, Object> operacion) {
        if (!"CARTERA".equals(Objects.toString(operacion.get("modulo_origen"), ""))) {
            return false;
        }
        String proceso = Objects.toString(operacion.get("proceso_origen"), "");
        String tabla = Objects.toString(operacion.get("tabla_origen"), "");
        return ("REVERSA_PAGO_CUOTAS".equals(proceso)
                    && "cartera.pagos_cuotas".equals(tabla))
                || ("REVERSA_PRORROGA".equals(proceso)
                    && "cartera.creditos_prorrogas".equals(tabla));
    }

    /** Vincula cada devolución con el proceso y la tabla de su recaudo original. */
    private String procesoRecaudoOriginal(Map<String, Object> devolucion) {
        String proceso = Objects.toString(devolucion.get("proceso_origen"), "");
        if ("REVERSA_PAGO_CUOTAS".equals(proceso)) {
            return "PAGO_CUOTAS";
        }
        if ("REVERSA_PRORROGA".equals(proceso)) {
            return "PRORROGA";
        }
        throw new IllegalArgumentException("La operación no es una devolución de Cartera válida.");
    }

    private String tablaRecaudoOriginal(Map<String, Object> devolucion) {
        return "PRORROGA".equals(procesoRecaudoOriginal(devolucion))
                ? "cartera.creditos_prorrogas" : "cartera.pagos_cuotas";
    }

    /**
     * Prepara una devolución completa y distribuida por los bancos del recaudo original.
     * Cada banco debe devolverse exactamente por lo efectivamente recibido en él.
     * Las distintas líneas se contabilizan en un único CE, sin pagos parciales.
     */
    private OperacionComprobantePreviewDTO generarVistaPreviaDevolucion(
            OperacionPagoRequestDTO request,
            Map<String, Object> devolucion
    ) {
        validarPerfilTesoreria(devolucion, "DEVOLUCION_CXC_CARTERA", false);
        Long idDevolucion = numeroLong(devolucion, "id_operacion_pendiente");
        Long idOrigen = numeroLong(devolucion, "id_origen");
        Integer agencia = numeroEntero(devolucion, "id_agencia");
        Integer tercero = numeroEntero(devolucion, "id_datos_personal");
        BigDecimal original = numeroDecimal(devolucion, "valor_original");
        BigDecimal ejecutado = numeroDecimal(devolucion, "valor_ejecutado");
        BigDecimal disponible = calcularSaldoDisponible(idDevolucion, devolucion);
        if (original.signum() <= 0 || ejecutado.signum() != 0
                || disponible.compareTo(original) != 0) {
            throw new IllegalStateException(
                    "La devolución debe ejecutarse una sola vez y por su valor total.");
        }

        List<Map<String, Object>> originales = jdbcTemplate.queryForList("""
                SELECT o.id_operacion_pendiente, o.valor_ejecutado,
                       r.estado_regularizacion
                FROM tesoreria.operaciones_pendientes o
                JOIN tesoreria.operaciones_reversiones_origen r
                  ON r.id_operacion_pendiente = o.id_operacion_pendiente
                WHERE o.id_agencia = ?
                  AND o.modulo_origen = 'CARTERA'
                  AND o.proceso_origen = ?
                  AND o.tabla_origen = ?
                  AND o.id_origen = ?
                  AND o.estado IN ('COMPLETADA', 'PARCIAL')
                  AND r.estado_regularizacion = 'PENDIENTE'
                """, agencia, procesoRecaudoOriginal(devolucion),
                tablaRecaudoOriginal(devolucion), idOrigen);
        if (originales.size() != 1) {
            throw new IllegalStateException(
                    "No existe una única regularización bancaria pendiente para esta operación de Cartera.");
        }
        Long idOriginal = numeroLong(originales.get(0), "id_operacion_pendiente");
        if (numeroDecimal(originales.get(0), "valor_ejecutado").compareTo(original) != 0) {
            throw new IllegalStateException(
                    "El importe a devolver no coincide con el recaudo bancario.");
        }

        List<Map<String, Object>> ejecuciones = jdbcTemplate.queryForList("""
                SELECT id_cuenta_bancaria, valor, tipo_comprobante,
                       numero_comprobante, fecha_ejecucion
                FROM tesoreria.operaciones_ejecuciones
                WHERE id_operacion_pendiente = ? AND estado = 'CONFIRMADA'
                ORDER BY id_operacion_ejecucion
                """, idOriginal);
        if (ejecuciones.isEmpty()) {
            throw new IllegalStateException(
                    "No hay ejecuciones bancarias originales confirmadas.");
        }

        Map<Long, BigDecimal> importesPorBanco = new LinkedHashMap<>();
        Map<Long, Integer> cuentasContablesPorBanco = new LinkedHashMap<>();
        Integer cuentaCxC = null;
        BigDecimal totalRecaudado = BigDecimal.ZERO;

        for (Map<String, Object> ejecucion : ejecuciones) {
            Long idBanco = numeroLong(ejecucion, "id_cuenta_bancaria");
            BigDecimal valor = numeroDecimal(ejecucion, "valor");
            if (valor.signum() <= 0
                    || !"CI".equals(Objects.toString(ejecucion.get("tipo_comprobante"), "").trim())
                    || ejecucion.get("numero_comprobante") == null
                    || ejecucion.get("fecha_ejecucion") == null) {
                throw new IllegalStateException(
                        "Una ejecución bancaria original carece de valor, fecha o comprobante CI válido.");
            }

            // Se cruza también la fecha: los números de comprobante pueden repetirse
            // entre ejercicios históricos migrados.
            List<Map<String, Object>> movimientosCI = jdbcTemplate.queryForList("""
                    SELECT ac.id_catalogo_cuenta, ac.valor_debito, ac.valor_credito,
                           ac.id_datos_personal,
                           cba.id_cuenta_bancaria AS banco_asociado
                    FROM contabilidad.auxiliares_contables ac
                    LEFT JOIN tesoreria.cuentas_bancarias_agencias cba
                      ON cba.id_agencia = ac.id_agencia
                     AND cba.id_cuenta_contable = ac.id_catalogo_cuenta
                     AND cba.id_cuenta_bancaria = ?
                    WHERE ac.id_agencia = ?
                      AND TRIM(ac.tipo_comprobante) = 'CI'
                      AND TRIM(ac.numero_comprobante) = TRIM(CAST(? AS VARCHAR))
                      AND ac.fecha_auxiliar = CAST(? AS DATE)
                      AND ac.estado_movimiento = 'A'
                    """, idBanco, agencia, ejecucion.get("numero_comprobante"),
                    ejecucion.get("fecha_ejecucion"));

            Integer bancoContable = null;
            Integer cxcCI = null;
            BigDecimal debitoBanco = BigDecimal.ZERO;
            BigDecimal creditoCxC = BigDecimal.ZERO;
            for (Map<String, Object> movimiento : movimientosCI) {
                BigDecimal debito = numeroDecimal(movimiento, "valor_debito");
                BigDecimal credito = numeroDecimal(movimiento, "valor_credito");
                if (movimiento.get("banco_asociado") != null && debito.signum() > 0
                        && credito.signum() == 0) {
                    Integer cuenta = numeroEntero(movimiento, "id_catalogo_cuenta");
                    if (bancoContable != null && !bancoContable.equals(cuenta)) {
                        throw new IllegalStateException("El CI tiene varias cuentas bancarias de débito.");
                    }
                    bancoContable = cuenta;
                    debitoBanco = debitoBanco.add(debito);
                } else if (Objects.equals(movimiento.get("id_datos_personal"), tercero)
                        && credito.signum() > 0 && debito.signum() == 0) {
                    Integer cuenta = numeroEntero(movimiento, "id_catalogo_cuenta");
                    if (cxcCI != null && !cxcCI.equals(cuenta)) {
                        throw new IllegalStateException("El CI tiene varias cuentas por cobrar.");
                    }
                    cxcCI = cuenta;
                    creditoCxC = creditoCxC.add(credito);
                }
            }
            if (bancoContable == null || cxcCI == null
                    || debitoBanco.compareTo(valor) != 0
                    || creditoCxC.compareTo(valor) != 0) {
                throw new IllegalStateException(
                        "El CI original no respalda exactamente el banco y la CxC del recaudo.");
            }
            if (cuentaCxC != null && !cuentaCxC.equals(cxcCI)) {
                throw new IllegalStateException("Los CI originales utilizan distintas cuentas por cobrar.");
            }
            cuentaCxC = cxcCI;
            Integer anterior = cuentasContablesPorBanco.putIfAbsent(idBanco, bancoContable);
            if (anterior != null && !anterior.equals(bancoContable)) {
                throw new IllegalStateException("El mismo banco utiliza cuentas contables distintas en los CI.");
            }
            importesPorBanco.merge(idBanco, valor, BigDecimal::add);
            totalRecaudado = totalRecaudado.add(valor);
        }
        if (totalRecaudado.compareTo(original) != 0) {
            throw new IllegalStateException("Los CI originales no respaldan el importe a devolver.");
        }
        if (request.lineas().size() != importesPorBanco.size()) {
            throw new IllegalArgumentException(
                    "Debe indicar una línea de devolución por cada cuenta bancaria original.");
        }

        Map<Long, OperacionPagoLineaDTO> lineasPorBanco = new LinkedHashMap<>();
        BigDecimal totalSolicitado = BigDecimal.ZERO;
        for (OperacionPagoLineaDTO linea : request.lineas()) {
            if (linea == null || linea.idCuentaBancaria() == null) {
                throw new IllegalArgumentException("Cada devolución debe identificar su banco original.");
            }
            validarValor(linea.valor());
            validarBeneficiario(devolucion, linea.idBeneficiario());
            String medio = normalizarMedioPago(linea.medioPago());
            if (!TRANSFERENCIA.equals(medio) && !CHEQUE.equals(medio)) {
                throw new IllegalArgumentException("Medio de devolución no permitido.");
            }
            if (limpiar(linea.numeroDocumento()) == null) {
                throw new IllegalArgumentException("Cada devolución requiere referencia bancaria o número de cheque.");
            }
            Long banco = linea.idCuentaBancaria().longValue();
            if (lineasPorBanco.putIfAbsent(banco, linea) != null) {
                throw new IllegalArgumentException("No puede repetir una cuenta bancaria en la devolución.");
            }
            BigDecimal esperado = importesPorBanco.get(banco);
            if (esperado == null || esperado.compareTo(linea.valor()) != 0) {
                throw new IllegalArgumentException(
                        "Cada banco debe devolver exactamente el importe recibido en él.");
            }
            totalSolicitado = totalSolicitado.add(linea.valor());
        }
        if (totalSolicitado.compareTo(original) != 0) {
            throw new IllegalArgumentException("La devolución debe cubrir la totalidad del recaudo original.");
        }

        List<MovimientoContableDTO> movimientos = new ArrayList<>();
        movimientos.add(movimiento(agencia, tercero, cuentaCxC, original,
                BigDecimal.ZERO, "Devolución recaudo Cartera - operación " + idOriginal,
                "OPERACION-" + idOriginal, idDevolucion));
        for (Map.Entry<Long, BigDecimal> banco : importesPorBanco.entrySet()) {
            OperacionPagoLineaDTO linea = lineasPorBanco.get(banco.getKey());
            movimientos.add(movimiento(agencia, tercero,
                    cuentasContablesPorBanco.get(banco.getKey()), BigDecimal.ZERO,
                    banco.getValue(), "Egreso bancario devolución Cartera - operación "
                            + idOriginal + " - banco " + banco.getKey(),
                    limpiar(linea.numeroDocumento()), idDevolucion));
        }
        return new OperacionComprobantePreviewDTO(idDevolucion, "CE", agencia, tercero,
                original, ejecutado, disponible, original, BigDecimal.ZERO,
                original, original, true, List.copyOf(movimientos),
                consultarCuentasContables(agencia, movimientos));
    }

    private String detallePago(
            String concepto,
            String medio,
            String referencia
    ) {
        String detalle = "Pago de " + concepto
                + (CHEQUE.equals(medio)
                ? " mediante cheque"
                : " mediante transferencia bancaria");
        return referencia == null ? detalle : detalle + " N.º " + referencia;
    }

    /** Valida una CxP explícita contra el comprobante original y el importe publicado. */
    private Integer obtenerCuentaPorPagarHistorica(
            Map<String, Object> operacion,
            Integer idAgencia,
            Integer idBeneficiario,
            BigDecimal valorOriginal
    ) {
        Integer idOrigenComprobante = numeroEntero(operacion, "id_origen_comprobante");
        Integer idCuentaCxp = numeroEntero(operacion, "id_catalogo_cuenta_cxp");
        List<BigDecimal> respaldos = jdbcTemplate.queryForList("""
                SELECT SUM(ac.valor_credito)
                FROM contabilidad.origen_comprobantes oc
                JOIN contabilidad.auxiliares_contables ac
                  ON ac.id_agencia = oc.id_agencia
                 AND TRIM(ac.tipo_comprobante) = TRIM(oc.tipo_comprobante)
                 AND TRIM(ac.numero_comprobante) = TRIM(oc.numero_comprobante)
                JOIN contabilidad.catalogo_cuentas cc
                  ON cc.id_catalogo_cuenta = ac.id_catalogo_cuenta
                 AND cc.id_agencia = oc.id_agencia
                WHERE oc.id_origen_comprobante = ?
                  AND oc.id_agencia = ?
                  AND oc.modulo_origen = ?
                  AND oc.proceso_origen = ?
                  AND oc.tabla_origen = ?
                  AND oc.id_origen = ?
                  AND oc.origen_tipo = 'AUTO'
                  AND oc.estado_origen = 'ACTIVO'
                  AND ac.id_catalogo_cuenta = ?
                  AND ac.id_datos_personal = ?
                  AND ac.estado_movimiento = 'A'
                  AND ac.valor_debito = 0
                  AND ac.valor_credito > 0
                  AND cc.naturaleza_cuenta = 'C'
                  AND cc.cuenta_operable = TRUE
                """, BigDecimal.class,
                idOrigenComprobante, idAgencia,
                Objects.toString(operacion.get("modulo_origen"), "").trim(),
                Objects.toString(operacion.get("proceso_origen"), "").trim(),
                Objects.toString(operacion.get("tabla_origen"), "").trim(),
                numeroLong(operacion, "id_origen"), idCuentaCxp, idBeneficiario);
        BigDecimal respaldo = respaldos.isEmpty() ? null : respaldos.get(0);
        if (respaldo == null || respaldo.compareTo(valorOriginal) != 0) {
            throw new IllegalStateException(
                    "La cuenta por pagar histórica no respalda exactamente el valor "
                            + "publicado en la operación de Tesorería.");
        }
        return idCuentaCxp;
    }

    /** Recupera la CxP efectivamente acreditada en la cancelación CDAT original. */
    private Integer obtenerCuentaPorPagarCancelacionCdat(
            Long idCuentaCdat,
            Integer idAgencia,
            Integer idBeneficiario,
            BigDecimal valorOriginal
    ) {
        List<Integer> cuentas = jdbcTemplate.queryForList("""
                SELECT ac.id_catalogo_cuenta
                FROM contabilidad.origen_comprobantes oc
                JOIN contabilidad.auxiliares_contables ac
                  ON ac.id_agencia = oc.id_agencia
                 AND ac.tipo_comprobante = oc.tipo_comprobante
                 AND ac.numero_comprobante = oc.numero_comprobante
                JOIN contabilidad.catalogo_cuentas cc
                  ON cc.id_catalogo_cuenta = ac.id_catalogo_cuenta
                 AND cc.id_agencia = ac.id_agencia
                WHERE oc.id_agencia = ?
                  AND oc.origen_tipo = 'AUTO'
                  AND oc.modulo_origen = 'CDAT'
                  AND oc.proceso_origen = 'CANCELACION_CDAT'
                  AND oc.tabla_origen = 'cdat.cuentas_cdats'
                  AND oc.id_origen = ?
                  AND ac.id_datos_personal = ?
                  AND ac.detalle_movimiento = 'Cancelación CDAT - cuenta por pagar'
                  AND ac.valor_credito = ?
                  AND ac.valor_debito = 0
                  AND ac.estado_movimiento = 'A'
                  AND cc.naturaleza_cuenta = 'C'
                  AND cc.cuenta_operable = TRUE
                """, Integer.class,
                idAgencia, idCuentaCdat, idBeneficiario, valorOriginal);
        if (cuentas.size() != 1 || cuentas.get(0) == null) {
            throw new IllegalStateException(
                    "No se encontró una única cuenta por pagar contabilizada "
                            + "en la cancelación original del CDAT " + idCuentaCdat + "."
            );
        }
        return cuentas.get(0);
    }

    /**
     * Valida la CxP histórica contra el NC consolidado, sin depender del texto descriptivo.
     * La cuenta puede respaldar varias obligaciones individuales del mismo retiro.
     */
    private Integer obtenerCuentaPorPagarRetiroAsociado(
            Map<String, Object> operacion,
            Integer idAgencia,
            Integer idBeneficiario
    ) {
        Long idCuentaAhorro = numeroLong(operacion, "id_origen");
        Object origen = operacion.get("id_origen_comprobante");
        Object cuenta = operacion.get("id_catalogo_cuenta_cxp");
        if (!(origen instanceof Number) || !(cuenta instanceof Number)) {
            throw new IllegalStateException(
                    "El retiro integral de la cuenta " + idCuentaAhorro
                            + " no tiene referencias contables históricas completas. "
                            + "Se requiere regularización antes del pago."
            );
        }
        Integer idOrigenComprobante = ((Number) origen).intValue();
        Integer idCuentaCxp = ((Number) cuenta).intValue();
        List<BigDecimal> respaldos = jdbcTemplate.queryForList("""
                SELECT SUM(ac.valor_credito) AS total_credito
                FROM depositos.cuentas_ahorro ca
                JOIN contabilidad.origen_comprobantes oc
                  ON oc.id_origen_comprobante = ?
                 AND oc.id_agencia = ca.id_agencia
                 AND oc.modulo_origen = 'DEPOSITOS'
                 AND oc.proceso_origen = 'RETIRO_ASOCIADO'
                 AND oc.tabla_origen = 'depositos.cuentas_ahorro'
                 AND TRIM(oc.tipo_comprobante) = 'NC'
                 AND oc.estado_origen = 'ACTIVO'
                JOIN contabilidad.auxiliares_contables ac
                  ON ac.id_agencia = oc.id_agencia
                 AND TRIM(ac.tipo_comprobante) = TRIM(oc.tipo_comprobante)
                 AND TRIM(ac.numero_comprobante) = TRIM(oc.numero_comprobante)
                 AND ac.id_catalogo_cuenta = ?
                 AND ac.id_datos_personal = ca.id_datos_personal
                JOIN contabilidad.catalogo_cuentas cc
                  ON cc.id_catalogo_cuenta = ac.id_catalogo_cuenta
                 AND cc.id_agencia = ca.id_agencia
                WHERE ca.id_cuenta_ahorro = ?
                  AND ca.id_agencia = ?
                  AND ca.id_datos_personal = ?
                  AND ac.valor_credito > 0
                  AND ac.valor_debito = 0
                  AND ac.estado_movimiento = 'A'
                  AND cc.naturaleza_cuenta = 'C'
                  AND cc.cuenta_operable = TRUE
                """, BigDecimal.class,
                idOrigenComprobante, idCuentaCxp,
                idCuentaAhorro, idAgencia, idBeneficiario);
        BigDecimal respaldo = respaldos.isEmpty() ? null : respaldos.get(0);
        if (respaldo == null || respaldo.compareTo(numeroDecimal(operacion, "valor_original")) < 0) {
            throw new IllegalStateException(
                    "La CxP histórica del retiro integral " + idCuentaAhorro
                            + " no tiene respaldo suficiente en el comprobante NC original."
            );
        }
        return idCuentaCxp;
    }

    /** Recupera la cuenta efectivamente acreditada en el desembolso original. */
    private Integer obtenerCuentaPorPagarDesembolsoCredito(
            Long idCredito,
            Integer idAgencia,
            Integer idBeneficiario,
            BigDecimal valorOriginal
    ) {
        List<Integer> cuentas = jdbcTemplate.queryForList("""
                SELECT ac.id_catalogo_cuenta
                FROM cartera.carteras_creditos cr
                JOIN contabilidad.origen_comprobantes oc
                  ON oc.id_agencia = cr.id_agencia
                 AND oc.modulo_origen = 'CARTERA'
                 AND oc.proceso_origen = 'DESEMBOLSO_CREDITO'
                 AND oc.tabla_origen = 'cartera.carteras_creditos'
                 AND oc.id_origen = cr.id_cartera_credito
                JOIN contabilidad.auxiliares_contables ac
                  ON ac.id_agencia = oc.id_agencia
                 AND ac.tipo_comprobante = oc.tipo_comprobante
                 AND ac.numero_comprobante = oc.numero_comprobante
                 AND ac.id_datos_personal = cr.id_datos_personal
                JOIN contabilidad.catalogo_cuentas cc
                  ON cc.id_catalogo_cuenta = ac.id_catalogo_cuenta
                 AND cc.id_agencia = cr.id_agencia
                WHERE cr.id_cartera_credito = ?
                  AND cr.id_agencia = ?
                  AND cr.id_datos_personal = ?
                  AND ac.valor_credito = ?
                  AND ac.valor_debito = 0
                  AND ac.detalle_movimiento LIKE '% - cuenta por pagar'
                  AND ac.estado_movimiento = 'A'
                  AND cc.naturaleza_cuenta = 'C'
                """, Integer.class,
                idCredito, idAgencia, idBeneficiario, valorOriginal);

        if (cuentas.size() != 1 || cuentas.get(0) == null) {
            throw new IllegalStateException(
                    "No se encontró una única cuenta por pagar contabilizada "
                            + "para el desembolso del crédito " + idCredito + "."
            );
        }
        return cuentas.get(0);
    }

    /** Datos de presentación; no altera los movimientos enviados a Contabilidad. */
    private Map<Integer, String> consultarCuentasContables(
            Integer idAgencia,
            List<MovimientoContableDTO> movimientos
    ) {
        Map<Integer, String> cuentas = new LinkedHashMap<>();
        for (MovimientoContableDTO movimiento : movimientos) {
            Integer idCuenta = movimiento.getIdCatalogoCuenta();
            if (cuentas.containsKey(idCuenta)) {
                continue;
            }
            List<Map<String, Object>> filas = jdbcTemplate.queryForList("""
                    SELECT codigo_cuenta, nombre_cuenta
                    FROM contabilidad.catalogo_cuentas
                    WHERE id_catalogo_cuenta = ? AND id_agencia = ?
                    """, idCuenta, idAgencia);
            if (filas.size() != 1) {
                throw new IllegalStateException(
                        "No se encontró la cuenta contable " + idCuenta
                                + " para la agencia " + idAgencia);
            }
            Map<String, Object> fila = filas.get(0);
            cuentas.put(idCuenta,
                    fila.get("codigo_cuenta") + " - " + fila.get("nombre_cuenta"));
        }
        return cuentas;
    }

    private MovimientoContableDTO movimiento(
            Integer idAgencia,
            Integer idBeneficiario,
            Integer idCuenta,
            BigDecimal debito,
            BigDecimal credito,
            String detalle,
            String soporte,
            Long idOperacion
    ) {
        MovimientoContableDTO m = new MovimientoContableDTO();
        m.setIdAgencia(idAgencia);
        m.setIdDatosPersonal(idBeneficiario);
        m.setIdCatalogoCuenta(idCuenta);
        m.setFechaAuxiliar(LocalDate.now());
        m.setValorDebito(debito);
        m.setValorCredito(credito);
        m.setDetalleMovimiento(detalle);
        m.setDocumentoSoporte(soporte);
        m.setOrigenModulo("TESORERIA");
        m.setDocumentoOrigen("OPERACION-" + idOperacion);
        return m;
    }

    // =========================================================
    // CONFIRMACIÓN DEFINITIVA - TRANSACCIÓN ÚNICA
    // =========================================================

    @Transactional
    public OperacionPagoResponseDTO confirmarPago(OperacionPagoRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("La solicitud es obligatoria.");
        }
        validarId(request.idOperacion(), "La operación");
        if (request.lineas() == null || request.lineas().isEmpty()) {
            throw new IllegalArgumentException("Debe agregar líneas de pago.");
        }

        Long idOperacion = request.idOperacion();
        Map<String, Object> operacion = repository.bloquearOperacion(idOperacion);
        validarAgencia(operacion);
        validarEstadoPreparacion(operacion);

        // La vista previa comparte las validaciones contables y bancarias.
        // El bloqueo de la obligación permanece hasta el commit.
        OperacionComprobantePreviewDTO preview = generarVistaPrevia(request);
        Integer idAgencia = numeroEntero(operacion, "id_agencia");
        Integer idUsuario = usuarioSesionService.idUsuario();
        BigDecimal total = preview.totalPago();
        BigDecimal ejecutado = numeroDecimal(operacion, "valor_ejecutado");
        BigDecimal original = numeroDecimal(operacion, "valor_original");
        BigDecimal nuevoEjecutado = ejecutado.add(total);
        BigDecimal saldo = original.subtract(nuevoEjecutado);
        if (saldo.signum() < 0) {
            throw new IllegalStateException("El pago supera el saldo de la obligación.");
        }

        // CE: comprobante de egreso, según la convención usada por Tesorería.
        String tipo = "CE";
        String numero = consecutivosComprobantesService.generarNumeroDefinitivo(
                tipo, idAgencia, idUsuario);
        OrigenComprobanteDTO origen = new OrigenComprobanteDTO();
        origen.setOrigenTipo("AUTO");
        origen.setModuloOrigen("TESORERIA");
        origen.setProcesoOrigen("OPERACIONES_PENDIENTES");
        origen.setTablaOrigen("tesoreria.operaciones_pendientes");
        origen.setIdOrigen(idOperacion);

        String conceptoOperacion = Objects.toString(
                operacion.get("concepto"), ""
        ).trim();

        String conceptoComprobante =
                "PAGO TESORERÍA - " + conceptoOperacion
                        + " - VALOR $" + total.toPlainString();

        contabilidadRegistroService.registrarComprobante(
                idAgencia,
                tipo,
                numero,
                conceptoComprobante,
                origen,
                preview.movimientos(),
                idUsuario
        );

        List<Long> ids = new ArrayList<>();
        for (OperacionPagoLineaDTO linea : request.lineas()) {
            Integer numeroLinea = repository.siguienteNumeroLinea(idOperacion);
            Long idEjecucion = repository.insertarPreparada(
                    idOperacion, numeroLinea, linea.idBeneficiario(),
                    linea.idCuentaBancaria(), normalizarMedioPago(linea.medioPago()),
                    limpiar(linea.numeroDocumento()), linea.valor(),
                    limpiar(linea.observaciones()), idUsuario);
            repository.confirmarEjecucion(idEjecucion, tipo, numero, idUsuario);
            ids.add(idEjecucion);
        }

        String estado = saldo.signum() == 0 ? "COMPLETADA" : PARCIAL;

        repository.actualizarSaldoOperacion(
                idOperacion,
                nuevoEjecutado,
                estado,
                idUsuario
        );

        if (esDevolucionCartera(operacion)) {
            if (!"COMPLETADA".equals(estado)) {
                throw new IllegalStateException("La devolución bancaria debe ejecutarse completamente.");
            }
            int actualizadas = jdbcTemplate.update("""
                    UPDATE tesoreria.operaciones_reversiones_origen r
                    SET estado_regularizacion = 'REGULARIZADA',
                        fk_seguridad_edicion = ?, fecha_edicion = CURRENT_TIMESTAMP
                    FROM tesoreria.operaciones_pendientes o
                    WHERE o.id_operacion_pendiente = r.id_operacion_pendiente
                      AND o.modulo_origen = 'CARTERA'
                      AND o.proceso_origen = ?
                      AND o.tabla_origen = ?
                      AND o.id_origen = ?
                      AND o.id_agencia = ?
                      AND r.estado_regularizacion = 'PENDIENTE'
                    """, idUsuario, procesoRecaudoOriginal(operacion),
                    tablaRecaudoOriginal(operacion),
                    numeroLong(operacion, "id_origen"), idAgencia);
            if (actualizadas != 1) {
                throw new IllegalStateException("No se pudo regularizar de forma única la reversión de Cartera.");
            }
        }

        return new OperacionPagoResponseDTO(
                idOperacion, idAgencia, tipo, numero, LocalDateTime.now(),
                total, nuevoEjecutado, saldo, estado, ids.size(),
                List.copyOf(ids), "Pago contabilizado correctamente.");
    }

    /** Previsualiza el recaudo de una CxC de Cartera sin modificar saldos ni comprobantes. */
    @Transactional(readOnly = true)
    public OperacionComprobantePreviewDTO generarVistaPreviaRecaudo(OperacionPagoRequestDTO request) {
        if (request == null || request.lineas() == null || request.lineas().isEmpty()) {
            throw new IllegalArgumentException("Debe indicar al menos una línea de recaudo.");
        }
        validarId(request.idOperacion(), "La operación");
        Map<String, Object> operacion = obtenerOperacion(request.idOperacion());
        validarAgencia(operacion);
        validarEntradaCartera(operacion);
        validarSinReversionOrigen(operacion);
        // El validador concreto conserva las reglas de vigencia y comprobante del origen.
        TesoreriaOrigenFinancieroValidator validador = resolverValidadorOrigen(operacion);
        validador.validarVistaPrevia(operacion);
        Integer agencia = numeroEntero(operacion, "id_agencia");
        Integer tercero = numeroEntero(operacion, "id_datos_personal");
        BigDecimal original = numeroDecimal(operacion, "valor_original");
        BigDecimal ejecutado = numeroDecimal(operacion, "valor_ejecutado");
        BigDecimal disponible = calcularSaldoDisponible(request.idOperacion(), operacion);
        if (disponible.signum() <= 0) {
            throw new IllegalStateException("La cuenta por cobrar no tiene saldo pendiente.");
        }

        // Recupera la CxC del comprobante histórico, nunca de parámetros actuales.
        Integer cuentaCxC = validador.obtenerCuentaCxCOriginal(operacion);
        TipoOperacionFinancieraDTO configuracion = validarPerfilTesoreria(
                operacion, "RECAUDO_CXC_CARTERA", false);
        String conceptoRecaudo = configuracion.getNombreOperacion();
        if (conceptoRecaudo == null || conceptoRecaudo.isBlank()) {
            throw new IllegalStateException("El catálogo no tiene nombre para esta operación financiera.");
        }
        conceptoRecaudo = conceptoRecaudo.trim();
        BigDecimal total = BigDecimal.ZERO;
        List<MovimientoContableDTO> movimientos = new ArrayList<>();
        for (OperacionPagoLineaDTO linea : request.lineas()) {
            if (linea == null) {
                throw new IllegalArgumentException("Hay una línea de recaudo vacía.");
            }
            validarValor(linea.valor());
            validarBeneficiario(operacion, linea.idBeneficiario());
            validarId(linea.idCuentaBancaria(), "La cuenta bancaria");
            String medio = normalizarMedioPago(linea.medioPago());
            if (!TRANSFERENCIA.equals(medio) && !CHEQUE.equals(medio)) {
                throw new IllegalArgumentException("Medio de recaudo no permitido: " + medio);
            }
            String referencia = limpiar(linea.numeroDocumento());
            if (referencia == null) {
                throw new IllegalArgumentException("Debe indicar la referencia del recaudo bancario.");
            }
            // No se usa permite_pagar: es un ingreso, no un egreso bancario.
            List<Integer> cuentasBanco = jdbcTemplate.queryForList("""
                    SELECT cba.id_cuenta_contable
                    FROM tesoreria.cuentas_bancarias_agencias cba
                    JOIN tesoreria.cuentas_bancarias cb
                      ON cb.id_cuenta_bancaria = cba.id_cuenta_bancaria
                    JOIN tesoreria.bancos b ON b.codigo_banco = cb.codigo_banco
                    JOIN contabilidad.catalogo_cuentas cc
                      ON cc.id_catalogo_cuenta = cba.id_cuenta_contable
                    WHERE cba.id_agencia = ? AND cba.id_cuenta_bancaria = ?
                      AND cba.activo = TRUE AND cb.activo = TRUE
                      AND cb.estado_cuenta = 'ACTIVA' AND b.activo = TRUE
                      AND cc.id_agencia = cba.id_agencia AND cc.cuenta_operable = TRUE
                    """, Integer.class, agencia, linea.idCuentaBancaria());
            if (cuentasBanco.size() != 1 || cuentasBanco.get(0) == null) {
                throw new IllegalArgumentException("La cuenta bancaria no está activa para recaudos en esta agencia.");
            }
            movimientos.add(movimiento(agencia, tercero, cuentasBanco.get(0),
                    linea.valor(), BigDecimal.ZERO, "Recaudo bancario Cartera - " + conceptoRecaudo,
                    referencia, request.idOperacion()));
            total = total.add(linea.valor());
        }
        if (total.compareTo(disponible) > 0) {
            throw new IllegalArgumentException("El recaudo supera el saldo de la cuenta por cobrar.");
        }
        validarPerfilTesoreria(operacion, "RECAUDO_CXC_CARTERA", total.compareTo(disponible) < 0);
        movimientos.add(movimiento(agencia, tercero, cuentaCxC,
                BigDecimal.ZERO, total, "Cancelación CxC de recaudo bancario Cartera - " + conceptoRecaudo,
                "OPERACION-" + request.idOperacion(), request.idOperacion()));
        return new OperacionComprobantePreviewDTO(request.idOperacion(), "CI", agencia, tercero,
                original, ejecutado, disponible, total, disponible.subtract(total),
                total, total, true, List.copyOf(movimientos),
                consultarCuentasContables(agencia, movimientos));
    }

    /** Confirma un ingreso bancario, cancela la CxC y actualiza solo la operación de Tesorería. */
    @Transactional
    public OperacionPagoResponseDTO confirmarRecaudo(OperacionPagoRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("La solicitud es obligatoria.");
        }
        validarId(request.idOperacion(), "La operación");
        // Mantiene el orden de bloqueo: origen financiero antes de operación de Tesorería.
        Map<String, Object> consulta = obtenerOperacion(request.idOperacion());
        validarAgencia(consulta);
        // Bloquea y valida el origen antes de bloquear la operación de Tesorería.
        TesoreriaOrigenFinancieroValidator validador = resolverValidadorOrigen(consulta);
        validador.validarConfirmacion(consulta);
        Map<String, Object> operacion = repository.bloquearOperacion(request.idOperacion());
        validarAgencia(operacion);
        validarEntradaCartera(operacion);
        if (!validador.soporta(operacion)
                || !Objects.equals(consulta.get("modulo_origen"), operacion.get("modulo_origen"))
                || !Objects.equals(consulta.get("proceso_origen"), operacion.get("proceso_origen"))
                || !Objects.equals(consulta.get("tabla_origen"), operacion.get("tabla_origen"))
                || !Objects.equals(consulta.get("id_origen"), operacion.get("id_origen"))
                || !Objects.equals(consulta.get("id_agencia"), operacion.get("id_agencia"))) {
            throw new IllegalStateException("El origen de la operación cambió durante la confirmación.");
        }
        validarSinReversionOrigen(operacion);
        OperacionComprobantePreviewDTO preview = generarVistaPreviaRecaudo(request);
        Integer agencia = numeroEntero(operacion, "id_agencia");
        Integer usuario = usuarioSesionService.idUsuario();
        BigDecimal total = preview.totalPago();
        BigDecimal acumulado = numeroDecimal(operacion, "valor_ejecutado").add(total);
        BigDecimal saldo = numeroDecimal(operacion, "valor_original").subtract(acumulado);
        if (saldo.signum() < 0) {
            throw new IllegalStateException("El recaudo supera el saldo disponible.");
        }
        String tipo = "CI";
        String numero = consecutivosComprobantesService.generarNumeroDefinitivo(tipo, agencia, usuario);
        OrigenComprobanteDTO origen = new OrigenComprobanteDTO();
        origen.setOrigenTipo("AUTO");
        origen.setModuloOrigen("TESORERIA");
        origen.setProcesoOrigen("OPERACIONES_PENDIENTES");
        origen.setTablaOrigen("tesoreria.operaciones_pendientes");
        origen.setIdOrigen(request.idOperacion());
        // Conserva el concepto de Cartera e identifica el valor de esta ejecución bancaria.
        String conceptoOrigen = Objects.toString(operacion.get("concepto"), "").trim();
        String conceptoComprobante = conceptoOrigen
                + " - RECAUDO BANCARIO $" + total.toPlainString();

        contabilidadRegistroService.registrarComprobante(
                agencia,
                tipo,
                numero,
                conceptoComprobante,
                origen,
                preview.movimientos(),
                usuario
        );
        List<Long> ids = new ArrayList<>();
        for (OperacionPagoLineaDTO linea : request.lineas()) {
            Integer lineaNumero = repository.siguienteNumeroLinea(request.idOperacion());
            Long id = repository.insertarPreparada(request.idOperacion(), lineaNumero,
                    linea.idBeneficiario(), linea.idCuentaBancaria(),
                    normalizarMedioPago(linea.medioPago()), limpiar(linea.numeroDocumento()),
                    linea.valor(), limpiar(linea.observaciones()), usuario);
            repository.confirmarEjecucion(id, tipo, numero, usuario);
            ids.add(id);
        }
        String estado = saldo.signum() == 0 ? "COMPLETADA" : PARCIAL;
        repository.actualizarSaldoOperacion(request.idOperacion(), acumulado, estado, usuario);
        return new OperacionPagoResponseDTO(request.idOperacion(), agencia, tipo, numero,
                LocalDateTime.now(), total, acumulado, saldo, estado, ids.size(),
                List.copyOf(ids), "Recaudo contabilizado correctamente.");
    }

    /** Recupera del CC original la CxC bancaria de Seguros o Costas, con importe y origen verificados. */
    /** Resuelve un único validador para el origen, rechazando ausencias y ambigüedades. */
    private TesoreriaOrigenFinancieroValidator resolverValidadorOrigen(Map<String, Object> operacion) {
        TesoreriaOrigenFinancieroValidator encontrado = null;
        for (TesoreriaOrigenFinancieroValidator candidato : validadoresOrigen) {
            if (candidato.soporta(operacion)) {
                if (encontrado != null) {
                    throw new IllegalStateException("Existe más de un validador para el origen financiero.");
                }
                encontrado = candidato;
            }
        }
        if (encontrado == null) {
            throw new IllegalStateException("El perfil de recaudo no tiene un validador financiero para este origen.");
        }
        return encontrado;
    }

    /** Impide nuevos recaudos cuando el origen fue reversado, incluso si el estado es PARCIAL. */
    private void validarSinReversionOrigen(Map<String, Object> operacion) {
        Boolean reversada = jdbcTemplate.queryForObject("""
                SELECT EXISTS (
                    SELECT 1
                    FROM tesoreria.operaciones_reversiones_origen
                    WHERE id_operacion_pendiente = ?
                )
                """, Boolean.class, numeroLong(operacion, "id_operacion_pendiente"));
        if (Boolean.TRUE.equals(reversada)) {
            throw new IllegalStateException(
                    "La operación fue afectada por una reversión de origen; no admite nuevos recaudos.");
        }
    }

    /** Valida el estado de la bandeja y exige un perfil de recaudo implementado. */
    private void validarEntradaCartera(Map<String, Object> operacion) {
        String estado = Objects.toString(operacion.get("estado"), "");
        if (!PENDIENTE.equals(estado) && !PARCIAL.equals(estado)) {
            throw new IllegalStateException("La operación no admite recaudos en estado " + estado);
        }
        validarPerfilTesoreria(operacion, "RECAUDO_CXC_CARTERA", false);
        // El catálogo habilita el perfil; el validador de origen controla el documento.
        resolverValidadorOrigen(operacion);
    }

    /** Verifica la configuración del proceso sin asumir que el catálogo valida el documento original. */
    private TipoOperacionFinancieraDTO validarPerfilTesoreria(
            Map<String, Object> operacion, String perfilEsperado, boolean parcial) {
        // Usa la configuración vinculada al publicar; conserva el origen para registros históricos.
        Object idRegistrado = operacion.get("id_tipo_operacion");
        TipoOperacionFinancieraDTO configuracion;
        if (idRegistrado == null) {
            configuracion = tiposOperacionesFinancierasService.validarOperacion(
                    Objects.toString(operacion.get("modulo_origen"), ""),
                    Objects.toString(operacion.get("proceso_origen"), ""),
                    Objects.toString(operacion.get("tabla_origen"), ""),
                    "TESORERIA",
                    Objects.toString(operacion.get("naturaleza"), ""),
                    parcial);
        } else {
            Long idTipoOperacion = ((Number) idRegistrado).longValue();
            TipoOperacionFinancieraDTO tipo = tiposOperacionesFinancierasService.listarParaTesoreria()
                    .stream()
                    .filter(item -> idTipoOperacion.equals(item.getIdTipoOperacion()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "La configuración financiera vinculada a la operación no está activa o habilitada para Tesorería."));
            configuracion = tiposOperacionesFinancierasService.validarOperacionPorCodigo(
                    Objects.toString(operacion.get("modulo_origen"), ""),
                    Objects.toString(operacion.get("proceso_origen"), ""),
                    Objects.toString(operacion.get("tabla_origen"), ""),
                    tipo.getCodigoOperacionFinanciera(),
                    "TESORERIA",
                    Objects.toString(operacion.get("naturaleza"), ""),
                    parcial);
            if (!idTipoOperacion.equals(configuracion.getIdTipoOperacion())) {
                throw new IllegalStateException("La configuración financiera no corresponde a la operación publicada.");
            }
        }
        if (!perfilEsperado.equals(configuracion.getPerfilEjecucion())) {
            throw new IllegalStateException("El perfil de ejecución no corresponde al flujo contable solicitado.");
        }
        return configuracion;
    }

    // =========================================================
    // VALIDACIONES DE LA OPERACIÓN
    // =========================================================

    private Map<String, Object> obtenerOperacion(
            Long idOperacion
    ) {
        Map<String, Object> operacion =
                repository.buscarOperacion(idOperacion);

        if (operacion == null) {
            throw new IllegalArgumentException(
                    "La operación pendiente no existe."
            );
        }

        return operacion;
    }

    private void validarAgencia(
            Map<String, Object> operacion
    ) {
        Integer idAgencia =
                numeroEntero(operacion, "id_agencia");

        usuarioSesionService.validarAgencia(idAgencia);
    }

    private void validarEstadoPreparacion(
            Map<String, Object> operacion
    ) {
        String estado = Objects.toString(
                operacion.get("estado"),
                ""
        );

        if (!PENDIENTE.equals(estado)
                && !PARCIAL.equals(estado)) {
            throw new IllegalStateException(
                    "La obligación no permite nuevas "
                            + "ejecuciones. Estado: "
                            + estado
            );
        }

        String naturaleza = Objects.toString(
                operacion.get("naturaleza"),
                ""
        );

        if (!"SALIDA".equals(naturaleza)) {
            throw new IllegalStateException(
                    "Esta preparación corresponde únicamente "
                            + "a operaciones de salida."
            );
        }
    }

    // =========================================================
    // VALIDACIÓN DEL BENEFICIARIO
    // =========================================================

    /**
     * Por seguridad, el beneficiario debe corresponder
     * al tercero de la obligación.
     *
     * El pago a terceros diferentes requerirá
     * una funcionalidad de autorización específica.
     */
    private void validarBeneficiario(
            Map<String, Object> operacion,
            Integer idBeneficiario
    ) {
        if (idBeneficiario == null
                || idBeneficiario <= 0) {
            throw new IllegalArgumentException(
                    "Debe seleccionar un beneficiario válido."
            );
        }

        Object terceroOrigen =
                operacion.get("id_datos_personal");

        if (!(terceroOrigen instanceof Number numero)) {
            throw new IllegalStateException(
                    "La obligación no tiene un beneficiario "
                            + "identificado."
            );
        }

        if (numero.intValue() != idBeneficiario) {
            throw new IllegalArgumentException(
                    "El beneficiario debe corresponder "
                            + "al tercero de la obligación."
            );
        }

        Integer cantidad = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM hoja_vida.datos_personales
                WHERE id_datos_personal = ?
                """,
                Integer.class,
                idBeneficiario
        );

        if (cantidad == null || cantidad != 1) {
            throw new IllegalArgumentException(
                    "El beneficiario no existe."
            );
        }
    }


    // =========================================================
    // CONTROL DE SALDOS
    // =========================================================

    private BigDecimal calcularSaldoDisponible(
            Long idOperacion,
            Map<String, Object> operacion
    ) {
        BigDecimal valorOriginal =
                numeroDecimal(
                        operacion,
                        "valor_original"
                );

        BigDecimal valorEjecutado =
                numeroDecimal(
                        operacion,
                        "valor_ejecutado"
                );

        BigDecimal valorPreparado =
                repository.sumarPreparadas(idOperacion);

        if (valorPreparado == null) {
            valorPreparado = BigDecimal.ZERO;
        }

        BigDecimal disponible =
                valorOriginal
                        .subtract(valorEjecutado)
                        .subtract(valorPreparado);

        if (disponible.signum() < 0) {
            throw new IllegalStateException(
                    "La obligación presenta ejecuciones "
                            + "preparadas superiores al saldo "
                            + "pendiente."
            );
        }

        return disponible;
    }

    // =========================================================
    // VALIDACIONES GENERALES
    // =========================================================

    private void validarValor(BigDecimal valor) {
        if (valor == null || valor.signum() <= 0) {
            throw new IllegalArgumentException(
                    "El valor debe ser mayor que cero."
            );
        }

        if (valor.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException(
                    "El valor no puede superar dos decimales."
            );
        }
    }

    private void validarId(
            Long id,
            String descripcion
    ) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    descripcion
                            + " debe tener un identificador válido."
            );
        }
    }

    private String normalizarMedioPago(
            String medioPago
    ) {
        if (medioPago == null || medioPago.isBlank()) {
            throw new IllegalArgumentException(
                    "Debe indicar el medio de pago."
            );
        }

        return medioPago.trim()
                .toUpperCase(Locale.ROOT);
    }

    private String limpiar(String valor) {
        if (valor == null) {
            return null;
        }

        String limpio = valor.trim();

        return limpio.isEmpty() ? null : limpio;
    }

    // =========================================================
    // CONVERSIÓN DE RESULTADOS SQL
    // =========================================================

    private Long numeroLong(
            Map<String, Object> registro,
            String columna
    ) {
        Object valor = registro.get(columna);

        if (!(valor instanceof Number numero)) {
            throw new IllegalStateException(
                    "Falta el identificador: " + columna
            );
        }

        return numero.longValue();
    }

    private Integer numeroEntero(
            Map<String, Object> registro,
            String columna
    ) {
        Object valor = registro.get(columna);

        if (!(valor instanceof Number numero)) {
            throw new IllegalStateException(
                    "Falta el campo obligatorio: " + columna
            );
        }

        return numero.intValue();
    }

    private BigDecimal numeroDecimal(
            Map<String, Object> registro,
            String columna
    ) {
        Object valor = registro.get(columna);

        if (valor instanceof BigDecimal decimal) {
            return decimal;
        }

        if (valor instanceof Number numero) {
            return new BigDecimal(numero.toString());
        }

        throw new IllegalStateException(
                "Falta el valor monetario: " + columna
        );
    }
}
