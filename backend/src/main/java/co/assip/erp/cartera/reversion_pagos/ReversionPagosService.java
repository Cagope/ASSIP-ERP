package co.assip.erp.cartera.reversion_pagos;

import co.assip.erp.cartera.pago_cuotas_v1.PagoCuotasV1Repository;
import co.assip.erp.cartera.pago_cuotas_v1.PagoCuotasV1Service;
import co.assip.erp.contabilidad.auxiliares_contables.ContabilidadRegistroService;
import co.assip.erp.contabilidad.auxiliares_contables.dto.MovimientoContableDTO;
import co.assip.erp.contabilidad.consecutivos_comprobantes.ConsecutivosComprobantesService;
import co.assip.erp.contabilidad.origen_comprobantes.dto.OrigenComprobanteDTO;
import co.assip.erp.depositos.movimientos.DepositosMovimientoService;
import co.assip.erp.depositos.movimientos.dto.DepositosMovimientoDTO;

import co.assip.erp.tesoreria.operaciones_pendientes.TesoreriaOperacionesPendientesService;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Consultas de reversión y operaciones financieras reutilizables.
 *
 * Las escrituras requieren una transacción coordinadora.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReversionPagosService {

    private final ReversionPagosRepository repository;
    private final PagoCuotasV1Service pagoCuotasService;
    private final UsuarioSesionService usuarioSesionService;

    private final ContabilidadRegistroService contabilidadRegistroService;
    private final ConsecutivosComprobantesService consecutivosComprobantesService;

    private final DepositosMovimientoService depositosMovimientoService;
    private final ObjectMapper objectMapper;
    private final JdbcTemplate jdbcTemplate;
    private final TesoreriaOperacionesPendientesService operacionesPendientesService;

    // =========================================================
    // CONSULTA DE CRÉDITOS
    // =========================================================

    /**
     * Lista créditos del asociado reutilizando Pago de Cuotas V1.
     */
    public List<PagoCuotasV1Repository.CreditoResumen> listarCreditos(
            Integer idAsociado
    ) {
        if (idAsociado == null || idAsociado <= 0) {
            throw new IllegalArgumentException(
                    "Debe seleccionar un asociado válido."
            );
        }

        return pagoCuotasService.listarCreditosPorAsociado(
                idAsociado
        );
    }

    // =========================================================
    // CONSULTA DE PAGOS
    // =========================================================

    /**
     * Lista los pagos después de validar la agencia del crédito.
     */
    public List<ReversionPagosRepository.PagoOriginal> listarPagos(
            Integer idCredito
    ) {
        if (idCredito == null || idCredito <= 0) {
            throw new IllegalArgumentException(
                    "Debe seleccionar un crédito válido."
            );
        }

        var credito = pagoCuotasService.consultarCredito(
                idCredito
        );

        usuarioSesionService.validarAgencia(
                credito.idAgencia()
        );

        return repository.listarPagos(idCredito);
    }

    // =========================================================
    // VISTA PREVIA
    // =========================================================

    /**
     * Comprueba la elegibilidad inicial del pago.
     * No realiza modificaciones financieras.
     */
    public VistaPrevia consultarVistaPrevia(Long idPago) {

        if (idPago == null || idPago <= 0) {
            throw new IllegalArgumentException(
                    "Debe seleccionar un pago válido."
            );
        }

        var pago = repository.consultarPago(idPago);

        var credito = pagoCuotasService.consultarCredito(
                pago.idCredito()
        );

        usuarioSesionService.validarAgencia(
                credito.idAgencia()
        );

        Long ultimo = repository.ultimoPagoAplicado(
                pago.idCredito()
        );

        boolean aplicado = "APLICADO".equals(
                pago.estado()
        );

        boolean ultimoAplicado = idPago.equals(ultimo);

        boolean comprobanteValido =
                "CC".equals(pago.tipoComprobante())
                        && pago.numeroComprobante() != null
                        && !pago.numeroComprobante().isBlank();

        String observacion = !aplicado
                ? "El pago no está aplicado."
                : !ultimoAplicado
                ? "Solo puede reversarse el último pago vigente."
                : !comprobanteValido
                ? "El pago no tiene comprobante CC válido."
                : "Elegible para continuar las validaciones financieras.";

        // Estado físico del recaudo: informativo, no bloquea la reversión.
        // La ejecución vuelve a comprobarlo bajo bloqueo transaccional.
        String estadoCaja = null;
        BigDecimal valorCaja = BigDecimal.ZERO;
        Long idMovimientoCaja = pago.idMovimientoCaja();

        if (idMovimientoCaja != null) {
            var movimientosCaja = jdbcTemplate.queryForList("""
                    SELECT m.estado, m.valor
                      FROM cajas.movimientos_caja m
                     WHERE m.id_movimiento_caja = ?
                       AND m.modulo_origen = 'CARTERA'
                       AND m.proceso_origen = 'PAGO_CUOTAS'
                       AND m.tabla_origen = 'cartera.pagos_cuotas'
                       AND m.id_origen = ?
                       AND m.naturaleza = 'INGRESO'
                       AND m.medio_pago = 'EFECTIVO'
                    """, idMovimientoCaja, idPago);
            if (movimientosCaja.size() != 1) {
                throw new IllegalStateException(
                        "No se encontró el movimiento de efectivo vinculado al pago.");
            }
            var movimientoCaja = movimientosCaja.get(0);
            estadoCaja = (String) movimientoCaja.get("estado");
            valorCaja = (BigDecimal) movimientoCaja.get("valor");
        }

        return new VistaPrevia(
                pago,
                ultimo,
                aplicado && ultimoAplicado && comprobanteValido,
                observacion,
                estadoCaja,
                valorCaja,
                idMovimientoCaja
        );
    }

    // =========================================================
    // CONTRAPARTIDA CONTABLE
    // =========================================================

    /**
     * Construye y valida las contrapartidas del CC original.
     * No registra comprobantes.
     */
    public ComprobanteContrario consultarComprobanteContrario(
            Long idPago
    ) {
        VistaPrevia vista = consultarVistaPrevia(idPago);

        if (!vista.elegibleParaAnalisis()) {
            throw new IllegalStateException(
                    vista.observacion()
            );
        }

        var pago = vista.pago();

        var lineas = repository.consultarContrapartidas(pago);

        if (lineas.isEmpty()) {
            throw new IllegalStateException(
                    "El comprobante CC original no tiene auxiliares activos."
            );
        }

        BigDecimal debitos = BigDecimal.ZERO;
        BigDecimal creditos = BigDecimal.ZERO;

        for (var linea : lineas) {

            if (linea.idCuenta() == null
                    || linea.idCuenta() <= 0
                    || linea.debito() == null
                    || linea.credito() == null
                    || linea.debito().signum() < 0
                    || linea.credito().signum() < 0
                    || (linea.debito().signum() > 0)
                    == (linea.credito().signum() > 0)) {

                throw new IllegalStateException(
                        "El CC original contiene líneas no reversables."
                );
            }

            debitos = debitos.add(linea.debito());
            creditos = creditos.add(linea.credito());
        }

        if (debitos.compareTo(creditos) != 0
                || debitos.compareTo(pago.valor()) != 0) {

            throw new IllegalStateException(
                    "Las contrapartidas no cuadran con el valor del pago."
            );
        }

        return new ComprobanteContrario(
                pago.idPago(),
                pago.tipoComprobante(),
                pago.numeroComprobante(),
                debitos,
                creditos,
                List.copyOf(lineas)
        );
    }

    // =========================================================
    // VALIDACIÓN DE RESTITUCIÓN
    // =========================================================

    /**
     * Valida el crédito contra los datos históricos
     * almacenados en liquidacion.aplicacion.
     */
    public ReversionPagosRepository.EstadoRestitucion
    consultarEstadoRestitucion(Long idPago) {

        VistaPrevia vista = consultarVistaPrevia(idPago);

        if (!vista.elegibleParaAnalisis()) {
            throw new IllegalStateException(
                    vista.observacion()
            );
        }

        return repository.validarEstadoRestitucion(
                vista.pago()
        );
    }

    // =========================================================
    // REGISTRO DEL COMPROBANTE CC CONTRARIO
    // =========================================================

    /**
     * Genera un CC nuevo con los débitos y créditos invertidos.
     *
     * Exige una transacción externa para garantizar
     * la integridad de la reversión financiera.
     */
    @Transactional(
            propagation = Propagation.MANDATORY,
            readOnly = false
    )
    public String registrarComprobanteReversion(
            ReversionPagosRepository.PagoDetalle pago,
            LocalDate fechaReversion,
            Integer idUsuario
    ) {
        if (pago == null
                || fechaReversion == null
                || idUsuario == null
                || idUsuario <= 0) {

            throw new IllegalArgumentException(
                    "Datos incompletos para registrar el CC de reversión."
            );
        }

        ComprobanteContrario comprobante =
                consultarComprobanteContrario(pago.idPago());

        String tipo = "CC";

        String numero =
                consecutivosComprobantesService.generarNumeroDefinitivo(
                        tipo,
                        pago.idAgencia(),
                        idUsuario
                );

        List<MovimientoContableDTO> movimientos =
                new ArrayList<>();

        for (var linea : comprobante.lineas()) {

            MovimientoContableDTO movimiento =
                    new MovimientoContableDTO();

            movimiento.setIdCatalogoCuenta(
                    linea.idCuenta()
            );

            movimiento.setIdAgencia(
                    pago.idAgencia()
            );

            movimiento.setIdDatosPersonal(
                    linea.idTercero()
            );

            movimiento.setFechaAuxiliar(
                    fechaReversion
            );

            movimiento.setTipoComprobante(tipo);

            movimiento.setNumeroComprobante(
                    numero
            );

            movimiento.setValorDebito(
                    linea.debito()
            );

            movimiento.setValorCredito(
                    linea.credito()
            );

            movimiento.setDetalleMovimiento(
                    "REVERSIÓN PAGO CARTERA PC-" + pago.idPago()
            );

            movimiento.setOrigenModulo("CARTERA");

            movimiento.setDocumentoOrigen(
                    "PC-" + pago.idPago()
            );

            movimientos.add(movimiento);
        }

        OrigenComprobanteDTO origen =
                new OrigenComprobanteDTO();

        origen.setIdAgencia(pago.idAgencia());
        origen.setTipoComprobante(tipo);
        origen.setNumeroComprobante(numero);
        origen.setOrigenTipo("AUTO");
        origen.setModuloOrigen("CARTERA");

        origen.setProcesoOrigen(
                "REVERSION_PAGO_CUOTAS"
        );

        origen.setTablaOrigen(
                "cartera.pagos_cuotas"
        );

        origen.setIdOrigen(
                pago.idPago()
        );

        String concepto =
                "REVERSIÓN PAGO CARTERA PC-" + pago.idPago()
                        + " / CC ORIGINAL " + pago.numeroComprobante();

        contabilidadRegistroService.registrarComprobanteNuevo(
                pago.idAgencia(),
                tipo,
                numero,
                concepto,
                origen,
                movimientos,
                idUsuario
        );

        return numero;
    }


    /**
     * Registra el CC contrario de una prórroga a partir de los auxiliares originales.
     * No utiliza las validaciones de elegibilidad de Pago de Cuotas V1.
     * Debe invocarse dentro de la transacción coordinadora de la reversión.
     */
    @Transactional(propagation = Propagation.MANDATORY, readOnly = false)
    public String registrarComprobanteReversionProrroga(
            Long idProrroga, Long idPago, Integer idAgencia,
            String tipoOriginal, String numeroOriginal,
            BigDecimal valorOriginal, LocalDate fechaReversion, Integer idUsuario) {
        if (idProrroga == null || idProrroga <= 0 || idPago == null || idPago <= 0
                || idAgencia == null || idAgencia <= 0
                || !"CC".equals(tipoOriginal)
                || numeroOriginal == null || numeroOriginal.isBlank()
                || valorOriginal == null || valorOriginal.signum() <= 0
                || fechaReversion == null || idUsuario == null || idUsuario <= 0) {
            throw new IllegalArgumentException("Datos inválidos del CC original de prórroga.");
        }
        var referencia = new ReversionPagosRepository.PagoDetalle(
                idPago, null, idAgencia, null, null, valorOriginal,
                "APLICADO", null, tipoOriginal, numeroOriginal, null, null);
        var lineas = repository.consultarContrapartidas(referencia);
        if (lineas.isEmpty()) {
            throw new IllegalStateException("El CC original de la prórroga no tiene auxiliares activos.");
        }
        BigDecimal debitos = BigDecimal.ZERO;
        BigDecimal creditos = BigDecimal.ZERO;
        for (var linea : lineas) {
            if (linea.idCuenta() == null || linea.idCuenta() <= 0
                    || linea.debito() == null || linea.credito() == null
                    || linea.debito().signum() < 0 || linea.credito().signum() < 0
                    || (linea.debito().signum() > 0) == (linea.credito().signum() > 0)) {
                throw new IllegalStateException("El CC original tiene líneas no reversables.");
            }
            debitos = debitos.add(linea.debito());
            creditos = creditos.add(linea.credito());
        }
        if (debitos.compareTo(creditos) != 0 || debitos.compareTo(valorOriginal) != 0) {
            throw new IllegalStateException("El CC original no cuadra con el valor de la prórroga.");
        }
        String numeroNuevo = consecutivosComprobantesService.generarNumeroDefinitivo(
                "CC", idAgencia, idUsuario);
        List<MovimientoContableDTO> movimientos = new ArrayList<>();
        for (var linea : lineas) {
            MovimientoContableDTO movimiento = new MovimientoContableDTO();
            movimiento.setIdCatalogoCuenta(linea.idCuenta());
            movimiento.setIdAgencia(idAgencia);
            movimiento.setIdDatosPersonal(linea.idTercero());
            movimiento.setFechaAuxiliar(fechaReversion);
            movimiento.setTipoComprobante("CC");
            movimiento.setNumeroComprobante(numeroNuevo);
            movimiento.setValorDebito(linea.debito());
            movimiento.setValorCredito(linea.credito());
            movimiento.setDetalleMovimiento("REVERSIÓN PRÓRROGA " + idProrroga);
            movimiento.setOrigenModulo("CARTERA");
            movimiento.setDocumentoOrigen("PR-" + idProrroga);
            movimientos.add(movimiento);
        }
        OrigenComprobanteDTO origen = new OrigenComprobanteDTO();
        origen.setIdAgencia(idAgencia);
        origen.setTipoComprobante("CC");
        origen.setNumeroComprobante(numeroNuevo);
        origen.setOrigenTipo("AUTO");
        origen.setModuloOrigen("CARTERA");
        origen.setProcesoOrigen("REVERSION_PRORROGA");
        origen.setTablaOrigen("cartera.creditos_prorrogas");
        origen.setIdOrigen(idProrroga);
        contabilidadRegistroService.registrarComprobanteNuevo(
                idAgencia, "CC", numeroNuevo,
                "REVERSIÓN PRÓRROGA " + idProrroga + " / CC ORIGINAL " + numeroOriginal,
                origen, movimientos, idUsuario);
        return numeroNuevo;
    }


    // =========================================================
    // DEVOLUCIÓN DE AHORROS - MOVIMIENTO 222
    // =========================================================

    /**
     * Devuelve los valores debitados de las cuentas de ahorro
     * mediante movimientos crédito tipo 222.
     *
     * Reutiliza el CC contrario sin generar otro comprobante.
     * Exige una transacción financiera externa.
     */
    /** Conserva el origen original para la reversión de Pago de Cuotas V1. */
    @Transactional(propagation = Propagation.MANDATORY, readOnly = false)
    public BigDecimal devolverAhorros(
            ReversionPagosRepository.PagoDetalle pago,
            String numeroComprobanteReversion,
            LocalDate fechaReversion,
            Integer idUsuario
    ) {
        return devolverAhorros(
                pago, numeroComprobanteReversion, fechaReversion,
                idUsuario, "REVERSION_PAGO_CUOTAS",
                pago == null ? null : pago.idPago()
        );
    }

    /** Devuelve ahorros con la identificación del proceso que origina la reversión. */
    @Transactional(propagation = Propagation.MANDATORY, readOnly = false)
    public BigDecimal devolverAhorros(
            ReversionPagosRepository.PagoDetalle pago,
            String numeroComprobanteReversion,
            LocalDate fechaReversion,
            Integer idUsuario,
            String procesoOrigen,
            Long idOrigen
    ) {
        if (!"REVERSION_PAGO_CUOTAS".equals(procesoOrigen)
                && !"REVERSION_PRORROGA".equals(procesoOrigen)) {
            throw new IllegalArgumentException("Proceso de reversión de ahorros inválido.");
        }
        if (idOrigen == null || idOrigen <= 0) {
            throw new IllegalArgumentException("Identificador de origen inválido.");
        }
        if (pago == null
                || pago.idPago() == null
                || numeroComprobanteReversion == null
                || numeroComprobanteReversion.isBlank()
                || fechaReversion == null
                || idUsuario == null
                || idUsuario <= 0) {

            throw new IllegalArgumentException(
                    "Datos incompletos para devolver ahorros."
            );
        }

        if (!"APLICADO".equals(pago.estado())) {
            throw new IllegalStateException(
                    "Solo pueden devolverse ahorros de un pago aplicado."
            );
        }

        JsonNode medios;

        try {
            medios = objectMapper.readTree(
                    pago.mediosRecaudoJson()
            );
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Los medios originales no son JSON válido.",
                    ex
            );
        }

        if (medios == null || !medios.isArray()) {
            throw new IllegalStateException(
                    "El pago no contiene medios de recaudo válidos."
            );
        }

        List<AhorroOriginal> ahorros = new ArrayList<>();

        // Identificar los medios AHORROS del pago original.
        for (JsonNode medio : medios) {

            if (!medio.isObject()
                    || !medio.path("tipoMedio").isTextual()) {

                throw new IllegalStateException(
                        "El pago contiene un medio de recaudo inválido."
                );
            }

            if (!"AHORROS".equals(
                    medio.path("tipoMedio").asText()
            )) {
                continue;
            }

            JsonNode cuenta = medio.get("idCuentaAhorro");
            JsonNode importe = medio.get("valor");

            if (cuenta == null
                    || !cuenta.canConvertToInt()
                    || cuenta.intValue() <= 0
                    || importe == null
                    || !importe.isNumber()
                    || importe.decimalValue().signum() <= 0) {

                throw new IllegalStateException(
                        "Cuenta o valor de ahorro inválido en el pago original."
                );
            }

            ahorros.add(
                    new AhorroOriginal(
                            cuenta.intValue(),
                            importe.decimalValue()
                    )
            );
        }

        // Orden estable para los bloqueos de cuentas.
        ahorros.sort(
                Comparator.comparing(
                        AhorroOriginal::idCuenta
                )
        );

        BigDecimal total = BigDecimal.ZERO;

        for (AhorroOriginal ahorro : ahorros) {

            DepositosMovimientoDTO dto =
                    new DepositosMovimientoDTO();

            dto.setIdCuentaAhorro(
                    ahorro.idCuenta()
            );

            dto.setFechaMovimiento(
                    fechaReversion
            );

            dto.setTipoComprobante("CC");

            dto.setNumeroComprobante(
                    numeroComprobanteReversion
            );

            dto.setTipoMovimiento("222");
            dto.setModulo("03");

            dto.setValorDebito(
                    BigDecimal.ZERO
            );

            dto.setValorCredito(
                    ahorro.valor()
            );

            dto.setTarjeta("N");
            dto.setModuloOrigen("CARTERA");

            dto.setProcesoOrigen(procesoOrigen);
            dto.setIdOrigen(idOrigen);

            depositosMovimientoService.registrarCredito(
                    dto,
                    idUsuario
            );

            total = total.add(
                    ahorro.valor()
            );
        }

        return total;
    }


    /** Revierte Cartera y Depósitos atómicamente, sin ejecutar Caja ni Tesorería. */
    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public ResultadoReversion confirmarReversion(
            Long idPago,
            LocalDate fechaReversion,
            String motivo
    ) {
        if (idPago == null || idPago <= 0
                || fechaReversion == null
                || motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException(
                    "Pago, fecha y motivo de reversión son obligatorios.");
        }
        Integer idUsuario = usuarioSesionService.idUsuario();
        var pago = repository.bloquearPagoParaReversion(idPago);
        var credito = pagoCuotasService.consultarCredito(pago.idCredito());
        usuarioSesionService.validarAgencia(credito.idAgencia());
        if (!credito.idAgencia().equals(pago.idAgencia())) {
            throw new IllegalStateException(
                    "La agencia del pago no corresponde a la del crédito.");
        }
        if (!"CC".equals(pago.tipoComprobante())
                || pago.numeroComprobante() == null
                || pago.numeroComprobante().isBlank()) {
            throw new IllegalStateException(
                    "El pago no tiene comprobante CC válido.");
        }
        JsonNode medios = leerMediosRecaudo(pago);
        BigDecimal sumaMedios = BigDecimal.ZERO;
        for (JsonNode medio : medios) {
            if (!medio.isObject()
                    || !medio.path("tipoMedio").isTextual()
                    || !medio.path("valor").isNumber()
                    || medio.path("valor").decimalValue().signum() <= 0) {
                throw new IllegalStateException(
                        "El pago tiene medios de recaudo inválidos.");
            }
            String tipo = medio.path("tipoMedio").asText();
            // Cajas y Tesorería resuelven sus efectos en sus propios módulos.
            if (!"CAJA".equals(tipo)
                    && !"AHORROS".equals(tipo)
                    && !"CXP".equals(tipo)) {
                throw new IllegalStateException(
                        "Medio de recaudo no soportado para reversión: " + tipo);
            }
            sumaMedios = sumaMedios.add(medio.path("valor").decimalValue());
        }
        if (sumaMedios.compareTo(pago.valor()) != 0) {
            throw new IllegalStateException(
                    "Los medios de recaudo no suman el valor del pago original.");
        }
        var estado = repository.validarEstadoRestitucion(pago);
        if (!estado.consistente()) {
            throw new IllegalStateException(estado.observacion());
        }
        consultarComprobanteContrario(idPago);
        // Impide una reversión parcial de Seguros, Alivios o Costas Judiciales.
        repository.validarAbonosAuxiliaresPago(
                idPago, pago.idCredito(), pago.numeroComprobante());
        String numeroCC = registrarComprobanteReversion(
                pago, fechaReversion, idUsuario);
        repository.revertirAbonosAuxiliares(
                pago.idCredito(),
                pago.numeroComprobante(),
                numeroCC,
                fechaReversion,
                idUsuario);
        BigDecimal saldoRestituido = repository.restituirCreditoDesdePago(
                idPago, idUsuario);
        BigDecimal ahorros = devolverAhorros(
                pago, numeroCC, fechaReversion, idUsuario);
        // Conserva el extracto original como R y registra el CC contrario como V.
        repository.registrarReversionExtracto(
                pago, numeroCC, fechaReversion, motivo, idUsuario);
        repository.marcarPagoReversado(
                idPago, numeroCC, fechaReversion, motivo, idUsuario);
        registrarReversionEnTesoreria(idPago, pago.idAgencia(), medios, motivo, idUsuario);
        BigDecimal cajaPendiente = anularMovimientoCajaPendiente(
                idPago, medios, idUsuario);
        return new ResultadoReversion(
                idPago, numeroCC, saldoRestituido, ahorros, cajaPendiente);
    }

    /**
     * Resuelve el efectivo de Caja dentro de la misma transacción de reversión.
     * Un ingreso pendiente se anula; uno registrado conserva su historia y
     * genera una devolución pendiente que Caja ejecutará posteriormente.
     */
    private BigDecimal anularMovimientoCajaPendiente(
            Long idPago, JsonNode medios, Integer idUsuario) {
        BigDecimal efectivo = BigDecimal.ZERO;
        for (JsonNode medio : medios) {
            if ("CAJA".equals(medio.path("tipoMedio").asText())) {
                efectivo = efectivo.add(medio.path("valor").decimalValue());
            }
        }
        if (efectivo.signum() == 0) {
            return BigDecimal.ZERO;
        }

        List<java.util.Map<String, Object>> movimientos = jdbcTemplate.queryForList("""
                SELECT m.id_movimiento_caja, m.id_provision, m.id_caja,
                       m.id_departamento_operativo, m.estado, m.valor,
                       m.codigo_referencia
                  FROM cajas.movimientos_caja m
                  JOIN cartera.pagos_cuotas pc
                    ON pc.id_pago = m.id_origen
                   AND pc.id_movimiento_caja = m.id_movimiento_caja
                   AND pc.id_caja = m.id_caja
                 WHERE pc.id_pago = ?
                   AND m.modulo_origen = 'CARTERA'
                   AND m.proceso_origen = 'PAGO_CUOTAS'
                   AND m.tabla_origen = 'cartera.pagos_cuotas'
                   AND m.naturaleza = 'INGRESO'
                   AND m.medio_pago = 'EFECTIVO'
                 FOR UPDATE OF m
                """, idPago);
        if (movimientos.size() != 1) {
            throw new IllegalStateException(
                    "El pago con efectivo no tiene un único movimiento de Caja vinculado.");
        }
        var movimiento = movimientos.get(0);
        Long idMovimiento = ((Number) movimiento.get("id_movimiento_caja")).longValue();
        BigDecimal valor = (BigDecimal) movimiento.get("valor");
        if (valor.compareTo(efectivo) != 0) {
            throw new IllegalStateException(
                    "El valor de Caja no coincide con el efectivo del pago original.");
        }

        String estadoCaja = movimiento.get("estado").toString();
        if ("PENDIENTE".equals(estadoCaja)) {
            int anulados = jdbcTemplate.update("""
                    UPDATE cajas.movimientos_caja
                       SET estado = 'ANULADO',
                           fk_seguridad_edicion = ?,
                           fecha_edicion = CURRENT_TIMESTAMP
                     WHERE id_movimiento_caja = ?
                       AND estado = 'PENDIENTE'
                    """, idUsuario, idMovimiento);
            if (anulados != 1) {
                throw new IllegalStateException(
                        "No fue posible anular el movimiento pendiente de Caja.");
            }
            return BigDecimal.ZERO;
        }
        if (!"REGISTRADO".equals(estadoCaja)) {
            throw new IllegalStateException(
                    "Estado de Caja incompatible con la reversión: " + estadoCaja);
        }

        // La fila original está bloqueada por FOR UPDATE, evitando dos
        // devoluciones concurrentes del mismo recaudo.
        Integer duplicados = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                  FROM cajas.movimientos_caja
                 WHERE modulo_origen = 'CARTERA'
                   AND proceso_origen = 'REVERSA_PAGO_CUOTAS'
                   AND tabla_origen = 'cartera.pagos_cuotas'
                   AND id_origen = ?
                   AND naturaleza = 'EGRESO'
                """, Integer.class, idPago);
        if (duplicados == null || duplicados != 0) {
            throw new IllegalStateException(
                    "Ya existe una devolución de Caja para este pago.");
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
                       'CARTERA', 'REVERSA_PAGO_CUOTAS',
                       'cartera.pagos_cuotas', ?, ?,
                       'EGRESO', 'EFECTIVO', ?,
                       'Devolución de efectivo por reversión de pago de cuotas. Movimiento original ' || ?,
                       'PENDIENTE', ?, ?
                  FROM cajas.operaciones_caja o
                 WHERE o.codigo_operacion = 'REVERSA_PAGO_CUOTAS'
                   AND o.naturaleza = 'EGRESO'
                   AND o.afecta_efectivo = TRUE
                   AND o.estado = 'A'
                """,
                movimiento.get("id_provision"),
                movimiento.get("id_caja"),
                movimiento.get("id_departamento_operativo"),
                idPago,
                movimiento.get("codigo_referencia"),
                efectivo,
                idMovimiento,
                idUsuario,
                idUsuario);
        if (creados != 1) {
            throw new IllegalStateException(
                    "No se encontró una única operación activa REVERSA_PAGO_CUOTAS para Caja.");
        }
        return efectivo;
    }

    /** Bloquea nuevas ejecuciones de Tesorería y conserva los recaudos bancarios confirmados. */
    private void registrarReversionEnTesoreria(
            Long idPago, Integer idAgencia, JsonNode medios,
            String motivo, Integer idUsuario) {
        BigDecimal cxC = BigDecimal.ZERO;
        for (JsonNode medio : medios) {
            if ("CXP".equals(medio.path("tipoMedio").asText())) {
                cxC = cxC.add(medio.path("valor").decimalValue());
            }
        }
        if (cxC.signum() == 0) {
            return;
        }
        List<java.util.Map<String, Object>> operaciones = jdbcTemplate.queryForList("""
                SELECT id_operacion_pendiente, id_datos_personal, valor_original, valor_ejecutado, estado
                FROM tesoreria.operaciones_pendientes
                WHERE modulo_origen = 'CARTERA'
                  AND proceso_origen = 'PAGO_CUOTAS'
                  AND tabla_origen = 'cartera.pagos_cuotas'
                  AND id_origen = ?
                  AND id_agencia = ?
                FOR UPDATE
                """, idPago, idAgencia);
        if (operaciones.size() != 1) {
            throw new IllegalStateException(
                    "No se encontró una única operación de Tesorería para el recaudo CxC del pago.");
        }
        var operacion = operaciones.get(0);
        Long idOperacion = ((Number) operacion.get("id_operacion_pendiente")).longValue();
        BigDecimal original = (BigDecimal) operacion.get("valor_original");
        BigDecimal ejecutado = (BigDecimal) operacion.get("valor_ejecutado");
        String estado = operacion.get("estado").toString();
        if (original.compareTo(cxC) != 0) {
            throw new IllegalStateException(
                    "La CxC del pago no coincide con la obligación publicada en Tesorería.");
        }
        if ("ANULADA".equals(estado)) {
            throw new IllegalStateException("La operación de Tesorería ya está anulada.");
        }
        if (ejecutado.signum() == 0) {
            if (!"PENDIENTE".equals(estado)) {
                throw new IllegalStateException(
                        "La operación sin recaudos no está en estado PENDIENTE.");
            }
            jdbcTemplate.update("""
                    UPDATE tesoreria.operaciones_pendientes
                    SET estado = 'ANULADA', fk_seguridad_edicion = ?,
                        fecha_edicion = CURRENT_TIMESTAMP
                    WHERE id_operacion_pendiente = ?
                    """, idUsuario, idOperacion);
        } else if (!"PARCIAL".equals(estado) && !"COMPLETADA".equals(estado)) {
            throw new IllegalStateException(
                    "La operación tiene recaudos pero su estado no es válido: " + estado);
        }
        jdbcTemplate.update("""
                INSERT INTO tesoreria.operaciones_reversiones_origen (
                    id_operacion_pendiente, modulo_origen, proceso_origen,
                    tabla_origen, id_origen, motivo, estado_regularizacion,
                    fk_seguridad_creacion)
                VALUES (?, 'CARTERA', 'PAGO_CUOTAS', 'cartera.pagos_cuotas',
                        ?, ?, ?, ?)
                """, idOperacion, idPago, motivo.trim(),
                ejecutado.signum() == 0 ? "NO_REQUIERE" : "PENDIENTE", idUsuario);

        if (ejecutado.signum() > 0) {
            publicarDevolucionBancaria(idPago, idAgencia, idOperacion,
                    operacion, ejecutado, idUsuario);
        }
    }

    /**
     * Publica una única devolución por pago reversado. La ejecución bancaria original
     * se conserva intacta; Tesorería debe usar su cuenta bancaria y generar el CE.
     * La restricción única de origen en operaciones_pendientes impide duplicaciones.
     */
    private void publicarDevolucionBancaria(
            Long idPago,
            Integer idAgencia,
            Long idOperacionOriginal,
            java.util.Map<String, Object> operacionOriginal,
            BigDecimal valorRecaudado,
            Integer idUsuario
    ) {
        List<java.util.Map<String, Object>> ejecuciones = jdbcTemplate.queryForList("""
                SELECT id_operacion_ejecucion, id_cuenta_bancaria, valor
                FROM tesoreria.operaciones_ejecuciones
                WHERE id_operacion_pendiente = ?
                  AND estado = 'CONFIRMADA'
                ORDER BY id_operacion_ejecucion
                FOR UPDATE
                """, idOperacionOriginal);

        if (ejecuciones.isEmpty()) {
            throw new IllegalStateException(
                    "La operación recaudada no tiene ejecuciones bancarias confirmadas.");
        }
        BigDecimal totalConfirmado = BigDecimal.ZERO;
        Long cuentaOriginal = null;
        for (var ejecucion : ejecuciones) {
            if (!(ejecucion.get("id_cuenta_bancaria") instanceof Number numeroCuenta)) {
                throw new IllegalStateException(
                        "La ejecución original no identifica una cuenta bancaria.");
            }
            long cuenta = numeroCuenta.longValue();
            if (cuentaOriginal != null && cuentaOriginal.longValue() != cuenta) {
                throw new IllegalStateException(
                        "El recaudo se ejecutó en varias cuentas bancarias; "
                                + "no puede publicarse una devolución única.");
            }
            cuentaOriginal = cuenta;
            totalConfirmado = totalConfirmado.add((BigDecimal) ejecucion.get("valor"));
        }
        if (totalConfirmado.compareTo(valorRecaudado) != 0) {
            throw new IllegalStateException(
                    "Las ejecuciones bancarias confirmadas no coinciden con el recaudo.");
        }

        // La cuenta original se obtiene al ejecutar; no se selecciona libremente.
        operacionesPendientesService.publicarEstricto(
                idAgencia, "CARTERA", "REVERSA_PAGO_CUOTAS",
                "cartera.pagos_cuotas", idPago,
                (Integer) operacionOriginal.get("id_datos_personal"),
                "SALIDA", LocalDate.now(),
                "Devolución del recaudo bancario de Cartera. Operación original "
                        + idOperacionOriginal + ", cuenta bancaria original " + cuentaOriginal,
                valorRecaudado, idUsuario,
                null, "DEVOLUCION_CXC_CARTERA", false);

    }

    /** Lee y valida la estructura JSON de los medios originales del pago. */
    private JsonNode leerMediosRecaudo(
            ReversionPagosRepository.PagoDetalle pago
    ) {
        try {
            JsonNode medios = objectMapper.readTree(pago.mediosRecaudoJson());
            if (medios == null || !medios.isArray() || medios.isEmpty()) {
                throw new IllegalStateException(
                        "El pago no contiene medios de recaudo válidos.");
            }
            return medios;
        } catch (com.fasterxml.jackson.core.JsonProcessingException ex) {
            throw new IllegalStateException(
                    "El pago contiene JSON de medios inválido.", ex);
        }
    }

    public record ResultadoReversion(
            Long idPago,
            String numeroComprobante,
            BigDecimal saldoRestituido,
            BigDecimal valorAhorros,
            BigDecimal valorCajaPendiente
    ) {}

    // =========================================================
    // ESTRUCTURAS INTERNAS
    // =========================================================

    private record AhorroOriginal(
            Integer idCuenta,
            BigDecimal valor
    ) {
    }

    // =========================================================
    // RESPUESTAS DE CONSULTA
    // =========================================================

    /**
     * Contrapartidas del comprobante original.
     */
    public record ComprobanteContrario(
            Long idPago,
            String tipoOriginal,
            String numeroOriginal,
            BigDecimal totalDebitos,
            BigDecimal totalCreditos,
            List<ReversionPagosRepository.LineaContrapartida> lineas
    ) {
    }

    /**
     * Resultado de la elegibilidad preliminar del pago.
     */
    public record VistaPrevia(
            ReversionPagosRepository.PagoDetalle pago,
            Long ultimoPagoAplicado,
            boolean elegibleParaAnalisis,
            String observacion,
            String estadoCaja,
            BigDecimal valorCaja,
            Long idMovimientoCaja
    ) {
    }
}
