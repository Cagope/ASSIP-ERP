
package co.assip.erp.tesoreria.movimientos_fondos;

import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;

import co.assip.erp.tesoreria.consignaciones_bancarias.ConsignacionesBancariasRepository;
import co.assip.erp.tesoreria.traslados_cajas.TrasladosCajasRepository;
import co.assip.erp.tesoreria.retiros_bancarios.RetirosBancariosRepository;
import co.assip.erp.tesoreria.retiros_bancarios.RetirosBancariosService;
import co.assip.erp.tesoreria.traslados_bancarios.TrasladosBancariosRepository;
import co.assip.erp.tesoreria.traslados_bancarios.TrasladosBancariosService;

import co.assip.erp.tesoreria.movimientos_fondos.dto.MovimientoFondoPreviewDTO;
import co.assip.erp.tesoreria.movimientos_fondos.dto.MovimientoFondoRequestDTO;
import co.assip.erp.tesoreria.movimientos_fondos.dto.MovimientoFondoResponseDTO;

import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador de Movimientos de Fondos de Tesorería.
 *
 * Centraliza las operaciones de:
 *
 * - Consignaciones bancarias.
 * - Traslados entre cajas (provisión y reembolso).
 * - Preparación de retiros bancarios.
 *
 * Los retiros bancarios generan comprobantes pendientes
 * que deben ser autorizados posteriormente desde el
 * proceso de Autorización de Comprobantes.
 *
 * La autorización y contabilización definitiva
 * no se realizan desde este controlador.
 */
@RestController
@RequestMapping("/tesoreria/movimientos-fondos")
@RequiredArgsConstructor
public class MovimientosFondosController {

    private static final String PERMISO_VIEW =
            "TESORERIA_MOVIMIENTOS_FONDOS_VIEW";

    private static final String PERMISO_EXECUTE =
            "TESORERIA_MOVIMIENTOS_FONDOS_EXECUTE";

    private final MovimientosFondosService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    // =========================================================
    // CATÁLOGOS - CONSIGNACIONES BANCARIAS
    // =========================================================

    /**
     * Consulta las cuentas bancarias habilitadas
     * para recibir consignaciones en una agencia.
     *
     * GET /cuentas-bancarias?idAgencia=2
     *
     * @param idAgencia identificador de la agencia.
     * @return cuentas bancarias disponibles.
     */
    @GetMapping("/cuentas-bancarias")
    public ResponseEntity<List<
            ConsignacionesBancariasRepository.CuentaBancariaConsignacion>>
    listarCuentasBancarias(
            @RequestParam Integer idAgencia
    ) {

        validarAcceso(PERMISO_VIEW);

        return ResponseEntity.ok(
                service.listarCuentasBancarias(idAgencia)
        );
    }

    /**
     * Consulta los cheques recibidos disponibles
     * para consignación bancaria.
     *
     * GET /cheques?idAgencia=2
     *
     * @param idAgencia identificador de la agencia.
     * @return cheques disponibles.
     */
    @GetMapping("/cheques")
    public ResponseEntity<List<
            ConsignacionesBancariasRepository.ChequeDisponible>>
    listarCheques(
            @RequestParam Integer idAgencia
    ) {

        validarAcceso(PERMISO_VIEW);

        return ResponseEntity.ok(
                service.listarChequesRecibidos(idAgencia)
        );
    }

    /**
     * Consulta las cajas CONTROL con provisión
     * disponible para consignaciones de efectivo.
     *
     * GET /cajas?idAgencia=2&fechaConsignacion=2026-10-15
     *
     * @param idAgencia identificador de la agencia.
     * @param fechaConsignacion fecha de la consignación.
     * @return cajas disponibles.
     */
    @GetMapping("/cajas")
    public ResponseEntity<List<
            ConsignacionesBancariasRepository.CajaEfectivoDisponible>>
    listarCajas(
            @RequestParam Integer idAgencia,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaConsignacion
    ) {

        validarAcceso(PERMISO_VIEW);

        return ResponseEntity.ok(
                service.listarCajasConProvision(
                        idAgencia,
                        fechaConsignacion
                )
        );
    }

    // =========================================================
    // CATÁLOGOS - TRASLADOS ENTRE CAJAS
    // =========================================================

    /**
     * Consulta las cajas CONTROL y OPERATIVA
     * habilitadas para movimientos internos.
     *
     * GET /cajas-movimiento?idAgencia=2
     *
     * @param idAgencia identificador de la agencia.
     * @return cajas disponibles para traslados.
     */
    @GetMapping("/cajas-movimiento")
    public ResponseEntity<List<
            TrasladosCajasRepository.CajaMovimientoDisponible>>
    listarCajasMovimiento(
            @RequestParam Integer idAgencia
    ) {

        validarAcceso(PERMISO_VIEW);

        return ResponseEntity.ok(
                service.listarCajasMovimiento(idAgencia)
        );
    }

    /**
     * Consulta las cajas CONTROL y OPERATIVA
     * con provisión ABIERTA en la fecha indicada.
     *
     * GET /cajas-traslado-abiertas
     *
     * @param idAgencia identificador de la agencia.
     * @param fechaContable fecha contable del traslado.
     * @return cajas con provisión abierta.
     */
    @GetMapping("/cajas-traslado-abiertas")
    public ResponseEntity<List<
            TrasladosCajasRepository.CajaTrasladoAbierta>>
    listarCajasTrasladoAbiertas(
            @RequestParam Integer idAgencia,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaContable
    ) {

        validarAcceso(PERMISO_VIEW);

        return ResponseEntity.ok(
                service.listarCajasTrasladoAbiertas(
                        idAgencia,
                        fechaContable
                )
        );
    }

    // =========================================================
    // CATÁLOGOS - RETIROS BANCARIOS
    // =========================================================

    /**
     * Consulta las cuentas bancarias habilitadas
     * para realizar retiros de efectivo.
     *
     * Solo incluye cuentas con permiso de retiro
     * para la agencia seleccionada.
     *
     * GET /cuentas-retiro?idAgencia=2
     *
     * @param idAgencia identificador de la agencia.
     * @return cuentas bancarias habilitadas para retiro.
     */
    @GetMapping("/cuentas-retiro")
    public ResponseEntity<List<
            RetirosBancariosRepository.CuentaBancariaRetiro>>
    listarCuentasRetiro(
            @RequestParam Integer idAgencia
    ) {

        validarAcceso(PERMISO_VIEW);

        return ResponseEntity.ok(
                service.listarCuentasRetiro(idAgencia)
        );
    }

    /**
     * Consulta las cajas CONTROL habilitadas
     * para recibir retiros bancarios.
     *
     * GET /cajas-control-retiro?idAgencia=2
     *
     * @param idAgencia identificador de la agencia.
     * @return cajas CONTROL disponibles.
     */
    @GetMapping("/cajas-control-retiro")
    public ResponseEntity<List<
            RetirosBancariosRepository.CajaControl>>
    listarCajasControlRetiro(
            @RequestParam Integer idAgencia
    ) {

        validarAcceso(PERMISO_VIEW);

        return ResponseEntity.ok(
                service.listarCajasControlRetiro(idAgencia)
        );
    }

    // =========================================================
    // CATÁLOGOS - TRASLADOS BANCARIOS
    // =========================================================

    /**
     * Consulta las cuentas bancarias habilitadas para
     * retirar fondos de la agencia.
     *
     * GET /cuentas-origen-traslado-bancario?idAgencia=2
     */
    @GetMapping("/cuentas-origen-traslado-bancario")
    public ResponseEntity<List<
            TrasladosBancariosRepository.CuentaBancaria>>
    listarCuentasOrigenTrasladoBancario(
            @RequestParam Integer idAgencia
    ) {

        validarAcceso(PERMISO_VIEW);

        return ResponseEntity.ok(
                service.listarCuentasOrigenTrasladoBancario(idAgencia)
        );
    }

    /**
     * Consulta las cuentas bancarias habilitadas para
     * recibir fondos en la agencia.
     *
     * GET /cuentas-destino-traslado-bancario?idAgencia=2
     */
    @GetMapping("/cuentas-destino-traslado-bancario")
    public ResponseEntity<List<
            TrasladosBancariosRepository.CuentaBancaria>>
    listarCuentasDestinoTrasladoBancario(
            @RequestParam Integer idAgencia
    ) {

        validarAcceso(PERMISO_VIEW);

        return ResponseEntity.ok(
                service.listarCuentasDestinoTrasladoBancario(idAgencia)
        );
    }



    // =========================================================
    // VISTA PREVIA CONTABLE
    // =========================================================

    /**
     * Construye la vista previa de las partidas contables
     * de un movimiento de fondos.
     *
     * No genera consecutivos, auxiliares contables
     * ni movimientos físicos de caja.
     *
     * POST /preview-contable
     *
     * @param request datos del movimiento.
     * @return vista previa contable.
     */
    @PostMapping("/preview-contable")
    public ResponseEntity<MovimientoFondoPreviewDTO>
    previewContable(
            @RequestBody MovimientoFondoRequestDTO request
    ) {

        validarAcceso(PERMISO_EXECUTE);

        return ResponseEntity.ok(
                service.preview(request)
        );
    }

    // =========================================================
    // APLICACIÓN - CONSIGNACIONES BANCARIAS
    // =========================================================

    /**
     * Ejecuta una consignación bancaria.
     *
     * Delega la contabilización y los movimientos
     * físicos al servicio de consignaciones.
     *
     * POST /consignaciones
     *
     * @param request datos de la consignación.
     * @return resultado de la consignación aplicada.
     */
    @PostMapping("/consignaciones")
    public ResponseEntity<MovimientoFondoResponseDTO>
    aplicarConsignacion(
            @RequestBody MovimientoFondoRequestDTO request
    ) {

        validarAcceso(PERMISO_EXECUTE);

        validarTipoMovimiento(
                request,
                MovimientoFondoRequestDTO.TipoMovimiento.CONSIGNACION
        );

        return ResponseEntity.ok(
                service.aplicar(request)
        );
    }

    // =========================================================
    // APLICACIÓN - TRASLADOS ENTRE CAJAS
    // =========================================================

    /**
     * Ejecuta una provisión o reembolso
     * entre cajas CONTROL y OPERATIVA.
     *
     * POST /traslados-cajas
     *
     * @param request datos del traslado.
     * @return resultado del traslado aplicado.
     */
    @PostMapping("/traslados-cajas")
    public ResponseEntity<MovimientoFondoResponseDTO>
    aplicarTrasladoCajas(
            @RequestBody MovimientoFondoRequestDTO request
    ) {

        validarAcceso(PERMISO_EXECUTE);

        validarTipoMovimiento(
                request,
                MovimientoFondoRequestDTO.TipoMovimiento.TRASLADO_CAJAS
        );

        return ResponseEntity.ok(
                service.aplicar(request)
        );
    }

    // =========================================================
    // PREPARACIÓN - RETIROS BANCARIOS
    // =========================================================

    /**
     * Prepara un retiro bancario para autorización.
     *
     * Guarda:
     *
     * - Cabecera del comprobante.
     * - Partidas contables pendientes.
     * - Movimiento físico de caja proyectado.
     *
     * El comprobante queda PENDIENTE.
     *
     * No genera consecutivo ni contabiliza.
     *
     * POST /retiros/preparar
     *
     * @param request datos del retiro.
     * @return comprobante pendiente de autorización.
     */
    @PostMapping("/retiros/preparar")
    public ResponseEntity<
            RetirosBancariosService.RetiroPendienteResponse>
    prepararRetiro(
            @RequestBody MovimientoFondoRequestDTO request
    ) {

        validarAcceso(PERMISO_EXECUTE);

        validarTipoMovimiento(
                request,
                MovimientoFondoRequestDTO.TipoMovimiento.RETIRO
        );

        return ResponseEntity.ok(
                service.prepararRetiro(request)
        );
    }

    // =========================================================
    // PREPARACIÓN - TRASLADOS BANCARIOS
    // =========================================================

    /**
     * Prepara un traslado entre cuentas bancarias.
     *
     * Modalidades:
     * - EFECTIVO: transporte físico entre bancos.
     * - BRE_B: transferencia electrónica inmediata.
     * - TRANSFERENCIA_NORMAL: transferencia tradicional.
     *
     * El comprobante queda PENDIENTE de autorización.
     *
     * No contabiliza ni genera consecutivo definitivo.
     * No registra movimientos de caja.
     *
     * POST /traslados-bancarios/preparar
     */
    @PostMapping("/traslados-bancarios/preparar")
    public ResponseEntity<
            TrasladosBancariosService.TrasladoPendienteResponse>
    prepararTrasladoBancario(
            @RequestBody MovimientoFondoRequestDTO request
    ) {

        validarAcceso(PERMISO_EXECUTE);

        validarTipoMovimiento(
                request,
                MovimientoFondoRequestDTO.TipoMovimiento
                        .TRASLADO_BANCARIO
        );

        return ResponseEntity.ok(
                service.prepararTrasladoBancario(request)
        );
    }

    // =========================================================
    // VALIDACIONES INTERNAS
    // =========================================================

    /**
     * Verifica que la solicitud corresponda
     * al tipo de movimiento permitido por el endpoint.
     *
     * @param request solicitud recibida.
     * @param tipoEsperado tipo permitido.
     */
    private void validarTipoMovimiento(
            MovimientoFondoRequestDTO request,
            MovimientoFondoRequestDTO.TipoMovimiento tipoEsperado
    ) {

        if (request == null
                || request.getTipoMovimiento() != tipoEsperado) {

            throw new IllegalArgumentException(
                    "La operación debe ser "
                            + tipoEsperado.name() + "."
            );
        }
    }

    /**
     * Valida que el usuario autenticado tenga
     * el permiso requerido para la operación.
     *
     * Utiliza los permisos efectivos del usuario,
     * independientemente de sus roles asignados.
     *
     * @param permiso código del permiso requerido.
     */
    private void validarAcceso(String permiso) {

        Usuario usuarioActual = new Usuario();

        usuarioActual.setIdUsuario(
                usuarioSesionService.idUsuario()
        );

        accessValidator.validarAcceso(
                usuarioActual,
                permiso
        );
    }
}
