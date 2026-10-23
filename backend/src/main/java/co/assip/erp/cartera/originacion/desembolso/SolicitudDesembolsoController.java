package co.assip.erp.cartera.originacion.desembolso;

import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import co.assip.erp.cartera.originacion.desembolso.dto.ComprobanteDesembolsoPreviewDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cartera/originacion/desembolso")
public class SolicitudDesembolsoController {

    private static final String PERMISO_VIEW =
            "CARTERA_ORIGINACION_VIEW";

    private static final String PERMISO_DESEMBOLSO =
            "CARTERA_ORIGINACION_DESEMBOLSO";

    // =========================================================
    // DEPENDENCIAS
    // =========================================================

    private final SolicitudDesembolsoService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    // =========================================================
    // BANDEJA
    //
    // GET /cartera/originacion/desembolso/pendientes
    // =========================================================

    @GetMapping("/pendientes")
    public ResponseEntity<
            List<SolicitudDesembolsoService.CreditoPendienteDesembolso>
            > listarPendientes() {

        validarConsulta();

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

        validarConsulta();

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

        validarDesembolso();

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
    // - Efectivo y caja cuando corresponda
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

        validarDesembolso();

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
    // - Publicación de operaciones pendientes en Tesorería
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

        validarDesembolso();

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
    // SEGURIDAD
    // =========================================================

    @PostMapping("/{idCarteraCredito}/verificar-comprobantes")
    public ResponseEntity<List<ComprobanteDesembolsoPreviewDTO>>
    verificarComprobantes(
            @PathVariable Integer idCarteraCredito,
            @RequestBody VerificarComprobantesRequest request
    ) {

        validarDesembolso();

        if (request == null) {
            throw new IllegalArgumentException(
                    "Los datos de verificación son obligatorios."
            );
        }

        return ResponseEntity.ok(
                service.verificarComprobantes(
                        idCarteraCredito,
                        request.idCaja(),
                        request.fechaContable(),
                        request.tipoComprobanteDesembolso(),
                        request.tipoComprobanteCobros(),
                        request.cantidadCentral(),
                        request.valorEfectivo(),
                        convertirDepositos(request.depositos()),
                        request.valorCuentaPorPagar()
                )
        );
    }

    public record VerificarComprobantesRequest(
            Long idCaja,
            LocalDate fechaContable,
            String tipoComprobanteDesembolso,
            String tipoComprobanteCobros,
            Integer cantidadCentral,
            BigDecimal valorEfectivo,
            List<DepositoDesembolsoRequest> depositos,
            BigDecimal valorCuentaPorPagar
    ) {}

    private void validarConsulta() {

        accessValidator.validarAcceso(
                usuarioActual(),
                PERMISO_VIEW
        );
    }

    private void validarDesembolso() {

        accessValidator.validarAcceso(
                usuarioActual(),
                PERMISO_DESEMBOLSO
        );
    }

    private Usuario usuarioActual() {

        Usuario usuarioActual = new Usuario();

        usuarioActual.setIdUsuario(
                usuarioSesionService.idUsuario()
        );

        return usuarioActual;
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
    // Caja que atenderá los movimientos de efectivo.
    // Obligatoria únicamente cuando exista efectivo por
    // entregar o cobros por recibir.
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

}
