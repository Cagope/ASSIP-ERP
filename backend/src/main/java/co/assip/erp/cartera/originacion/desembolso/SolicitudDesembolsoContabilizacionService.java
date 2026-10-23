package co.assip.erp.cartera.originacion.desembolso;

import co.assip.erp.cajas.movimientos.CajasMovimientosService;
import co.assip.erp.cartera.originacion.desembolso.dto.ComprobanteDesembolsoPreviewDTO;
import co.assip.erp.contabilidad.auxiliares_contables.dto.MovimientoContablePreviewDTO;
import co.assip.erp.contabilidad.plan_cuentas.PlanCuenta;
import co.assip.erp.contabilidad.plan_cuentas.PlanCuentaRepository;
import co.assip.erp.contabilidad.auxiliares_contables.ContabilidadRegistroService;
import co.assip.erp.contabilidad.auxiliares_contables.dto.MovimientoContableDTO;
import co.assip.erp.contabilidad.consecutivos_comprobantes.ConsecutivosComprobantesService;
import co.assip.erp.contabilidad.origen_comprobantes.dto.OrigenComprobanteDTO;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.text.NumberFormat;
import java.util.Locale;

@Service
public class SolicitudDesembolsoContabilizacionService {

    private static final BigDecimal CERO = BigDecimal.ZERO;

    private final SolicitudDesembolsoRepository repository;
    private final PlanCuentaRepository planCuentaRepository;
    private final CajasMovimientosService cajasMovimientosService;
    private final ContabilidadRegistroService contabilidadRegistroService;
    private final ConsecutivosComprobantesService consecutivosComprobantesService;

    public SolicitudDesembolsoContabilizacionService(
            SolicitudDesembolsoRepository repository,
            PlanCuentaRepository planCuentaRepository,
            CajasMovimientosService cajasMovimientosService,
            ContabilidadRegistroService contabilidadRegistroService,
            ConsecutivosComprobantesService consecutivosComprobantesService
    ) {
        this.repository = repository;
        this.planCuentaRepository = planCuentaRepository;
        this.cajasMovimientosService = cajasMovimientosService;
        this.contabilidadRegistroService = contabilidadRegistroService;
        this.consecutivosComprobantesService =
                consecutivosComprobantesService;
    }

    // =========================================================
    // CONTABILIZACIÓN DEL DESEMBOLSO
    // =========================================================

    @Transactional
    public ResultadoContabilizacion contabilizar(
            DatosContabilizacion datos
    ) {

        validarDatos(datos);

        SolicitudDesembolsoRepository.CreditoDesembolsoDatos credito =
                repository.buscarCredito(datos.idCarteraCredito())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "No existe el crédito "
                                                + datos.idCarteraCredito()
                                                + "."
                                )
                        );

        if (!"P".equals(credito.codigoEstadoCartera())) {
            throw new IllegalStateException(
                    "El crédito "
                            + credito.idCarteraCredito()
                            + " no se encuentra pendiente de desembolso."
            );
        }

        if (!credito.idAgencia().equals(datos.idAgencia())) {
            throw new IllegalArgumentException(
                    "La agencia del crédito no coincide con la agencia "
                            + "de la contabilización."
            );
        }

        Integer cuentaCaja = null;

        if (datos.valorEfectivo().compareTo(CERO) > 0
                || datos.totalCobros().compareTo(CERO) > 0) {

            cuentaCaja =
                    cajasMovimientosService.obtenerCuentaContableCaja(
                            datos.idCaja()
                    );
        }

        String numeroComprobanteDesembolso =
                contabilizarDesembolso(
                        credito,
                        cuentaCaja,
                        datos
                );

        String numeroComprobanteCobros = null;

        if (datos.totalCobros().compareTo(CERO) > 0) {

            numeroComprobanteCobros =
                    contabilizarCobros(
                            credito,
                            cuentaCaja,
                            datos
                    );
        }

        return new ResultadoContabilizacion(
                datos.tipoComprobanteDesembolso(),
                numeroComprobanteDesembolso,
                datos.totalCobros().compareTo(CERO) > 0
                        ? datos.tipoComprobanteCobros()
                        : null,
                numeroComprobanteCobros
        );
    }

    /** Construye los mismos movimientos de la ejecución, sin generar consecutivos ni registrar. */
    @Transactional(readOnly = true)
    public VistaPreviaMovimientos verificarMovimientos(DatosContabilizacion datos) {
        validarDatos(datos);
        var credito = repository.buscarCredito(datos.idCarteraCredito())
                .orElseThrow(() -> new IllegalArgumentException("No existe el crédito."));
        if (!"P".equals(credito.codigoEstadoCartera())) {
            throw new IllegalStateException("El crédito no está pendiente de desembolso.");
        }
        if (!credito.idAgencia().equals(datos.idAgencia())) {
            throw new IllegalArgumentException("La agencia del crédito no coincide.");
        }
        Integer cuentaCaja = null;
        if (datos.valorEfectivo().signum() > 0 || datos.totalCobros().signum() > 0) {
            cuentaCaja = cajasMovimientosService.obtenerCuentaContableCaja(datos.idCaja());
        }
        var desembolso = construirMovimientosDesembolso(credito, cuentaCaja, datos);
        List<MovimientoContableDTO> cobros = List.of();
        if (datos.totalCobros().signum() > 0) {
            if (datos.tipoComprobanteCobros() == null || datos.tipoComprobanteCobros().isBlank()) {
                throw new IllegalArgumentException("El tipo de comprobante de cobros es obligatorio.");
            }
            cobros = construirMovimientosCobros(credito, cuentaCaja, datos);
        }
        return new VistaPreviaMovimientos(
                construirConceptoDesembolso(credito, datos, desembolso),
                desembolso,
                cobros.isEmpty() ? null : construirConceptoCobros(credito, datos),
                cobros
        );
    }

    public record VistaPreviaMovimientos(
            String conceptoDesembolso,
            List<MovimientoContableDTO> movimientosDesembolso,
            String conceptoCobros,
            List<MovimientoContableDTO> movimientosCobros
    ) {}

    /** Vista previa con cuentas legibles y totales; no registra movimientos. */
    @Transactional(readOnly = true)
    public List<ComprobanteDesembolsoPreviewDTO> verificarComprobantes(DatosContabilizacion datos) {
        VistaPreviaMovimientos vista = verificarMovimientos(datos);
        List<ComprobanteDesembolsoPreviewDTO> comprobantes = new ArrayList<>();
        comprobantes.add(construirComprobantePreview(
                datos.idAgencia(), datos.tipoComprobanteDesembolso(),
                vista.conceptoDesembolso(), vista.movimientosDesembolso()));
        if (!vista.movimientosCobros().isEmpty()) {
            comprobantes.add(construirComprobantePreview(
                    datos.idAgencia(), datos.tipoComprobanteCobros(),
                    vista.conceptoCobros(), vista.movimientosCobros()));
        }
        return List.copyOf(comprobantes);
    }

    private ComprobanteDesembolsoPreviewDTO construirComprobantePreview(
            Integer idAgencia, String tipoComprobante, String concepto,
            List<MovimientoContableDTO> movimientos) {
        List<MovimientoContablePreviewDTO> detalle = new ArrayList<>();
        BigDecimal debitos = CERO;
        BigDecimal creditos = CERO;
        for (MovimientoContableDTO movimiento : movimientos) {
            PlanCuenta cuenta = planCuentaRepository.findById(movimiento.getIdCatalogoCuenta())
                    .orElseThrow(() -> new IllegalStateException(
                            "No existe la cuenta contable " + movimiento.getIdCatalogoCuenta() + "."));
            if (!idAgencia.equals(cuenta.getIdAgencia())) {
                throw new IllegalStateException("La cuenta " + cuenta.getCodigoCuenta()
                        + " no pertenece a la agencia del desembolso.");
            }
            MovimientoContablePreviewDTO linea = new MovimientoContablePreviewDTO();
            linea.setIdAgencia(idAgencia);
            linea.setFechaAuxiliar(movimiento.getFechaAuxiliar());
            linea.setIdCatalogoCuenta(cuenta.getId());
            linea.setCodigoCuenta(cuenta.getCodigoCuenta());
            linea.setNombreCuenta(cuenta.getNombre());
            linea.setIdDatosPersonal(movimiento.getIdDatosPersonal());
            linea.setDetalleMovimiento(movimiento.getDetalleMovimiento());
            linea.setValorDebito(nvl(movimiento.getValorDebito()));
            linea.setValorCredito(nvl(movimiento.getValorCredito()));
            debitos = debitos.add(linea.getValorDebito());
            creditos = creditos.add(linea.getValorCredito());
            detalle.add(linea);
        }
        BigDecimal diferencia = debitos.subtract(creditos);
        return new ComprobanteDesembolsoPreviewDTO(
                tipoComprobante, concepto, List.copyOf(detalle),
                debitos, creditos, diferencia, diferencia.signum() == 0);
    }

    // =========================================================
    // COMPROBANTE 1 - DESEMBOLSO
    // =========================================================

    private String contabilizarDesembolso(
            SolicitudDesembolsoRepository.CreditoDesembolsoDatos credito,
            Integer cuentaCaja,
            DatosContabilizacion datos
    ) {

        String numeroComprobante =
                consecutivosComprobantesService
                        .generarNumeroDefinitivo(
                                datos.tipoComprobanteDesembolso(),
                                datos.idAgencia(),
                                datos.idUsuario()
                        );

        List<MovimientoContableDTO> movimientos = construirMovimientosDesembolso(credito, cuentaCaja, datos);

        String concepto = construirConceptoDesembolso(credito, datos, movimientos);

        contabilidadRegistroService.registrarComprobante(
                datos.idAgencia(),
                datos.tipoComprobanteDesembolso(),
                numeroComprobante,
                concepto,
                construirOrigen(credito.idCarteraCredito()),
                movimientos,
                datos.idUsuario()
        );

        return numeroComprobante;
    }

    private List<MovimientoContableDTO> construirMovimientosDesembolso(
            SolicitudDesembolsoRepository.CreditoDesembolsoDatos credito,
            Integer cuentaCaja,
            DatosContabilizacion datos
    ) {
        List<MovimientoContableDTO> movimientos =
                new ArrayList<>();

        // ---------------------------------------------------------
        // DÉBITO - CAPITAL DE CARTERA
        // ---------------------------------------------------------

        agregarMovimiento(
                movimientos,
                datos.idCuentaCapital(),
                credito.idDatosPersonal(),
                datos.fechaContable(),
                "Desembolso crédito "
                        + credito.pagareCartera()
                        + " - capital",
                datos.valorCredito(),
                CERO,
                datos.valorCredito(),
                credito.pagareCartera()
        );

        // ---------------------------------------------------------
        // CRÉDITO - FONDO DE GARANTÍAS
        // ---------------------------------------------------------

        if (datos.valorFondoGarantia().compareTo(CERO) > 0) {

            validarCuenta(
                    credito.idCuentaPasivoFondo(),
                    "Fondo de garantías"
            );

            agregarMovimiento(
                    movimientos,
                    credito.idCuentaPasivoFondo(),
                    credito.idDatosPersonal(),
                    datos.fechaContable(),
                    "Desembolso crédito "
                            + credito.pagareCartera()
                            + " - fondo de garantías",
                    CERO,
                    datos.valorFondoGarantia(),
                    datos.valorFondoGarantia(),
                    credito.pagareCartera()
            );
        }

        // ---------------------------------------------------------
        // CRÉDITO - IVA FONDO DE GARANTÍAS
        // ---------------------------------------------------------

        if (datos.valorIvaFondo().compareTo(CERO) > 0) {

            validarCuenta(
                    credito.idCuentaIvaFondo(),
                    "IVA fondo de garantías"
            );

            agregarMovimiento(
                    movimientos,
                    credito.idCuentaIvaFondo(),
                    credito.idDatosPersonal(),
                    datos.fechaContable(),
                    "Desembolso crédito "
                            + credito.pagareCartera()
                            + " - IVA fondo de garantías",
                    CERO,
                    datos.valorIvaFondo(),
                    datos.valorIvaFondo(),
                    credito.pagareCartera()
            );
        }

        // ---------------------------------------------------------
        // CRÉDITO - EFECTIVO
        // ---------------------------------------------------------

        if (datos.valorEfectivo().compareTo(CERO) > 0) {

            agregarMovimiento(
                    movimientos,
                    cuentaCaja,
                    credito.idDatosPersonal(),
                    datos.fechaContable(),
                    "Desembolso crédito "
                            + credito.pagareCartera()
                            + " - efectivo",
                    CERO,
                    datos.valorEfectivo(),
                    datos.valorEfectivo(),
                    credito.pagareCartera()
            );
        }

        // ---------------------------------------------------------
        // CRÉDITOS - CUENTAS DE AHORRO
        // ---------------------------------------------------------

        for (DepositoContabilizacion deposito : datos.depositos()) {

            if (deposito.valor().compareTo(CERO) <= 0) {
                continue;
            }

            SolicitudDesembolsoRepository.CuentaAhorroDesembolsoDatos
                    cuentaAhorro =
                    repository.buscarCuentaAhorroDesembolso(
                                    deposito.idCuentaAhorro()
                            )
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "No existe la cuenta de ahorro "
                                                    + deposito.idCuentaAhorro()
                                                    + "."
                                    )
                            );

            validarCuenta(
                    cuentaAhorro.idCatalogoCuenta(),
                    "Cuenta contable de ahorro "
                            + deposito.idCuentaAhorro()
            );

            agregarMovimiento(
                    movimientos,
                    cuentaAhorro.idCatalogoCuenta(),
                    credito.idDatosPersonal(),
                    datos.fechaContable(),
                    "ABONO AHORROS CTA " + cuentaAhorro.codigoCuenta()
                            + " - PAGARÉ " + credito.pagareCartera(),
                    CERO,
                    deposito.valor(),
                    deposito.valor(),
                    credito.pagareCartera()
            );
        }

        // ---------------------------------------------------------
        // CRÉDITO - CUENTA POR PAGAR
        // ---------------------------------------------------------

        if (datos.valorCuentaPorPagar().compareTo(CERO) > 0) {

            validarCuenta(
                    datos.idCuentaPorPagar(),
                    "Cuenta por pagar"
            );

            agregarMovimiento(
                    movimientos,
                    datos.idCuentaPorPagar(),
                    credito.idDatosPersonal(),
                    datos.fechaContable(),
                    "Desembolso crédito "
                            + credito.pagareCartera()
                            + " - cuenta por pagar",
                    CERO,
                    datos.valorCuentaPorPagar(),
                    datos.valorCuentaPorPagar(),
                    credito.pagareCartera()
            );
        }

        validarCuadre(
                movimientos,
                "desembolso"
        );

        return movimientos;
    }

    // =========================================================
    // COMPROBANTE 2 - COBROS
    // =========================================================

    private String contabilizarCobros(
            SolicitudDesembolsoRepository.CreditoDesembolsoDatos credito,
            Integer cuentaCaja,
            DatosContabilizacion datos
    ) {

        if (datos.tipoComprobanteCobros() == null
                || datos.tipoComprobanteCobros().isBlank()) {

            throw new IllegalArgumentException(
                    "El tipo de comprobante de cobros es obligatorio."
            );
        }

        String numeroComprobante =
                consecutivosComprobantesService
                        .generarNumeroDefinitivo(
                                datos.tipoComprobanteCobros(),
                                datos.idAgencia(),
                                datos.idUsuario()
                        );

        List<MovimientoContableDTO> movimientos = construirMovimientosCobros(credito, cuentaCaja, datos);

        String concepto = construirConceptoCobros(credito, datos);

        contabilidadRegistroService.registrarComprobante(
                datos.idAgencia(),
                datos.tipoComprobanteCobros(),
                numeroComprobante,
                concepto,
                construirOrigen(credito.idCarteraCredito()),
                movimientos,
                datos.idUsuario()
        );

        return numeroComprobante;
    }

    private List<MovimientoContableDTO> construirMovimientosCobros(
            SolicitudDesembolsoRepository.CreditoDesembolsoDatos credito,
            Integer cuentaCaja,
            DatosContabilizacion datos
    ) {
        List<MovimientoContableDTO> movimientos =
                new ArrayList<>();

        // ---------------------------------------------------------
        // DÉBITO - CAJA QUE ATIENDE EL DESEMBOLSO
        // ---------------------------------------------------------

        agregarMovimiento(
                movimientos,
                cuentaCaja,
                credito.idDatosPersonal(),
                datos.fechaContable(),
                "Cobros desembolso crédito "
                        + credito.pagareCartera()
                        + " - caja",
                datos.totalCobros(),
                CERO,
                datos.totalCobros(),
                credito.pagareCartera()
        );

        // ---------------------------------------------------------
        // CRÉDITO - CENTRAL DE RIESGO
        // ---------------------------------------------------------

        if (datos.valorCentralRiesgo().compareTo(CERO) > 0) {

            validarCuenta(
                    datos.idCuentaCentralRiesgo(),
                    "Central de riesgo"
            );

            agregarMovimiento(
                    movimientos,
                    datos.idCuentaCentralRiesgo(),
                    credito.idDatosPersonal(),
                    datos.fechaContable(),
                    "Cobros desembolso crédito "
                            + credito.pagareCartera()
                            + " - central de riesgo",
                    CERO,
                    datos.valorCentralRiesgo(),
                    datos.valorCentralRiesgo(),
                    credito.pagareCartera()
            );
        }

        // ---------------------------------------------------------
        // CRÉDITO - PAPELERÍA
        // ---------------------------------------------------------

        if (datos.valorPapeleria().compareTo(CERO) > 0) {

            validarCuenta(
                    datos.idCuentaPapeleria(),
                    "Papelería"
            );

            agregarMovimiento(
                    movimientos,
                    datos.idCuentaPapeleria(),
                    credito.idDatosPersonal(),
                    datos.fechaContable(),
                    "Cobros desembolso crédito "
                            + credito.pagareCartera()
                            + " - papelería",
                    CERO,
                    datos.valorPapeleria(),
                    datos.valorPapeleria(),
                    credito.pagareCartera()
            );
        }

        // ---------------------------------------------------------
        // CRÉDITO - IVA PAPELERÍA
        // ---------------------------------------------------------

        if (datos.valorIvaPapeleria().compareTo(CERO) > 0) {

            validarCuenta(
                    datos.idCuentaIva(),
                    "IVA papelería"
            );

            agregarMovimiento(
                    movimientos,
                    datos.idCuentaIva(),
                    credito.idDatosPersonal(),
                    datos.fechaContable(),
                    "Cobros desembolso crédito "
                            + credito.pagareCartera()
                            + " - IVA papelería",
                    CERO,
                    datos.valorIvaPapeleria(),
                    datos.valorIvaPapeleria(),
                    credito.pagareCartera()
            );
        }

        // ---------------------------------------------------------
        // CRÉDITO - APORTES
        // ---------------------------------------------------------

        if (datos.valorAportes().compareTo(CERO) > 0) {

            if (credito.idCuentaAportes() == null
                    || credito.idCuentaAportes() <= 0) {

                throw new IllegalStateException(
                        "El crédito no tiene configurada la cuenta "
                                + "de aportes."
                );
            }

            SolicitudDesembolsoRepository.CuentaAhorroDesembolsoDatos
                    cuentaAportes =
                    repository.buscarCuentaAhorroDesembolso(
                                    credito.idCuentaAportes()
                            )
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "No fue posible obtener la cuenta "
                                                    + "de aportes "
                                                    + credito.idCuentaAportes()
                                                    + "."
                                    )
                            );

            validarCuenta(
                    cuentaAportes.idCatalogoCuenta(),
                    "Cuenta contable de aportes"
            );

            agregarMovimiento(
                    movimientos,
                    cuentaAportes.idCatalogoCuenta(),
                    credito.idDatosPersonal(),
                    datos.fechaContable(),
                    "Cobros desembolso crédito "
                            + credito.pagareCartera()
                            + " - aportes",
                    CERO,
                    datos.valorAportes(),
                    datos.valorAportes(),
                    credito.pagareCartera()
            );
        }

        validarCuadre(
                movimientos,
                "cobros del desembolso"
        );

        return movimientos;
    }

    // =========================================================
    // CONCEPTOS DE LOS COMPROBANTES
    // =========================================================

    private String encabezadoConcepto(
            SolicitudDesembolsoRepository.CreditoDesembolsoDatos credito
    ) {
        return "LÍNEA " + credito.codigoLineaCredito()
                + " - PAGARÉ " + credito.pagareCartera()
                + " - ASOCIADO " + credito.documento()
                + " " + credito.nombreCompleto();
    }

    private String moneda(BigDecimal valor) {
        NumberFormat formato = NumberFormat.getNumberInstance(new Locale("es", "CO"));
        formato.setMinimumFractionDigits(0);
        formato.setMaximumFractionDigits(2);
        return "$" + formato.format(nvl(valor));
    }

    private String construirConceptoDesembolso(
            SolicitudDesembolsoRepository.CreditoDesembolsoDatos credito,
            DatosContabilizacion datos,
            List<MovimientoContableDTO> movimientos
    ) {
        StringBuilder concepto = new StringBuilder("DESEMBOLSO CRÉDITO ")
                .append(encabezadoConcepto(credito))
                .append(" - VALOR CRÉDITO ").append(moneda(datos.valorCredito()));

        for (MovimientoContableDTO movimiento : movimientos) {
            if (movimiento.getDetalleMovimiento().startsWith("ABONO AHORROS CTA ")) {
                String numeroCuenta = movimiento.getDetalleMovimiento()
                        .substring("ABONO AHORROS CTA ".length())
                        .split(" - PAGARÉ ", 2)[0];
                concepto.append(" - ABONO AHORROS CTA ").append(numeroCuenta)
                        .append(" ").append(moneda(movimiento.getValorCredito()));
            }
        }
        if (datos.valorEfectivo().signum() > 0) {
            concepto.append(" - EFECTIVO CAJA ").append(moneda(datos.valorEfectivo()));
        }
        if (datos.valorCuentaPorPagar().signum() > 0) {
            concepto.append(" - CUENTA POR PAGAR ").append(moneda(datos.valorCuentaPorPagar()));
        }
        if (datos.valorFondoGarantia().signum() > 0) {
            concepto.append(" - FONDO GARANTÍAS ").append(moneda(datos.valorFondoGarantia()));
        }
        if (datos.valorIvaFondo().signum() > 0) {
            concepto.append(" - IVA FONDO GARANTÍAS ").append(moneda(datos.valorIvaFondo()));
        }
        return concepto.toString();
    }

    private String construirConceptoCobros(
            SolicitudDesembolsoRepository.CreditoDesembolsoDatos credito,
            DatosContabilizacion datos
    ) {
        StringBuilder concepto = new StringBuilder("RECAUDO COBROS DESEMBOLSO CRÉDITO ")
                .append(encabezadoConcepto(credito));
        if (datos.valorCentralRiesgo().signum() > 0) {
            concepto.append(" - CENTRAL DE RIESGO ").append(moneda(datos.valorCentralRiesgo()));
        }
        if (datos.valorPapeleria().signum() > 0) {
            concepto.append(" - PAPELERÍA ").append(moneda(datos.valorPapeleria()));
        }
        if (datos.valorIvaPapeleria().signum() > 0) {
            concepto.append(" - IVA PAPELERÍA ").append(moneda(datos.valorIvaPapeleria()));
        }
        if (datos.valorAportes().signum() > 0) {
            concepto.append(" - APORTES ").append(moneda(datos.valorAportes()));
        }
        return concepto.append(" - TOTAL RECAUDADO ").append(moneda(datos.totalCobros()))
                .append(" - EFECTIVO CAJA").toString();
    }

    // =========================================================
    // MOVIMIENTO
    // =========================================================

    private void agregarMovimiento(
            List<MovimientoContableDTO> movimientos,
            Integer idCatalogoCuenta,
            Integer idDatosPersonal,
            LocalDate fechaContable,
            String detalle,
            BigDecimal debito,
            BigDecimal credito,
            BigDecimal base,
            String documentoSoporte
    ) {

        validarCuenta(
                idCatalogoCuenta,
                detalle
        );

        MovimientoContableDTO movimiento =
                new MovimientoContableDTO();

        movimiento.setIdCatalogoCuenta(idCatalogoCuenta);
        movimiento.setIdDatosPersonal(idDatosPersonal);
        movimiento.setFechaAuxiliar(fechaContable);
        movimiento.setDetalleMovimiento(detalle);

        movimiento.setValorDebito(nvl(debito));
        movimiento.setValorCredito(nvl(credito));
        movimiento.setValorBase(nvl(base));

        movimiento.setDocumentoSoporte(documentoSoporte);

        movimientos.add(movimiento);
    }

    // =========================================================
    // ORIGEN
    // =========================================================

    private OrigenComprobanteDTO construirOrigen(
            Integer idCarteraCredito
    ) {

        OrigenComprobanteDTO origen =
                new OrigenComprobanteDTO();

        origen.setOrigenTipo("AUTO");
        origen.setModuloOrigen("CARTERA");
        origen.setProcesoOrigen("DESEMBOLSO_CREDITO");
        origen.setTablaOrigen("cartera.carteras_creditos");
        origen.setIdOrigen(idCarteraCredito.longValue());

        return origen;
    }

    // =========================================================
    // VALIDACIONES
    // =========================================================

    private void validarDatos(
            DatosContabilizacion datos
    ) {

        if (datos == null) {
            throw new IllegalArgumentException(
                    "Los datos de contabilización son obligatorios."
            );
        }

        if (datos.idCarteraCredito() == null
                || datos.idCarteraCredito() <= 0) {

            throw new IllegalArgumentException(
                    "El crédito es obligatorio."
            );
        }

        if (datos.idAgencia() == null
                || datos.idAgencia() <= 0) {

            throw new IllegalArgumentException(
                    "La agencia es obligatoria."
            );
        }

        boolean requiereCaja =
                nvl(datos.valorEfectivo()).compareTo(CERO) > 0
                        || nvl(datos.totalCobros()).compareTo(CERO) > 0;

        if (requiereCaja
                && (datos.idCaja() == null || datos.idCaja() <= 0)) {

            throw new IllegalArgumentException(
                    "Debe seleccionar una caja cuando exista desembolso "
                            + "en efectivo o cobros asociados."
            );
        }

        if (datos.fechaContable() == null) {
            throw new IllegalArgumentException(
                    "La fecha contable es obligatoria."
            );
        }

        if (datos.idUsuario() == null
                || datos.idUsuario() <= 0) {

            throw new IllegalArgumentException(
                    "El usuario es obligatorio."
            );
        }

        if (datos.tipoComprobanteDesembolso() == null
                || datos.tipoComprobanteDesembolso().isBlank()) {

            throw new IllegalArgumentException(
                    "El tipo de comprobante de desembolso "
                            + "es obligatorio."
            );
        }

        if (datos.idCuentaCapital() == null
                || datos.idCuentaCapital() <= 0) {

            throw new IllegalArgumentException(
                    "La cuenta contable de capital es obligatoria."
            );
        }

        validarNoNegativo(
                datos.valorCredito(),
                "Valor del crédito"
        );

        validarNoNegativo(
                datos.valorFondoGarantia(),
                "Fondo de garantías"
        );

        validarNoNegativo(
                datos.valorIvaFondo(),
                "IVA fondo de garantías"
        );

        validarNoNegativo(
                datos.valorEfectivo(),
                "Efectivo"
        );

        validarNoNegativo(
                datos.valorCuentaPorPagar(),
                "Cuenta por pagar"
        );

        validarNoNegativo(
                datos.valorCentralRiesgo(),
                "Central de riesgo"
        );

        validarNoNegativo(
                datos.valorPapeleria(),
                "Papelería"
        );

        validarNoNegativo(
                datos.valorIvaPapeleria(),
                "IVA papelería"
        );

        validarNoNegativo(
                datos.valorAportes(),
                "Aportes"
        );

        if (datos.valorCredito().compareTo(CERO) <= 0) {
            throw new IllegalArgumentException(
                    "El valor del crédito debe ser mayor que cero."
            );
        }

        if (datos.depositos() == null) {
            throw new IllegalArgumentException(
                    "La lista de depósitos no puede ser nula."
            );
        }

        for (DepositoContabilizacion deposito : datos.depositos()) {

            if (deposito == null) {
                throw new IllegalArgumentException(
                        "La distribución contiene un depósito inválido."
                );
            }

            if (deposito.idCuentaAhorro() == null
                    || deposito.idCuentaAhorro() <= 0) {

                throw new IllegalArgumentException(
                        "La cuenta de ahorro del depósito "
                                + "es obligatoria."
                );
            }

            validarNoNegativo(
                    deposito.valor(),
                    "Valor del depósito"
            );
        }

        BigDecimal totalCobrosCalculado =
                nvl(datos.valorCentralRiesgo())
                        .add(nvl(datos.valorPapeleria()))
                        .add(nvl(datos.valorIvaPapeleria()))
                        .add(nvl(datos.valorAportes()));

        if (totalCobrosCalculado.compareTo(
                nvl(datos.totalCobros())
        ) != 0) {

            throw new IllegalStateException(
                    "El total de cobros no coincide con el detalle."
            );
        }
    }

    private void validarCuadre(
            List<MovimientoContableDTO> movimientos,
            String proceso
    ) {

        BigDecimal debitos =
                movimientos.stream()
                        .map(MovimientoContableDTO::getValorDebito)
                        .map(this::nvl)
                        .reduce(CERO, BigDecimal::add);

        BigDecimal creditos =
                movimientos.stream()
                        .map(MovimientoContableDTO::getValorCredito)
                        .map(this::nvl)
                        .reduce(CERO, BigDecimal::add);

        if (debitos.compareTo(creditos) != 0) {

            throw new IllegalStateException(
                    "El comprobante de "
                            + proceso
                            + " no está cuadrado. Débitos: "
                            + debitos
                            + ", créditos: "
                            + creditos
                            + "."
            );
        }
    }

    private void validarCuenta(
            Integer idCatalogoCuenta,
            String concepto
    ) {

        if (idCatalogoCuenta == null
                || idCatalogoCuenta <= 0) {

            throw new IllegalStateException(
                    "No existe cuenta contable configurada para "
                            + concepto
                            + "."
            );
        }
    }

    private void validarNoNegativo(
            BigDecimal valor,
            String concepto
    ) {

        if (nvl(valor).compareTo(CERO) < 0) {

            throw new IllegalArgumentException(
                    concepto
                            + " no puede ser negativo."
            );
        }
    }

    private BigDecimal nvl(
            BigDecimal valor
    ) {
        return valor != null
                ? valor
                : CERO;
    }

    // =========================================================
    // DATOS DE ENTRADA
    // =========================================================

    public record DatosContabilizacion(

            Integer idCarteraCredito,
            Integer idAgencia,
            Long idCaja,

            LocalDate fechaContable,
            Integer idUsuario,

            String tipoComprobanteDesembolso,
            String tipoComprobanteCobros,

            Integer idCuentaCapital,
            Integer idCuentaCentralRiesgo,
            Integer idCuentaPapeleria,
            Integer idCuentaIva,
            Integer idCuentaPorPagar,

            BigDecimal valorCredito,

            BigDecimal valorFondoGarantia,
            BigDecimal valorIvaFondo,

            BigDecimal valorEfectivo,
            List<DepositoContabilizacion> depositos,
            BigDecimal valorCuentaPorPagar,

            BigDecimal valorCentralRiesgo,
            BigDecimal valorPapeleria,
            BigDecimal valorIvaPapeleria,
            BigDecimal valorAportes,

            BigDecimal totalCobros
    ) {
    }

    public record DepositoContabilizacion(
            Integer idCuentaAhorro,
            BigDecimal valor
    ) {
    }

    // =========================================================
    // RESULTADO
    // =========================================================

    public record ResultadoContabilizacion(
            String tipoComprobanteDesembolso,
            String numeroComprobanteDesembolso,
            String tipoComprobanteCobros,
            String numeroComprobanteCobros
    ) {
    }
}
