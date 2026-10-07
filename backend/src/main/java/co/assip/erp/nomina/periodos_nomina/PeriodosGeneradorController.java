package co.assip.erp.nomina.periodos_nomina;

import co.assip.erp.nomina.periodos_nomina.dto.PeriodoGeneradoDTO;
import co.assip.erp.nomina.periodos_nomina.dto.PeriodosGenerarRequestDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/nomina/periodos-nomina/generar")
@RequiredArgsConstructor
public class PeriodosGeneradorController {

    private final PeriodosGeneradorService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    // ============================================================
    // GENERAR PERÍODOS
    // ============================================================
    @PostMapping
    public ResponseEntity<List<PeriodoGeneradoDTO>> generar(
            @RequestBody PeriodosGenerarRequestDTO req
    ) {

        validarAcceso("NOMINA_PARAMETROS_EDIT");

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        List<PeriodoGeneradoDTO> result =
                service.generar(
                        req,
                        idUsuario
                );

        return ResponseEntity.ok(result);
    }

    // ============================================================
    // AUTORIZACIÓN
    // ============================================================
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