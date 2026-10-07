package co.assip.erp.nomina.informes.novedades_concepto;

import co.assip.erp.nomina.informes.novedades_concepto.dto.NovedadesConceptoInformeDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/nomina/informes/novedades-concepto")
@RequiredArgsConstructor
public class NovedadesConceptoInformeController {

    private final NovedadesConceptoInformeService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    // ============================================================
    // CONSULTAR
    // ============================================================
    @GetMapping
    public List<NovedadesConceptoInformeDTO> consultar(
            @RequestParam(required = false) String codigoConcepto,
            @RequestParam(required = false) Integer idEmpleado,
            @RequestParam(required = false) Integer idPeriodo,
            @RequestParam(required = false) LocalDate fechaInicial,
            @RequestParam(required = false) LocalDate fechaFinal
    ) {

        validarAcceso("NOMINA_INFORMES_VIEW");

        return service.consultar(
                codigoConcepto,
                idEmpleado,
                idPeriodo,
                fechaInicial,
                fechaFinal
        );
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