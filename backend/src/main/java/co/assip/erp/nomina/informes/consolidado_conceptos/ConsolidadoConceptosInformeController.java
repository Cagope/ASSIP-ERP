package co.assip.erp.nomina.informes.consolidado_conceptos;

import co.assip.erp.nomina.informes.consolidado_conceptos.dto.ConsolidadoConceptosInformeDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/nomina/informes/consolidado-conceptos")
@RequiredArgsConstructor
public class ConsolidadoConceptosInformeController {

    private final ConsolidadoConceptosInformeService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    // ============================================================
    // CONSULTAR
    // ============================================================
    @GetMapping
    public List<ConsolidadoConceptosInformeDTO> consultar(
            @RequestParam(required = false) String codigoConcepto,
            @RequestParam(required = false) String tipoConcepto,
            @RequestParam(required = false) Integer idPeriodo,
            @RequestParam(required = false) LocalDate fechaInicial,
            @RequestParam(required = false) LocalDate fechaFinal
    ) {

        validarAcceso("NOMINA_INFORMES_VIEW");

        return service.consultar(
                codigoConcepto,
                tipoConcepto,
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