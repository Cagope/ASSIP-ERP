package co.assip.erp.cartera.pago_cuotas_v1;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Consultas, liquidación y simulación de aplicación de pagos de cartera.
 * No registra pagos ni modifica saldos o fechas.
 * El prefijo /api/v1 lo proporciona el context-path de la aplicación.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/cartera/pago-cuotas-v1")
public class PagoCuotasV1Controller {

    private final PagoCuotasV1Service service;

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

    /** Consulta los pagos agrupados por fecha y comprobante. */
    @GetMapping("/creditos/{idCarteraCredito}/pagos-realizados")
    public List<PagoCuotasV1Repository.PagoRealizado> listarPagosRealizados(
            @PathVariable Integer idCarteraCredito) {
        return service.listarPagosRealizados(idCarteraCredito);
    }

    @GetMapping("/creditos/{idCarteraCredito}/datos-previos")
    public PagoCuotasV1Service.DatosPrevios consultarDatosPrevios(
            @PathVariable Integer idCarteraCredito,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaPago) {
        return service.consultarDatosPrevios(idCarteraCredito, fechaPago);
    }

    /** Si no se indica cuotasACancelar, se calcula la cantidad automáticamente. */
    @GetMapping("/creditos/{idCarteraCredito}/liquidacion")
    public PagoCuotasV1Service.LiquidacionCuotas liquidar(
            @PathVariable Integer idCarteraCredito,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaPago,
            @RequestParam Integer periodoInteresMeses,
            @RequestParam(required = false) Integer cuotasACancelar) {
        return service.liquidar(
                idCarteraCredito, fechaPago, periodoInteresMeses, cuotasACancelar);
    }

    /**
     * Simula la distribución del valor recibido entre conceptos y capital.
     * cuotasACancelar es opcional; valorPago es obligatorio.
     * Esta operación es exclusivamente de consulta.
     */
    @GetMapping("/creditos/{idCarteraCredito}/simulacion-pago")
    public PagoCuotasV1Service.SimulacionPago simularPago(
            @PathVariable Integer idCarteraCredito,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaPago,
            @RequestParam Integer periodoInteresMeses,
            @RequestParam(required = false) Integer cuotasACancelar,
            @RequestParam BigDecimal valorPago) {
        return service.simularPago(
                idCarteraCredito, fechaPago, periodoInteresMeses, cuotasACancelar, valorPago);
    }
}
