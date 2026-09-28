package co.assip.erp.cartera.originacion.desembolso;

import co.assip.erp.cajas.movimientos.CajasMovimientosService;
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

@Service
public class SolicitudDesembolsoContabilizacionService {

    private static final BigDecimal CERO = BigDecimal.ZERO;

    private final SolicitudDesembolsoRepository repository;
    private final CajasMovimientosService cajasMovimientosService;
    private final ContabilidadRegistroService contabilidadRegistroService;
    private final ConsecutivosComprobantesService consecutivosComprobantesService;

    public SolicitudDesembolsoContabilizacionService(
            SolicitudDesembolsoRepository repository,
            CajasMovimientosService cajasMovimientosService,
            ContabilidadRegistroService contabilidadRegistroService,
            ConsecutivosComprobantesService consecutivosComprobantesService
    ) {
        this.repository = repository;
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

        Integer cuentaCaja =
                cajasMovimientosService.obtenerCuentaContableCaja(
                        datos.idCaja()
                );

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
                    "Desembolso crédito "
                            + credito.pagareCartera()
                            + " - abono cuenta "
                            + deposito.idCuentaAhorro(),
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

        String concepto =
                "Desembolso crédito "
                        + credito.pagareCartera();

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

        String concepto =
                "Cobros desembolso crédito "
                        + credito.pagareCartera();

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

        if (datos.idCaja() == null
                || datos.idCaja() <= 0) {

            throw new IllegalArgumentException(
                    "Debe seleccionar la caja que atenderá "
                            + "el desembolso."
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