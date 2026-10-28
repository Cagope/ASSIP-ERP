package co.assip.erp.cartera.pago_cuotas_v1;

import co.assip.erp.cartera.pago_cuotas_v1.motor.PagoCuotasMotorRepository;
import co.assip.erp.cartera.pago_cuotas_v1.motor.PagoCuotasMotorService;
import co.assip.erp.cartera.pago_cuotas_v1.motor.dto.PagoCuotasMedioRecaudoDTO;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;

import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Consultas, liquidación y simulación de aplicación de pagos de cartera.
 *
 * No registra pagos ni modifica saldos o fechas.
 * El prefijo /api/v1 lo proporciona el context-path de la aplicación.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/cartera/pago-cuotas-v1")
public class PagoCuotasV1Controller {

    private final PagoCuotasV1Service service;
    private final PagoCuotasMotorService motorService;
    private final UsuarioSesionService usuarioSesionService;

    @GetMapping("/asociados/{idDatosPersonal}/creditos")
    public List<PagoCuotasV1Repository.CreditoResumen> listarCreditosPorAsociado(
            @PathVariable Integer idDatosPersonal) {

        return service.listarCreditosPorAsociado(idDatosPersonal);
    }

    @GetMapping("/creditos/{idCarteraCredito}")
    public PagoCuotasV1Repository.CreditoLiquidacion consultarCredito(
            @PathVariable Integer idCarteraCredito) {

        return service.consultarCredito(idCarteraCredito);
    }

    /**
     * Consulta los pagos agrupados por fecha y comprobante.
     */
    @GetMapping("/creditos/{idCarteraCredito}/pagos-realizados")
    public List<PagoCuotasV1Repository.PagoRealizado> listarPagosRealizados(
            @PathVariable Integer idCarteraCredito) {

        return service.listarPagosRealizados(idCarteraCredito);
    }

    @GetMapping("/creditos/{idCarteraCredito}/datos-previos")
    public PagoCuotasV1Service.DatosPrevios consultarDatosPrevios(
            @PathVariable Integer idCarteraCredito,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaPago) {

        return service.consultarDatosPrevios(
                idCarteraCredito,
                fechaPago
        );
    }

    /**
     * Si no se indica cuotasACancelar, se calcula la cantidad automáticamente.
     */
    @GetMapping("/creditos/{idCarteraCredito}/liquidacion")
    public PagoCuotasV1Service.LiquidacionCuotas liquidar(
            @PathVariable Integer idCarteraCredito,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaPago,
            @RequestParam Integer periodoInteresMeses,
            @RequestParam(required = false) Integer cuotasACancelar) {

        return service.liquidar(
                idCarteraCredito,
                fechaPago,
                periodoInteresMeses,
                cuotasACancelar
        );
    }

    /**
     * Simula la distribución del valor recibido entre conceptos y capital.
     *
     * cuotasACancelar es opcional; valorPago es obligatorio.
     * Esta operación es exclusivamente de consulta.
     */
    @GetMapping("/creditos/{idCarteraCredito}/simulacion-pago")
    public PagoCuotasV1Service.SimulacionPago simularPago(
            @PathVariable Integer idCarteraCredito,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaPago,
            @RequestParam Integer periodoInteresMeses,
            @RequestParam(required = false) Integer cuotasACancelar,
            @RequestParam BigDecimal valorPago) {

        return service.simularPago(
                idCarteraCredito,
                fechaPago,
                periodoInteresMeses,
                cuotasACancelar,
                valorPago
        );
    }

    /**
     * Liquidación informativa para cancelación total del crédito.
     * No modifica saldos ni registra movimientos.
     */
    @GetMapping("/creditos/{idCarteraCredito}/liquidacion-pago-total")
    public PagoCuotasV1Service.LiquidacionCuotas liquidarPagoTotal(
            @PathVariable Integer idCarteraCredito,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaPago,
            @RequestParam Integer periodoInteresMeses) {

        return service.liquidarPagoTotal(
                idCarteraCredito,
                fechaPago,
                periodoInteresMeses
        );
    }

    /**
     * Simulación informativa de cancelación total.
     * Exige el importe exacto de la liquidación.
     */
    @GetMapping("/creditos/{idCarteraCredito}/simulacion-pago-total")
    public PagoCuotasV1Service.SimulacionPago simularPagoTotal(
            @PathVariable Integer idCarteraCredito,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaPago,
            @RequestParam Integer periodoInteresMeses,
            @RequestParam BigDecimal valorPago) {

        return service.simularPagoTotal(
                idCarteraCredito,
                fechaPago,
                periodoInteresMeses,
                valorPago
        );
    }

    /**
     * Cajas disponibles en la agencia receptora autorizada para el usuario.
     */
    @GetMapping("/medios-recaudo/cajas")
    public List<PagoCuotasMotorRepository.CajaRecaudo> listarCajasRecaudo(
            @RequestParam Integer idAgenciaReceptora) {

        usuarioSesionService.validarAgencia(idAgenciaReceptora);

        return motorService.listarCajasRecaudo(
                idAgenciaReceptora
        );
    }


    /**
     * Consulta las cuentas de ahorro operativas del titular del crédito.
     * La validación de agencia y elegibilidad se realiza en el servicio.
     * No registra movimientos ni modifica saldos.
     */
    @GetMapping("/creditos/{idCarteraCredito}/cuentas-ahorro-recaudo")
    public List<PagoCuotasMotorRepository.CuentaAhorroRecaudo> listarCuentasAhorroRecaudo(
            @PathVariable Integer idCarteraCredito) {

        return motorService.listarCuentasAhorroRecaudo(idCarteraCredito);
    }

    /**
     * Valida la selección de un medio de recaudo.
     *
     * No registra pagos ni afecta saldos.
     * AHORROS y CXP requieren validación operativa antes de ejecutarse.
     */
    @PostMapping("/medios-recaudo/validar")
    public PagoCuotasMedioRecaudoDTO validarMedioRecaudo(
            @RequestParam Integer idAgenciaReceptora,
            @RequestBody PagoCuotasMedioRecaudoDTO medio) {

        usuarioSesionService.validarAgencia(idAgenciaReceptora);

        return motorService.validarMedioRecaudo(
                medio,
                idAgenciaReceptora
        );
    }

    /**
     * Valida una operación completa con uno o varios medios de recaudo.
     *
     * Comprueba que la suma de los medios coincida con el valor total
     * y determina si la operación requiere confirmación de Caja.
     *
     * No registra pagos ni modifica información financiera.
     */
    @PostMapping("/medios-recaudo/validar-operacion")
    public PagoCuotasMotorService.OperacionRecaudoValidada validarOperacionRecaudo(
            @RequestParam Integer idAgenciaReceptora,
            @RequestBody SolicitudValidacionOperacion solicitud) {

        usuarioSesionService.validarAgencia(idAgenciaReceptora);

        if (solicitud == null) {
            throw new IllegalArgumentException(
                    "Debe indicar los datos de la operación de recaudo."
            );
        }

        return motorService.validarOperacionRecaudo(
                solicitud.valorTotal(),
                solicitud.medios(),
                idAgenciaReceptora
        );
    }

    /**
     * Datos de entrada para validar los medios de una operación.
     */
    public record SolicitudValidacionOperacion(
            BigDecimal valorTotal,
            List<PagoCuotasMedioRecaudoDTO> medios
    ) {
    }

    /**
     * Prepara una operación integral de pago de cuotas.
     *
     * Simula la distribución financiera y valida los medios
     * de recaudo, sin registrar movimientos ni modificar saldos.
     */
    @PostMapping("/operaciones/preparar")
    public PagoCuotasMotorService.OperacionPagoPreparada prepararOperacionPago(
            @RequestBody SolicitudPreparacionPago solicitud) {

        if (solicitud == null) {
            throw new IllegalArgumentException(
                    "Debe indicar los datos de la operación de pago.");
        }

        usuarioSesionService.validarAgencia(
                solicitud.idAgenciaReceptora());

        return motorService.prepararOperacionPago(
                solicitud.idCarteraCredito(),
                solicitud.fechaPago(),
                solicitud.periodoInteresMeses(),
                solicitud.cuotasACancelar(),
                solicitud.valorPago(),
                solicitud.idAgenciaReceptora(),
                solicitud.medios(),
                Boolean.TRUE.equals(solicitud.pagoTotal())
        );
    }

    /** Presenta el asiento contable de la operación antes de enviarla a Caja. */
    @PostMapping("/operaciones/comprobante-previo")
    public PagoCuotasMotorService.ComprobantePrevio comprobantePrevio(
            @RequestBody SolicitudPreparacionPago solicitud) {
        if (solicitud == null) {
            throw new IllegalArgumentException("Debe indicar los datos del pago.");
        }
        usuarioSesionService.validarAgencia(solicitud.idAgenciaReceptora());
        var operacion = motorService.prepararOperacionPago(
                solicitud.idCarteraCredito(), solicitud.fechaPago(),
                solicitud.periodoInteresMeses(), solicitud.cuotasACancelar(),
                solicitud.valorPago(), solicitud.idAgenciaReceptora(),
                solicitud.medios(), Boolean.TRUE.equals(solicitud.pagoTotal()));
        return motorService.previsualizarOperacionPreparada(operacion);
    }

    /**
     * Registra el pago como PENDIENTE_CAJA y crea el movimiento de Caja.
     * No confirma ni aplica el recaudo al crédito.
     */
    @PostMapping("/operaciones/enviar-caja")
    public Long enviarPagoCaja(@RequestBody SolicitudPreparacionPago solicitud) {
        if (solicitud == null) {
            throw new IllegalArgumentException("Debe indicar los datos del pago.");
        }
        usuarioSesionService.validarAgencia(solicitud.idAgenciaReceptora());

        var operacion = motorService.prepararOperacionPago(
                solicitud.idCarteraCredito(),
                solicitud.fechaPago(),
                solicitud.periodoInteresMeses(),
                solicitud.cuotasACancelar(),
                solicitud.valorPago(),
                solicitud.idAgenciaReceptora(),
                solicitud.medios(),
                Boolean.TRUE.equals(solicitud.pagoTotal())
        );

        // El número de pagaré se consulta en backend, no se confía en el navegador.
        String pagare = service.consultarCredito(solicitud.idCarteraCredito()).pagareCartera();
        return motorService.enviarPagoPendienteCaja(
                operacion, LocalDate.now(), pagare, usuarioSesionService.idUsuario());
    }

    /**
     * Ejecuta recaudos sin efectivo: AHORROS, CxC o su combinación.
     * Revalida los medios y delega la aplicación transaccional al motor.
     */
    @PostMapping("/operaciones/ejecutar-ahorros")
    public Long ejecutarPagoAhorros(@RequestBody SolicitudPreparacionPago solicitud) {
        if (solicitud == null) {
            throw new IllegalArgumentException("Debe indicar los datos del pago.");
        }

        usuarioSesionService.validarAgencia(solicitud.idAgenciaReceptora());

        var operacion = motorService.prepararOperacionPago(
                solicitud.idCarteraCredito(),
                solicitud.fechaPago(),
                solicitud.periodoInteresMeses(),
                solicitud.cuotasACancelar(),
                solicitud.valorPago(),
                solicitud.idAgenciaReceptora(),
                solicitud.medios(),
                Boolean.TRUE.equals(solicitud.pagoTotal())
        );

        // Esta ruta no admite efectivo: Caja conserva su propio flujo de confirmación.
        if (operacion.recaudo().medios().isEmpty()
                || operacion.recaudo().medios().stream().anyMatch(medio ->
                medio.tipoMedio() != PagoCuotasMedioRecaudoDTO.TipoMedioRecaudo.AHORROS
                        && medio.tipoMedio() != PagoCuotasMedioRecaudoDTO.TipoMedioRecaudo.CXP)) {
            throw new IllegalArgumentException(
                    "Esta operación admite únicamente AHORROS, CxC o una combinación de ambos.");
        }

        return motorService.ejecutarPagoAhorros(
                operacion, LocalDate.now(), usuarioSesionService.idUsuario());
    }

    /**
     * Revalida en servidor un recaudo pendiente de Caja antes de su ejecución.
     * No acepta liquidaciones ni valores financieros enviados por el navegador.
     * No contabiliza, aplica ni confirma físicamente el recaudo.
     */
    @PostMapping("/operaciones/{idPago}/validar-pendiente")
    public PagoPendienteValidadoResponse validarPagoPendiente(
            @PathVariable Long idPago,
            @RequestBody SolicitudValidacionPendiente solicitud) {

        if (idPago == null || idPago <= 0 || solicitud == null
                || solicitud.idMovimientoCaja() == null || solicitud.idMovimientoCaja() <= 0
                || solicitud.idCaja() == null || solicitud.idCaja() <= 0) {
            throw new IllegalArgumentException("Debe identificar el pago y el movimiento de Caja.");
        }

        var validado = motorService.validarPendiente(
                idPago, solicitud.idMovimientoCaja(), solicitud.idCaja());
        usuarioSesionService.validarAgencia(validado.pago().idAgenciaReceptora());

        return new PagoPendienteValidadoResponse(
                validado.pago().idPago(),
                validado.pago().idCarteraCredito(),
                validado.pago().valorPago(),
                validado.efectivo(),
                validado.aplicacion().saldoNuevo());
    }

    public record SolicitudValidacionPendiente(Long idMovimientoCaja, Long idCaja) { }

    public record PagoPendienteValidadoResponse(
            Long idPago,
            Integer idCarteraCredito,
            BigDecimal valorPago,
            BigDecimal efectivo,
            BigDecimal saldoCapitalProyectado) { }

    /**
     * Datos necesarios para preparar una operación de pago.
     */
    public record SolicitudPreparacionPago(
            Integer idCarteraCredito,
            LocalDate fechaPago,
            Integer periodoInteresMeses,
            Integer cuotasACancelar,
            BigDecimal valorPago,
            Integer idAgenciaReceptora,
            List<PagoCuotasMedioRecaudoDTO> medios,
            Boolean pagoTotal
    ) {
    }
}
