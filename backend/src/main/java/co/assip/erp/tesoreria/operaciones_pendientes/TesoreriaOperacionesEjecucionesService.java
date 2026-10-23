package co.assip.erp.tesoreria.operaciones_pendientes;

import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.cdat.cancelacion.CdatCancelacionRepository;
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
    private final JdbcTemplate jdbcTemplate;
    private final CdatCancelacionRepository cdatCancelacionRepository;
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

        if (esCdat) {
            cuentaPorPagar =
                    cdatCancelacionRepository.obtenerCuentaConceptoCancelacionCdat(
                            idAgencia,
                            "CUENTA_POR_PAGAR_CDAT"
                    );
        } else if (esRetiroAsociado) {
            cuentaPorPagar = obtenerCuentaPorPagarRetiroAsociado(
                    numeroLong(operacion, "id_origen"),
                    idAgencia,
                    idBeneficiario
            );
        } else {
            cuentaPorPagar = obtenerCuentaPorPagarDesembolsoCredito(
                    numeroLong(operacion, "id_origen"),
                    idAgencia,
                    idBeneficiario,
                    original
            );
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

    private Integer obtenerCuentaPorPagarRetiroAsociado(
            Long idCuentaAhorro,
            Integer idAgencia,
            Integer idBeneficiario
    ) {
        List<Integer> cuentas = jdbcTemplate.queryForList("""
                SELECT cc.id_catalogo_cuenta
                FROM depositos.cuentas_ahorro ca
                JOIN depositos.formas_ahorro fa
                  ON fa.id_forma_ahorro = ca.id_forma_ahorro
                JOIN contabilidad.catalogo_cuentas cc
                  ON cc.id_agencia = ca.id_agencia
                 AND cc.codigo_cuenta = CASE
                     WHEN TRIM(fa.codigo_forma) = '01'
                         THEN '24650501'
                     ELSE '24651001'
                 END
                WHERE ca.id_cuenta_ahorro = ?
                  AND ca.id_agencia = ?
                  AND ca.id_datos_personal = ?
                  AND cc.naturaleza_cuenta = 'C'
                  AND cc.cuenta_operable = TRUE
                """, Integer.class, idCuentaAhorro, idAgencia, idBeneficiario);

        if (cuentas.size() != 1 || cuentas.get(0) == null) {
            throw new IllegalStateException(
                    "No se encontró una única cuenta por pagar "
                            + "para el retiro integral de la cuenta " + idCuentaAhorro
            );
        }
        return cuentas.get(0);
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

        return new OperacionPagoResponseDTO(
                idOperacion, idAgencia, tipo, numero, LocalDateTime.now(),
                total, nuevoEjecutado, saldo, estado, ids.size(),
                List.copyOf(ids), "Pago contabilizado correctamente.");
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
