package co.assip.erp.cartera.reversion_pagos;

import co.assip.erp.cartera.pago_cuotas_v1.PagoCuotasV1Repository;
import co.assip.erp.cartera.pago_cuotas_v1.PagoCuotasV1Service;
import co.assip.erp.contabilidad.auxiliares_contables.ContabilidadRegistroService;
import co.assip.erp.contabilidad.auxiliares_contables.dto.MovimientoContableDTO;
import co.assip.erp.contabilidad.consecutivos_comprobantes.ConsecutivosComprobantesService;
import co.assip.erp.contabilidad.origen_comprobantes.dto.OrigenComprobanteDTO;
import co.assip.erp.depositos.movimientos.DepositosMovimientoService;
import co.assip.erp.depositos.movimientos.dto.DepositosMovimientoDTO;

import co.assip.erp.seguridad.sesion.UsuarioSesionService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
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

        return new VistaPrevia(
                pago,
                ultimo,
                aplicado && ultimoAplicado && comprobanteValido,
                observacion
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
    @Transactional(
            propagation = Propagation.MANDATORY,
            readOnly = false
    )
    public BigDecimal devolverAhorros(
            ReversionPagosRepository.PagoDetalle pago,
            String numeroComprobanteReversion,
            LocalDate fechaReversion,
            Integer idUsuario
    ) {
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

            dto.setProcesoOrigen(
                    "REVERSION_PAGO_CUOTAS"
            );

            dto.setIdOrigen(
                    pago.idPago()
            );

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
        return new ResultadoReversion(
                idPago, numeroCC, saldoRestituido, ahorros, BigDecimal.ZERO);
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
            String observacion
    ) {
    }
}

