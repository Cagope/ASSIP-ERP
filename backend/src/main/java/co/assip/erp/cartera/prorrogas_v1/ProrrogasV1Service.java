
package co.assip.erp.cartera.prorrogas_v1;

import co.assip.erp.cartera.pago_cuotas_v1.PagoCuotasV1Service;
import co.assip.erp.cartera.pago_cuotas_v1.motor.PagoCuotasMotorRepository;
import co.assip.erp.cartera.pago_cuotas_v1.motor.PagoCuotasMotorService;
import co.assip.erp.cartera.pago_cuotas_v1.motor.dto.PagoCuotasMedioRecaudoDTO;
import co.assip.erp.cartera.prorrogas_v1.dto.ProrrogasV1RequestDTO;
import co.assip.erp.cartera.prorrogas_v1.dto.ProrrogasV1ResponseDTO;
import co.assip.erp.cajas.movimientos.CajasMovimientosService;
import co.assip.erp.contabilidad.auxiliares_contables.ContabilidadRegistroService;
import co.assip.erp.contabilidad.auxiliares_contables.dto.MovimientoContableDTO;
import co.assip.erp.contabilidad.consecutivos_comprobantes.ConsecutivosComprobantesService;
import co.assip.erp.contabilidad.origen_comprobantes.dto.OrigenComprobanteDTO;
import co.assip.erp.depositos.movimientos.DepositosMovimientoService;
import co.assip.erp.depositos.movimientos.dto.DepositosMovimientoDTO;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.tesoreria.operaciones_pendientes.TesoreriaOperacionesPendientesService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Coordina la validación, contabilización y aplicación financiera de Prórrogas V1. */
@Service
@RequiredArgsConstructor
public class ProrrogasV1Service {

    private final ProrrogasV1Repository repository;
    private final PagoCuotasMotorService pagoCuotasMotorService;
    private final PagoCuotasMotorRepository pagoCuotasMotorRepository;
    private final PagoCuotasV1Service pagoCuotasV1Service;
    private final ProrrogasV1LiquidacionService prorrogasV1LiquidacionService;
    private final UsuarioSesionService usuarioSesionService;
    private final CajasMovimientosService cajasMovimientosService;
    private final DepositosMovimientoService depositosMovimientoService;
    private final ContabilidadRegistroService contabilidadRegistroService;
    private final ConsecutivosComprobantesService consecutivosComprobantesService;
    private final TesoreriaOperacionesPendientesService tesoreriaOperacionesPendientesService;

    /**
     * Ejecuta la prórroga y su recaudo en una sola transacción.
     * Ante cualquier error se revierten los registros financieros.
     */
    @Transactional
    public ProrrogasV1ResponseDTO ejecutarProrroga(
            ProrrogasV1RequestDTO solicitud,
            LocalDate fechaContable) {

        validarConceptos(solicitud);
        validarFechaContable(fechaContable);

        var credito = repository.bloquearCredito(solicitud.idCarteraCredito());
        validarCreditoElegible(credito, solicitud.idCarteraCredito());
        usuarioSesionService.validarAgencia(credito.idAgencia());
        validarLiquidacionFinanciera(solicitud, fechaContable);
        validarCapital(solicitud.valorCapital(), credito.saldoActual());

        Integer idUsuario = usuarioSesionService.idUsuario();
        BigDecimal total = totalSolicitado(solicitud);

        var recaudo = pagoCuotasMotorService.validarRecaudoProrrogaConBloqueo(
                credito.idCredito(),
                credito.idAgencia(),
                total,
                solicitud.mediosRecaudo());

        List<MovimientoContableDTO> movimientos =
                construirMovimientos(solicitud, fechaContable,
                        credito.idAgencia(), recaudo.medios());

        validarCuadreContable(movimientos, total);

        FechasProrroga fechas = calcularFechas(
                credito, solicitud.mesesProrroga());

        int numeroProrroga = repository.siguienteNumeroProrroga(
                credito.idCredito());

        String numeroCC = consecutivosComprobantesService.generarNumeroDefinitivo(
                "CC", credito.idAgencia(), idUsuario);

        var registro = new ProrrogasV1Repository.RegistroProrroga(
                credito.idCredito(),
                numeroProrroga,
                credito.idAgencia(),
                idUsuario,
                fechaContable,
                fechas.dias(),
                solicitud.mesesProrroga(),
                credito.proximaFechaCapital(),
                credito.proximaFechaInteres(),
                credito.fechaFinal(),
                credito.plazo(),
                fechas.capitalNueva(),
                fechas.interesNueva(),
                fechas.vencimientoNuevo(),
                credito.plazo(),
                solicitud.valorInteresCorriente(),
                importe(solicitud.valorInteresMora()),
                importe(solicitud.valorSeguro()),
                importe(solicitud.valorAportes()),
                importe(solicitud.valorFondoGarantia()),
                importe(solicitud.valorOtrosConceptos()),
                total,
                "CC",
                numeroCC,
                fechaContable,
                solicitud.observacion());

        Long idProrroga = registrarOtorgamientoRecaudado(
                solicitud, registro);

        String soporte = "PR-" + idProrroga;

        if (soporte.length() > 20) {
            throw new IllegalStateException(
                    "El documento soporte excede la longitud permitida.");
        }

        for (var movimiento : movimientos) {
            movimiento.setDocumentoSoporte(soporte);
        }

        var origen = new OrigenComprobanteDTO();
        origen.setOrigenTipo("AUTO");
        origen.setModuloOrigen("CARTERA");
        origen.setProcesoOrigen("PRORROGA");
        origen.setTablaOrigen("cartera.creditos_prorrogas");
        origen.setIdOrigen(idProrroga);

        contabilidadRegistroService.registrarComprobante(
                credito.idAgencia(),
                "CC",
                numeroCC,
                "Prórroga crédito " + credito.idCredito()
                        + " / " + numeroProrroga,
                origen,
                movimientos,
                idUsuario);

        BigDecimal efectivo = BigDecimal.ZERO;
        BigDecimal ahorros = BigDecimal.ZERO;
        BigDecimal banco = BigDecimal.ZERO;
        Long idCaja = null;

        List<PagoCuotasMedioRecaudoDTO> cuentas = new ArrayList<>();

        for (var medio : recaudo.medios()) {
            switch (medio.tipoMedio()) {
                case CAJA -> {
                    efectivo = efectivo.add(medio.valor());
                    idCaja = medio.idCaja();
                }
                case AHORROS -> {
                    ahorros = ahorros.add(medio.valor());
                    cuentas.add(medio);
                }
                case CXP -> banco = banco.add(medio.valor());
            }
        }

        cuentas.sort(Comparator.comparing(
                PagoCuotasMedioRecaudoDTO::idCuentaAhorro));

        for (var medio : cuentas) {
            var dto = new DepositosMovimientoDTO();
            dto.setIdCuentaAhorro(
                    Math.toIntExact(medio.idCuentaAhorro()));
            dto.setFechaMovimiento(fechaContable);
            dto.setTipoComprobante("CC");
            dto.setNumeroComprobante(numeroCC);
            dto.setTipoMovimiento("772");
            dto.setModulo("03");
            dto.setValorDebito(medio.valor());
            dto.setValorCredito(BigDecimal.ZERO);
            dto.setTarjeta("N");
            dto.setModuloOrigen("CARTERA");
            dto.setProcesoOrigen("PRORROGA");
            dto.setIdOrigen(idProrroga);

            depositosMovimientoService.registrarDebito(
                    dto, idUsuario);
        }

        if (efectivo.signum() > 0) {
            cajasMovimientosService.registrarRecaudoProrroga(
                    idCaja,
                    efectivo,
                    fechaContable,
                    idProrroga,
                    String.valueOf(credito.idCredito()),
                    "CC",
                    numeroCC,
                    idUsuario);
        }

        if (banco.signum() > 0) {
            Integer titular = pagoCuotasV1Service
                    .consultarCredito(credito.idCredito())
                    .idDatosPersonal();

            tesoreriaOperacionesPendientesService.publicar(
                    credito.idAgencia(),
                    "CARTERA",
                    "PRORROGA",
                    "cartera.creditos_prorrogas",
                    idProrroga,
                    titular,
                    "ENTRADA",
                    fechaContable,
                    "RECAUDO CXC - Prórroga crédito "
                            + credito.idCredito()
                            + " (PR-" + idProrroga + ")",
                    banco,
                    idUsuario);
        }

        var extracto = new PagoCuotasMotorRepository.ExtractoPago(
                credito.idCredito(),
                fechaContable,
                "CC",
                numeroCC,
                importe(solicitud.valorCapital()),
                BigDecimal.ZERO,
                solicitud.valorInteresCorriente(),
                BigDecimal.ZERO,
                importe(solicitud.valorInteresMora()),
                0,
                importe(solicitud.valorSeguro()),
                importe(solicitud.valorAportes())
                        .add(importe(solicitud.valorFondoGarantia()))
                        .add(importe(solicitud.valorOtrosConceptos())),
                efectivo,
                BigDecimal.ZERO,
                ahorros,
                banco,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                null,
                "Prórroga " + numeroProrroga
                        + " / PR-" + idProrroga,
                credito.alturaCuota(),
                BigDecimal.ZERO,
                fechaContable,
                credito.ultimaFechaCapital(),
                credito.ultimaFechaInteres(),
                fechas.capitalNueva(),
                fechas.interesNueva(),
                idUsuario);

        Integer idExtracto = pagoCuotasMotorRepository
                .registrarExtracto(extracto);

        if (idExtracto == null || idExtracto <= 0) {
            throw new IllegalStateException(
                    "No fue posible registrar el extracto de prórroga.");
        }

        // Registra la trazabilidad del recaudo ya ejecutado, sin generar nuevos movimientos.
        // Los medios corresponden a los validados y bloqueados por el motor financiero.
        Map<String, Object> liquidacionPago = new LinkedHashMap<>();
        liquidacionPago.put("tipoOperacion", "PRORROGA");
        liquidacionPago.put("idCreditoProrroga", idProrroga);
        liquidacionPago.put("numeroProrroga", numeroProrroga);
        liquidacionPago.put("mesesProrroga", solicitud.mesesProrroga());
        liquidacionPago.put("diasProrroga", fechas.dias());
        liquidacionPago.put("valorCapital", importe(solicitud.valorCapital()));
        liquidacionPago.put("valorInteresCorriente", solicitud.valorInteresCorriente());
        liquidacionPago.put("valorInteresMora", importe(solicitud.valorInteresMora()));
        liquidacionPago.put("valorSeguro", importe(solicitud.valorSeguro()));
        liquidacionPago.put("valorAportes", importe(solicitud.valorAportes()));
        liquidacionPago.put("valorFondoGarantia", importe(solicitud.valorFondoGarantia()));
        liquidacionPago.put("valorOtrosConceptos", importe(solicitud.valorOtrosConceptos()));
        liquidacionPago.put("valorTotalPagado", total);

        Long idPagoProrroga = repository.registrarPagoProrroga(
                credito.idCredito(),
                credito.idAgencia(),
                fechaContable,
                fechaContable,
                solicitud.mesesProrroga(),
                total,
                liquidacionPago,
                recaudo.medios(),
                idExtracto,
                "CC",
                numeroCC,
                idUsuario);
        if (idPagoProrroga == null || idPagoProrroga <= 0) {
            throw new IllegalStateException(
                    "No fue posible registrar el pago asociado a la prórroga.");
        }

        return new ProrrogasV1ResponseDTO(
                idProrroga,
                credito.idCredito(),
                numeroProrroga,
                solicitud.mesesProrroga(),
                fechas.dias(),
                fechaContable,
                credito.proximaFechaCapital(),
                fechas.capitalNueva(),
                credito.proximaFechaInteres(),
                fechas.interesNueva(),
                null,
                null,
                solicitud.valorInteresCorriente(),
                importe(solicitud.valorInteresMora()),
                importe(solicitud.valorSeguro()),
                importe(solicitud.valorAportes()),
                importe(solicitud.valorFondoGarantia()),
                importe(solicitud.valorOtrosConceptos()),
                total,
                "CC",
                numeroCC,
                fechaContable,
                "A",
                "Prórroga aplicada correctamente.");
    }

    /** Bloquea y valida el crédito solicitado. */
    @Transactional
    public ProrrogasV1Repository.CreditoProrroga validarSolicitud(
            ProrrogasV1RequestDTO solicitud) {

        validarConceptos(solicitud);

        var credito = repository.bloquearCredito(
                solicitud.idCarteraCredito());

        validarCreditoElegible(
                credito, solicitud.idCarteraCredito());

        usuarioSesionService.validarAgencia(credito.idAgencia());

        return credito;
    }

    /** Valida y bloquea los recursos financieros del recaudo. */
    @Transactional
    public PagoCuotasMotorService.OperacionRecaudoValidada validarRecaudo(
            ProrrogasV1RequestDTO solicitud,
            Integer idAgenciaReceptora) {

        validarConceptos(solicitud);

        var credito = repository.bloquearCredito(
                solicitud.idCarteraCredito());

        validarCreditoElegible(
                credito, solicitud.idCarteraCredito());

        usuarioSesionService.validarAgencia(credito.idAgencia());

        if (!Objects.equals(
                credito.idAgencia(), idAgenciaReceptora)) {
            throw new IllegalArgumentException(
                    "La agencia receptora no corresponde al crédito.");
        }

        validarCapital(
                solicitud.valorCapital(), credito.saldoActual());

        return pagoCuotasMotorService
                .validarRecaudoProrrogaConBloqueo(
                        credito.idCredito(),
                        credito.idAgencia(),
                        totalSolicitado(solicitud),
                        solicitud.mediosRecaudo());
    }

    /** Previsualiza el comprobante sin generar consecutivo ni registrar pagos. */
    @Transactional
    public List<MovimientoContableDTO> previsualizarComprobante(
            ProrrogasV1RequestDTO solicitud,
            LocalDate fechaContable) {

        validarConceptos(solicitud);
        validarFechaContable(fechaContable);

        var credito = repository.bloquearCredito(
                solicitud.idCarteraCredito());

        validarCreditoElegible(
                credito, solicitud.idCarteraCredito());

        usuarioSesionService.validarAgencia(credito.idAgencia());

        validarLiquidacionFinanciera(solicitud, fechaContable);
        validarCapital(
                solicitud.valorCapital(), credito.saldoActual());

        calcularFechas(credito, solicitud.mesesProrroga());

        BigDecimal total = totalSolicitado(solicitud);

        pagoCuotasMotorService.validarOperacionRecaudo(
                total,
                solicitud.mediosRecaudo(),
                credito.idAgencia());

        List<MovimientoContableDTO> movimientos =
                construirMovimientos(
                        solicitud,
                        fechaContable,
                        credito.idAgencia(),
                        solicitud.mediosRecaudo());

        validarCuadreContable(movimientos, total);

        return movimientos;
    }

    /**
     * Registra la prórroga y actualiza capital y fechas.
     * Requiere una transacción financiera previamente iniciada.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public Long registrarOtorgamientoRecaudado(
            ProrrogasV1RequestDTO solicitud,
            ProrrogasV1Repository.RegistroProrroga registro) {

        validarConceptos(solicitud);

        if (registro == null
                || registro.idCredito() == null
                || !registro.idCredito().equals(
                solicitud.idCarteraCredito())) {
            throw new IllegalArgumentException(
                    "El registro no corresponde al crédito solicitado.");
        }

        if (!"CC".equals(registro.tipoComprobante())
                || registro.numeroComprobante() == null
                || registro.numeroComprobante().isBlank()
                || registro.fechaComprobante() == null) {
            throw new IllegalArgumentException(
                    "El recaudo debe tener un comprobante CC válido.");
        }

        if (registro.fechaProrroga() == null
                || registro.proximaCapitalAnterior() == null
                || registro.proximaCapitalNueva() == null
                || registro.vencimientoAnterior() == null
                || registro.vencimientoNuevo() == null
                || registro.diasProrroga() == null
                || registro.diasProrroga() <= 0
                || !Objects.equals(
                registro.mesesProrroga(),
                solicitud.mesesProrroga())) {
            throw new IllegalArgumentException(
                    "Las fechas de la prórroga son inválidas.");
        }

        int diasEsperados = Math.multiplyExact(
                solicitud.mesesProrroga(), 30);

        if (registro.diasProrroga() != diasEsperados) {
            throw new IllegalArgumentException(
                    "Los días de prórroga no coinciden con el plazo solicitado.");
        }

        if (registro.totalPagado() == null
                || registro.totalPagado().compareTo(
                totalSolicitado(solicitud)) != 0) {
            throw new IllegalArgumentException(
                    "El total recaudado no coincide con los conceptos.");
        }

        var credito = repository.bloquearCredito(
                solicitud.idCarteraCredito());

        validarCreditoElegible(
                credito, solicitud.idCarteraCredito());

        usuarioSesionService.validarAgencia(credito.idAgencia());

        if (!Objects.equals(
                usuarioSesionService.idUsuario(),
                registro.idUsuario())) {
            throw new SecurityException(
                    "El usuario del registro no corresponde a la sesión.");
        }

        if (!Objects.equals(
                credito.idAgencia(), registro.idAgencia())
                || !Objects.equals(
                credito.proximaFechaCapital(),
                registro.proximaCapitalAnterior())
                || !Objects.equals(
                credito.proximaFechaInteres(),
                registro.proximaInteresAnterior())
                || !Objects.equals(
                credito.fechaFinal(),
                registro.vencimientoAnterior())
                || !Objects.equals(
                credito.plazo(),
                registro.plazoAnterior())
                || !Objects.equals(
                registro.plazoAnterior(),
                registro.plazoNuevo())) {
            throw new IllegalStateException(
                    "La agencia o las condiciones del crédito cambiaron.");
        }

        FechasProrroga fechas = calcularFechas(
                credito, solicitud.mesesProrroga());

        if (!Objects.equals(
                fechas.capitalNueva(),
                registro.proximaCapitalNueva())
                || !Objects.equals(
                fechas.interesNueva(),
                registro.proximaInteresNueva())
                || !Objects.equals(
                fechas.vencimientoNuevo(),
                registro.vencimientoNuevo())) {
            throw new IllegalStateException(
                    "Las nuevas fechas no coinciden con la prórroga calculada.");
        }

        if (!Objects.equals(
                registro.fechaProrroga(),
                registro.fechaComprobante())) {
            throw new IllegalArgumentException(
                    "La fecha de prórroga debe coincidir con la fecha contable.");
        }

        validarLiquidacionFinanciera(
                solicitud, registro.fechaProrroga());

        validarCapital(
                solicitud.valorCapital(), credito.saldoActual());

        int consecutivo = repository.siguienteNumeroProrroga(
                solicitud.idCarteraCredito());

        if (!Objects.equals(
                consecutivo, registro.numeroProrroga())) {
            throw new IllegalStateException(
                    "El consecutivo de la prórroga cambió.");
        }

        Long idProrroga = repository.registrarProrroga(registro);

        BigDecimal capital = importe(solicitud.valorCapital());

        if (capital.signum() > 0) {
            repository.aplicarAbonoCapitalProrroga(
                    credito.idCredito(),
                    credito.idAgencia(),
                    credito.saldoActual(),
                    capital,
                    registro.idUsuario());
        }

        repository.actualizarFechasProrroga(
                registro.idCredito(),
                registro.idAgencia(),
                registro.proximaCapitalAnterior(),
                registro.proximaInteresAnterior(),
                registro.proximaCapitalNueva(),
                registro.proximaInteresNueva(),
                registro.vencimientoAnterior(),
                registro.vencimientoNuevo(),
                registro.idUsuario());

        return idProrroga;
    }

    /** Construye los movimientos contables con conceptos normalizados. */
    private List<MovimientoContableDTO> construirMovimientos(
            ProrrogasV1RequestDTO solicitud,
            LocalDate fechaContable,
            Integer idAgencia,
            List<PagoCuotasMedioRecaudoDTO> medios) {

        return pagoCuotasMotorService
                .construirMovimientosContablesProrroga(
                        solicitud.idCarteraCredito(),
                        idAgencia,
                        fechaContable,
                        medios,
                        importe(solicitud.valorCapital()),
                        solicitud.valorInteresCorriente(),
                        importe(solicitud.valorInteresMora()),
                        importe(solicitud.valorSeguro()),
                        importe(solicitud.valorAportes()),
                        importe(solicitud.valorFondoGarantia()),
                        importe(solicitud.valorOtrosConceptos()));
    }

    /** Comprueba que débitos y créditos coincidan con el recaudo total. */
    private static void validarCuadreContable(
            List<MovimientoContableDTO> movimientos,
            BigDecimal total) {

        if (movimientos == null || movimientos.isEmpty()) {
            throw new IllegalStateException(
                    "No se generaron movimientos contables.");
        }

        BigDecimal debitos = BigDecimal.ZERO;
        BigDecimal creditos = BigDecimal.ZERO;

        for (var movimiento : movimientos) {
            if (movimiento == null) {
                throw new IllegalStateException(
                        "El comprobante contiene movimientos inválidos.");
            }

            BigDecimal debito = importe(
                    movimiento.getValorDebito());
            BigDecimal credito = importe(
                    movimiento.getValorCredito());

            if (debito.signum() < 0 || credito.signum() < 0
                    || (debito.signum() > 0
                    && credito.signum() > 0)) {
                throw new IllegalStateException(
                        "El comprobante contiene un movimiento inválido.");
            }

            debitos = debitos.add(debito);
            creditos = creditos.add(credito);
        }

        if (debitos.compareTo(total) != 0
                || creditos.compareTo(total) != 0) {
            throw new IllegalStateException(
                    "El comprobante de prórroga no cuadra con el recaudo.");
        }
    }

    /** Calcula y valida las fechas nuevas sin modificar plazo ni altura. */
    private static FechasProrroga calcularFechas(
            ProrrogasV1Repository.CreditoProrroga credito,
            Integer mesesProrroga) {

        if (credito.proximaFechaCapital() == null
                || credito.proximaFechaInteres() == null
                || credito.fechaFinal() == null) {
            throw new IllegalStateException(
                    "El crédito no tiene todas las fechas contractuales.");
        }

        int dias = Math.multiplyExact(mesesProrroga, 30);

        return new FechasProrroga(
                dias,
                credito.proximaFechaCapital().plusDays(dias),
                credito.proximaFechaInteres().plusDays(dias),
                credito.fechaFinal().plusDays(dias));
    }

    /**
     * Recalcula los conceptos obligatorios con información del servidor.
     * Impide alterar intereses o mora y limita los seguros pendientes.
     */
    private void validarLiquidacionFinanciera(
            ProrrogasV1RequestDTO solicitud,
            LocalDate fechaContable) {

        validarFechaContable(fechaContable);

        var liquidacion = prorrogasV1LiquidacionService.liquidar(
                solicitud.idCarteraCredito(),
                fechaContable);

        if (solicitud.valorInteresCorriente().compareTo(
                liquidacion.valorInteresCorriente()) != 0) {
            throw new IllegalArgumentException(
                    "Los intereses corrientes no coinciden "
                            + "con la liquidación del servidor.");
        }

        if (importe(solicitud.valorInteresMora()).compareTo(
                liquidacion.valorInteresMora()) != 0) {
            throw new IllegalArgumentException(
                    "Los intereses de mora no coinciden "
                            + "con la liquidación del servidor.");
        }

        if (importe(solicitud.valorSeguro()).compareTo(
                liquidacion.valorSeguro()) > 0) {
            throw new IllegalArgumentException(
                    "El recaudo de seguros supera el saldo pendiente.");
        }
    }

    /** Verifica el estado, saldo y ausencia de otra prórroga activa. */
    private void validarCreditoElegible(
            ProrrogasV1Repository.CreditoProrroga credito,
            Integer idCredito) {

        if (!"A".equals(credito.estado())
                || credito.saldoActual() == null
                || credito.saldoActual().signum() <= 0) {
            throw new IllegalArgumentException(
                    "El crédito no está activo o no tiene saldo pendiente.");
        }

        if (repository.existeProrrogaActiva(idCredito)) {
            throw new IllegalArgumentException(
                    "El crédito ya tiene una prórroga activa.");
        }
    }

    /** Valida capital opcional sin permitir cancelar totalmente el crédito. */
    private static void validarCapital(
            BigDecimal capital,
            BigDecimal saldo) {

        if (saldo == null || saldo.signum() <= 0
                || importe(capital).compareTo(saldo) >= 0) {
            throw new IllegalArgumentException(
                    "El abono a capital debe ser inferior al saldo del crédito.");
        }
    }

    /** Comprueba los conceptos y el período solicitado. */
    private static void validarConceptos(
            ProrrogasV1RequestDTO solicitud) {

        if (solicitud == null
                || solicitud.idCarteraCredito() == null
                || solicitud.idCarteraCredito() <= 0) {
            throw new IllegalArgumentException(
                    "Debe seleccionar un crédito válido.");
        }

        if (solicitud.mesesProrroga() == null
                || !List.of(1, 2, 3).contains(
                solicitud.mesesProrroga())) {
            throw new IllegalArgumentException(
                    "La prórroga debe ser de 30, 60 o 90 días.");
        }

        validarImporte(
                solicitud.valorInteresCorriente(),
                "intereses corrientes",
                true);
        validarImporte(
                solicitud.valorInteresMora(),
                "intereses de mora",
                true);
        validarImporte(
                solicitud.valorCapital(),
                "capital",
                false);
        validarImporte(
                solicitud.valorSeguro(),
                "seguros",
                false);
        validarImporte(
                solicitud.valorAportes(),
                "aportes",
                false);
        validarImporte(
                solicitud.valorFondoGarantia(),
                "fondo de garantía",
                false);
        validarImporte(
                solicitud.valorOtrosConceptos(),
                "otros conceptos",
                false);
    }

    /** Valida importes enteros en pesos, incluyendo valores iguales a cero. */
    private static void validarImporte(
            BigDecimal valor,
            String concepto,
            boolean obligatorio) {

        if (valor == null) {
            if (obligatorio) {
                throw new IllegalArgumentException(
                        "Debe indicar el valor de " + concepto + ".");
            }
            return;
        }

        if (valor.signum() < 0
                || valor.stripTrailingZeros().scale() > 0) {
            throw new IllegalArgumentException(
                    "Valor inválido para " + concepto + ".");
        }
    }

    /** Valida la fecha utilizada en liquidación y contabilización. */
    private static void validarFechaContable(
            LocalDate fechaContable) {

        if (fechaContable == null) {
            throw new IllegalArgumentException(
                    "Debe indicar la fecha contable.");
        }
    }

    /** Suma todos los conceptos del recaudo. */
    private static BigDecimal totalSolicitado(
            ProrrogasV1RequestDTO solicitud) {

        return importe(solicitud.valorInteresCorriente())
                .add(importe(solicitud.valorInteresMora()))
                .add(importe(solicitud.valorCapital()))
                .add(importe(solicitud.valorSeguro()))
                .add(importe(solicitud.valorAportes()))
                .add(importe(solicitud.valorFondoGarantia()))
                .add(importe(solicitud.valorOtrosConceptos()));
    }

    /** Normaliza los importes opcionales ausentes a cero. */
    private static BigDecimal importe(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }

    /** Fechas resultantes de extender el calendario contractual. */
    private record FechasProrroga(
            int dias,
            LocalDate capitalNueva,
            LocalDate interesNueva,
            LocalDate vencimientoNuevo) {
    }
}
