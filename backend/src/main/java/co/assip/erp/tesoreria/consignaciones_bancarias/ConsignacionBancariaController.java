package co.assip.erp.tesoreria.consignaciones_bancarias;

import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import co.assip.erp.tesoreria.consignaciones_bancarias.dto.ConsignacionBancariaPreviewResponse;
import co.assip.erp.tesoreria.consignaciones_bancarias.dto.ConsignacionBancariaRequest;
import co.assip.erp.tesoreria.consignaciones_bancarias.dto.ConsignacionBancariaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/tesoreria/consignaciones-bancarias")
@RequiredArgsConstructor
public class ConsignacionBancariaController {

    private static final String PERMISO_VIEW =
            "TESORERIA_CONSIGNACIONES_BANCARIAS_VIEW";

    private static final String PERMISO_EXECUTE =
            "TESORERIA_CONSIGNACIONES_BANCARIAS_EXECUTE";

    private final ConsignacionBancariaService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    // =========================================================
    // CUENTAS BANCARIAS DISPONIBLES PARA CONSIGNAR
    // =========================================================

    @GetMapping("/cuentas-bancarias")
    public ResponseEntity<
            List<ConsignacionBancariaRepository.CuentaBancariaConsignacion>>
    listarCuentasBancarias(
            @RequestParam Integer idAgencia
    ) {

        validarAcceso(PERMISO_VIEW);

        return ResponseEntity.ok(
                service.listarCuentasBancarias(
                        idAgencia
                )
        );
    }

    // =========================================================
    // CHEQUES DISPONIBLES
    // =========================================================

    @GetMapping("/cheques")
    public ResponseEntity<
            List<ConsignacionBancariaRepository.ChequeDisponible>>
    listarCheques(
            @RequestParam Integer idAgencia
    ) {

        validarAcceso(PERMISO_VIEW);

        return ResponseEntity.ok(
                service.listarChequesRecibidos(
                        idAgencia
                )
        );
    }

    // =========================================================
    // CAJAS CON PROVISIÓN CREADA
    // =========================================================

    @GetMapping("/cajas")
    public ResponseEntity<
            List<ConsignacionBancariaRepository.CajaEfectivoDisponible>>
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
    // PREVIEW CONTABLE
    // =========================================================

    @PostMapping("/preview-contable")
    public ResponseEntity<ConsignacionBancariaPreviewResponse>
    previewContable(
            @RequestBody ConsignacionBancariaRequest request
    ) {

        validarAcceso(PERMISO_EXECUTE);

        return ResponseEntity.ok(
                service.preview(
                        request
                )
        );
    }

    // =========================================================
    // APLICAR CONSIGNACIÓN BANCARIA
    // =========================================================

    @PostMapping
    public ResponseEntity<ConsignacionBancariaResponse> aplicar(
            @RequestBody ConsignacionBancariaRequest request
    ) {

        validarAcceso(PERMISO_EXECUTE);

        return ResponseEntity.ok(
                service.aplicar(
                        request
                )
        );
    }

    // =========================================================
    // SEGURIDAD
    // =========================================================

    private void validarAcceso(
            String permiso
    ) {

        Usuario usuarioActual =
                new Usuario();

        usuarioActual.setIdUsuario(
                usuarioSesionService.idUsuario()
        );

        accessValidator.validarAcceso(
                usuarioActual,
                permiso
        );
    }
}