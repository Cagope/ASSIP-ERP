
package co.assip.erp.tesoreria.movimientos_fondos;

import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.tesoreria.consignaciones_bancarias.ConsignacionesBancariasRepository;
import co.assip.erp.tesoreria.traslados_cajas.TrasladosCajasRepository;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import co.assip.erp.tesoreria.movimientos_fondos.dto.MovimientoFondoPreviewDTO;
import co.assip.erp.tesoreria.movimientos_fondos.dto.MovimientoFondoRequestDTO;
import co.assip.erp.tesoreria.movimientos_fondos.dto.MovimientoFondoResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

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
    // CUENTAS BANCARIAS
    // =========================================================

    @GetMapping("/cuentas-bancarias")
    public ResponseEntity<List<ConsignacionesBancariasRepository.CuentaBancariaConsignacion>>
    listarCuentasBancarias(@RequestParam Integer idAgencia) {

        validarAcceso(PERMISO_VIEW);

        return ResponseEntity.ok(
                service.listarCuentasBancarias(idAgencia)
        );
    }

    // =========================================================
    // CHEQUES DISPONIBLES
    // =========================================================

    @GetMapping("/cheques")
    public ResponseEntity<List<ConsignacionesBancariasRepository.ChequeDisponible>>
    listarCheques(@RequestParam Integer idAgencia) {

        validarAcceso(PERMISO_VIEW);

        return ResponseEntity.ok(
                service.listarChequesRecibidos(idAgencia)
        );
    }

    // =========================================================
    // CAJAS CON PROVISIÓN - CONSIGNACIONES
    // =========================================================

    @GetMapping("/cajas")
    public ResponseEntity<List<ConsignacionesBancariasRepository.CajaEfectivoDisponible>>
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
    // CAJAS CONTROL Y OPERATIVAS
    // =========================================================

    @GetMapping("/cajas-movimiento")
    public ResponseEntity<List<TrasladosCajasRepository.CajaMovimientoDisponible>>
    listarCajasMovimiento(@RequestParam Integer idAgencia) {

        validarAcceso(PERMISO_VIEW);

        return ResponseEntity.ok(
                service.listarCajasMovimiento(idAgencia)
        );
    }

    // =========================================================
    // VISTA PREVIA CONTABLE
    // =========================================================

    @PostMapping("/preview-contable")
    public ResponseEntity<MovimientoFondoPreviewDTO>
    previewContable(@RequestBody MovimientoFondoRequestDTO request) {

        validarAcceso(PERMISO_EXECUTE);

        return ResponseEntity.ok(service.preview(request));
    }

    // =========================================================
    // APLICAR CONSIGNACIÓN
    // =========================================================

    @PostMapping("/consignaciones")
    public ResponseEntity<MovimientoFondoResponseDTO>
    aplicarConsignacion(@RequestBody MovimientoFondoRequestDTO request) {

        validarAcceso(PERMISO_EXECUTE);

        if (request == null ||
                request.getTipoMovimiento() !=
                        MovimientoFondoRequestDTO.TipoMovimiento.CONSIGNACION) {

            throw new IllegalArgumentException(
                    "La operación debe ser CONSIGNACION."
            );
        }

        return ResponseEntity.ok(service.aplicar(request));
    }

    // =========================================================
    // PROVISIÓN Y REEMBOLSO ENTRE CAJAS
    // =========================================================

    @PostMapping("/traslados-cajas")
    public ResponseEntity<MovimientoFondoResponseDTO>
    aplicarTrasladoCajas(@RequestBody MovimientoFondoRequestDTO request) {

        validarAcceso(PERMISO_EXECUTE);

        if (request == null ||
                request.getTipoMovimiento() !=
                        MovimientoFondoRequestDTO.TipoMovimiento.TRASLADO_CAJAS) {

            throw new IllegalArgumentException(
                    "La operación debe ser TRASLADO_CAJAS."
            );
        }

        return ResponseEntity.ok(service.aplicar(request));
    }

    // =========================================================
    // SEGURIDAD
    // =========================================================

    private void validarAcceso(String permiso) {

        Usuario usuarioActual = new Usuario();
        usuarioActual.setIdUsuario(usuarioSesionService.idUsuario());

        accessValidator.validarAcceso(
                usuarioActual,
                permiso
        );
    }
}
