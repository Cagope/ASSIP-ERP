package co.assip.erp.cartera.originacion.desembolso;

import co.assip.erp.depositos.movimientos.DepositosMovimientoService;
import co.assip.erp.depositos.movimientos.dto.DepositosMovimientoDTO;

import co.assip.erp.seguridad.sesion.UsuarioSesionService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.assip.erp.cajas.movimientos.CajasMovimientosService;

import co.assip.erp.tesoreria.operaciones_pendientes.TesoreriaOperacionesPendientesService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Service
public class SolicitudDesembolsoService {

    private static final String ESTADO_PENDIENTE = "P";

    private static final String EDAD_A = "A";

    private static final String MODIFICACION_NINGUNA = "4";
    private static final String MODIFICACION_NOVADO = "3";

    private static final BigDecimal CIEN =
            new BigDecimal("100");

    private final SolicitudDesembolsoRepository repository;
    private final UsuarioSesionService usuarioSesionService;

    private final SolicitudDesembolsoContabilizacionService
            contabilizacionService;

    private final DepositosMovimientoService
            depositosMovimientoService;

    private final CajasMovimientosService
            cajasMovimientosService;

    private final TesoreriaOperacionesPendientesService
            tesoreriaOperacionesPendientesService;

    private static final String CONCEPTO_CENTRAL_RIESGO =
            "CENTRAL_RIESGO";

    private static final String CONCEPTO_PAPELERIA =
            "PAPELERIA";

    private static final String CONCEPTO_IVA =
            "IVA";

    private static final String CONCEPTO_CUENTA_POR_PAGAR =
            "CXP";

    public SolicitudDesembolsoService(
            SolicitudDesembolsoRepository repository,
            UsuarioSesionService usuarioSesionService,
            SolicitudDesembolsoContabilizacionService contabilizacionService,
            DepositosMovimientoService depositosMovimientoService,
            CajasMovimientosService cajasMovimientosService,
            TesoreriaOperacionesPendientesService tesoreriaOperacionesPendientesService
    ) {
        this.repository = repository;
        this.usuarioSesionService = usuarioSesionService;
        this.contabilizacionService = contabilizacionService;
        this.depositosMovimientoService = depositosMovimientoService;
        this.cajasMovimientosService = cajasMovimientosService;
        this.tesoreriaOperacionesPendientesService =
                tesoreriaOperacionesPendientesService;
    }

    // =========================================================
    // BANDEJA
    // =========================================================

    @Transactional(readOnly = true)
    public List<CreditoPendienteDesembolso> listarPendientes() {

        return repository
                .listarPendientes()
                .stream()
                .filter(item ->
                        usuarioSesionService.perteneceA(
                                item.idAgencia()
                        )
                )
                .map(item ->
                        new CreditoPendienteDesembolso(
                                item.idCarteraCredito(),
                                item.idSolicitudCredito(),

                                item.idAgencia(),
                                item.idLineaCredito(),
                                item.idDatosPersonal(),

                                item.pagareCartera(),

                                item.documento(),
                                item.nombreCompleto(),

                                item.codigoLineaCredito(),
                                item.nombreLineaCredito(),

                                normalizar(
                                        item.valorInicialCredito()
                                ),

                                item.plazo(),

                                item.fechaInclusionSistema(),
                                item.fechaPrimeraCuota(),
                                item.fechaFinal(),

                                item.codigoEstadoCartera()
                        )
                )
                .toList();
    }

    // =========================================================
    // PREPARAR DESEMBOLSO
    //
    // COMPROBANTE 1
    // ---------------------------------------------------------
    // Crédito
    // (-) Fondo de garantías
    // (-) IVA Fondo
    // = Valor a distribuir
    //
    // COMPROBANTE 2
    // ---------------------------------------------------------
    // Central de riesgo
    // Papelería
    // IVA Papelería
    // Aportes
    // = Total cobros contra Caja
    //
    // Los cobros del comprobante 2 NO reducen el valor
    // a distribuir del comprobante 1.
    // =========================================================

    @Transactional(readOnly = true)
    public PreparacionDesembolso preparar(
            Integer idCarteraCredito
    ) {

        SolicitudDesembolsoRepository.CreditoDesembolsoDatos credito =
                obtenerCreditoPendiente(
                        idCarteraCredito
                );

        BigDecimal valorCredito =
                normalizar(
                        credito.valorInicialCredito()
                );

        if (valorCredito.signum() <= 0) {
            throw new IllegalStateException(
                    "El crédito no tiene un valor inicial válido."
            );
        }

        // =====================================================
        // IVA GENERAL DE LA AGENCIA
        // =====================================================

        BigDecimal porcentajeIva =
                normalizarPorcentaje(
                        repository.obtenerPorcentajeIva(
                                credito.idAgencia()
                        )
                );

        validarPorcentaje(
                porcentajeIva,
                "IVA"
        );

        // =====================================================
        // COMPROBANTE 1
        // FONDO DE GARANTÍAS
        // =====================================================

        BigDecimal porcentajeFondo =
                normalizarPorcentaje(
                        credito.porcentajeFondoAplicado()
                );

        BigDecimal valorFondo =
                normalizar(
                        credito.valorFondoGarantia()
                );

        BigDecimal ivaFondo =
                calcularIva(
                        valorFondo,
                        porcentajeIva
                );

        // =====================================================
        // COMPROBANTE 2
        // CENTRAL DE RIESGO
        // =====================================================

        int cantidadCentral =
                repository.contarConsultasCentralRiesgo(
                        credito.idSolicitudCredito()
                );

        BigDecimal valorUnitarioCentral =
                normalizar(
                        credito.valorConsultaCentralRiesgo()
                );

        BigDecimal valorCentral =
                valorUnitarioCentral
                        .multiply(
                                BigDecimal.valueOf(
                                        cantidadCentral
                                )
                        )
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        // =====================================================
        // COMPROBANTE 2
        // PAPELERÍA
        // =====================================================

        BigDecimal valorPapeleria =
                calcularCargoLinea(
                        valorCredito,
                        credito.valorPapeleria(),
                        credito.porcentajePapeleria(),
                        "papelería"
                );

        BigDecimal ivaPapeleria =
                calcularIva(
                        valorPapeleria,
                        porcentajeIva
                );

        // =====================================================
        // COMPROBANTE 2
        // APORTES
        // =====================================================

        BigDecimal valorAportes =
                calcularCargoLinea(
                        valorCredito,
                        credito.valorAportes(),
                        credito.porcentajeAportes(),
                        "aportes"
                );

        // =====================================================
        // CONSTRUIR LIQUIDACIÓN
        // =====================================================

        LiquidacionDesembolso liquidacion =
                construirLiquidacion(
                        valorCredito,

                        valorFondo,
                        ivaFondo,

                        cantidadCentral,
                        valorUnitarioCentral,
                        valorCentral,

                        valorPapeleria,
                        ivaPapeleria,

                        valorAportes
                );

        return new PreparacionDesembolso(
                credito.idCarteraCredito(),
                credito.idSolicitudCredito(),

                credito.idAgencia(),
                credito.idLineaCredito(),
                credito.idDatosPersonal(),
                credito.idCuentaAportes(),

                credito.pagareCartera(),

                credito.documento(),
                credito.nombreCompleto(),

                credito.codigoLineaCredito(),
                credito.nombreLineaCredito(),

                valorCredito,

                // Comprobante 1
                porcentajeFondo,
                valorFondo,

                porcentajeIva,
                ivaFondo,

                liquidacion.totalComprobante1(),
                liquidacion.valorDistribuir(),

                // Comprobante 2
                cantidadCentral,
                valorUnitarioCentral,
                valorCentral,

                normalizar(
                        credito.valorPapeleria()
                ),
                normalizarPorcentaje(
                        credito.porcentajePapeleria()
                ),
                valorPapeleria,
                ivaPapeleria,

                normalizar(
                        credito.valorAportes()
                ),
                normalizarPorcentaje(
                        credito.porcentajeAportes()
                ),
                valorAportes,

                liquidacion.totalComprobante2(),
                liquidacion.contrapartidaCaja(),

                credito.fechaPrimeraCuota(),
                credito.fechaFinal()
        );
    }

    // =========================================================
    // RECALCULAR
    //
    // Solamente cambia la cantidad de consultas a Central.
    //
    // El IVA siempre se obtiene internamente desde:
    //
    // general.parametros
    // codigo_parametro = 1
    // por agencia.
    // =========================================================

    @Transactional(readOnly = true)
    public LiquidacionDesembolso calcularLiquidacion(
            Integer idCarteraCredito,
            Integer cantidadCentral
    ) {

        if (cantidadCentral == null
                || cantidadCentral < 0) {

            throw new IllegalArgumentException(
                    "La cantidad de consultas a Central de Riesgo "
                            + "no puede ser negativa."
            );
        }

        SolicitudDesembolsoRepository.CreditoDesembolsoDatos credito =
                obtenerCreditoPendiente(
                        idCarteraCredito
                );

        BigDecimal valorCredito =
                normalizar(
                        credito.valorInicialCredito()
                );

        BigDecimal porcentajeIva =
                normalizarPorcentaje(
                        repository.obtenerPorcentajeIva(
                                credito.idAgencia()
                        )
                );

        validarPorcentaje(
                porcentajeIva,
                "IVA"
        );

        // =====================================================
        // COMPROBANTE 1
        // =====================================================

        BigDecimal valorFondo =
                normalizar(
                        credito.valorFondoGarantia()
                );

        BigDecimal ivaFondo =
                calcularIva(
                        valorFondo,
                        porcentajeIva
                );

        // =====================================================
        // COMPROBANTE 2 - CENTRAL
        // =====================================================

        BigDecimal valorUnitarioCentral =
                normalizar(
                        credito.valorConsultaCentralRiesgo()
                );

        BigDecimal valorCentral =
                valorUnitarioCentral
                        .multiply(
                                BigDecimal.valueOf(
                                        cantidadCentral
                                )
                        )
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        // =====================================================
        // COMPROBANTE 2 - PAPELERÍA
        // =====================================================

        BigDecimal valorPapeleria =
                calcularCargoLinea(
                        valorCredito,
                        credito.valorPapeleria(),
                        credito.porcentajePapeleria(),
                        "papelería"
                );

        BigDecimal ivaPapeleria =
                calcularIva(
                        valorPapeleria,
                        porcentajeIva
                );

        // =====================================================
        // COMPROBANTE 2 - APORTES
        // =====================================================

        BigDecimal valorAportes =
                calcularCargoLinea(
                        valorCredito,
                        credito.valorAportes(),
                        credito.porcentajeAportes(),
                        "aportes"
                );

        if (valorAportes.compareTo(BigDecimal.ZERO) > 0) {

            if (credito.idCuentaAportes() == null
                    || credito.idCuentaAportes() <= 0) {
                throw new IllegalStateException(
                        "El crédito genera cobro de aportes, "
                                + "pero no tiene una cuenta de aportes asociada."
                );
            }

            SolicitudDesembolsoRepository.CuentaAhorroDesembolsoDatos
                    cuentaAportes =
                    repository.buscarCuentaAhorroDesembolso(
                            credito.idCuentaAportes()
                    ).orElseThrow(() ->
                            new IllegalStateException(
                                    "La cuenta de aportes "
                                            + credito.idCuentaAportes()
                                            + " no existe."
                            )
                    );

            if (!credito.idDatosPersonal().equals(
                    cuentaAportes.idDatosPersonal()
            )) {
                throw new IllegalStateException(
                        "La cuenta de aportes "
                                + credito.idCuentaAportes()
                                + " no pertenece al titular del crédito."
                );
            }

            if (cuentaAportes.idCatalogoCuenta() == null
                    || cuentaAportes.idCatalogoCuenta() <= 0) {
                throw new IllegalStateException(
                        "La cuenta de aportes "
                                + credito.idCuentaAportes()
                                + " no tiene configurada una cuenta contable "
                                + "para su forma de ahorro."
                );
            }
        }

        return construirLiquidacion(
                valorCredito,

                valorFondo,
                ivaFondo,

                cantidadCentral,
                valorUnitarioCentral,
                valorCentral,

                valorPapeleria,
                ivaPapeleria,

                valorAportes
        );
    }

    // =========================================================
    // CONSTRUIR LIQUIDACIÓN
    // =========================================================

    private LiquidacionDesembolso construirLiquidacion(
            BigDecimal valorCredito,

            BigDecimal valorFondo,
            BigDecimal ivaFondo,

            Integer cantidadCentral,
            BigDecimal valorUnitarioCentral,
            BigDecimal valorCentral,

            BigDecimal valorPapeleria,
            BigDecimal ivaPapeleria,

            BigDecimal valorAportes
    ) {

        // =====================================================
        // COMPROBANTE 1
        //
        // Fondo e IVA Fondo forman parte de la operación
        // principal del desembolso.
        // =====================================================

        BigDecimal totalComprobante1 =
                normalizar(valorFondo)
                        .add(
                                normalizar(ivaFondo)
                        )
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        BigDecimal valorDistribuir =
                normalizar(valorCredito)
                        .subtract(
                                totalComprobante1
                        )
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        if (valorDistribuir.signum() < 0) {
            throw new IllegalStateException(
                    "El Fondo de Garantías y su IVA superan "
                            + "el valor del crédito."
            );
        }

        // =====================================================
        // COMPROBANTE 2
        //
        // Estos cobros NO disminuyen valorDistribuir.
        //
        // Su contrapartida contable es Caja.
        // =====================================================

        BigDecimal totalComprobante2 =
                normalizar(valorCentral)
                        .add(
                                normalizar(valorPapeleria)
                        )
                        .add(
                                normalizar(ivaPapeleria)
                        )
                        .add(
                                normalizar(valorAportes)
                        )
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        BigDecimal contrapartidaCaja =
                totalComprobante2;

        return new LiquidacionDesembolso(
                normalizar(valorCredito),

                // Comprobante 1
                normalizar(valorFondo),
                normalizar(ivaFondo),

                totalComprobante1,
                valorDistribuir,

                // Comprobante 2
                cantidadCentral,
                normalizar(valorUnitarioCentral),
                normalizar(valorCentral),

                normalizar(valorPapeleria),
                normalizar(ivaPapeleria),

                normalizar(valorAportes),

                totalComprobante2,
                contrapartidaCaja
        );
    }

    // =========================================================
// VALIDAR DISTRIBUCIÓN DEL DESEMBOLSO
//
// Valida:
//
// - Efectivo
// - Una o varias cuentas de depósito
// - Cuenta por pagar
//
// La suma debe ser exactamente igual al valor a distribuir
// calculado por el backend.
//
// NO registra movimientos.
// NO contabiliza.
// NO activa el crédito.
// =========================================================

    @Transactional(readOnly = true)
    public ResultadoValidacionDistribucion validarDistribucion(
            Integer idCarteraCredito,
            DistribucionDesembolso distribucion
    ) {

        if (distribucion == null) {
            throw new IllegalArgumentException(
                    "La distribución del desembolso es obligatoria."
            );
        }

        SolicitudDesembolsoRepository.CreditoDesembolsoDatos credito =
                obtenerCreditoPendiente(
                        idCarteraCredito
                );

        BigDecimal valorCredito =
                normalizar(
                        credito.valorInicialCredito()
                );

        BigDecimal porcentajeIva =
                normalizarPorcentaje(
                        repository.obtenerPorcentajeIva(
                                credito.idAgencia()
                        )
                );

        validarPorcentaje(
                porcentajeIva,
                "IVA"
        );

        // =====================================================
        // DETERMINAR VALOR A DISTRIBUIR
        //
        // Solamente Fondo + IVA Fondo afectan el comprobante 1.
        // =====================================================

        BigDecimal valorFondo =
                normalizar(
                        credito.valorFondoGarantia()
                );

        BigDecimal ivaFondo =
                calcularIva(
                        valorFondo,
                        porcentajeIva
                );

        BigDecimal valorDistribuir =
                valorCredito
                        .subtract(valorFondo)
                        .subtract(ivaFondo)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        if (valorDistribuir.signum() < 0) {
            throw new IllegalStateException(
                    "El Fondo de Garantías y su IVA superan "
                            + "el valor del crédito."
            );
        }

        // =====================================================
        // EFECTIVO
        // =====================================================

        BigDecimal valorEfectivo =
                normalizar(
                        distribucion.valorEfectivo()
                );

        if (valorEfectivo.signum() < 0) {
            throw new IllegalArgumentException(
                    "El valor en efectivo no puede ser negativo."
            );
        }

        // =====================================================
        // DEPÓSITOS
        // =====================================================

        List<DepositoDistribucion> depositos =
                distribucion.depositos() == null
                        ? List.of()
                        : distribucion.depositos();

        BigDecimal totalDepositos =
                BigDecimal.ZERO.setScale(
                        2,
                        RoundingMode.HALF_UP
                );

        Set<Integer> cuentasUtilizadas =
                new HashSet<>();

        for (DepositoDistribucion deposito : depositos) {

            if (deposito == null) {
                throw new IllegalArgumentException(
                        "La distribución contiene un depósito inválido."
                );
            }

            if (deposito.idCuentaAhorro() == null
                    || deposito.idCuentaAhorro() <= 0) {

                throw new IllegalArgumentException(
                        "La cuenta de ahorro del depósito es obligatoria."
                );
            }

            if (!cuentasUtilizadas.add(
                    deposito.idCuentaAhorro()
            )) {

                throw new IllegalArgumentException(
                        "La cuenta de ahorro "
                                + deposito.idCuentaAhorro()
                                + " está repetida en la distribución."
                );
            }

            // =====================================================
// VALIDAR CUENTA DE AHORRO
// =====================================================

            SolicitudDesembolsoRepository.CuentaAhorroDesembolsoDatos
                    cuentaAhorro =
                    repository.buscarCuentaAhorroDesembolso(
                            deposito.idCuentaAhorro()
                    ).orElseThrow(() ->
                            new IllegalArgumentException(
                                    "La cuenta de ahorro "
                                            + deposito.idCuentaAhorro()
                                            + " no existe."
                            )
                    );

            if (!credito.idDatosPersonal().equals(
                    cuentaAhorro.idDatosPersonal()
            )) {

                throw new IllegalArgumentException(
                        "La cuenta de ahorro "
                                + deposito.idCuentaAhorro()
                                + " no pertenece al titular del crédito."
                );
            }

            if (cuentaAhorro.idCatalogoCuenta() == null
                    || cuentaAhorro.idCatalogoCuenta() <= 0) {

                throw new IllegalStateException(
                        "La cuenta de ahorro "
                                + deposito.idCuentaAhorro()
                                + " no tiene configurada una cuenta contable "
                                + "para su forma de ahorro."
                );
            }

            BigDecimal valorDeposito =
                    normalizar(
                            deposito.valor()
                    );

            if (valorDeposito.signum() <= 0) {
                throw new IllegalArgumentException(
                        "El valor destinado a la cuenta de ahorro "
                                + deposito.idCuentaAhorro()
                                + " debe ser mayor que cero."
                );
            }

            totalDepositos =
                    totalDepositos.add(
                            valorDeposito
                    );
        }

        totalDepositos =
                totalDepositos.setScale(
                        2,
                        RoundingMode.HALF_UP
                );

        // =====================================================
        // CUENTA POR PAGAR
        // =====================================================

        BigDecimal valorCuentaPorPagar =
                normalizar(
                        distribucion.valorCuentaPorPagar()
                );

        if (valorCuentaPorPagar.signum() < 0) {
            throw new IllegalArgumentException(
                    "El valor de la cuenta por pagar "
                            + "no puede ser negativo."
            );
        }

        // =====================================================
        // TOTAL DISTRIBUCIÓN
        // =====================================================

        BigDecimal totalDistribuido =
                valorEfectivo
                        .add(totalDepositos)
                        .add(valorCuentaPorPagar)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        BigDecimal diferencia =
                valorDistribuir
                        .subtract(totalDistribuido)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        boolean cuadra =
                diferencia.compareTo(
                        BigDecimal.ZERO
                ) == 0;

        return new ResultadoValidacionDistribucion(
                idCarteraCredito,

                valorDistribuir,

                distribucion.idCaja(),
                valorEfectivo,

                totalDepositos,
                valorCuentaPorPagar,

                totalDistribuido,
                diferencia,

                cuadra,

                cuadra
                        ? "La distribución del desembolso está cuadrada."
                        : "La distribución del desembolso no cuadra "
                        + "con el valor a distribuir."
        );
    }

    // =========================================================
    // EDAD INICIAL
    // =========================================================

    @Transactional(readOnly = true)
    public String determinarEdadRiesgoInicial(
            Integer idCarteraCredito,
            Integer idDatosPersonal
    ) {

        validarIdCarteraCredito(
                idCarteraCredito
        );

        if (idDatosPersonal == null
                || idDatosPersonal <= 0) {

            throw new IllegalArgumentException(
                    "El asociado del crédito es obligatorio."
            );
        }

        List<SolicitudDesembolsoRepository.CreditoEdadDatos> edades =
                repository.consultarEdadesCreditosActivos(
                        idDatosPersonal,
                        idCarteraCredito
                );

        return edades
                .stream()
                .map(
                        SolicitudDesembolsoRepository
                                .CreditoEdadDatos::edadRiesgo
                )
                .filter(edad ->
                        edad != null
                                && !edad.isBlank()
                )
                .map(String::trim)
                .map(String::toUpperCase)
                .max(
                        Comparator.comparingInt(
                                this::ordenEdadRiesgo
                        )
                )
                .orElse(
                        EDAD_A
                );
    }

    // =========================================================
    // DTO - DISTRIBUCIÓN DESEMBOLSO
    // =========================================================

    public record DistribucionDesembolso(
            Long idCaja,
            BigDecimal valorEfectivo,
            List<DepositoDistribucion> depositos,
            BigDecimal valorCuentaPorPagar
    ) {
    }

    // =========================================================
    // DTO - DEPÓSITO DE LA DISTRIBUCIÓN
    // =========================================================

    public record DepositoDistribucion(
            Integer idCuentaAhorro,
            BigDecimal valor
    ) {
    }

    // =========================================================
    // DTO - RESULTADO VALIDACIÓN DISTRIBUCIÓN
    // =========================================================

    public record ResultadoValidacionDistribucion(
            Integer idCarteraCredito,

            BigDecimal valorDistribuir,

            Long idCaja,
            BigDecimal valorEfectivo,

            BigDecimal totalDepositos,
            BigDecimal valorCuentaPorPagar,

            BigDecimal totalDistribuido,
            BigDecimal diferencia,

            boolean cuadra,
            String mensaje
    ) {
    }

    // =========================================================
    // NOVACIÓN
    // =========================================================

    @Transactional(readOnly = true)
    public DatosNovacion determinarNovacion(
            Integer idCarteraCredito,
            Integer idDatosPersonal,
            LocalDate fechaDesembolso
    ) {

        validarIdCarteraCredito(
                idCarteraCredito
        );

        if (idDatosPersonal == null
                || idDatosPersonal <= 0) {

            throw new IllegalArgumentException(
                    "El asociado del crédito es obligatorio."
            );
        }

        if (fechaDesembolso == null) {
            throw new IllegalArgumentException(
                    "La fecha de desembolso es obligatoria."
            );
        }

        return repository
                .consultarCreditosCancelados(
                        idDatosPersonal,
                        idCarteraCredito
                )
                .stream()
                .filter(anterior ->
                        fechaDesembolso.equals(
                                anterior.ultimaFechaCapital()
                        )
                )
                .findFirst()
                .map(anterior -> {

                    int numeroAnterior =
                            anterior.numeroNovaciones() != null
                                    ? anterior.numeroNovaciones()
                                    : 0;

                    return new DatosNovacion(
                            MODIFICACION_NOVADO,
                            numeroAnterior + 1,

                            anterior.idCarteraCredito(),
                            anterior.pagareCartera()
                    );
                })
                .orElseGet(() ->
                        new DatosNovacion(
                                MODIFICACION_NINGUNA,
                                0,
                                null,
                                null
                        )
                );
    }

    // =========================================================
// RESOLVER PARÁMETRO CONTABLE
// =========================================================

    private Integer resolverParametroContable(
            SolicitudDesembolsoRepository.CreditoDesembolsoDatos credito,
            String concepto
    ) {

        SolicitudDesembolsoRepository.CuentaContableDatos cuenta =
                repository.buscarParametroContable(
                        credito.idAgencia(),
                        credito.idLineaCredito(),
                        concepto
                ).orElseThrow(() ->
                        new IllegalStateException(
                                "No existe configuración contable para "
                                        + concepto
                                        + ", agencia "
                                        + credito.idAgencia()
                                        + " y línea "
                                        + credito.idLineaCredito()
                                        + "."
                        )
                );

        if (cuenta.idCatalogoCuenta() == null
                || cuenta.idCatalogoCuenta() <= 0) {

            throw new IllegalStateException(
                    "La configuración contable de "
                            + concepto
                            + " no tiene una cuenta válida."
            );
        }

        return cuenta.idCatalogoCuenta();
    }


    // =========================================================
    // RESOLVER CUENTA CAPITAL
    // =========================================================

    private Integer resolverCuentaCapital(
            SolicitudDesembolsoRepository.CreditoDesembolsoDatos credito,
            String edadRiesgo
    ) {

        SolicitudDesembolsoRepository.CuentaContableDatos cuenta =
                repository.buscarCuentaCapital(
                        credito.idAgencia(),
                        credito.codigoClasificacionCredito(),
                        credito.codigoFormaPago(),
                        credito.tipoGarantia(),
                        edadRiesgo,
                        null
                ).orElseThrow(() ->
                        new IllegalStateException(
                                "No existe configuración contable de CAPITAL "
                                        + "para el crédito "
                                        + credito.idCarteraCredito()
                                        + "."
                        )
                );

        if (cuenta.idCatalogoCuenta() == null
                || cuenta.idCatalogoCuenta() <= 0) {

            throw new IllegalStateException(
                    "La configuración contable de CAPITAL "
                            + "no tiene una cuenta válida."
            );
        }

        return cuenta.idCatalogoCuenta();
    }

    // =========================================================
    // REGISTRAR CRÉDITO EN DEPÓSITOS
    //
    // Utilizado para:
    //
    // - Distribución del desembolso a cuentas de ahorro.
    // - Cobro de aportes.
    //
    // El movimiento 222 corresponde a NOTA CRÉDITO CARTERA.
    // El módulo 03 corresponde a CARTERA.
    // =========================================================

    private void registrarCreditoDeposito(
            Integer idCuentaAhorro,
            BigDecimal valor,
            LocalDate fechaMovimiento,

            String tipoComprobante,
            String numeroComprobante,

            Integer idCarteraCredito,
            Integer idUsuario
    ) {

        BigDecimal valorNormalizado =
                normalizar(valor);

        if (valorNormalizado.signum() <= 0) {
            return;
        }

        if (idCuentaAhorro == null
                || idCuentaAhorro <= 0) {

            throw new IllegalArgumentException(
                    "La cuenta de ahorro es obligatoria "
                            + "para registrar el movimiento."
            );
        }

        if (fechaMovimiento == null) {
            throw new IllegalArgumentException(
                    "La fecha del movimiento de depósitos es obligatoria."
            );
        }

        validarTexto(
                tipoComprobante,
                "El tipo de comprobante del movimiento "
                        + "de depósitos es obligatorio."
        );

        validarTexto(
                numeroComprobante,
                "El número de comprobante del movimiento "
                        + "de depósitos es obligatorio."
        );

        DepositosMovimientoDTO movimiento =
                new DepositosMovimientoDTO();

        movimiento.setIdCuentaAhorro(
                idCuentaAhorro
        );

        movimiento.setFechaMovimiento(
                fechaMovimiento
        );

        movimiento.setTipoComprobante(
                tipoComprobante.trim()
        );

        movimiento.setNumeroComprobante(
                numeroComprobante.trim()
        );

        movimiento.setTipoMovimiento(
                "222"
        );

        movimiento.setModulo(
                "03"
        );

        movimiento.setValorDebito(
                BigDecimal.ZERO
        );

        movimiento.setValorCredito(
                valorNormalizado
        );

        movimiento.setTarjeta(
                "N"
        );

        movimiento.setEstablecimiento(
                "OFICINA"
        );

        movimiento.setModuloOrigen(
                "CARTERA"
        );

        movimiento.setProcesoOrigen(
                "DESEMBOLSO_CREDITO"
        );

        movimiento.setIdOrigen(
                idCarteraCredito.longValue()
        );

        depositosMovimientoService.registrarCredito(
                movimiento,
                idUsuario
        );
    }


    // =========================================================
    // EJECUTAR DESEMBOLSO
    //
    // Ejecuta toda la operación dentro de una sola transacción:
    //
    // 1. Bloquea el crédito.
    // 2. Recalcula liquidación.
    // 3. Valida distribución.
    // 4. Resuelve cuentas contables.
    // 5. Genera comprobante(s).
    // 6. Publica la cuenta por pagar en Tesorería.
    // 7. Registra Caja.
    // 8. Registra depósitos.
    // 9. Registra aportes.
    // 10. Activa el crédito.
    //
    // Cualquier error revierte toda la operación.
    // =========================================================

    // Vista previa sin bloqueo, movimientos ni consecutivos definitivos.
    @Transactional(readOnly = true)
    public List<co.assip.erp.cartera.originacion.desembolso.dto.ComprobanteDesembolsoPreviewDTO> verificarComprobantes(
            Integer idCarteraCredito,
            Long idCaja,
            LocalDate fechaContable,
            String tipoComprobanteDesembolso,
            String tipoComprobanteCobros,
            Integer cantidadCentral,
            BigDecimal valorEfectivo,
            List<DepositoDistribucion> depositos,
            BigDecimal valorCuentaPorPagar
    ) {
        validarIdCarteraCredito(idCarteraCredito);
        if (fechaContable == null) {
            throw new IllegalArgumentException("La fecha contable es obligatoria.");
        }
        validarTexto(tipoComprobanteDesembolso,
                "El tipo de comprobante de desembolso es obligatorio.");
        if (cantidadCentral == null || cantidadCentral < 0) {
            throw new IllegalArgumentException("La cantidad de consultas a Central de Riesgo no es válida.");
        }
        List<DepositoDistribucion> depositosNormalizados =
                depositos == null ? List.of() : depositos;
        var credito = obtenerCreditoPendiente(idCarteraCredito);
        usuarioSesionService.validarAgencia(credito.idAgencia());
        validarIntegridadPendiente(credito);
        Integer idUsuario = usuarioSesionService.idUsuario();
        LiquidacionDesembolso liquidacion = calcularLiquidacion(idCarteraCredito, cantidadCentral);
        boolean requiereCaja = normalizar(valorEfectivo).signum() > 0
                || liquidacion.totalComprobante2().signum() > 0;
        if (requiereCaja && (idCaja == null || idCaja <= 0)) {
            throw new IllegalArgumentException("Debe seleccionar una caja para el efectivo o los cobros.");
        }
        var distribucion = new DistribucionDesembolso(
                idCaja, valorEfectivo, depositosNormalizados, valorCuentaPorPagar);
        var validacion = validarDistribucion(idCarteraCredito, distribucion);
        if (!validacion.cuadra()) {
            throw new IllegalStateException(validacion.mensaje()
                    + " Diferencia: " + validacion.diferencia());
        }
        if (liquidacion.totalComprobante2().signum() > 0) {
            validarTexto(tipoComprobanteCobros,
                    "El tipo de comprobante de cobros es obligatorio.");
        }
        String edadRiesgo = determinarEdadRiesgoInicial(
                credito.idCarteraCredito(), credito.idDatosPersonal());
        // CUENTA CAPITAL
        // =====================================================

        Integer idCuentaCapital =
                resolverCuentaCapital(
                        credito,
                        edadRiesgo
                );

        // =====================================================
        // CUENTAS DEL COMPROBANTE 2
        //
        // Solo se resuelven cuando el concepto realmente
        // genera movimiento.
        // =====================================================

        Integer idCuentaCentral = null;

        if (liquidacion.valorCentral()
                .compareTo(BigDecimal.ZERO) > 0) {

            idCuentaCentral =
                    resolverParametroContable(
                            credito,
                            CONCEPTO_CENTRAL_RIESGO
                    );
        }

        Integer idCuentaPapeleria = null;

        if (liquidacion.valorPapeleria()
                .compareTo(BigDecimal.ZERO) > 0) {

            idCuentaPapeleria =
                    resolverParametroContable(
                            credito,
                            CONCEPTO_PAPELERIA
                    );
        }

        Integer idCuentaIva = null;

        if (liquidacion.ivaPapeleria()
                .compareTo(BigDecimal.ZERO) > 0) {

            idCuentaIva =
                    resolverParametroContable(
                            credito,
                            CONCEPTO_IVA
                    );
        }

        Integer idCuentaPorPagar = null;

        if (normalizar(valorCuentaPorPagar)
                .compareTo(BigDecimal.ZERO) > 0) {

            idCuentaPorPagar =
                    resolverParametroContable(
                            credito,
                            CONCEPTO_CUENTA_POR_PAGAR
                    );
        }

        // =====================================================
        // DEPÓSITOS PARA CONTABILIZACIÓN
        // =====================================================

        List<SolicitudDesembolsoContabilizacionService.DepositoContabilizacion>
                depositosContables =
                depositosNormalizados
                        .stream()
                        .map(deposito ->
                                new SolicitudDesembolsoContabilizacionService
                                        .DepositoContabilizacion(
                                        deposito.idCuentaAhorro(),
                                        normalizar(
                                                deposito.valor()
                                        )
                                )
                        )
                        .toList();

        // =====================================================
        return contabilizacionService.verificarComprobantes(
                new SolicitudDesembolsoContabilizacionService.DatosContabilizacion(
                        credito.idCarteraCredito(), credito.idAgencia(), idCaja,
                        fechaContable, idUsuario,
                        tipoComprobanteDesembolso.trim(),
                        liquidacion.totalComprobante2().signum() > 0
                                ? tipoComprobanteCobros.trim() : null,
                        idCuentaCapital, idCuentaCentral, idCuentaPapeleria,
                        idCuentaIva, idCuentaPorPagar,
                        liquidacion.valorCredito(), liquidacion.valorFondo(),
                        liquidacion.ivaFondo(), normalizar(valorEfectivo),
                        depositosContables, normalizar(valorCuentaPorPagar),
                        liquidacion.valorCentral(), liquidacion.valorPapeleria(),
                        liquidacion.ivaPapeleria(), liquidacion.valorAportes(),
                        liquidacion.totalComprobante2()));
    }

    @Transactional
    public ResultadoEjecucionDesembolso ejecutarDesembolso(
            Integer idCarteraCredito,
            Long idCaja,
            LocalDate fechaContable,
            LocalDate fechaDesembolso,
            String tipoComprobanteDesembolso,
            String tipoComprobanteCobros,
            Integer cantidadCentral,
            BigDecimal valorEfectivo,
            List<DepositoDistribucion> depositos,
            BigDecimal valorCuentaPorPagar
    ) {

        validarIdCarteraCredito(idCarteraCredito);

        if (fechaContable == null) {
            throw new IllegalArgumentException(
                    "La fecha contable es obligatoria."
            );
        }

        if (fechaDesembolso == null) {
            throw new IllegalArgumentException(
                    "La fecha de desembolso es obligatoria."
            );
        }

        validarTexto(
                tipoComprobanteDesembolso,
                "El tipo de comprobante de desembolso es obligatorio."
        );

        if (cantidadCentral == null || cantidadCentral < 0) {
            throw new IllegalArgumentException(
                    "La cantidad de consultas a Central de Riesgo "
                            + "no puede ser negativa."
            );
        }

        List<DepositoDistribucion> depositosNormalizados =
                depositos == null
                        ? List.of()
                        : depositos;

        // =====================================================
        // BLOQUEAR CRÉDITO
        // =====================================================

        SolicitudDesembolsoRepository.CreditoDesembolsoDatos credito =
                repository.bloquearCredito(
                        idCarteraCredito
                ).orElseThrow(() ->
                        new IllegalStateException(
                                "El crédito no existe o ya no se encuentra "
                                        + "pendiente de desembolso."
                        )
                );

        usuarioSesionService.validarAgencia(
                credito.idAgencia()
        );

        validarIntegridadPendiente(
                credito
        );

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        // =====================================================
        // LIQUIDACIÓN DEFINITIVA
        // =====================================================

        LiquidacionDesembolso liquidacion =
                calcularLiquidacion(
                        idCarteraCredito,
                        cantidadCentral
                );

        boolean requiereCaja =
                normalizar(valorEfectivo).signum() > 0
                        || liquidacion.totalComprobante2().signum() > 0;

        if (requiereCaja && (idCaja == null || idCaja <= 0)) {
            throw new IllegalArgumentException(
                    "Debe seleccionar una caja porque el desembolso "
                            + "genera movimientos de efectivo."
            );
        }

        // =====================================================
        // VALIDAR DISTRIBUCIÓN
        // =====================================================

        DistribucionDesembolso distribucion =
                new DistribucionDesembolso(
                        idCaja,
                        valorEfectivo,
                        depositosNormalizados,
                        valorCuentaPorPagar
                );

        ResultadoValidacionDistribucion validacionDistribucion =
                validarDistribucion(
                        idCarteraCredito,
                        distribucion
                );

        if (!validacionDistribucion.cuadra()) {
            throw new IllegalStateException(
                    validacionDistribucion.mensaje()
                            + " Diferencia: "
                            + validacionDistribucion.diferencia()
            );
        }

        // =====================================================
        // SEGUNDO COMPROBANTE
        // =====================================================

        if (liquidacion.totalComprobante2()
                .compareTo(BigDecimal.ZERO) > 0) {

            validarTexto(
                    tipoComprobanteCobros,
                    "El tipo de comprobante de cobros es obligatorio."
            );
        }

        // =====================================================
        // EDAD DE RIESGO INICIAL
        // =====================================================

        String edadRiesgo =
                determinarEdadRiesgoInicial(
                        credito.idCarteraCredito(),
                        credito.idDatosPersonal()
                );

        // =====================================================
        // CUENTA CAPITAL
        // =====================================================

        Integer idCuentaCapital =
                resolverCuentaCapital(
                        credito,
                        edadRiesgo
                );

        // =====================================================
        // CUENTAS DEL COMPROBANTE 2
        //
        // Solo se resuelven cuando el concepto realmente
        // genera movimiento.
        // =====================================================

        Integer idCuentaCentral = null;

        if (liquidacion.valorCentral()
                .compareTo(BigDecimal.ZERO) > 0) {

            idCuentaCentral =
                    resolverParametroContable(
                            credito,
                            CONCEPTO_CENTRAL_RIESGO
                    );
        }

        Integer idCuentaPapeleria = null;

        if (liquidacion.valorPapeleria()
                .compareTo(BigDecimal.ZERO) > 0) {

            idCuentaPapeleria =
                    resolverParametroContable(
                            credito,
                            CONCEPTO_PAPELERIA
                    );
        }

        Integer idCuentaIva = null;

        if (liquidacion.ivaPapeleria()
                .compareTo(BigDecimal.ZERO) > 0) {

            idCuentaIva =
                    resolverParametroContable(
                            credito,
                            CONCEPTO_IVA
                    );
        }

        Integer idCuentaPorPagar = null;

        if (normalizar(valorCuentaPorPagar)
                .compareTo(BigDecimal.ZERO) > 0) {

            idCuentaPorPagar =
                    resolverParametroContable(
                            credito,
                            CONCEPTO_CUENTA_POR_PAGAR
                    );
        }

        // =====================================================
        // DEPÓSITOS PARA CONTABILIZACIÓN
        // =====================================================

        List<SolicitudDesembolsoContabilizacionService.DepositoContabilizacion>
                depositosContables =
                depositosNormalizados
                        .stream()
                        .map(deposito ->
                                new SolicitudDesembolsoContabilizacionService
                                        .DepositoContabilizacion(
                                        deposito.idCuentaAhorro(),
                                        normalizar(
                                                deposito.valor()
                                        )
                                )
                        )
                        .toList();

        // =====================================================
        // CONTABILIZAR
        // =====================================================

        SolicitudDesembolsoContabilizacionService.ResultadoContabilizacion
                contabilizacion =
                contabilizacionService.contabilizar(
                        new SolicitudDesembolsoContabilizacionService
                                .DatosContabilizacion(

                                credito.idCarteraCredito(),
                                credito.idAgencia(),
                                idCaja,

                                fechaContable,
                                idUsuario,

                                tipoComprobanteDesembolso.trim(),
                                liquidacion.totalComprobante2()
                                        .compareTo(BigDecimal.ZERO) > 0
                                        ? tipoComprobanteCobros.trim()
                                        : null,

                                idCuentaCapital,
                                idCuentaCentral,
                                idCuentaPapeleria,
                                idCuentaIva,
                                idCuentaPorPagar,

                                liquidacion.valorCredito(),

                                liquidacion.valorFondo(),
                                liquidacion.ivaFondo(),

                                normalizar(valorEfectivo),
                                depositosContables,
                                normalizar(valorCuentaPorPagar),

                                liquidacion.valorCentral(),
                                liquidacion.valorPapeleria(),
                                liquidacion.ivaPapeleria(),
                                liquidacion.valorAportes(),

                                liquidacion.totalComprobante2()
                        )
                );

        // =====================================================
// TESORERÍA - CUENTA POR PAGAR DEL DESEMBOLSO
// =====================================================

        if (normalizar(valorCuentaPorPagar).signum() > 0) {

            tesoreriaOperacionesPendientesService.publicar(
                    credito.idAgencia(),
                    "CARTERA",
                    "DESEMBOLSO_CREDITO",
                    "cartera.carteras_creditos",
                    credito.idCarteraCredito().longValue(),
                    credito.idDatosPersonal(),
                    "SALIDA",
                    fechaContable,
                    "DESEMBOLSO CREDITO " + credito.pagareCartera(),
                    normalizar(valorCuentaPorPagar),
                    idUsuario
            );
        }

        // =====================================================
        // CAJA - EGRESO DEL DESEMBOLSO
        // =====================================================

        cajasMovimientosService.registrarDesembolsoCredito(
                idCaja,
                normalizar(valorEfectivo),
                fechaContable,
                credito.idCarteraCredito().longValue(),
                credito.pagareCartera(),
                contabilizacion.tipoComprobanteDesembolso(),
                contabilizacion.numeroComprobanteDesembolso(),
                idUsuario
        );

        // =====================================================
        // DEPÓSITOS - DISTRIBUCIÓN DEL DESEMBOLSO
        // =====================================================

        for (DepositoDistribucion deposito : depositosNormalizados) {

            registrarCreditoDeposito(
                    deposito.idCuentaAhorro(),
                    deposito.valor(),
                    fechaContable,

                    contabilizacion.tipoComprobanteDesembolso(),
                    contabilizacion.numeroComprobanteDesembolso(),

                    credito.idCarteraCredito(),
                    idUsuario
            );
        }

        // =====================================================
        // CAJA - INGRESO DE LOS COBROS
        // =====================================================

        if (liquidacion.totalComprobante2()
                .compareTo(BigDecimal.ZERO) > 0) {

            cajasMovimientosService
                    .registrarCobrosDesembolsoCredito(
                            idCaja,
                            liquidacion.totalComprobante2(),
                            fechaContable,
                            credito.idCarteraCredito().longValue(),
                            credito.pagareCartera(),
                            contabilizacion.tipoComprobanteCobros(),
                            contabilizacion.numeroComprobanteCobros(),
                            idUsuario
                    );
        }

        // =====================================================
        // APORTES
        // =====================================================

        if (liquidacion.valorAportes()
                .compareTo(BigDecimal.ZERO) > 0) {

            registrarCreditoDeposito(
                    credito.idCuentaAportes(),
                    liquidacion.valorAportes(),
                    fechaContable,

                    contabilizacion.tipoComprobanteCobros(),
                    contabilizacion.numeroComprobanteCobros(),

                    credito.idCarteraCredito(),
                    idUsuario
            );
        }

        // =====================================================
        // NOVACIÓN
        // =====================================================

        DatosNovacion novacion =
                determinarNovacion(
                        credito.idCarteraCredito(),
                        credito.idDatosPersonal(),
                        fechaDesembolso
                );

        // =====================================================
        // ACTIVAR MAESTRO
        //
        // El maestro conserva el comprobante principal
        // del desembolso.
        // =====================================================

        int actualizados =
                repository.activarCredito(
                        credito.idCarteraCredito(),

                        contabilizacion.tipoComprobanteDesembolso(),
                        contabilizacion.numeroComprobanteDesembolso(),

                        fechaContable,
                        fechaDesembolso,

                        edadRiesgo,
                        edadRiesgo,

                        novacion.codigoModificacionCredito(),
                        novacion.numeroNovaciones(),

                        idUsuario
                );

        if (actualizados != 1) {
            throw new IllegalStateException(
                    "No fue posible activar el crédito "
                            + credito.idCarteraCredito()
                            + "."
            );
        }

        // =====================================================
        // RESULTADO
        // =====================================================

        return new ResultadoEjecucionDesembolso(
                credito.idCarteraCredito(),
                credito.pagareCartera(),

                liquidacion.valorCredito(),
                liquidacion.valorDistribuir(),
                liquidacion.totalComprobante2(),

                contabilizacion.tipoComprobanteDesembolso(),
                contabilizacion.numeroComprobanteDesembolso(),

                contabilizacion.tipoComprobanteCobros(),
                contabilizacion.numeroComprobanteCobros(),

                fechaContable,
                fechaDesembolso,

                edadRiesgo,

                novacion.codigoModificacionCredito(),
                novacion.numeroNovaciones()
        );
    }

    // =========================================================
    // OBTENER CRÉDITO PENDIENTE
    // =========================================================

    private SolicitudDesembolsoRepository.CreditoDesembolsoDatos
    obtenerCreditoPendiente(
            Integer idCarteraCredito
    ) {

        validarIdCarteraCredito(
                idCarteraCredito
        );

        SolicitudDesembolsoRepository.CreditoDesembolsoDatos credito =
                repository.buscarCredito(
                        idCarteraCredito
                ).orElseThrow(() ->
                        new IllegalArgumentException(
                                "No existe el crédito "
                                        + idCarteraCredito
                                        + "."
                        )
                );

        usuarioSesionService.validarAgencia(
                credito.idAgencia()
        );

        validarPendiente(
                credito
        );

        return credito;
    }

    // =========================================================
    // CARGO CONFIGURADO EN LA LÍNEA
    // =========================================================

    private BigDecimal calcularCargoLinea(
            BigDecimal valorCredito,
            BigDecimal valorFijo,
            BigDecimal porcentaje,
            String nombreCargo
    ) {

        BigDecimal fijo =
                normalizar(
                        valorFijo
                );

        BigDecimal porcentajeNormalizado =
                normalizarPorcentaje(
                        porcentaje
                );

        if (fijo.signum() < 0
                || porcentajeNormalizado.signum() < 0) {

            throw new IllegalStateException(
                    "La configuración de "
                            + nombreCargo
                            + " no puede contener valores negativos."
            );
        }

        /*
         * Regla:
         *
         * Si existe porcentaje, tiene prioridad.
         * En caso contrario se utiliza el valor fijo.
         */

        if (porcentajeNormalizado.signum() > 0) {

            return normalizar(valorCredito)
                    .multiply(
                            porcentajeNormalizado
                    )
                    .divide(
                            CIEN,
                            2,
                            RoundingMode.HALF_UP
                    );
        }

        return fijo;
    }

    // =========================================================
    // IVA
    // =========================================================

    private BigDecimal calcularIva(
            BigDecimal base,
            BigDecimal porcentajeIva
    ) {

        BigDecimal valorBase =
                normalizar(
                        base
                );

        BigDecimal porcentaje =
                normalizarPorcentaje(
                        porcentajeIva
                );

        if (valorBase.signum() <= 0
                || porcentaje.signum() <= 0) {

            return BigDecimal.ZERO.setScale(
                    2,
                    RoundingMode.HALF_UP
            );
        }

        return valorBase
                .multiply(
                        porcentaje
                )
                .divide(
                        CIEN,
                        2,
                        RoundingMode.HALF_UP
                );
    }

    // =========================================================
    // VALIDACIONES
    // =========================================================

    private void validarIdCarteraCredito(
            Integer idCarteraCredito
    ) {

        if (idCarteraCredito == null
                || idCarteraCredito <= 0) {

            throw new IllegalArgumentException(
                    "El crédito es obligatorio."
            );
        }
    }

    private void validarPendiente(
            SolicitudDesembolsoRepository.CreditoDesembolsoDatos credito
    ) {

        if (!ESTADO_PENDIENTE.equalsIgnoreCase(
                credito.codigoEstadoCartera()
        )) {

            throw new IllegalStateException(
                    "El crédito "
                            + credito.idCarteraCredito()
                            + " no se encuentra pendiente de desembolso."
            );
        }
    }

    private void validarIntegridadPendiente(
            SolicitudDesembolsoRepository.CreditoDesembolsoDatos credito
    ) {

        validarPendiente(
                credito
        );

        if (normalizar(
                credito.valorDesembolsado()
        ).signum() != 0) {

            throw new IllegalStateException(
                    "El crédito está pendiente, pero ya tiene "
                            + "valor desembolsado."
            );
        }

        if (normalizar(
                credito.saldoActual()
        ).signum() != 0) {

            throw new IllegalStateException(
                    "El crédito está pendiente, pero ya tiene "
                            + "saldo actual."
            );
        }
    }

    private void validarPorcentaje(
            BigDecimal porcentaje,
            String nombre
    ) {

        if (porcentaje == null
                || porcentaje.signum() < 0) {

            throw new IllegalStateException(
                    "El porcentaje de "
                            + nombre
                            + " no es válido."
            );
        }
    }

    private void validarTexto(
            String valor,
            String mensaje
    ) {

        if (valor == null
                || valor.isBlank()) {

            throw new IllegalArgumentException(
                    mensaje
            );
        }
    }

    // =========================================================
    // ORDEN DE EDAD
    // =========================================================

    private int ordenEdadRiesgo(
            String edad
    ) {

        return switch (
                edad.trim().toUpperCase()
                ) {
            case "A" -> 1;
            case "B" -> 2;
            case "C" -> 3;
            case "D" -> 4;
            case "E" -> 5;

            default ->
                    throw new IllegalStateException(
                            "Edad de riesgo no reconocida: "
                                    + edad
                    );
        };
    }

    // =========================================================
    // NORMALIZACIÓN
    // =========================================================

    private BigDecimal normalizar(
            BigDecimal valor
    ) {

        if (valor == null) {
            return BigDecimal.ZERO.setScale(
                    2,
                    RoundingMode.HALF_UP
            );
        }

        return valor.setScale(
                2,
                RoundingMode.HALF_UP
        );
    }

    private BigDecimal normalizarPorcentaje(
            BigDecimal valor
    ) {

        if (valor == null) {
            return BigDecimal.ZERO.setScale(
                    4,
                    RoundingMode.HALF_UP
            );
        }

        return valor.setScale(
                4,
                RoundingMode.HALF_UP
        );
    }

    // =========================================================
    // DTO - BANDEJA
    // =========================================================

    public record CreditoPendienteDesembolso(
            Integer idCarteraCredito,
            Integer idSolicitudCredito,

            Integer idAgencia,
            Integer idLineaCredito,
            Integer idDatosPersonal,

            String pagareCartera,

            String documento,
            String nombreCompleto,

            String codigoLineaCredito,
            String nombreLineaCredito,

            BigDecimal valorCredito,
            Integer plazo,

            LocalDate fechaInclusionSistema,
            LocalDate fechaPrimeraCuota,
            LocalDate fechaFinal,

            String codigoEstadoCartera
    ) {
    }

    // =========================================================
    // DTO - PREPARACIÓN
    // =========================================================

    public record PreparacionDesembolso(
            Integer idCarteraCredito,
            Integer idSolicitudCredito,

            Integer idAgencia,
            Integer idLineaCredito,
            Integer idDatosPersonal,
            Integer idCuentaAportes,

            String pagareCartera,

            String documento,
            String nombreCompleto,

            String codigoLineaCredito,
            String nombreLineaCredito,

            BigDecimal valorCredito,

            // -------------------------------------------------
            // COMPROBANTE 1
            // -------------------------------------------------

            BigDecimal porcentajeFondo,
            BigDecimal valorFondo,

            BigDecimal porcentajeIva,
            BigDecimal ivaFondo,

            BigDecimal totalComprobante1,
            BigDecimal valorDistribuir,

            // -------------------------------------------------
            // COMPROBANTE 2
            // -------------------------------------------------

            Integer cantidadCentralSugerida,
            BigDecimal valorUnitarioCentral,
            BigDecimal valorCentralSugerido,

            BigDecimal valorFijoPapeleria,
            BigDecimal porcentajePapeleria,
            BigDecimal valorPapeleria,
            BigDecimal ivaPapeleria,

            BigDecimal valorFijoAportes,
            BigDecimal porcentajeAportes,
            BigDecimal valorAportes,

            BigDecimal totalComprobante2,
            BigDecimal contrapartidaCaja,

            LocalDate fechaPrimeraCuota,
            LocalDate fechaFinal
    ) {
    }

    // =========================================================
    // DTO - LIQUIDACIÓN
    // =========================================================

    public record LiquidacionDesembolso(
            BigDecimal valorCredito,

            // -------------------------------------------------
            // COMPROBANTE 1
            // -------------------------------------------------

            BigDecimal valorFondo,
            BigDecimal ivaFondo,

            BigDecimal totalComprobante1,
            BigDecimal valorDistribuir,

            // -------------------------------------------------
            // COMPROBANTE 2
            // -------------------------------------------------

            Integer cantidadCentral,
            BigDecimal valorUnitarioCentral,
            BigDecimal valorCentral,

            BigDecimal valorPapeleria,
            BigDecimal ivaPapeleria,

            BigDecimal valorAportes,

            BigDecimal totalComprobante2,
            BigDecimal contrapartidaCaja
    ) {
    }

    // =========================================================
// DTO - RESULTADO EJECUCIÓN DESEMBOLSO
// =========================================================

    public record ResultadoEjecucionDesembolso(
            Integer idCarteraCredito,
            String pagareCartera,

            BigDecimal valorCredito,
            BigDecimal valorDistribuido,
            BigDecimal totalCobros,

            String tipoComprobanteDesembolso,
            String numeroComprobanteDesembolso,

            String tipoComprobanteCobros,
            String numeroComprobanteCobros,

            LocalDate fechaContable,
            LocalDate fechaDesembolso,

            String edadRiesgoInicial,

            String codigoModificacionCredito,
            Integer numeroNovaciones
    ) {
    }

    // =========================================================
    // DTO - NOVACIÓN
    // =========================================================

    public record DatosNovacion(
            String codigoModificacionCredito,
            Integer numeroNovaciones,

            Integer idCarteraCreditoAnterior,
            String pagareAnterior
    ) {
    }

}
