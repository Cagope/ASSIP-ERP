package co.assip.erp.cajas.captura_depositos;

import co.assip.erp.cajas.captura_depositos.dto.CajaCapturaDepositosChequeDTO;
import co.assip.erp.cajas.captura_depositos.dto.CajaCapturaDepositosCuentaDTO;
import co.assip.erp.cajas.captura_depositos.dto.CajaCapturaDepositosRequestDTO;
import co.assip.erp.contabilidad.auxiliares_contables.ContabilidadRegistroService;
import co.assip.erp.contabilidad.auxiliares_contables.dto.MovimientoContableDTO;
import co.assip.erp.contabilidad.consecutivos_comprobantes.ConsecutivosComprobantesService;
import co.assip.erp.contabilidad.origen_comprobantes.dto.OrigenComprobanteDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CajaCapturaDepositosContabilidadService {

    private static final String TIPO_COMPROBANTE = "RC";

    private static final String MODULO_ORIGEN = "DEPOSITOS";
    private static final String PROCESO_ORIGEN = "CAPTURA_DEPOSITOS";
    private static final String TABLA_ORIGEN = "depositos.cuentas_ahorro";

    private static final String OPERACION_CONSIGNACION = "CONSIGNACION_AHORRO";
    private static final String OPERACION_RETIRO = "RETIRO_AHORRO";

    private static final int MAX_CONCEPTO = 250;
    private static final int MAX_DETALLE = 100;
    private static final int MAX_DOCUMENTO_SOPORTE = 20;

    private final CajaCapturaDepositosRepository repository;
    private final ConsecutivosComprobantesService consecutivosComprobantesService;
    private final ContabilidadRegistroService contabilidadRegistroService;

    /**
     * Registra la contabilización definitiva de una operación de depósitos
     * realizada físicamente por Caja.
     *
     * Debe ejecutarse dentro de la misma transacción de aplicar().
     */
    @Transactional
    public String contabilizar(
            CajaCapturaDepositosRequestDTO request,
            CajaCapturaDepositosCuentaDTO cuenta,
            Integer idUsuario
    ) {

        validarDatosBase(request, cuenta, idUsuario);
        boolean esInteragencia =
                !request.getIdAgencia().equals(cuenta.getIdAgencia());

        if (esInteragencia) {
            return contabilizarInteragencia(
                    request,
                    cuenta,
                    idUsuario
            );
        }

        Integer idCuentaCaja = repository.obtenerCuentaContableCaja(
                request.getIdCaja()
        );

        if (idCuentaCaja == null) {
            throw new IllegalStateException(
                    "La caja seleccionada no tiene una cuenta contable configurada."
            );
        }

        Integer idCuentaProducto = resolverCuentaProducto(cuenta);

        /*
         * El CSC del RC se obtiene con el mecanismo centralizado de
         * contabilidad, que bloquea el consecutivo mediante FOR UPDATE.
         *
         * Si posteriormente falla cualquier parte de la operación,
         * el rollback de la transacción también revierte este incremento.
         */
        String numeroRC =
                consecutivosComprobantesService.generarNumeroDefinitivo(
                        TIPO_COMPROBANTE,
                        request.getIdAgencia(),
                        idUsuario
                );

        String documentoSoporte = construirDocumentoSoporte(request);

        List<MovimientoContableDTO> movimientos;

        if (OPERACION_CONSIGNACION.equalsIgnoreCase(request.getCodigoOperacion())) {

            movimientos = construirConsignacion(
                    request,
                    cuenta,
                    idCuentaCaja,
                    idCuentaProducto,
                    documentoSoporte
            );

        } else if (OPERACION_RETIRO.equalsIgnoreCase(request.getCodigoOperacion())) {

            movimientos = construirRetiro(
                    request,
                    cuenta,
                    idCuentaCaja,
                    idCuentaProducto,
                    documentoSoporte
            );

        } else {

            throw new IllegalArgumentException(
                    "La operación no está habilitada para contabilización desde Caja: "
                            + request.getCodigoOperacion()
            );
        }

        validarCuadre(movimientos);

        OrigenComprobanteDTO origen = new OrigenComprobanteDTO();
        origen.setOrigenTipo("AUTO");
        origen.setModuloOrigen(MODULO_ORIGEN);
        origen.setProcesoOrigen(PROCESO_ORIGEN);
        origen.setTablaOrigen(TABLA_ORIGEN);
        origen.setIdOrigen(request.getIdCuentaAhorro());

        String concepto = construirConcepto(
                request,
                cuenta,
                numeroRC
        );

        contabilidadRegistroService.registrarComprobante(
                request.getIdAgencia(),
                TIPO_COMPROBANTE,
                numeroRC,
                concepto,
                origen,
                movimientos,
                idUsuario
        );

        return numeroRC;
    }

    private String contabilizarInteragencia(
            CajaCapturaDepositosRequestDTO request,
            CajaCapturaDepositosCuentaDTO cuenta,
            Integer idUsuario
    ) {

        Integer idCuentaCaja =
                repository.obtenerCuentaContableCaja(
                        request.getIdCaja()
                );

        if (idCuentaCaja == null) {
            throw new IllegalStateException(
                    "La caja seleccionada no tiene una cuenta contable configurada."
            );
        }

        Integer idCuentaInteragenciaPropietaria =
                cuenta.getIdCatalogoCuentaInteragencia();

        if (idCuentaInteragenciaPropietaria == null) {
            throw new IllegalStateException(
                    "La forma de ahorro no tiene configurada la cuenta interagencia "
                            + "en la agencia propietaria de la cuenta."
            );
        }

        if (cuenta.getTipoCaptacionForma() == null
                || cuenta.getTipoCaptacionForma().isBlank()) {

            throw new IllegalStateException(
                    "La forma de ahorro no tiene tipo de captación configurado."
            );
        }

        Integer idCuentaInteragenciaCaja =
                repository.obtenerCuentaInteragencia(
                        request.getIdAgencia(),
                        cuenta.getTipoCaptacionForma()
                );

        if (idCuentaInteragenciaCaja == null) {
            throw new IllegalStateException(
                    "No existe cuenta interagencia configurada para el tipo de captación "
                            + cuenta.getTipoCaptacionForma()
                            + " en la agencia de la caja."
            );
        }

        String numeroRC =
                consecutivosComprobantesService.generarNumeroDefinitivo(
                        TIPO_COMPROBANTE,
                        request.getIdAgencia(),
                        idUsuario
                );

        String documentoSoporte =
                construirDocumentoSoporte(request);

        List<MovimientoContableDTO> movimientosRC =
                new ArrayList<>();

        BigDecimal efectivo =
                nvl(request.getValorEfectivo());

        BigDecimal cheques =
                nvl(request.getValorCheques());

        BigDecimal transferencias =
                nvl(request.getValorTransferencias());

        /*
         * Los movimientos interagencia atendidos por Caja
         * se realizan únicamente en efectivo.
         */
        if (cheques.signum() != 0
                || transferencias.signum() != 0
                || (request.getCheques() != null
                && !request.getCheques().isEmpty())) {

            throw new IllegalArgumentException(
                    "Los movimientos interagencia por Caja se realizan únicamente en efectivo."
            );
        }

        if (efectivo.signum() <= 0) {
            throw new IllegalArgumentException(
                    "El valor en efectivo debe ser mayor que cero."
            );
        }

        if (OPERACION_CONSIGNACION.equalsIgnoreCase(
                request.getCodigoOperacion()
        )) {

            /*
             * ENTRADA DE DINERO
             *
             * RC - Agencia de la Caja
             *
             * D Caja
             * C Cuenta temporal interagencia
             */
            movimientosRC.add(
                    crearMovimiento(
                            idCuentaCaja,
                            request,
                            cuenta,
                            efectivo,
                            BigDecimal.ZERO,
                            "Ingreso efectivo interagencia cta "
                                    + valorSeguro(cuenta.getCodigoCuenta())
                                    + " sop. "
                                    + valorSeguro(request.getNumeroComprobante()),
                            documentoSoporte
                    )
            );

            movimientosRC.add(
                    crearMovimiento(
                            idCuentaInteragenciaCaja,
                            request,
                            cuenta,
                            BigDecimal.ZERO,
                            efectivo,
                            "Interagencia consignacion "
                                    + valorSeguro(cuenta.getNombreForma())
                                    + " cta "
                                    + valorSeguro(cuenta.getCodigoCuenta()),
                            documentoSoporte
                    )
            );

        } else if (OPERACION_RETIRO.equalsIgnoreCase(
                request.getCodigoOperacion()
        )) {

            /*
             * SALIDA DE DINERO
             *
             * RC - Agencia de la Caja
             *
             * D Cuenta temporal interagencia
             * C Caja
             */
            movimientosRC.add(
                    crearMovimiento(
                            idCuentaInteragenciaCaja,
                            request,
                            cuenta,
                            efectivo,
                            BigDecimal.ZERO,
                            "Interagencia retiro "
                                    + valorSeguro(cuenta.getNombreForma())
                                    + " cta "
                                    + valorSeguro(cuenta.getCodigoCuenta()),
                            documentoSoporte
                    )
            );

            movimientosRC.add(
                    crearMovimiento(
                            idCuentaCaja,
                            request,
                            cuenta,
                            BigDecimal.ZERO,
                            efectivo,
                            "Salida efectivo interagencia cta "
                                    + valorSeguro(cuenta.getCodigoCuenta())
                                    + " sop. "
                                    + valorSeguro(request.getNumeroComprobante()),
                            documentoSoporte
                    )
            );

        } else {

            throw new IllegalArgumentException(
                    "La operación no está habilitada para contabilización interagencia desde Caja: "
                            + request.getCodigoOperacion()
            );
        }

        validarCuadre(movimientosRC);

        OrigenComprobanteDTO origenRC =
                new OrigenComprobanteDTO();

        origenRC.setOrigenTipo("AUTO");
        origenRC.setModuloOrigen(MODULO_ORIGEN);
        origenRC.setProcesoOrigen(PROCESO_ORIGEN);
        origenRC.setTablaOrigen(TABLA_ORIGEN);
        origenRC.setIdOrigen(request.getIdCuentaAhorro());

        String conceptoRC =
                limitar(
                        "MOVIMIENTO INTERAGENCIA "
                                + (OPERACION_CONSIGNACION.equalsIgnoreCase(
                                request.getCodigoOperacion()
                        )
                                ? "CONSIGNACION "
                                : "RETIRO ")
                                + valorSeguro(cuenta.getNombreForma())
                                + " CTA "
                                + valorSeguro(cuenta.getCodigoCuenta())
                                + " - AGENCIA CUENTA "
                                + valorSeguro(cuenta.getCodigoAgencia())
                                + " - SOP "
                                + valorSeguro(request.getNumeroComprobante())
                                + " - CAJA "
                                + request.getIdCaja()
                                + " - RC "
                                + numeroRC,
                        MAX_CONCEPTO
                );

        contabilidadRegistroService.registrarComprobante(
                request.getIdAgencia(),
                TIPO_COMPROBANTE,
                numeroRC,
                conceptoRC,
                origenRC,
                movimientosRC,
                idUsuario
        );

        /*
         * =========================================================
         * CA - AGENCIA PROPIETARIA DE LA CUENTA
         * =========================================================
         *
         * El RC anterior registra el movimiento físico del dinero
         * en la agencia de la Caja.
         *
         * El CA registra la afectación del producto en la agencia
         * propietaria de la cuenta.
         */

        Integer idCuentaProducto =
                resolverCuentaProducto(cuenta);

        String numeroCA =
                consecutivosComprobantesService.generarNumeroDefinitivo(
                        "CA",
                        cuenta.getIdAgencia(),
                        idUsuario
                );

        List<MovimientoContableDTO> movimientosCA =
                new ArrayList<>();

        if (OPERACION_CONSIGNACION.equalsIgnoreCase(
                request.getCodigoOperacion()
        )) {

            /*
             * ENTRADA DE DINERO
             *
             * CA - Agencia propietaria
             *
             * D Cuenta temporal interagencia
             * C Cuenta real del producto
             */
            movimientosCA.add(
                    crearMovimientoInteragencia(
                            idCuentaInteragenciaPropietaria,
                            cuenta.getIdAgencia(),
                            request,
                            cuenta,
                            efectivo,
                            BigDecimal.ZERO,
                            "Interagencia consignacion "
                                    + valorSeguro(cuenta.getNombreForma())
                                    + " cta "
                                    + valorSeguro(cuenta.getCodigoCuenta()),
                            documentoSoporte
                    )
            );

            movimientosCA.add(
                    crearMovimientoInteragencia(
                            idCuentaProducto,
                            cuenta.getIdAgencia(),
                            request,
                            cuenta,
                            BigDecimal.ZERO,
                            efectivo,
                            "Consignacion "
                                    + valorSeguro(cuenta.getNombreForma())
                                    + " cta "
                                    + valorSeguro(cuenta.getCodigoCuenta())
                                    + " sop. "
                                    + valorSeguro(request.getNumeroComprobante()),
                            documentoSoporte
                    )
            );

        } else {

            /*
             * SALIDA DE DINERO
             *
             * CA - Agencia propietaria
             *
             * D Cuenta real del producto
             * C Cuenta temporal interagencia
             */
            movimientosCA.add(
                    crearMovimientoInteragencia(
                            idCuentaProducto,
                            cuenta.getIdAgencia(),
                            request,
                            cuenta,
                            efectivo,
                            BigDecimal.ZERO,
                            "Retiro "
                                    + valorSeguro(cuenta.getNombreForma())
                                    + " cta "
                                    + valorSeguro(cuenta.getCodigoCuenta())
                                    + " sop. "
                                    + valorSeguro(request.getNumeroComprobante()),
                            documentoSoporte
                    )
            );

            movimientosCA.add(
                    crearMovimientoInteragencia(
                            idCuentaInteragenciaPropietaria,
                            cuenta.getIdAgencia(),
                            request,
                            cuenta,
                            BigDecimal.ZERO,
                            efectivo,
                            "Interagencia retiro "
                                    + valorSeguro(cuenta.getNombreForma())
                                    + " cta "
                                    + valorSeguro(cuenta.getCodigoCuenta()),
                            documentoSoporte
                    )
            );
        }

        validarCuadre(movimientosCA);

        OrigenComprobanteDTO origenCA =
                new OrigenComprobanteDTO();

        origenCA.setOrigenTipo("AUTO");
        origenCA.setModuloOrigen(MODULO_ORIGEN);
        origenCA.setProcesoOrigen(PROCESO_ORIGEN);
        origenCA.setTablaOrigen(TABLA_ORIGEN);
        origenCA.setIdOrigen(request.getIdCuentaAhorro());

        String conceptoCA =
                limitar(
                        "MOVIMIENTO INTERAGENCIA "
                                + (OPERACION_CONSIGNACION.equalsIgnoreCase(
                                request.getCodigoOperacion()
                        )
                                ? "CONSIGNACION "
                                : "RETIRO ")
                                + valorSeguro(cuenta.getNombreForma())
                                + " CTA "
                                + valorSeguro(cuenta.getCodigoCuenta())
                                + " - AGENCIA CAJA "
                                + request.getIdAgencia()
                                + " - SOP "
                                + valorSeguro(request.getNumeroComprobante())
                                + " - CA "
                                + numeroCA,
                        MAX_CONCEPTO
                );

        contabilidadRegistroService.registrarComprobante(
                cuenta.getIdAgencia(),
                "CA",
                numeroCA,
                conceptoCA,
                origenCA,
                movimientosCA,
                idUsuario
        );

        return numeroRC;
    }

    // =========================================================
    // CONSIGNACIÓN
    // =========================================================

    private List<MovimientoContableDTO> construirConsignacion(
            CajaCapturaDepositosRequestDTO request,
            CajaCapturaDepositosCuentaDTO cuenta,
            Integer idCuentaCaja,
            Integer idCuentaProducto,
            String documentoSoporte
    ) {

        BigDecimal efectivo = nvl(request.getValorEfectivo());
        BigDecimal valorCheques = nvl(request.getValorCheques());
        BigDecimal transferencias = nvl(request.getValorTransferencias());

        if (transferencias.signum() != 0) {
            throw new IllegalArgumentException(
                    "Las transferencias no se procesan por Caja."
            );
        }

        if (efectivo.signum() < 0 || valorCheques.signum() < 0) {
            throw new IllegalArgumentException(
                    "Los valores de efectivo y cheques no pueden ser negativos."
            );
        }

        List<CajaCapturaDepositosChequeDTO> cheques =
                request.getCheques() != null
                        ? request.getCheques()
                        : List.of();

        BigDecimal sumaCheques = BigDecimal.ZERO;

        for (CajaCapturaDepositosChequeDTO cheque : cheques) {

            if (cheque == null) {
                throw new IllegalArgumentException(
                        "La lista de cheques contiene un registro vacío."
                );
            }

            BigDecimal valorCheque = nvl(cheque.getValorCheque());

            if (valorCheque.signum() <= 0) {
                throw new IllegalArgumentException(
                        "Cada cheque debe tener un valor mayor que cero."
                );
            }

            sumaCheques = sumaCheques.add(valorCheque);
        }

        if (valorCheques.compareTo(sumaCheques) != 0) {
            throw new IllegalArgumentException(
                    "El valor total de cheques no coincide con la suma "
                            + "de los cheques registrados."
            );
        }

        BigDecimal total = efectivo.add(sumaCheques);

        if (total.signum() <= 0) {
            throw new IllegalArgumentException(
                    "La consignación debe tener un valor mayor que cero."
            );
        }

        List<MovimientoContableDTO> movimientos = new ArrayList<>();

        /*
         * EFECTIVO
         *
         * DÉBITO a la cuenta contable de la Caja.
         */
        if (efectivo.signum() > 0) {

            movimientos.add(
                    crearMovimiento(
                            idCuentaCaja,
                            request,
                            cuenta,
                            efectivo,
                            BigDecimal.ZERO,
                            "Ingreso efectivo caja "
                                    + cuenta.getCodigoCuenta()
                                    + " sop. "
                                    + valorSeguro(request.getNumeroComprobante()),
                            documentoSoporte
                    )
            );
        }

        /*
         * CHEQUES
         *
         * Cada cheque genera su propio auxiliar.
         * Todos afectan la misma cuenta contable de Caja.
         */
        for (CajaCapturaDepositosChequeDTO cheque : cheques) {

            String detalle =
                    "Ingreso cheque "
                            + valorSeguro(cheque.getNumeroCheque())
                            + " banco "
                            + valorSeguro(
                                    cheque.getCodigoBanco() != null
                                            ? cheque.getCodigoBanco().toString()
                                            : null
                            )
                            + " cta "
                            + valorSeguro(cuenta.getCodigoCuenta());

            String soporteCheque =
                    limitar(
                            valorSeguro(cheque.getNumeroCheque()),
                            MAX_DOCUMENTO_SOPORTE
                    );

            movimientos.add(
                    crearMovimiento(
                            idCuentaCaja,
                            request,
                            cuenta,
                            cheque.getValorCheque(),
                            BigDecimal.ZERO,
                            detalle,
                            soporteCheque
                    )
            );
        }

        /*
         * CONTRAPARTIDA DEL PRODUCTO
         *
         * La cuenta de ahorro/aporte/TAC recibe el total completo
         * de la consignación.
         */
        movimientos.add(
                crearMovimiento(
                        idCuentaProducto,
                        request,
                        cuenta,
                        BigDecimal.ZERO,
                        total,
                        "Consignacion "
                                + valorSeguro(cuenta.getNombreForma())
                                + " cta "
                                + valorSeguro(cuenta.getCodigoCuenta())
                                + " sop. "
                                + valorSeguro(request.getNumeroComprobante()),
                        documentoSoporte
                )
        );

        return movimientos;
    }

    // =========================================================
    // RETIRO EN EFECTIVO
    // =========================================================

    private List<MovimientoContableDTO> construirRetiro(
            CajaCapturaDepositosRequestDTO request,
            CajaCapturaDepositosCuentaDTO cuenta,
            Integer idCuentaCaja,
            Integer idCuentaProducto,
            String documentoSoporte
    ) {

        BigDecimal efectivo = nvl(request.getValorEfectivo());
        BigDecimal cheques = nvl(request.getValorCheques());
        BigDecimal transferencias = nvl(request.getValorTransferencias());

        if (cheques.signum() != 0) {
            throw new IllegalArgumentException(
                    "Los retiros por cheque no se procesan por Caja."
            );
        }

        if (transferencias.signum() != 0) {
            throw new IllegalArgumentException(
                    "Las transferencias no se procesan por Caja."
            );
        }

        if (efectivo.signum() <= 0) {
            throw new IllegalArgumentException(
                    "El retiro en efectivo debe tener un valor mayor que cero."
            );
        }

        if (request.getCheques() != null && !request.getCheques().isEmpty()) {
            throw new IllegalArgumentException(
                    "Un retiro de Caja no puede contener cheques."
            );
        }

        List<MovimientoContableDTO> movimientos = new ArrayList<>();

        /*
         * DÉBITO AL PRODUCTO
         */
        movimientos.add(
                crearMovimiento(
                        idCuentaProducto,
                        request,
                        cuenta,
                        efectivo,
                        BigDecimal.ZERO,
                        "Retiro "
                                + valorSeguro(cuenta.getNombreForma())
                                + " cta "
                                + valorSeguro(cuenta.getCodigoCuenta())
                                + " sop. "
                                + valorSeguro(request.getNumeroComprobante()),
                        documentoSoporte
                )
        );

        /*
         * CRÉDITO A CAJA
         */
        movimientos.add(
                crearMovimiento(
                        idCuentaCaja,
                        request,
                        cuenta,
                        BigDecimal.ZERO,
                        efectivo,
                        "Salida efectivo caja "
                                + cuenta.getCodigoCuenta()
                                + " sop. "
                                + valorSeguro(request.getNumeroComprobante()),
                        documentoSoporte
                )
        );

        return movimientos;
    }

    // =========================================================
    // MOVIMIENTO CONTABLE
    // =========================================================

    private MovimientoContableDTO crearMovimiento(
            Integer idCatalogoCuenta,
            CajaCapturaDepositosRequestDTO request,
            CajaCapturaDepositosCuentaDTO cuenta,
            BigDecimal debito,
            BigDecimal credito,
            String detalle,
            String documentoSoporte
    ) {

        MovimientoContableDTO movimiento = new MovimientoContableDTO();

        movimiento.setIdCatalogoCuenta(idCatalogoCuenta);
        movimiento.setIdAgencia(request.getIdAgencia());
        movimiento.setIdDatosPersonal(
                Math.toIntExact(cuenta.getIdDatosPersonal())
        );

        movimiento.setFechaAuxiliar(request.getFechaContable());

        movimiento.setDetalleMovimiento(
                limitar(detalle, MAX_DETALLE)
        );

        movimiento.setValorDebito(nvl(debito));
        movimiento.setValorCredito(nvl(credito));
        movimiento.setValorBase(BigDecimal.ZERO);

        movimiento.setDocumentoSoporte(
                limitar(documentoSoporte, MAX_DOCUMENTO_SOPORTE)
        );

        return movimiento;
    }

    private MovimientoContableDTO crearMovimientoInteragencia(
            Integer idCatalogoCuenta,
            Integer idAgenciaContable,
            CajaCapturaDepositosRequestDTO request,
            CajaCapturaDepositosCuentaDTO cuenta,
            BigDecimal debito,
            BigDecimal credito,
            String detalle,
            String documentoSoporte
    ) {

        MovimientoContableDTO movimiento =
                new MovimientoContableDTO();

        movimiento.setIdCatalogoCuenta(idCatalogoCuenta);
        movimiento.setIdAgencia(idAgenciaContable);

        movimiento.setIdDatosPersonal(
                Math.toIntExact(cuenta.getIdDatosPersonal())
        );

        movimiento.setFechaAuxiliar(
                request.getFechaContable()
        );

        movimiento.setDetalleMovimiento(
                limitar(detalle, MAX_DETALLE)
        );

        movimiento.setValorDebito(
                nvl(debito)
        );

        movimiento.setValorCredito(
                nvl(credito)
        );

        movimiento.setValorBase(
                BigDecimal.ZERO
        );

        movimiento.setDocumentoSoporte(
                limitar(
                        documentoSoporte,
                        MAX_DOCUMENTO_SOPORTE
                )
        );

        return movimiento;
    }

    // =========================================================
    // CUENTA CONTABLE DEL PRODUCTO
    // =========================================================

    private Integer resolverCuentaProducto(
            CajaCapturaDepositosCuentaDTO cuenta
    ) {

        Integer corto = cuenta.getIdCatalogoCuentaCorto();
        Integer largo = cuenta.getIdCatalogoCuentaLargo();

        if (corto == null && largo == null) {
            throw new IllegalStateException(
                    "La forma de ahorro no tiene cuentas contables configuradas."
            );
        }

        /*
         * Si ambas parametrizaciones son iguales, no necesitamos
         * evaluar plazo.
         */
        if (corto != null && corto.equals(largo)) {
            return corto;
        }

        Integer plazo = cuenta.getPlazoCuenta();

        if (plazo == null) {
            throw new IllegalStateException(
                    "La cuenta requiere plazo para determinar "
                            + "la cuenta contable de corto o largo plazo."
            );
        }

        /*
         * Regla TAC:
         * <= 12 meses = corto plazo
         * > 12 meses  = largo plazo
         */
        if (plazo <= 12) {

            if (corto == null) {
                throw new IllegalStateException(
                        "No está configurada la cuenta contable de corto plazo."
                );
            }

            return corto;
        }

        if (largo == null) {
            throw new IllegalStateException(
                    "No está configurada la cuenta contable de largo plazo."
            );
        }

        return largo;
    }

    // =========================================================
    // CONCEPTO CONTABLE
    // =========================================================

    private String construirConcepto(
            CajaCapturaDepositosRequestDTO request,
            CajaCapturaDepositosCuentaDTO cuenta,
            String numeroRC
    ) {

        String operacion =
                OPERACION_CONSIGNACION.equalsIgnoreCase(
                        request.getCodigoOperacion()
                )
                        ? "CONSIGNACION"
                        : "RETIRO";

        String concepto =
                operacion
                        + " "
                        + valorSeguro(cuenta.getNombreForma())
                        + " CTA "
                        + valorSeguro(cuenta.getCodigoCuenta())
                        + " - "
                        + valorSeguro(cuenta.getNombreAsociado())
                        + " DOC "
                        + valorSeguro(cuenta.getDocumento())
                        + " - SOP "
                        + valorSeguro(request.getNumeroComprobante())
                        + " - CAJA "
                        + request.getIdCaja()
                        + " - RC "
                        + numeroRC;

        return limitar(concepto, MAX_CONCEPTO);
    }

    // =========================================================
    // DOCUMENTO SOPORTE
    // =========================================================

    private String construirDocumentoSoporte(
            CajaCapturaDepositosRequestDTO request
    ) {

        return limitar(
                valorSeguro(request.getNumeroComprobante()),
                MAX_DOCUMENTO_SOPORTE
        );
    }

    // =========================================================
    // VALIDACIONES
    // =========================================================

    private void validarDatosBase(
            CajaCapturaDepositosRequestDTO request,
            CajaCapturaDepositosCuentaDTO cuenta,
            Integer idUsuario
    ) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "La solicitud de Caja es obligatoria."
            );
        }

        if (cuenta == null) {
            throw new IllegalArgumentException(
                    "La cuenta de depósitos es obligatoria."
            );
        }

        if (request.getIdCaja() == null) {
            throw new IllegalArgumentException(
                    "La caja es obligatoria."
            );
        }

        if (request.getIdAgencia() == null) {
            throw new IllegalArgumentException(
                    "La agencia es obligatoria."
            );
        }

        if (request.getFechaContable() == null) {
            throw new IllegalArgumentException(
                    "La fecha contable es obligatoria."
            );
        }

        if (request.getIdCuentaAhorro() == null) {
            throw new IllegalArgumentException(
                    "La cuenta de ahorro es obligatoria."
            );
        }

        if (request.getCodigoOperacion() == null
                || request.getCodigoOperacion().isBlank()) {

            throw new IllegalArgumentException(
                    "La operación es obligatoria."
            );
        }

        if (cuenta.getIdDatosPersonal() == null) {
            throw new IllegalStateException(
                    "La cuenta no tiene tercero asociado."
            );
        }

        if (!request.getIdCuentaAhorro().equals(cuenta.getIdCuentaAhorro())) {
            throw new IllegalStateException(
                    "La cuenta consultada no corresponde "
                            + "a la cuenta de la operación."
            );
        }

        if (idUsuario == null) {
            throw new IllegalArgumentException(
                    "El usuario es obligatorio para contabilizar."
            );
        }

        if (request.getNumeroComprobante() == null
                || request.getNumeroComprobante().isBlank()) {

            throw new IllegalArgumentException(
                    "El documento soporte de la operación es obligatorio."
            );
        }
    }

    private void validarCuadre(
            List<MovimientoContableDTO> movimientos
    ) {

        if (movimientos == null || movimientos.isEmpty()) {
            throw new IllegalStateException(
                    "No se generaron movimientos contables."
            );
        }

        BigDecimal debitos = movimientos.stream()
                .map(MovimientoContableDTO::getValorDebito)
                .map(this::nvl)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal creditos = movimientos.stream()
                .map(MovimientoContableDTO::getValorCredito)
                .map(this::nvl)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (debitos.compareTo(creditos) != 0) {
            throw new IllegalStateException(
                    "El comprobante contable no está cuadrado. Débitos: "
                            + debitos
                            + " - Créditos: "
                            + creditos
            );
        }

        if (debitos.signum() <= 0) {
            throw new IllegalStateException(
                    "El comprobante contable no tiene valor contable."
            );
        }
    }

    // =========================================================
    // UTILIDADES
    // =========================================================

    private BigDecimal nvl(BigDecimal valor) {
        return valor != null ? valor : BigDecimal.ZERO;
    }

    private String valorSeguro(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private String limitar(String valor, int longitud) {

        if (valor == null) {
            return null;
        }

        String texto = valor.trim();

        return texto.length() <= longitud
                ? texto
                : texto.substring(0, longitud);
    }
}