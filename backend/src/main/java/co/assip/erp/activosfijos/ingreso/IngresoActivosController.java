package co.assip.erp.activosfijos.ingreso;

import co.assip.erp.activosfijos.ingreso.dto.IngresoActivosRequestDTO;
import co.assip.erp.activosfijos.ingreso.dto.IngresoActivosResponseDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/activos-fijos/ingreso")
@RequiredArgsConstructor
public class IngresoActivosController {

    private static final String PERMISO =
            "ACTIVOS_FIJOS_INGRESO";

    private final IngresoActivosService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    @PostMapping
    public ResponseEntity<IngresoActivosResponseDTO> ingresar(
            @Valid @RequestBody IngresoActivosRequestDTO request
    ) {

        validarAcceso();

        IngresoActivosResponseDTO response =
                service.ingresarActivos(request);

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // SEGURIDAD
    // =========================================================
    private void validarAcceso() {

        Usuario usuarioActual = new Usuario();
        usuarioActual.setIdUsuario(
                usuarioSesionService.idUsuario()
        );

        accessValidator.validarAcceso(
                usuarioActual,
                PERMISO
        );
    }
}