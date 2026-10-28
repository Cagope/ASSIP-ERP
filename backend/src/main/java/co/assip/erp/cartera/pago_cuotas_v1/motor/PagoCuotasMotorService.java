package co.assip.erp.cartera.pago_cuotas_v1.motor;

import co.assip.erp.cartera.pago_cuotas_v1.motor.dto.PagoCuotasMedioRecaudoDTO;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.tesoreria.operaciones_pendientes.TesoreriaOperacionesPendientesService;
import lombok.RequiredArgsConstructor;
import co.assip.erp.contabilidad.plan_cuentas.PlanCuentaRepository;
import org.springframework.stereotype.Service;

import co.assip.erp.cartera.pago_cuotas_v1.PagoCuotasV1Service;
import co.assip.erp.cartera.pago_cuotas_v1.PagoCuotasV1Repository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import co.assip.erp.cajas.movimientos.CajasMovimientosService;
import co.assip.erp.depositos.movimientos.DepositosMovimientoService;
import co.assip.erp.depositos.movimientos.dto.DepositosMovimientoDTO;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import co.assip.erp.contabilidad.auxiliares_contables.ContabilidadRegistroService;
import co.assip.erp.contabilidad.consecutivos_comprobantes.ConsecutivosComprobantesService;
import co.assip.erp.contabilidad.auxiliares_contables.dto.MovimientoContableDTO;
import co.assip.erp.contabilidad.origen_comprobantes.dto.OrigenComprobanteDTO;

/**
 * Preparación y validación de operaciones de pago de cuotas.
 * Cartera aplica financieramente el pago; Caja confirma únicamente el movimiento físico.
 */
@Service
@RequiredArgsConstructor
public class PagoCuotasMotorService {

    private final PagoCuotasMotorRepository repository;
    private final UsuarioSesionService usuarioSesionService;
    private final PlanCuentaRepository planCuentaRepository;
    private final PagoCuotasV1Service pagoCuotasV1Service;
    private final ObjectMapper objectMapper;
    private final CajasMovimientosService cajasMovimientosService;
    private final DepositosMovimientoService depositosMovimientoService;
    private final ContabilidadRegistroService contabilidadRegistroService;
    private final ConsecutivosComprobantesService consecutivosComprobantesService;
    private final TesoreriaOperacionesPendientesService tesoreriaOperacionesPendientesService;

    /** Cajas receptoras habilitadas en la agencia de operación. */
    public List<PagoCuotasMotorRepository.CajaRecaudo> listarCajasRecaudo(Integer idAgencia) {
        return repository.listarCajasRecaudo(idAgencia);
    }

    /**
     * Lista cuentas de captación tipo 2 del titular y agencia del crédito.
     * Verifica acceso a la agencia; no reserva ni modifica fondos.
     */
    @Transactional(readOnly = true)
    public List<PagoCuotasMotorRepository.CuentaAhorroRecaudo> listarCuentasAhorroRecaudo(
            Integer idCarteraCredito) {
        if (idCarteraCredito == null || idCarteraCredito <= 0) {
            throw new IllegalArgumentException("Debe seleccionar un crédito válido.");
        }
        var credito = pagoCuotasV1Service.consultarCredito(idCarteraCredito);
        usuarioSesionService.validarAgencia(credito.idAgencia());
        return repository.listarCuentasAhorroRecaudo(idCarteraCredito);
    }

    /** Valida el medio y la caja seleccionada antes de preparar el recaudo. */
    public PagoCuotasMedioRecaudoDTO validarMedioRecaudo(
            PagoCuotasMedioRecaudoDTO medio, Integer idAgenciaReceptora) {
        validarDatosMedio(medio);
        if (medio.tipoMedio() == PagoCuotasMedioRecaudoDTO.TipoMedioRecaudo.CAJA) {
            if (idAgenciaReceptora == null || idAgenciaReceptora <= 0) {
                throw new IllegalArgumentException("Debe indicar una agencia receptora válida.");
            }
            repository.obtenerCajaRecaudo(medio.idCaja(), idAgenciaReceptora)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "La caja seleccionada no está habilitada para recibir recaudos en esta agencia."));
        }
        return medio;
    }

    /**
     * Valida todos los medios como una única operación, sin persistirla.
     * La confirmación física de Caja es independiente de la aplicación financiera.
     */
    public OperacionRecaudoValidada validarOperacionRecaudo(
            BigDecimal totalLiquidado,
            List<PagoCuotasMedioRecaudoDTO> medios,
            Integer idAgenciaReceptora) {

        validarPesosEnterosPositivos(totalLiquidado, "El valor total liquidado");
        if (medios == null || medios.isEmpty()) {
            throw new IllegalArgumentException("Debe indicar al menos un medio de recaudo.");
        }

        BigDecimal totalMedios = BigDecimal.ZERO;
        boolean requiereConfirmacionCaja = false;

        var mediosUtilizados = new HashSet<String>();

        for (PagoCuotasMedioRecaudoDTO medio : medios) {
            validarMedioRecaudo(medio, idAgenciaReceptora);

            String identificadorMedio = switch (medio.tipoMedio()) {
                case CAJA -> "CAJA:" + medio.idCaja();
                case AHORROS -> "AHORROS:" + medio.idCuentaAhorro();
                case CXP -> "CXP:" + medio.idCuentaPorPagar();
            };

            if (!mediosUtilizados.add(identificadorMedio)) {
                throw new IllegalArgumentException(
                        "El medio de recaudo está repetido: " + identificadorMedio
                );
            }

            totalMedios = totalMedios.add(medio.valor());
            if (medio.tipoMedio() == PagoCuotasMedioRecaudoDTO.TipoMedioRecaudo.CAJA) {
                requiereConfirmacionCaja = true;
            }
        }

        if (totalMedios.compareTo(totalLiquidado) != 0) {
            throw new IllegalArgumentException(
                    "La suma de los medios de recaudo debe coincidir con el valor total liquidado.");
        }

        return new OperacionRecaudoValidada(
                totalLiquidado,
                List.copyOf(medios),
                requiereConfirmacionCaja);
    }

    /** Resultado inmutable de la validación; no representa un pago registrado. */
    public record OperacionRecaudoValidada(
            BigDecimal total,
            List<PagoCuotasMedioRecaudoDTO> medios,
            boolean requiereConfirmacionCaja) {
    }

    private void validarDatosMedio(PagoCuotasMedioRecaudoDTO medio) {
        if (medio == null || medio.tipoMedio() == null) {
            throw new IllegalArgumentException("Debe seleccionar el medio de recaudo.");
        }

        validarPesosEnterosPositivos(medio.valor(), "El valor del recaudo");

        switch (medio.tipoMedio()) {
            case CAJA -> {
                exigirIdentificador(medio.idCaja(), "caja");
                exigirAusente(medio.idCuentaAhorro(), "cuenta de ahorro");
                exigirAusente(medio.idCuentaPorPagar(), "cuenta por pagar");
            }
            case AHORROS -> {
                exigirIdentificador(medio.idCuentaAhorro(), "cuenta de ahorro");
                exigirAusente(medio.idCaja(), "caja");
                exigirAusente(medio.idCuentaPorPagar(), "cuenta por pagar");
            }
            case CXP -> {
                exigirIdentificador(medio.idCuentaPorPagar(), "cuenta por pagar");
                exigirAusente(medio.idCaja(), "caja");
                exigirAusente(medio.idCuentaAhorro(), "cuenta de ahorro");
            }
        }
    }

    private static void validarPesosEnterosPositivos(BigDecimal valor, String nombre) {
        if (valor == null || valor.signum() <= 0 || valor.stripTrailingZeros().scale() > 0) {
            throw new IllegalArgumentException(nombre + " debe ser mayor que cero y expresado en pesos enteros.");
        }
    }

    private static void exigirIdentificador(Long id, String nombre) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Debe seleccionar la " + nombre + ".");
        }
    }

    private static void exigirAusente(Long id, String nombre) {
        if (id != null) {
            throw new IllegalArgumentException("No debe indicar " + nombre + " para el medio seleccionado.");
        }
    }

    /**
     * Prepara una operación integral de pago de cuotas.
     *
     * Reutiliza la simulación financiera existente y valida que
     * los medios de recaudo cubran exactamente el valor del pago.
     *
     * No registra operaciones ni modifica información financiera.
     */
    @Transactional
    public OperacionPagoPreparada prepararOperacionPago(
            Integer idCarteraCredito,
            LocalDate fechaPago,
            Integer periodoInteresMeses,
            Integer cuotasACancelar,
            BigDecimal valorPago,
            Integer idAgenciaReceptora,
            List<PagoCuotasMedioRecaudoDTO> medios) {

        return prepararOperacionPago(idCarteraCredito, fechaPago, periodoInteresMeses,
                cuotasACancelar, valorPago, idAgenciaReceptora, medios, false);
    }

    /** Prepara un pago ordinario o una cancelación total, según la modalidad solicitada. */
    @Transactional
    public OperacionPagoPreparada prepararOperacionPago(
            Integer idCarteraCredito,
            LocalDate fechaPago,
            Integer periodoInteresMeses,
            Integer cuotasACancelar,
            BigDecimal valorPago,
            Integer idAgenciaReceptora,
            List<PagoCuotasMedioRecaudoDTO> medios,
            boolean pagoTotal) {

        if (pagoTotal && cuotasACancelar != null) {
            throw new IllegalArgumentException(
                    "El pago total no debe indicar una cantidad de cuotas a cancelar.");
        }

        if (idCarteraCredito == null || idCarteraCredito <= 0) {
            throw new IllegalArgumentException(
                    "Debe seleccionar un crédito válido.");
        }

        if (fechaPago == null) {
            throw new IllegalArgumentException(
                    "Debe indicar la fecha del pago.");
        }

        if (periodoInteresMeses == null || periodoInteresMeses <= 0) {
            throw new IllegalArgumentException(
                    "Debe indicar un período de intereses válido.");
        }

        if (idAgenciaReceptora == null || idAgenciaReceptora <= 0) {
            throw new IllegalArgumentException(
                    "Debe indicar una agencia receptora válida.");
        }

        // Reutiliza la distribución financiera ya probada.
        PagoCuotasV1Service.SimulacionPago simulacion =
                pagoTotal
                        ? pagoCuotasV1Service.simularPagoTotal(
                        idCarteraCredito, fechaPago, periodoInteresMeses, valorPago)
                        : pagoCuotasV1Service.simularPago(
                        idCarteraCredito, fechaPago, periodoInteresMeses,
                        cuotasACancelar, valorPago);

        // Valida los medios contra el valor realmente simulado.
        OperacionRecaudoValidada recaudo =
                validarOperacionRecaudo(
                        simulacion.valorPago(),
                        medios,
                        idAgenciaReceptora
                );

        // Calcula el efecto proyectado del pago sobre el crédito.
        // No modifica información en PostgreSQL.
        ResultadoAplicacion aplicacion =
                calcularAplicacion(
                        idCarteraCredito,
                        simulacion,
                        periodoInteresMeses
                );

        return new OperacionPagoPreparada(
                idCarteraCredito,
                fechaPago,
                periodoInteresMeses,
                cuotasACancelar,
                idAgenciaReceptora,
                simulacion,
                recaudo,
                aplicacion,
                recaudo.requiereConfirmacionCaja(),
                pagoTotal
        );
    }

    /**
     * Resultado integral de preparación de un pago.
     *
     * Incluye la simulación financiera, los medios de recaudo
     * y el resultado proyectado de aplicación al crédito.
     *
     * No representa un pago registrado.
     */
    public record OperacionPagoPreparada(
            Integer idCarteraCredito,
            LocalDate fechaPago,
            Integer periodoInteresMeses,
            Integer cuotasACancelar,
            Integer idAgenciaReceptora,
            PagoCuotasV1Service.SimulacionPago simulacion,
            OperacionRecaudoValidada recaudo,
            ResultadoAplicacion aplicacion,
            boolean requiereConfirmacionCaja,
            boolean pagoTotal
    ) {
    }

    /**
     * Registra una operación preparada con recaudo en efectivo.
     *
     * Registra el pago, crea el movimiento pendiente de Caja y aplica
     * financieramente Cartera con comprobante CC en una única transacción.
     * Caja únicamente confirmará posteriormente la recepción física.
     */
    @Transactional
    public Long enviarPagoPendienteCaja(
            OperacionPagoPreparada operacion,
            LocalDate fechaContable,
            String pagareCartera,
            Integer idUsuario
    ) {
        if (operacion == null) {
            throw new IllegalArgumentException(
                    "Debe preparar la operación de pago.");
        }

        if (!operacion.requiereConfirmacionCaja()) {
            throw new IllegalArgumentException(
                    "La operación no requiere confirmación de Caja.");
        }

        if (fechaContable == null) {
            throw new IllegalArgumentException(
                    "Debe indicar la fecha contable.");
        }

        if (idUsuario == null || idUsuario <= 0) {
            throw new IllegalArgumentException(
                    "El usuario de sesión es obligatorio.");
        }

        // Bloquear el crédito durante el registro del pago.
        var credito = repository.bloquearCredito(
                operacion.idCarteraCredito()
        );

        if (!"A".equals(credito.estado())) {
            throw new IllegalStateException(
                    "El crédito ya no se encuentra activo.");
        }

        // Evitar múltiples pagos pendientes sobre el mismo crédito.
        if (repository.existePagoPendienteCaja(
                operacion.idCarteraCredito())) {

            throw new IllegalStateException(
                    "El crédito ya tiene un pago pendiente de confirmación en Caja. "
                            + "Debe confirmar o anular ese pago antes de registrar otro.");
        }

        if (credito.saldo().compareTo(
                operacion.aplicacion().saldoAnterior()) != 0) {
            throw new IllegalStateException(
                    "El saldo del crédito cambió. Debe recalcular el pago.");
        }

        // Verificar que las fechas financieras no hayan cambiado.
        // Verificar que la altura y las fechas financieras
        // originales no hayan cambiado desde la preparación.
        if (!Objects.equals(
                credito.alturaCuota(),
                operacion.aplicacion().alturaCuotaActual())
                || !Objects.equals(
                credito.ultimaFechaCapital(),
                operacion.aplicacion().ultimaFechaCapitalActual())
                || !Objects.equals(
                credito.ultimaFechaInteres(),
                operacion.aplicacion().ultimaFechaInteresActual())
                || !Objects.equals(
                credito.proximaFechaCapital(),
                operacion.aplicacion().proximaFechaCapitalActual())
                || !Objects.equals(
                credito.proximaFechaInteres(),
                operacion.aplicacion().proximaFechaInteresActual())) {

            throw new IllegalStateException(
                    "El cronograma del crédito cambió. Debe recalcular el pago.");
        }

        if (operacion.pagoTotal()) {
            comprobar(operacion.aplicacion().cancelaCredito()
                            && operacion.aplicacion().saldoNuevo().signum() == 0,
                    "La liquidación total no cancela completamente el crédito.");
        }

        // Validar nuevamente los medios de recaudo
        // antes de registrar la operación pendiente.
        OperacionRecaudoValidada recaudoValidado =
                validarOperacionRecaudo(
                        operacion.simulacion().valorPago(),
                        operacion.recaudo().medios(),
                        operacion.idAgenciaReceptora()
                );

        if (!recaudoValidado.requiereConfirmacionCaja()) {
            throw new IllegalStateException(
                    "La operación ya no contiene un recaudo válido por Caja.");
        }

        // Caja produce el movimiento físico; AHORROS y CxC se contabilizan en Cartera.
        // La CxC se publicará en Tesorería después de aplicar financieramente el pago.
        bloquearCuentasAhorroRecaudo(operacion.idCarteraCredito(), recaudoValidado.medios());

        // Continuar con la identificación del efectivo.
        BigDecimal efectivo = BigDecimal.ZERO;
        Long idCaja = null;

        for (PagoCuotasMedioRecaudoDTO medio : operacion.recaudo().medios()) {
            if (medio.tipoMedio()
                    == PagoCuotasMedioRecaudoDTO.TipoMedioRecaudo.CAJA) {
                efectivo = efectivo.add(medio.valor());
                idCaja = medio.idCaja();
            }
        }

        if (idCaja == null || efectivo.signum() <= 0) {
            throw new IllegalStateException(
                    "No existe un recaudo en efectivo válido.");
        }

        // Se valida nuevamente la caja antes de persistir.
        repository.obtenerCajaRecaudo(
                idCaja,
                operacion.idAgenciaReceptora()
        ).orElseThrow(() -> new IllegalStateException(
                "La caja ya no está habilitada para este recaudo."));

        // Se conserva la modalidad junto a la simulación para validarla en Caja.
        var liquidacion = java.util.Map.of(
                "simulacion", operacion.simulacion(),
                "aplicacion", operacion.aplicacion(),
                "pagoTotal", operacion.pagoTotal()
        );

        Long idPago = repository.registrarPagoPendienteCaja(
                operacion.idCarteraCredito(),
                operacion.idAgenciaReceptora(),
                operacion.fechaPago(),
                fechaContable,
                operacion.periodoInteresMeses(),
                operacion.cuotasACancelar(),
                operacion.simulacion().valorPago(),
                liquidacion,
                operacion.recaudo().medios(),
                idCaja,
                idUsuario
        );

        // Solo el efectivo genera movimiento físico de Caja.
        Long idMovimientoCaja =
                cajasMovimientosService.registrarRecaudoCuotasCartera(
                        idCaja,
                        efectivo,
                        fechaContable,
                        idPago,
                        pagareCartera,
                        null,
                        null,
                        idUsuario
                );

        repository.vincularMovimientoCaja(
                idPago,
                idMovimientoCaja
        );

        // Cartera registra el CC, el extracto y la aplicación financiera
        // antes de entregar a Caja el movimiento físico pendiente.
        PagoValidado pagoValidado = validarPendiente(idPago, idMovimientoCaja, idCaja);
        contabilizarPagoCaja(pagoValidado, "CC", idUsuario);
        publicarCxCTesoreria(idPago, operacion.idCarteraCredito(),
                operacion.idAgenciaReceptora(), operacion.fechaPago(),
                recaudoValidado.medios(), idUsuario);

        return idPago;
    }

    /**
     * Ejecuta un pago íntegro mediante una o varias cuentas de AHORROS.
     * Bloquea el crédito y las cuentas en orden estable; registra depósitos,
     * contabilidad y cartera en la misma transacción, sin intervención de Caja.
     */
    @Transactional
    public Long ejecutarPagoAhorros(OperacionPagoPreparada operacion,
                                    LocalDate fechaContable, Integer idUsuario) {
        comprobar(operacion != null && operacion.recaudo() != null
                        && operacion.simulacion() != null && operacion.aplicacion() != null,
                "Debe preparar una operación de pago válida.");
        comprobar(fechaContable != null && idUsuario != null && idUsuario > 0,
                "La fecha contable y el usuario son obligatorios.");
        usuarioSesionService.validarAgencia(operacion.idAgenciaReceptora());
        var credito = repository.bloquearCredito(operacion.idCarteraCredito());
        comprobar("A".equals(credito.estado()), "El crédito ya no está activo.");
        comprobar(credito.idAgencia().equals(operacion.idAgenciaReceptora()),
                "El recaudo por ahorros debe corresponder a la agencia del crédito.");
        comprobar(!repository.existePagoPendienteCaja(operacion.idCarteraCredito()),
                "El crédito tiene un pago pendiente de confirmación en Caja.");
        var aplicacion = operacion.aplicacion();
        comprobar(igual(credito.saldo(), aplicacion.saldoAnterior())
                        && Objects.equals(credito.alturaCuota(), aplicacion.alturaCuotaActual())
                        && Objects.equals(credito.ultimaFechaCapital(), aplicacion.ultimaFechaCapitalActual())
                        && Objects.equals(credito.ultimaFechaInteres(), aplicacion.ultimaFechaInteresActual())
                        && Objects.equals(credito.proximaFechaCapital(), aplicacion.proximaFechaCapitalActual())
                        && Objects.equals(credito.proximaFechaInteres(), aplicacion.proximaFechaInteresActual()),
                "El crédito cambió. Debe recalcular el pago.");
        if (operacion.pagoTotal()) {
            comprobar(aplicacion.cancelaCredito() && aplicacion.saldoNuevo().signum() == 0,
                    "La liquidación no cancela totalmente el crédito.");
        }
        var recaudo = validarOperacionRecaudo(operacion.simulacion().valorPago(),
                operacion.recaudo().medios(), operacion.idAgenciaReceptora());
        comprobar(!recaudo.requiereConfirmacionCaja()
                        && recaudo.medios().stream().allMatch(m ->
                        m.tipoMedio() == PagoCuotasMedioRecaudoDTO.TipoMedioRecaudo.AHORROS
                                || m.tipoMedio() == PagoCuotasMedioRecaudoDTO.TipoMedioRecaudo.CXP),
                "Esta operación sin Caja solo admite AHORROS y recaudo bancario.");
        var simulacion = operacion.simulacion();

        repository.bloquearYValidar(operacion.idCarteraCredito(),
                simulacion.segurosAplicados(), simulacion.aliviosAplicados(),
                simulacion.costasJudicialesAplicadas());

        // Bloquea únicamente las cuentas de ahorro, en orden estable.
        var mediosOrdenados = new java.util.ArrayList<PagoCuotasMedioRecaudoDTO>();
        for (var medio : recaudo.medios()) {
            if (medio.tipoMedio() == PagoCuotasMedioRecaudoDTO.TipoMedioRecaudo.AHORROS) {
                mediosOrdenados.add(medio);
            }
        }
        mediosOrdenados.sort(java.util.Comparator.comparing(PagoCuotasMedioRecaudoDTO::idCuentaAhorro));
        for (var medio : mediosOrdenados) {
            Integer idCuenta = Math.toIntExact(medio.idCuentaAhorro());
            BigDecimal saldo = repository.validarCuentaAhorroRecaudoConLock(
                    operacion.idCarteraCredito(), idCuenta);
            comprobar(saldo != null && saldo.compareTo(medio.valor()) >= 0,
                    "Saldo insuficiente en la cuenta de ahorro " + idCuenta + ".");
        }
        var creditoDatos = pagoCuotasV1Service.consultarCredito(operacion.idCarteraCredito());
        var movimientos = new java.util.ArrayList<>(construirMovimientosPagoIntegrado(
                operacion.idCarteraCredito(), operacion.idAgenciaReceptora(),
                fechaContable, recaudo.medios(), simulacion));
        BigDecimal debitos = BigDecimal.ZERO;
        BigDecimal creditos = BigDecimal.ZERO;
        for (var movimiento : movimientos) {
            debitos = debitos.add(movimiento.getValorDebito());
            creditos = creditos.add(movimiento.getValorCredito());
        }
        comprobar(igual(debitos, creditos) && igual(debitos, simulacion.valorPago()),
                "El comprobante del recaudo no cuadra con el pago.");

        var liquidacion = java.util.Map.of("simulacion", simulacion,
                "aplicacion", aplicacion, "pagoTotal", operacion.pagoTotal());
        Long idPago = repository.registrarPagoSinCaja(operacion.idCarteraCredito(),
                operacion.idAgenciaReceptora(), operacion.fechaPago(), fechaContable,
                operacion.periodoInteresMeses(), operacion.cuotasACancelar(),
                simulacion.valorPago(), liquidacion, recaudo.medios(), idUsuario);
        String numero = consecutivosComprobantesService.generarNumeroDefinitivo(
                "CC", operacion.idAgenciaReceptora(), idUsuario);
        String soporte = "PC-" + idPago;
        comprobar(soporte.length() <= 20, "El documento soporte excede la longitud permitida.");
        for (var movimiento : movimientos) {
            movimiento.setDocumentoSoporte(soporte);
        }
        var origen = new OrigenComprobanteDTO();
        origen.setOrigenTipo("AUTO");
        origen.setModuloOrigen("CARTERA");
        origen.setProcesoOrigen("PAGO_CUOTAS");
        origen.setTablaOrigen("cartera.pagos_cuotas");
        origen.setIdOrigen(idPago);
        contabilidadRegistroService.registrarComprobante(
                operacion.idAgenciaReceptora(), "CC", numero,
                conceptoGeneralPago(operacion.idCarteraCredito(), simulacion.valorPago()),
                origen, movimientos, idUsuario);

        for (var medio : mediosOrdenados) {
            var dto = new DepositosMovimientoDTO();
            dto.setIdCuentaAhorro(Math.toIntExact(medio.idCuentaAhorro()));
            dto.setFechaMovimiento(operacion.fechaPago());
            dto.setTipoComprobante("CC");
            dto.setNumeroComprobante(numero);
            dto.setTipoMovimiento("772");
            dto.setModulo("03");
            dto.setValorDebito(medio.valor());
            dto.setValorCredito(BigDecimal.ZERO);
            dto.setTarjeta("N");
            dto.setModuloOrigen("CARTERA");
            dto.setProcesoOrigen("PAGO_CUOTAS");
            dto.setIdOrigen(idPago);
            depositosMovimientoService.registrarDebito(dto, idUsuario);
        }

        repository.registrarAbonosAuxiliares(operacion.idCarteraCredito(),
                simulacion.segurosAplicados(), simulacion.aliviosAplicados(),
                simulacion.costasJudicialesAplicadas(), "CC", numero, idUsuario);
        BigDecimal totalAhorros = BigDecimal.ZERO;
        BigDecimal totalBancario = BigDecimal.ZERO;
        for (var medio : recaudo.medios()) {
            if (medio.tipoMedio() == PagoCuotasMedioRecaudoDTO.TipoMedioRecaudo.AHORROS) {
                totalAhorros = totalAhorros.add(medio.valor());
            } else if (medio.tipoMedio() == PagoCuotasMedioRecaudoDTO.TipoMedioRecaudo.CXP) {
                totalBancario = totalBancario.add(medio.valor());
            }
        }
        var extracto = new PagoCuotasMotorRepository.ExtractoPago(
                operacion.idCarteraCredito(), operacion.fechaPago(), "CC", numero,
                simulacion.capitalAplicado(), BigDecimal.ZERO,
                simulacion.interesesCorrientesAplicados(), BigDecimal.ZERO,
                simulacion.interesesMoraAplicados(),
                simulacion.liquidacion().mora().diasMora(), simulacion.segurosAplicados(),
                simulacion.aliviosAplicados().add(simulacion.costasJudicialesAplicadas()),
                BigDecimal.ZERO, BigDecimal.ZERO, totalAhorros,
                totalBancario, BigDecimal.ZERO, BigDecimal.ZERO,
                null, "Pago de cuotas mediante AHORROS y/o recaudo bancario",
                aplicacion.alturaCuotaNueva(), creditoDatos.abonosPendientes(), fechaContable,
                aplicacion.ultimaFechaCapitalNueva(), aplicacion.ultimaFechaInteresNueva(),
                aplicacion.proximaFechaCapitalNueva(), aplicacion.proximaFechaInteresNueva(), idUsuario);
        Integer idExtracto = repository.registrarExtracto(extracto);
        comprobar(idExtracto != null && idExtracto > 0,
                "No fue posible registrar el extracto de Cartera.");
        repository.actualizarCredito(aplicacion, idUsuario);
        repository.marcarPagoAhorrosAplicado(idPago, idExtracto, "CC", numero, idUsuario);
        publicarCxCTesoreria(idPago, operacion.idCarteraCredito(),
                operacion.idAgenciaReceptora(), operacion.fechaPago(),
                recaudo.medios(), idUsuario);
        return idPago;
    }

    /** Suma únicamente el componente CxC, sin incluir Caja ni AHORROS. */
    private static BigDecimal valorCxC(List<PagoCuotasMedioRecaudoDTO> medios) {
        return medios.stream()
                .filter(m -> m.tipoMedio() == PagoCuotasMedioRecaudoDTO.TipoMedioRecaudo.CXP)
                .map(PagoCuotasMedioRecaudoDTO::valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Publica la CxC aplicada por Cartera como entrada pendiente de recaudo en Tesorería. */
    private void publicarCxCTesoreria(Long idPago, Integer idCredito,
                                     Integer idAgencia, LocalDate fechaPago,
                                     List<PagoCuotasMedioRecaudoDTO> medios,
                                     Integer idUsuario) {
        BigDecimal valor = valorCxC(medios);
        if (valor.signum() == 0) {
            return;
        }
        Integer idDatosPersonal = pagoCuotasV1Service.consultarCredito(idCredito).idDatosPersonal();
        tesoreriaOperacionesPendientesService.publicar(
                idAgencia, "CARTERA", "PAGO_CUOTAS", "cartera.pagos_cuotas",
                idPago, idDatosPersonal, "ENTRADA", fechaPago,
                "RECAUDO CXC - " + conceptoGeneralPago(idCredito, valor) + " (PC-" + idPago + ")",
                valor, idUsuario);
    }

    /** Bloquea AHORROS en orden estable y valida saldo antes de registrar Caja y Cartera. */
    private void bloquearCuentasAhorroRecaudo(Integer idCredito,
                                              List<PagoCuotasMedioRecaudoDTO> medios) {
        var ahorros = new java.util.ArrayList<PagoCuotasMedioRecaudoDTO>();
        for (var medio : medios) {
            if (medio.tipoMedio() == PagoCuotasMedioRecaudoDTO.TipoMedioRecaudo.AHORROS) {
                ahorros.add(medio);
            }
        }
        ahorros.sort(java.util.Comparator.comparing(PagoCuotasMedioRecaudoDTO::idCuentaAhorro));
        for (var medio : ahorros) {
            Integer idCuenta = Math.toIntExact(medio.idCuentaAhorro());
            BigDecimal saldo = repository.validarCuentaAhorroRecaudoConLock(idCredito, idCuenta);
            comprobar(saldo != null && saldo.compareTo(medio.valor()) >= 0,
                    "Saldo insuficiente en la cuenta de ahorro " + idCuenta + ".");
        }
    }

    // Proyección financiera integrada, sin cambiar las fórmulas validadas.
    private ResultadoAplicacion calcularAplicacion(
            Integer idCarteraCredito,
            PagoCuotasV1Service.SimulacionPago simulacion,
            int periodoInteresMeses) {

        if (periodoInteresMeses <= 0) {
            throw new IllegalArgumentException(
                    "El período de intereses debe ser positivo.");
        }

        if (idCarteraCredito == null || idCarteraCredito <= 0) {
            throw new IllegalArgumentException(
                    "Debe seleccionar un crédito válido.");
        }

        if (simulacion == null || simulacion.liquidacion() == null) {
            throw new IllegalArgumentException(
                    "Debe suministrar una simulación válida.");
        }

        if (!idCarteraCredito.equals(
                simulacion.liquidacion().idCarteraCredito())) {
            throw new IllegalArgumentException(
                    "La simulación no corresponde al crédito seleccionado.");
        }

        PagoCuotasV1Repository.CreditoLiquidacion credito =
                pagoCuotasV1Service.consultarCredito(idCarteraCredito);

        if (!"A".equals(credito.codigoEstadoCartera())) {
            throw new IllegalArgumentException(
                    "El crédito no se encuentra activo.");
        }

        BigDecimal saldoAnterior = requeridoNoNegativo(
                credito.saldoActual(), "saldo actual");

        BigDecimal capital = requeridoNoNegativo(
                simulacion.capitalAplicado(), "capital aplicado");

        BigDecimal intereses = requeridoNoNegativo(
                simulacion.interesesCorrientesAplicados(),
                "intereses corrientes");

        BigDecimal mora = requeridoNoNegativo(
                simulacion.interesesMoraAplicados(),
                "intereses de mora");

        BigDecimal seguros = requeridoNoNegativo(
                simulacion.segurosAplicados(), "seguros");

        BigDecimal alivios = requeridoNoNegativo(
                simulacion.aliviosAplicados(), "alivios");

        BigDecimal costasJudiciales = requeridoNoNegativo(
                simulacion.costasJudicialesAplicadas(), "costas judiciales");

        BigDecimal costasPendientes = requeridoNoNegativo(
                simulacion.liquidacion().costasJudicialesPendientes(),
                "costas judiciales liquidadas");

        if (costasJudiciales.compareTo(costasPendientes) > 0) {
            throw new IllegalArgumentException(
                    "Las costas judiciales aplicadas superan el saldo liquidado.");
        }

        BigDecimal valorPago = requeridoNoNegativo(
                simulacion.valorPago(), "valor del pago");

        BigDecimal sumaConceptos = capital
                .add(intereses)
                .add(mora)
                .add(seguros)
                .add(alivios)
                .add(costasJudiciales);

        if (sumaConceptos.compareTo(valorPago) != 0) {
            throw new IllegalArgumentException(
                    "La distribución de conceptos no coincide con el valor del pago.");
        }

        if (capital.compareTo(saldoAnterior) > 0) {
            throw new IllegalArgumentException(
                    "El capital aplicado supera el saldo del crédito.");
        }

        if (saldoAnterior.compareTo(
                requeridoNoNegativo(
                        simulacion.liquidacion().saldoInicial(),
                        "saldo liquidado")) != 0) {
            throw new IllegalStateException(
                    "El saldo del crédito cambió desde la liquidación. Debe recalcular el pago.");
        }

        BigDecimal saldoNuevo = saldoAnterior.subtract(capital);

        if (saldoNuevo.compareTo(
                requeridoNoNegativo(
                        simulacion.saldoCapitalProyectado(),
                        "saldo proyectado")) != 0) {
            throw new IllegalArgumentException(
                    "El saldo proyectado no coincide con el capital aplicado.");
        }

        // Saldo de capital cero no equivale a cancelación de todas las obligaciones.
        // Los saldos auxiliares se validan nuevamente bajo bloqueo al confirmar.
        var saldosAuxiliares = repository.bloquearYValidar(
                idCarteraCredito, seguros, alivios, costasJudiciales);
        boolean auxiliaresSaldados =
                saldosAuxiliares.seguros().compareTo(seguros) == 0
                        && saldosAuxiliares.alivios().compareTo(alivios) == 0
                        && saldosAuxiliares.costasJudiciales().compareTo(costasJudiciales) == 0;
        boolean cancelaCredito = saldoNuevo.signum() == 0 && auxiliaresSaldados
                && intereses.compareTo(simulacion.liquidacion().interesesCorrientes()) >= 0
                && mora.compareTo(simulacion.liquidacion().mora().interesesMora()) >= 0;

        boolean cubreCapitalExigible =
                capital.compareTo(
                        requeridoNoNegativo(
                                simulacion.liquidacion().capitalExigible(),
                                "capital exigible")) >= 0;

        // La existencia de prórrogas históricas no prueba
        // que haya una vigente.
        // La aplicación transaccional debe consultar
        // el último registro en estado A.
        boolean consultarProrrogaVigente =
                repository.tieneProrrogaVigente(idCarteraCredito);

        boolean pagoCubreConceptosSimulados =
                intereses.compareTo(
                        requeridoNoNegativo(
                                simulacion.liquidacion().interesesCorrientes(),
                                "intereses liquidados")) >= 0

                        && mora.compareTo(
                        requeridoNoNegativo(
                                simulacion.liquidacion().mora().interesesMora(),
                                "mora liquidada")) >= 0

                        && seguros.compareTo(
                        requeridoNoNegativo(
                                simulacion.liquidacion().segurosPendientes(),
                                "seguros liquidados")) >= 0

                        && alivios.compareTo(
                        requeridoNoNegativo(
                                simulacion.liquidacion().cuotaAlivios(),
                                "alivios liquidados")) >= 0

                        && costasJudiciales.compareTo(costasPendientes) >= 0;

        ProyeccionCuotas proyeccion = proyectarCuotas(
                credito,
                simulacion,
                periodoInteresMeses
        );

        return new ResultadoAplicacion(
                idCarteraCredito,
                saldoAnterior,
                capital,
                saldoNuevo,
                cancelaCredito ? "C" : "A",
                cancelaCredito,
                cubreCapitalExigible,
                simulacion.abonoAdicionalCapital(),
                simulacion.capitalInferiorAlLiquidado(),
                consultarProrrogaVigente,
                pagoCubreConceptosSimulados && cubreCapitalExigible,
                credito.alturaCuota(),
                credito.ultimaFechaCapital(),
                credito.ultimaFechaInteres(),
                credito.proximaFechaCapital(),
                credito.proximaFechaInteres(),
                proyeccion.alturaCuotaNueva(),
                proyeccion.ultimaFechaCapitalNueva(),
                proyeccion.ultimaFechaInteresNueva(),
                proyeccion.proximaFechaCapitalNueva(),
                proyeccion.proximaFechaInteresNueva()
        );
    }

    /**
     * Proyecta las fechas dentro del mismo servicio de aplicación.
     */
    private ProyeccionCuotas proyectarCuotas(
            PagoCuotasV1Repository.CreditoLiquidacion credito,
            PagoCuotasV1Service.SimulacionPago simulacion,
            int periodoInteresMeses) {

        int cuotas = simulacion.liquidacion().cuotasConsideradas();

        if (cuotas < 0
                || credito.amortizacionCapital() == null
                || credito.amortizacionCapital() <= 0) {
            throw new IllegalArgumentException(
                    "Período o número de cuotas inválido.");
        }

        LocalDate fechaPago = simulacion.liquidacion().fechaPago();

        if (fechaPago == null) {
            throw new IllegalArgumentException(
                    "La fecha de pago es obligatoria.");
        }

        BigDecimal capital = requeridoNoNegativo(
                simulacion.capitalAplicado(), "capital aplicado");

        BigDecimal intereses = requeridoNoNegativo(
                simulacion.interesesCorrientesAplicados(),
                "intereses aplicados");

        boolean fija = "1".equals(credito.codigoTipoCuota());
        boolean variable = "2".equals(credito.codigoTipoCuota());

        if (!fija && !variable) {
            throw new IllegalArgumentException(
                    "Tipo de cuota no soportado.");
        }

        int altura = credito.alturaCuota() == null
                ? 0
                : credito.alturaCuota();

        LocalDate ultimaCapital = credito.ultimaFechaCapital();
        LocalDate ultimaInteres = credito.ultimaFechaInteres();
        LocalDate proximaCapital = credito.proximaFechaCapital();
        LocalDate proximaInteres = credito.proximaFechaInteres();

        boolean avanzaCapital = capital.signum() > 0;

        if (variable && avanzaCapital) {

            BigDecimal cuotaReferencia = altura == 0
                    ? credito.valorPrimeraCuota()
                    : credito.valorCuota();

            if (cuotaReferencia == null
                    || cuotaReferencia.signum() <= 0) {
                throw new IllegalStateException(
                        "No existe una cuota de capital válida para el crédito.");
            }

            avanzaCapital =
                    capital.compareTo(cuotaReferencia) >= 0;
        }

        if (avanzaCapital) {
            ultimaCapital = fechaPago;

            proximaCapital = avanzarMeses(
                    proximaCapital,
                    cuotas,
                    credito.amortizacionCapital()
            );

            altura = Math.addExact(altura, cuotas);
        }

        if (intereses.signum() > 0) {
            ultimaInteres = fechaPago;

            proximaInteres = avanzarMeses(
                    proximaInteres,
                    cuotas,
                    periodoInteresMeses
            );
        }

        return new ProyeccionCuotas(
                altura,
                ultimaCapital,
                ultimaInteres,
                proximaCapital,
                proximaInteres
        );
    }

    private static LocalDate avanzarMeses(
            LocalDate fecha,
            int cuotas,
            int meses) {

        return fecha == null || cuotas == 0
                ? fecha
                : fecha.plusMonths(
                Math.multiplyExact(cuotas, meses));
    }

    private record ProyeccionCuotas(
            int alturaCuotaNueva,
            LocalDate ultimaFechaCapitalNueva,
            LocalDate ultimaFechaInteresNueva,
            LocalDate proximaFechaCapitalNueva,
            LocalDate proximaFechaInteresNueva) {
    }

    private static BigDecimal requeridoNoNegativo(
            BigDecimal valor,
            String concepto) {

        if (valor == null || valor.signum() < 0) {
            throw new IllegalArgumentException(
                    "Valor inválido para " + concepto + ".");
        }

        return valor;
    }

    /**
     * Proyección sin persistencia.
     *
     * Conserva las fechas y la altura originales para validar
     * concurrencia, y las proyectadas para aplicar el pago.
     *
     * candidatoRegularizacionProrroga NO autoriza por sí solo
     * cerrar una prórroga: la operación final debe validar
     * el estado y la cobertura real.
     */

    // Validación de confirmación integrada; sin aplicar aún movimientos financieros.
    @Transactional
    public PagoValidado validarPendiente(Long idPago, Long idMovimientoCaja, Long idCaja) {
        // Orden de bloqueos: movimiento de Caja (en la bandeja), pago y crédito.
        var pago = repository.bloquearPagoPendienteCaja(idPago, idMovimientoCaja, idCaja);
        var credito = repository.bloquearCredito(pago.idCarteraCredito());

        if (!"A".equals(credito.estado())) {
            throw new IllegalStateException("El crédito ya no está activo; no se puede aplicar el pago.");
        }
        if (!credito.idAgencia().equals(pago.idAgenciaReceptora())) {
            throw new IllegalStateException("La agencia del crédito no coincide con la receptora; "
                    + "el pago interagencia requiere tratamiento contable específico.");
        }

        try {
            JsonNode raiz = objectMapper.readTree(pago.liquidacionJson());
            JsonNode simulacionNodo = raiz.required("simulacion");
            JsonNode aplicacionNodo = raiz.required("aplicacion");
            var simulacion = objectMapper.treeToValue(
                    simulacionNodo, PagoCuotasV1Service.SimulacionPago.class);
            var aplicacion = objectMapper.treeToValue(
                    aplicacionNodo, ResultadoAplicacion.class);
            if (simulacion == null || aplicacion == null || simulacion.liquidacion() == null) {
                throw new IllegalStateException("La liquidación almacenada está incompleta.");
            }
            if (raiz.path("pagoTotal").asBoolean(false)) {
                comprobar(aplicacion.cancelaCredito()
                                && aplicacion.saldoNuevo().signum() == 0
                                && igual(simulacion.capitalAplicado(), credito.saldo()),
                        "El pago total pendiente no cancela completamente el crédito.");
            }

            comprobar(pago.idCarteraCredito().equals(aplicacion.idCarteraCredito())
                            && pago.idCarteraCredito().equals(simulacion.liquidacion().idCarteraCredito()),
                    "La liquidación no corresponde al crédito.");
            comprobar(pago.fechaPago().equals(simulacion.liquidacion().fechaPago()),
                    "La fecha del pago no coincide con la liquidación.");
            comprobar(igual(pago.valorPago(), simulacion.valorPago()),
                    "El valor del pago no coincide con la liquidación.");
            comprobar(igual(credito.saldo(), aplicacion.saldoAnterior())
                            && igual(credito.saldo(), simulacion.liquidacion().saldoInicial()),
                    "El saldo del crédito cambió desde la preparación.");
            comprobar(Objects.equals(credito.alturaCuota(), aplicacion.alturaCuotaActual())
                            && Objects.equals(credito.ultimaFechaCapital(), aplicacion.ultimaFechaCapitalActual())
                            && Objects.equals(credito.ultimaFechaInteres(), aplicacion.ultimaFechaInteresActual())
                            && Objects.equals(credito.proximaFechaCapital(), aplicacion.proximaFechaCapitalActual())
                            && Objects.equals(credito.proximaFechaInteres(), aplicacion.proximaFechaInteresActual()),
                    "El cronograma del crédito cambió desde la preparación.");

            BigDecimal suma = simulacion.capitalAplicado()
                    .add(simulacion.interesesCorrientesAplicados())
                    .add(simulacion.interesesMoraAplicados())
                    .add(simulacion.segurosAplicados())
                    .add(simulacion.aliviosAplicados())
                    .add(simulacion.costasJudicialesAplicadas());
            comprobar(igual(suma, pago.valorPago()), "Los conceptos no cuadran con el recaudo.");
            // Revalidar saldos previamente causados bajo bloqueo transaccional.
            // No causa, descuenta ni registra valores; evita confirmar una
            // liquidación obsoleta si hubo otro recaudo de seguros o alivios.
            repository.bloquearYValidar(
                    pago.idCarteraCredito(),
                    simulacion.segurosAplicados(),
                    simulacion.aliviosAplicados(),
                    simulacion.costasJudicialesAplicadas());

            // Resolver las cuentas reales antes de cualquier actualización financiera.
            // No se permite confirmar si falta parametrización o existen cuentas ambiguas.
            var datosContables = repository.consultarDatosCredito(
                    pago.idCarteraCredito());
            comprobar(datosContables.idAgencia().equals(pago.idAgenciaReceptora()),
                    "La parametrización contable pertenece a otra agencia.");
            if (simulacion.capitalAplicado().signum() > 0) {
                repository.resolverCuenta(datosContables, "CAPITAL", null);
            }
            if (simulacion.interesesCorrientesAplicados().signum() > 0) {
                repository.resolverCuenta(datosContables, "INTERES", null);
            }

            comprobar(igual(simulacion.capitalAplicado(), aplicacion.capitalAplicado())
                            && igual(credito.saldo().subtract(aplicacion.capitalAplicado()), aplicacion.saldoNuevo()),
                    "La aplicación de capital no coincide con la simulación.");

            var tipoLista = objectMapper.getTypeFactory().constructCollectionType(
                    List.class, PagoCuotasMedioRecaudoDTO.class);
            List<PagoCuotasMedioRecaudoDTO> medios = objectMapper.readValue(
                    pago.mediosRecaudoJson(), tipoLista);
            var recaudo = validarOperacionRecaudo(
                    pago.valorPago(), medios, pago.idAgenciaReceptora());
            comprobar(recaudo.requiereConfirmacionCaja(),
                    "El recaudo almacenado no contiene un medio de Caja válido.");
            BigDecimal efectivo = BigDecimal.ZERO;
            for (var medio : medios) {
                if (medio.tipoMedio() == PagoCuotasMedioRecaudoDTO.TipoMedioRecaudo.CAJA) {
                    comprobar(pago.idCaja().equals(medio.idCaja()),
                            "El recaudo pertenece a otra caja.");
                    efectivo = efectivo.add(medio.valor());
                }
            }
            comprobar(efectivo.signum() > 0, "No existe efectivo asociado a la operación.");
            return new PagoValidado(pago, simulacion, aplicacion, List.copyOf(medios), efectivo);
        } catch (com.fasterxml.jackson.core.JsonProcessingException ex) {
            throw new IllegalStateException("Los datos almacenados del pago no se pueden interpretar.", ex);
        }
    }

    /**
     * Aplica los movimientos de Cartera después de que el comprobante contable
     * haya sido generado en la misma transacción por el proceso coordinador.
     * No confirma movimientos físicos de Caja ni genera asientos contables.
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.MANDATORY)
    public Integer aplicarMovimientosCartera(
            PagoValidado validado,
            PagoCuotasMotorRepository.ExtractoPago extracto,
            Integer idUsuario) {

        if (validado == null || extracto == null || idUsuario == null || idUsuario <= 0) {
            throw new IllegalArgumentException("Pago, extracto y usuario son obligatorios.");
        }
        var pago = validado.pago();
        var simulacion = validado.simulacion();
        var aplicacion = validado.aplicacion();
        comprobar(pago != null && simulacion != null && aplicacion != null,
                "La operación validada está incompleta.");
        comprobar(pago.idCarteraCredito().equals(extracto.idCredito()),
                "El extracto corresponde a otro crédito.");
        comprobar(pago.fechaPago().equals(extracto.fechaPago()),
                "La fecha del extracto no coincide con el pago.");
        comprobar(igual(simulacion.capitalAplicado(), extracto.capital())
                        && igual(simulacion.interesesMoraAplicados(), extracto.mora())
                        && igual(simulacion.segurosAplicados(), extracto.seguro()),
                "Los conceptos del extracto no coinciden con la liquidación.");
        comprobar(extracto.tipoComprobante() != null && !extracto.tipoComprobante().isBlank()
                        && extracto.numeroComprobante() != null && !extracto.numeroComprobante().isBlank(),
                "Se requiere un comprobante contable generado.");
        comprobar(idUsuario.equals(extracto.idUsuario()),
                "El usuario del extracto no coincide con la operación.");
        // El extracto refleja el efectivo real y los débitos a AHORROS por separado.
        BigDecimal totalAhorros = validado.medios().stream()
                .filter(m -> m.tipoMedio() == PagoCuotasMedioRecaudoDTO.TipoMedioRecaudo.AHORROS)
                .map(PagoCuotasMedioRecaudoDTO::valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCxC = valorCxC(validado.medios());
        comprobar(igual(validado.efectivo(), extracto.efectivo())
                        && igual(totalAhorros, extracto.notas())
                        && igual(totalCxC, extracto.consignacion())
                        && igual(validado.efectivo().add(totalAhorros).add(totalCxC), pago.valorPago()),
                "Los medios del extracto no coinciden con el recaudo registrado.");

        var credito = repository.bloquearCredito(pago.idCarteraCredito());
        comprobar("A".equals(credito.estado()) && igual(credito.saldo(), aplicacion.saldoAnterior()),
                "El crédito cambió desde la validación. Debe recalcular el pago.");
        comprobar(Objects.equals(credito.alturaCuota(), aplicacion.alturaCuotaActual())
                        && Objects.equals(credito.ultimaFechaCapital(), aplicacion.ultimaFechaCapitalActual())
                        && Objects.equals(credito.ultimaFechaInteres(), aplicacion.ultimaFechaInteresActual())
                        && Objects.equals(credito.proximaFechaCapital(), aplicacion.proximaFechaCapitalActual())
                        && Objects.equals(credito.proximaFechaInteres(), aplicacion.proximaFechaInteresActual()),
                "El cronograma cambió desde la validación.");

        repository.registrarAbonosAuxiliares(
                pago.idCarteraCredito(),
                simulacion.segurosAplicados(),
                simulacion.aliviosAplicados(),
                simulacion.costasJudicialesAplicadas(),
                extracto.tipoComprobante(), extracto.numeroComprobante(), idUsuario);
        Integer idExtracto = repository.registrarExtracto(extracto);
        if (idExtracto == null || idExtracto <= 0) {
            throw new IllegalStateException("No se pudo registrar el extracto de Cartera.");
        }
        repository.actualizarCredito(aplicacion, idUsuario);
        // El estado financiero se finaliza en la misma transacción.
        // La recepción física del efectivo continúa siendo responsabilidad de Caja.
        repository.marcarPagoAplicado(
                pago.idPago(), pago.idMovimientoCaja(), idExtracto,
                extracto.tipoComprobante(), extracto.numeroComprobante(), idUsuario);
        return idExtracto;
    }

    /**
     * Registra el comprobante y aplica Cartera en una única transacción.
     * Las líneas contables deben venir resueltas y balanceadas por el módulo
     * financiero: no se deducen cuentas de Caja, Tesorería ni de conceptos
     * que todavía no tengan parametrización verificada.
     */
    @Transactional
    public Integer contabilizarYAplicar(
            PagoValidado validado,
            PagoCuotasMotorRepository.ExtractoPago extracto,
            String tipoComprobante,
            List<MovimientoContableDTO> movimientos,
            Integer idDatosPersonal,
            Integer idUsuario) {

        if (validado == null || validado.pago() == null || extracto == null
                || idUsuario == null || idUsuario <= 0
                || idDatosPersonal == null || idDatosPersonal <= 0
                || tipoComprobante == null || tipoComprobante.isBlank()
                || movimientos == null || movimientos.size() < 2) {
            throw new IllegalArgumentException("Faltan datos del comprobante o del pago.");
        }
        var pago = validado.pago();
        comprobar(pago.idCarteraCredito().equals(extracto.idCredito()),
                "El extracto corresponde a otro crédito.");
        comprobar(pago.fechaPago().equals(extracto.fechaPago()),
                "La fecha del extracto no coincide con el pago.");
        comprobar(extracto.fechaContable() != null,
                "La fecha contable es obligatoria.");
        comprobar(idUsuario.equals(extracto.idUsuario()),
                "El usuario no coincide con el extracto.");

        BigDecimal debitos = BigDecimal.ZERO;
        BigDecimal creditos = BigDecimal.ZERO;
        for (var movimiento : movimientos) {
            comprobar(movimiento != null && movimiento.getIdCatalogoCuenta() != null
                            && movimiento.getIdCatalogoCuenta() > 0,
                    "Todos los movimientos deben tener cuenta contable válida.");
            comprobar(movimiento.getIdDatosPersonal() == null
                            || idDatosPersonal.equals(movimiento.getIdDatosPersonal()),
                    "El tercero contable no coincide con el asociado.");
            comprobar(movimiento.getFechaAuxiliar() == null
                            || extracto.fechaContable().equals(movimiento.getFechaAuxiliar()),
                    "La fecha de un movimiento no coincide con la fecha contable.");
            BigDecimal d = movimiento.getValorDebito() == null
                    ? BigDecimal.ZERO : movimiento.getValorDebito();
            BigDecimal c = movimiento.getValorCredito() == null
                    ? BigDecimal.ZERO : movimiento.getValorCredito();
            comprobar(d.signum() >= 0 && c.signum() >= 0
                            && (d.signum() > 0 ^ c.signum() > 0),
                    "Cada línea contable debe tener un único importe positivo.");
            debitos = debitos.add(d);
            creditos = creditos.add(c);
        }
        comprobar(debitos.compareTo(creditos) == 0
                        && debitos.compareTo(pago.valorPago()) == 0,
                "El comprobante debe cuadrar con el valor total del pago.");

        // Los locks y las validaciones financieras se ejecutan antes de
        // reservar consecutivo y registrar auxiliares.
        var credito = repository.bloquearCredito(pago.idCarteraCredito());
        comprobar("A".equals(credito.estado())
                        && igual(credito.saldo(), validado.aplicacion().saldoAnterior()),
                "El crédito cambió. Debe recalcular el pago.");
        repository.bloquearYValidar(pago.idCarteraCredito(),
                validado.simulacion().segurosAplicados(),
                validado.simulacion().aliviosAplicados(),
                validado.simulacion().costasJudicialesAplicadas());

        String numero = consecutivosComprobantesService.generarNumeroDefinitivo(
                tipoComprobante, pago.idAgenciaReceptora(), idUsuario);
        var origen = new OrigenComprobanteDTO();
        origen.setOrigenTipo("AUTO");
        origen.setModuloOrigen("CARTERA");
        origen.setProcesoOrigen("PAGO_CUOTAS");
        origen.setTablaOrigen("cartera.pagos_cuotas");
        origen.setIdOrigen(pago.idPago());

        // El pago registrado es el documento soporte trazable de los auxiliares.
        // El motor contable exige máximo 20 caracteres para cuentas controladas.
        String documentoSoporte = "PC-" + pago.idPago();
        comprobar(documentoSoporte.length() <= 20,
                "El identificador del pago excede la longitud del documento soporte.");
        for (var movimiento : movimientos) {
            movimiento.setIdDatosPersonal(idDatosPersonal);
            movimiento.setFechaAuxiliar(extracto.fechaContable());
            movimiento.setDocumentoSoporte(documentoSoporte);
            if (movimiento.getDetalleMovimiento() == null
                    || movimiento.getDetalleMovimiento().isBlank()) {
                movimiento.setDetalleMovimiento(conceptoGeneralPago(pago.idCarteraCredito(), pago.valorPago()));
            }
        }
        contabilidadRegistroService.registrarComprobante(
                pago.idAgenciaReceptora(), tipoComprobante, numero,
                conceptoGeneralPago(pago.idCarteraCredito(), pago.valorPago()),
                origen, movimientos, idUsuario);

        // Registra cada débito de AHORROS una sola vez, ligado al mismo pago y CC.
        for (var medio : validado.medios()) {
            if (medio.tipoMedio() != PagoCuotasMedioRecaudoDTO.TipoMedioRecaudo.AHORROS) {
                continue;
            }
            var dto = new DepositosMovimientoDTO();
            dto.setIdCuentaAhorro(Math.toIntExact(medio.idCuentaAhorro()));
            dto.setFechaMovimiento(pago.fechaPago());
            dto.setTipoComprobante("CC");
            dto.setNumeroComprobante(numero);
            dto.setTipoMovimiento("772");
            dto.setModulo("03");
            dto.setValorDebito(medio.valor());
            dto.setValorCredito(BigDecimal.ZERO);
            dto.setTarjeta("N");
            dto.setModuloOrigen("CARTERA");
            dto.setProcesoOrigen("PAGO_CUOTAS");
            dto.setIdOrigen(pago.idPago());
            depositosMovimientoService.registrarDebito(dto, idUsuario);
        }

        var definitivo = new PagoCuotasMotorRepository.ExtractoPago(
                extracto.idCredito(), extracto.fechaPago(), tipoComprobante, numero,
                extracto.capital(), extracto.interesCausado(), extracto.interesIngreso(),
                extracto.interesAnticipado(), extracto.mora(), extracto.diasMora(), extracto.seguro(),
                extracto.otros(), extracto.efectivo(), extracto.cheques(),
                extracto.notas(), extracto.consignacion(), extracto.pse(),
                extracto.otroPago(), extracto.idCuentaAhorro(), extracto.observacion(),
                extracto.alturaCuota(), extracto.abonosPendientes(),
                extracto.fechaContable(), extracto.ultimaFechaCapital(),
                extracto.ultimaFechaInteres(), extracto.proximaFechaCapital(),
                extracto.proximaFechaInteres(), idUsuario);
        return aplicarMovimientosCartera(validado, definitivo, idUsuario);
    }

    /**
     * Construye el comprobante de recaudo en efectivo con cuentas parametrizadas
     * y reutiliza el registro contable y la aplicación financiera existentes.
     * Se ejecuta desde Cartera antes de la confirmación física de Caja;
     * nunca debe invocarse desde la bandeja de operaciones pendientes.
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.MANDATORY)
    public Integer contabilizarPagoCaja(
            PagoValidado validado,
            String tipoComprobante,
            Integer idUsuario) {

        if (validado == null || validado.pago() == null
                || validado.simulacion() == null || validado.aplicacion() == null
                || tipoComprobante == null || tipoComprobante.isBlank()
                || idUsuario == null || idUsuario <= 0) {
            throw new IllegalArgumentException("Datos incompletos para contabilizar el pago de Caja.");
        }
        var pago = validado.pago();
        var simulacion = validado.simulacion();
        var aplicacion = validado.aplicacion();
        comprobar(validado.efectivo().signum() > 0
                        && validado.efectivo().compareTo(pago.valorPago()) <= 0,
                "El efectivo de Caja debe ser positivo y no superar el pago.");

        var credito = pagoCuotasV1Service.consultarCredito(pago.idCarteraCredito());
        var movimientos = construirMovimientosPagoIntegrado(
                pago.idCarteraCredito(), pago.idAgenciaReceptora(),
                pago.fechaContable(), validado.medios(), simulacion);
        Integer idDatosPersonal = credito.idDatosPersonal();
        LocalDate fechaContable = pago.fechaContable();

        BigDecimal totalAhorros = validado.medios().stream()
                .filter(m -> m.tipoMedio() == PagoCuotasMedioRecaudoDTO.TipoMedioRecaudo.AHORROS)
                .map(PagoCuotasMedioRecaudoDTO::valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCxC = valorCxC(validado.medios());
        var extracto = new PagoCuotasMotorRepository.ExtractoPago(
                pago.idCarteraCredito(), pago.fechaPago(), null, null,
                simulacion.capitalAplicado(), BigDecimal.ZERO,
                simulacion.interesesCorrientesAplicados(), BigDecimal.ZERO,
                simulacion.interesesMoraAplicados(),
                simulacion.liquidacion().mora().diasMora(), simulacion.segurosAplicados(),
                simulacion.aliviosAplicados().add(simulacion.costasJudicialesAplicadas()),
                validado.efectivo(), BigDecimal.ZERO, totalAhorros,
                totalCxC, BigDecimal.ZERO, BigDecimal.ZERO,
                null, "Pago de cuotas mediante Caja, AHORROS y/o CxC", aplicacion.alturaCuotaNueva(),
                credito.abonosPendientes(), fechaContable,
                aplicacion.ultimaFechaCapitalNueva(), aplicacion.ultimaFechaInteresNueva(),
                aplicacion.proximaFechaCapitalNueva(), aplicacion.proximaFechaInteresNueva(),
                idUsuario);
        return contabilizarYAplicar(validado, extracto, tipoComprobante,
                movimientos, idDatosPersonal, idUsuario);
    }

    /** Describe el pago completo con pagaré real, documento, titular e importe. */
    private String conceptoGeneralPago(Integer idCredito, BigDecimal valorTotal) {
        var credito = pagoCuotasV1Service.consultarCredito(idCredito);
        String pagare = credito.pagareCartera();
        comprobar(pagare != null && !pagare.isBlank(),
                "El crédito no tiene número de pagaré para identificar el comprobante.");
        var titular = repository.consultarIdentificacionTitular(idCredito);
        comprobar(valorTotal != null && valorTotal.signum() > 0,
                "El valor total del pago es obligatorio para describir el comprobante.");
        return "PAGO DE CUOTAS DE CARTERA - PAGARÉ " + pagare.trim()
                + " - CÉDULA " + titular.documento()
                + " - ASOCIADO: " + titular.nombreCompleto()
                + " - VALOR TOTAL: $" + String.format(java.util.Locale.forLanguageTag("es-CO"),
                "%,.0f", valorTotal);
    }

    /** Traduce el código financiero a una descripción legible para el auxiliar. */
    private static String descripcionConcepto(String concepto) {
        return switch (concepto) {
            case "CAPITAL" -> "ABONO A CAPITAL";
            case "INTERES" -> "PAGO INTERESES CORRIENTES";
            case "INGRESO_INTERES_MORA" -> "PAGO INTERESES DE MORA";
            case "ALIVIOS" -> "PAGO ALIVIOS";
            case "COSTAS_JUDICIALES" -> "PAGO COSTAS JUDICIALES";
            case "SEGUROS" -> "PAGO SEGUROS";
            default -> concepto;
        };
    }

    /** Misma construcción contable para vista previa y registro definitivo. */
    private List<MovimientoContableDTO> construirMovimientosPagoCaja(PagoValidado validado) {
        var pago = validado.pago();
        return construirMovimientosPagoIntegrado(pago.idCarteraCredito(),
                pago.idAgenciaReceptora(), pago.fechaContable(),
                validado.medios(), validado.simulacion());
    }

    /** Construye los mismos asientos desde una operación preparada o un pago pendiente. */
    private List<MovimientoContableDTO> construirMovimientosPagoCaja(
            Integer idCredito, Integer idAgencia, Long idCaja, LocalDate fechaContable,
            BigDecimal valorPago, PagoCuotasV1Service.SimulacionPago simulacion) {
        comprobar(simulacion != null && valorPago != null && idCaja != null,
                "La operación no contiene los datos necesarios para el comprobante.");
        comprobar(simulacion.interesesMoraAplicados().signum() == 0,
                "No se puede aplicar un pago con mora sin definir su cuenta contable.");
        var credito = pagoCuotasV1Service.consultarCredito(idCredito);
        comprobar(credito.idAgencia().equals(idAgencia),
                "El pago interagencia requiere contabilización específica.");
        var datosContables = repository.consultarDatosCredito(idCredito);
        var movimientos = new java.util.ArrayList<MovimientoContableDTO>();
        Integer idDatosPersonal = credito.idDatosPersonal();

        movimientos.add(lineaPagoCaja(
                cajasMovimientosService.obtenerCuentaContableCaja(idCaja),
                idAgencia, idDatosPersonal, fechaContable,
                valorPago, BigDecimal.ZERO,
                "RECAUDO EFECTIVO - PAGARÉ " + credito.pagareCartera()));

        agregarCreditoPago(movimientos, datosContables, "CAPITAL",
                simulacion.capitalAplicado(), idAgencia, idDatosPersonal, fechaContable);
        agregarCreditoPago(movimientos, datosContables, "INTERES",
                simulacion.interesesCorrientesAplicados(), idAgencia, idDatosPersonal, fechaContable);
        agregarCreditoPago(movimientos, datosContables, "ALIVIOS",
                simulacion.aliviosAplicados(), idAgencia, idDatosPersonal, fechaContable);
        agregarCreditoPago(movimientos, datosContables, "COSTAS_JUDICIALES",
                simulacion.costasJudicialesAplicadas(), idAgencia, idDatosPersonal, fechaContable);

        if (simulacion.segurosAplicados().signum() > 0) {
            var cuentaSeguro = repository.resolverParametroContable(idAgencia, "SEGUROS");
            movimientos.add(lineaPagoCaja(cuentaSeguro.idCatalogoCuenta(),
                    idAgencia, idDatosPersonal, fechaContable,
                    BigDecimal.ZERO, simulacion.segurosAplicados(), "PAGO SEGUROS - PAGARÉ " + credito.pagareCartera()));
        }

        return List.copyOf(movimientos);
    }

    public record LineaComprobantePrevio(Integer idCatalogoCuenta, String codigoCuenta, String nombreCuenta, String concepto,
                                         BigDecimal debito, BigDecimal credito) { }

    public record ComprobantePrevio(Long idMovimientoCaja, Long idPago, String tipoComprobante,
                                    LocalDate fechaContable, BigDecimal totalDebitos,
                                    BigDecimal totalCreditos, String huella,
                                    List<LineaComprobantePrevio> movimientos) { }

    /** Solo consulta. No reserva consecutivos, no registra auxiliares ni aplica cartera. */
    public ComprobantePrevio previsualizarPagoCaja(PagoValidado validado) {
        var pago = validado.pago();
        var movimientos = construirMovimientosPagoCaja(validado);
        BigDecimal debitos = BigDecimal.ZERO;
        BigDecimal creditos = BigDecimal.ZERO;
        var lineas = new java.util.ArrayList<LineaComprobantePrevio>();
        for (var m : movimientos) {
            BigDecimal d = m.getValorDebito() == null ? BigDecimal.ZERO : m.getValorDebito();
            BigDecimal c = m.getValorCredito() == null ? BigDecimal.ZERO : m.getValorCredito();
            debitos = debitos.add(d);
            creditos = creditos.add(c);
            lineas.add(lineaComprobantePrevio(m.getIdCatalogoCuenta(), pago.idAgenciaReceptora(),
                    m.getDetalleMovimiento(), d, c));
        }
        comprobar(debitos.compareTo(creditos) == 0
                        && debitos.compareTo(pago.valorPago()) == 0,
                "El comprobante previo no cuadra con el pago.");
        String huella = huellaComprobante(pago.idPago(), pago.idMovimientoCaja(),
                pago.fechaContable(), lineas);
        return new ComprobantePrevio(pago.idMovimientoCaja(), pago.idPago(), "CC",
                pago.fechaContable(), debitos, creditos, huella, List.copyOf(lineas));
    }

    /**
     * Construye los movimientos contables del pago integrado de Cartera.
     * Valida agencia y medios de recaudo, y resuelve las cuentas de cada concepto,
     * incluyendo los intereses de mora según la parametrización contable.
     */
    private List<MovimientoContableDTO> construirMovimientosPagoIntegrado(
            Integer idCredito, Integer idAgencia, LocalDate fechaContable,
            List<PagoCuotasMedioRecaudoDTO> medios,
            PagoCuotasV1Service.SimulacionPago simulacion) {

        comprobar(simulacion != null && medios != null && !medios.isEmpty(),
                "La operación no contiene una liquidación y medios válidos.");

        var credito = pagoCuotasV1Service.consultarCredito(idCredito);

        comprobar(idAgencia.equals(credito.idAgencia()),
                "El pago interagencia requiere contabilización específica.");

        var datos = repository.consultarDatosCredito(idCredito);

        comprobar(idAgencia.equals(datos.idAgencia()),
                "La parametrización contable del crédito pertenece a otra agencia.");

        Integer tercero = credito.idDatosPersonal();
        var titular = repository.consultarIdentificacionTitular(idCredito);
        comprobar(credito.pagareCartera() != null && !credito.pagareCartera().isBlank(),
                "El crédito no tiene número de pagaré para describir los movimientos.");
        String referencia = "PAGARÉ " + credito.pagareCartera().trim()
                + " - CÉDULA " + titular.documento();

        var movimientos = new java.util.ArrayList<MovimientoContableDTO>();

        for (var medio : medios) {
            switch (medio.tipoMedio()) {

                case CAJA -> movimientos.add(lineaPagoCaja(
                        cajasMovimientosService.obtenerCuentaContableCaja(medio.idCaja()),
                        idAgencia, tercero, fechaContable,
                        medio.valor(), BigDecimal.ZERO,
                        "RECAUDO EFECTIVO - " + referencia));

                case AHORROS -> {
                    Integer cuentaAhorro = Math.toIntExact(medio.idCuentaAhorro());

                    Integer catalogo = repository.resolverCuentaContableAhorroRecaudo(
                            idCredito, cuentaAhorro);

                    movimientos.add(lineaPagoCaja(
                            catalogo, idAgencia, tercero, fechaContable,
                            medio.valor(), BigDecimal.ZERO,
                            "RECAUDO AHORROS - CUENTA " + cuentaAhorro + " - " + referencia));
                }

                case CXP -> {
                    var cuenta = repository.resolverParametroContable(
                            idAgencia, "RECAUDO_BANCARIO");

                    movimientos.add(lineaPagoCaja(
                            cuenta.idCatalogoCuenta(),
                            idAgencia, tercero, fechaContable,
                            medio.valor(), BigDecimal.ZERO,
                            "RECAUDO POR CONSIGNACIÓN O TRANSFERENCIA - " + referencia));
                }
            }
        }

        agregarCreditoPago(
                movimientos, datos, "CAPITAL",
                simulacion.capitalAplicado(),
                idAgencia, tercero, fechaContable, referencia);

        agregarCreditoPago(
                movimientos, datos, "INTERES",
                simulacion.interesesCorrientesAplicados(),
                idAgencia, tercero, fechaContable, referencia);

        // Los intereses de mora se contabilizan en la cuenta de ingresos
        // parametrizada por clasificación y edad contable del crédito.
        agregarCreditoPago(
                movimientos, datos, "INGRESO_INTERES_MORA",
                simulacion.interesesMoraAplicados(),
                idAgencia, tercero, fechaContable, referencia);

        agregarCreditoPago(
                movimientos, datos, "ALIVIOS",
                simulacion.aliviosAplicados(),
                idAgencia, tercero, fechaContable, referencia);

        agregarCreditoPago(
                movimientos, datos, "COSTAS_JUDICIALES",
                simulacion.costasJudicialesAplicadas(),
                idAgencia, tercero, fechaContable, referencia);

        if (simulacion.segurosAplicados().signum() > 0) {
            var seguro = repository.resolverParametroContable(
                    idAgencia, "SEGUROS");

            movimientos.add(lineaPagoCaja(
                    seguro.idCatalogoCuenta(),
                    idAgencia, tercero, fechaContable,
                    BigDecimal.ZERO,
                    simulacion.segurosAplicados(),
                    "PAGO SEGUROS - " + referencia));
        }

        return List.copyOf(movimientos);
    }

    /** Vista contable anterior al envío a Caja; no persiste ni reserva consecutivos. */
    @Transactional(readOnly = true)
    public ComprobantePrevio previsualizarOperacionPreparada(OperacionPagoPreparada operacion) {
        comprobar(operacion != null && operacion.recaudo() != null,
                "Debe preparar una operación válida.");
        var medios = operacion.recaudo().medios();
        comprobar(medios != null && !medios.isEmpty(),
                "Debe indicar los medios del recaudo.");
        var recaudo = validarOperacionRecaudo(operacion.simulacion().valorPago(),
                medios, operacion.idAgenciaReceptora());
        LocalDate fechaContable = LocalDate.now();
        List<MovimientoContableDTO> movimientos = construirMovimientosPagoIntegrado(
                operacion.idCarteraCredito(), operacion.idAgenciaReceptora(),
                fechaContable, recaudo.medios(), operacion.simulacion());
        BigDecimal debitos = BigDecimal.ZERO;
        BigDecimal creditos = BigDecimal.ZERO;
        var lineas = new java.util.ArrayList<LineaComprobantePrevio>();
        for (var movimiento : movimientos) {
            BigDecimal debito = movimiento.getValorDebito() == null
                    ? BigDecimal.ZERO : movimiento.getValorDebito();
            BigDecimal credito = movimiento.getValorCredito() == null
                    ? BigDecimal.ZERO : movimiento.getValorCredito();
            debitos = debitos.add(debito);
            creditos = creditos.add(credito);
            lineas.add(lineaComprobantePrevio(movimiento.getIdCatalogoCuenta(), operacion.idAgenciaReceptora(),
                    movimiento.getDetalleMovimiento(), debito, credito));
        }
        comprobar(debitos.compareTo(creditos) == 0
                        && debitos.compareTo(operacion.simulacion().valorPago()) == 0,
                "El comprobante contable no está cuadrado.");
        return new ComprobantePrevio(null, null, "CC", LocalDate.now(),
                debitos, creditos, null, List.copyOf(lineas));
    }

    /** Resuelve el código y nombre de la cuenta en la agencia del pago. */
    private LineaComprobantePrevio lineaComprobantePrevio(
            Integer idCuenta, Integer idAgencia, String concepto,
            BigDecimal debito, BigDecimal credito) {
        var cuenta = planCuentaRepository.findById(idCuenta)
                .orElseThrow(() -> new IllegalStateException(
                        "La cuenta contable no existe: " + idCuenta));
        comprobar(idAgencia.equals(cuenta.getIdAgencia()),
                "La cuenta contable no pertenece a la agencia del pago.");
        return new LineaComprobantePrevio(idCuenta, cuenta.getCodigoCuenta(),
                cuenta.getNombre(), concepto, debito, credito);
    }

    private static String huellaComprobante(Long idPago, Long idMovimiento,
                                            LocalDate fecha, List<LineaComprobantePrevio> lineas) {
        try {
            var digest = java.security.MessageDigest.getInstance("SHA-256");
            var contenido = new StringBuilder().append(idPago).append('|')
                    .append(idMovimiento).append('|').append(fecha);
            for (var l : lineas) {
                contenido.append('|').append(l.idCatalogoCuenta()).append(':')
                        .append(l.debito().stripTrailingZeros().toPlainString()).append(':')
                        .append(l.credito().stripTrailingZeros().toPlainString());
            }
            byte[] bytes = digest.digest(contenido.toString()
                    .getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(bytes);
        } catch (java.security.NoSuchAlgorithmException ex) {
            throw new IllegalStateException("No fue posible verificar el comprobante.", ex);
        }
    }

    /** Conserva la firma anterior para otros constructores de movimientos. */
    private void agregarCreditoPago(List<MovimientoContableDTO> movimientos,
            PagoCuotasMotorRepository.DatosContablesCredito credito,
            String concepto, BigDecimal valor,
            Integer idAgencia, Integer idDatosPersonal, LocalDate fechaContable) {
        agregarCreditoPago(movimientos, credito, concepto, valor,
                idAgencia, idDatosPersonal, fechaContable, null);
    }

    /** Agrega el crédito con una descripción financiera y la referencia del pagaré. */
    private void agregarCreditoPago(
            List<MovimientoContableDTO> movimientos,
            PagoCuotasMotorRepository.DatosContablesCredito credito,
            String concepto, BigDecimal valor,
            Integer idAgencia, Integer idDatosPersonal, LocalDate fechaContable,
            String referencia) {
        if (valor == null || valor.signum() < 0) {
            throw new IllegalArgumentException("Importe inválido para " + concepto);
        }
        if (valor.signum() == 0) return;
        var cuenta = repository.resolverCuenta(credito, concepto, null);
        movimientos.add(lineaPagoCaja(cuenta.idCatalogoCuenta(), idAgencia,
                idDatosPersonal, fechaContable, BigDecimal.ZERO, valor,
                descripcionConcepto(concepto) + (referencia == null ? "" : " - " + referencia)));
    }

    private static MovimientoContableDTO lineaPagoCaja(
            Integer idCatalogoCuenta, Integer idAgencia, Integer idDatosPersonal,
            LocalDate fechaContable, BigDecimal debito, BigDecimal credito,
            String concepto) {
        var movimiento = new MovimientoContableDTO();
        movimiento.setIdCatalogoCuenta(idCatalogoCuenta);
        movimiento.setIdAgencia(idAgencia);
        movimiento.setIdDatosPersonal(idDatosPersonal);
        movimiento.setFechaAuxiliar(fechaContable);
        movimiento.setValorDebito(debito);
        movimiento.setValorCredito(credito);
        movimiento.setDetalleMovimiento(concepto);
        movimiento.setOrigenModulo("CARTERA");
        return movimiento;
    }

    private static boolean igual(BigDecimal a, BigDecimal b) {
        return a != null && b != null && a.compareTo(b) == 0;
    }

    private static void comprobar(boolean condicion, String mensaje) {
        if (!condicion) throw new IllegalStateException(mensaje);
    }

    /** Proyección financiera propia del motor; conserva los campos del contrato existente. */
    public record ResultadoAplicacion(
            Integer idCarteraCredito,
            BigDecimal saldoAnterior,
            BigDecimal capitalAplicado,
            BigDecimal saldoNuevo,
            String codigoEstadoCarteraProyectado,
            boolean cancelaCredito,
            boolean cubreCapitalExigible,
            boolean abonoAdicionalCapital,
            boolean capitalInferiorAlLiquidado,
            boolean consultarProrrogaVigente,
            boolean candidatoRegularizacionProrroga,
            Integer alturaCuotaActual,
            LocalDate ultimaFechaCapitalActual,
            LocalDate ultimaFechaInteresActual,
            LocalDate proximaFechaCapitalActual,
            LocalDate proximaFechaInteresActual,
            Integer alturaCuotaNueva,
            LocalDate ultimaFechaCapitalNueva,
            LocalDate ultimaFechaInteresNueva,
            LocalDate proximaFechaCapitalNueva,
            LocalDate proximaFechaInteresNueva) { }

    public record PagoValidado(
            PagoCuotasMotorRepository.PagoPendienteBloqueado pago,
            PagoCuotasV1Service.SimulacionPago simulacion,
            ResultadoAplicacion aplicacion,
            List<PagoCuotasMedioRecaudoDTO> medios,
            BigDecimal efectivo) { }
}
