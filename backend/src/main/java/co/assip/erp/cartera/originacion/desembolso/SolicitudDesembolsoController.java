package co.assip.erp.cartera.originacion.desembolso;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/cartera/originacion/desembolso")
public class SolicitudDesembolsoController {

    private final SolicitudDesembolsoService service;

    public SolicitudDesembolsoController(
            SolicitudDesembolsoService service
    ) {
        this.service = service;
    }

    // =========================================================
    // BANDEJA
    //
    // GET /cartera/originacion/desembolso/pendientes
    // =========================================================

    @GetMapping("/pendientes")
    public ResponseEntity<
            List<SolicitudDesembolsoService.CreditoPendienteDesembolso>
            > listarPendientes() {

        return ResponseEntity.ok(
                service.listarPendientes()
        );
    }

    // =========================================================
    // PREPARAR
    //
    // GET
    // /cartera/originacion/desembolso/{idCarteraCredito}/preparar
    //
    // No registra movimientos.
    // =========================================================

    @GetMapping("/{idCarteraCredito}/preparar")
    public ResponseEntity<
            SolicitudDesembolsoService.PreparacionDesembolso
            > preparar(
            @PathVariable Integer idCarteraCredito
    ) {

        return ResponseEntity.ok(
                service.preparar(
                        idCarteraCredito
                )
        );
    }

    // =========================================================
    // RECALCULAR
    //
    // POST
    // /cartera/originacion/desembolso/{idCarteraCredito}/calcular
    //
    // El frontend únicamente envía la cantidad de consultas
    // realizadas a Central de Riesgo.
    //
    // El IVA se obtiene internamente por agencia.
    // =========================================================

    @PostMapping("/{idCarteraCredito}/calcular")
    public ResponseEntity<
            SolicitudDesembolsoService.LiquidacionDesembolso
            > calcular(
            @PathVariable Integer idCarteraCredito,
            @RequestBody CalcularDesembolsoRequest request
    ) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Los datos de liquidación son obligatorios."
            );
        }

        return ResponseEntity.ok(
                service.calcularLiquidacion(
                        idCarteraCredito,
                        request.cantidadCentral()
                )
        );
    }

    // =========================================================
    // VALIDAR DISTRIBUCIÓN
    //
    // POST
    // /cartera/originacion/desembolso/
    // {idCarteraCredito}/validar-distribucion
    //
    // Valida:
    //
    // - Caja seleccionada
    // - Efectivo
    // - Una o varias cuentas de depósito
    // - Cuenta por pagar
    // - Cuadre contra valor a distribuir
    //
    // NO contabiliza.
    // NO registra Caja.
    // NO registra Depósitos.
    // NO registra Aportes.
    // NO activa el crédito.
    // =========================================================

    @PostMapping("/{idCarteraCredito}/validar-distribucion")
    public ResponseEntity<
            SolicitudDesembolsoService.ResultadoValidacionDistribucion
            > validarDistribucion(
            @PathVariable Integer idCarteraCredito,
            @RequestBody DistribucionDesembolsoRequest request
    ) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "La distribución del desembolso es obligatoria."
            );
        }

        List<SolicitudDesembolsoService.DepositoDistribucion> depositos =
                convertirDepositos(
                        request.depositos()
                );

        SolicitudDesembolsoService.DistribucionDesembolso distribucion =
                new SolicitudDesembolsoService.DistribucionDesembolso(
                        request.idCaja(),
                        request.valorEfectivo(),
                        depositos,
                        request.valorCuentaPorPagar()
                );

        return ResponseEntity.ok(
                service.validarDistribucion(
                        idCarteraCredito,
                        distribucion
                )
        );
    }

    // =========================================================
// EJECUTAR DESEMBOLSO
//
// POST
// /cartera/originacion/desembolso/{idCarteraCredito}/ejecutar
//
// Ejecuta integralmente:
//
// - Liquidación definitiva
// - Validación de distribución
// - Contabilización
// - Caja
// - Depósitos
// - Aportes
// - Activación del crédito
//
// Todo se ejecuta dentro de la transacción controlada
// por SolicitudDesembolsoService.
// =========================================================

    @PostMapping("/{idCarteraCredito}/ejecutar")
    public ResponseEntity<
            SolicitudDesembolsoService.ResultadoEjecucionDesembolso
            > ejecutar(
            @PathVariable Integer idCarteraCredito,
            @RequestBody EjecutarDesembolsoRequest request
    ) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Los datos del desembolso son obligatorios."
            );
        }

        List<SolicitudDesembolsoService.DepositoDistribucion> depositos =
                convertirDepositos(
                        request.depositos()
                );

        return ResponseEntity.ok(
                service.ejecutarDesembolso(
                        idCarteraCredito,

                        request.idCaja(),

                        request.fechaContable(),
                        request.fechaDesembolso(),

                        request.tipoComprobanteDesembolso(),
                        request.tipoComprobanteCobros(),

                        request.cantidadCentral(),

                        request.valorEfectivo(),
                        depositos,
                        request.valorCuentaPorPagar()
                )
        );
    }

    // =========================================================
    // ACTIVAR MAESTRO
    //
    // TEMPORAL.
    //
    // Este endpoint NO debe utilizarse desde Angular como
    // ejecución final del desembolso.
    //
    // Se retirará cuando quede terminado ejecutarDesembolso().
    // =========================================================

    @PostMapping("/{idCarteraCredito}/activar")
    public ResponseEntity<Void> activar(
            @PathVariable Integer idCarteraCredito,
            @RequestBody ActivarCreditoRequest request
    ) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Los datos de activación son obligatorios."
            );
        }

        service.activarCredito(
                idCarteraCredito,

                request.tipoComprobante(),
                request.numeroComprobante(),

                request.fechaContable(),
                request.fechaDesembolso()
        );

        return ResponseEntity.noContent().build();
    }

    // =========================================================
    // CONVERTIR DEPÓSITOS
    // =========================================================

    private List<SolicitudDesembolsoService.DepositoDistribucion>
    convertirDepositos(
            List<DepositoDesembolsoRequest> depositos
    ) {

        if (depositos == null) {
            return List.of();
        }

        return depositos
                .stream()
                .map(item ->
                        new SolicitudDesembolsoService.DepositoDistribucion(
                                item.idCuentaAhorro(),
                                item.valor()
                        )
                )
                .toList();
    }

    // =========================================================
    // REQUEST - RECALCULAR
    // =========================================================

    public record CalcularDesembolsoRequest(
            Integer cantidadCentral
    ) {
    }

    // =========================================================
    // REQUEST - DISTRIBUCIÓN
    //
    // idCaja:
    // Caja que atenderá el desembolso.
    //
    // La Caja es obligatoria aunque valorEfectivo sea cero,
    // porque también es la contrapartida del comprobante
    // de cobros cuando corresponda.
    //
    // valorEfectivo:
    // Valor realmente entregado físicamente en efectivo.
    //
    // depositos:
    // Una o varias cuentas de ahorro destino.
    //
    // valorCuentaPorPagar:
    // Valor que queda registrado como cuenta por pagar.
    // =========================================================

    public record DistribucionDesembolsoRequest(
            Long idCaja,
            BigDecimal valorEfectivo,
            List<DepositoDesembolsoRequest> depositos,
            BigDecimal valorCuentaPorPagar
    ) {
    }

    // =========================================================
    // REQUEST - DEPÓSITO
    // =========================================================

    public record DepositoDesembolsoRequest(
            Integer idCuentaAhorro,
            BigDecimal valor
    ) {
    }

    // =========================================================
    // REQUEST - EJECUTAR DESEMBOLSO
    //
    // Los dos tipos de comprobante se seleccionan de manera
    // independiente, conservando la lógica funcional del
    // proceso anterior.
    //
    // tipoComprobanteCobros solamente será obligatorio cuando
    // totalComprobante2 sea mayor que cero.
    // =========================================================

    public record EjecutarDesembolsoRequest(
            Long idCaja,

            LocalDate fechaContable,
            LocalDate fechaDesembolso,

            String tipoComprobanteDesembolso,
            String tipoComprobanteCobros,

            Integer cantidadCentral,

            BigDecimal valorEfectivo,
            List<DepositoDesembolsoRequest> depositos,
            BigDecimal valorCuentaPorPagar
    ) {
    }

    // =========================================================
    // REQUEST - ACTIVAR
    //
    // TEMPORAL.
    // Se eliminará al terminar la ejecución integral.
    // =========================================================

    public record ActivarCreditoRequest(
            String tipoComprobante,
            String numeroComprobante,
            LocalDate fechaContable,
            LocalDate fechaDesembolso
    ) {
    }
}