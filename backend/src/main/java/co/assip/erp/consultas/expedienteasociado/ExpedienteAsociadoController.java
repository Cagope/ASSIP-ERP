package co.assip.erp.consultas.expedienteasociado;

import co.assip.erp.consultas.expedienteasociado.dto.ExpedienteAsociadoDTO;
import co.assip.erp.consultas.expedienteasociado.dto.ExpedientePersonaBusquedaDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * ============================================================
 * Expediente Integral del Asociado
 * ============================================================
 *
 * Consulta consolidada de:
 *
 * - Datos generales.
 * - Afiliación.
 * - Contacto.
 * - Información financiera.
 * - SARLAFT.
 * - Participación institucional.
 * - Cuentas de ahorro.
 * - CDAT.
 * - Créditos.
 * - Bienes.
 * - Garantías.
 * - Indicadores.
 * - Alertas.
 *
 * El context-path general del proyecto es:
 *
 * /api/v1
 *
 * Por lo tanto, la ruta completa es:
 *
 * GET /api/v1/consultas/expediente-asociado/{idDatosPersonal}
 */
@RestController
@RequestMapping("/consultas/expediente-asociado")
@RequiredArgsConstructor
public class ExpedienteAsociadoController {

    private static final String PERMISO_VIEW =
            "EXPEDIENTE_ASOCIADO_VIEW";

    private final ExpedienteAsociadoService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    // =========================================================
    // Búsqueda de personas
    // =========================================================

    @GetMapping(
            value = "/buscar",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public List<ExpedientePersonaBusquedaDTO> buscar(
            @RequestParam(
                    value = "documento",
                    required = false,
                    defaultValue = ""
            )
            String documento,

            @RequestParam(
                    value = "nombres",
                    required = false,
                    defaultValue = ""
            )
            String nombres,

            @RequestParam(
                    value = "primerApellido",
                    required = false,
                    defaultValue = ""
            )
            String primerApellido,

            @RequestParam(
                    value = "segundoApellido",
                    required = false,
                    defaultValue = ""
            )
            String segundoApellido
    ) {
        validarConsulta();

        return service.buscarPersonas(
                documento,
                nombres,
                primerApellido,
                segundoApellido
        );
    }

    // =========================================================
    // Consulta integral
    // =========================================================

    /**
     * Consulta el expediente integral de una persona.
     *
     * @param idDatosPersonal identificador interno de la persona
     * @return expediente integral consolidado
     */
    @GetMapping(
            value = "/{idDatosPersonal}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ExpedienteAsociadoDTO consultar(
            @PathVariable("idDatosPersonal")
            Long idDatosPersonal
    ) {
        validarConsulta();

        return service.consultar(idDatosPersonal);
    }

    // =========================================================
    // Seguridad
    // =========================================================

    private void validarConsulta() {
        accessValidator.validarAcceso(
                usuarioActual(),
                PERMISO_VIEW
        );
    }

    private Usuario usuarioActual() {
        Usuario usuarioActual = new Usuario();

        usuarioActual.setIdUsuario(
                usuarioSesionService.idUsuario()
        );

        return usuarioActual;
    }
}