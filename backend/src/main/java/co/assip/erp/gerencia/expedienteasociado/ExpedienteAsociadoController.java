package co.assip.erp.gerencia.expedienteasociado;

import co.assip.erp.gerencia.expedienteasociado.dto.ExpedienteAsociadoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import co.assip.erp.gerencia.expedienteasociado.dto.ExpedientePersonaBusquedaDTO;
import org.springframework.web.bind.annotation.RequestParam;

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
 * GET /api/v1/gerencia/expediente-asociado/{idDatosPersonal}
 */
@RestController
@RequestMapping("/gerencia/expediente-asociado")
@RequiredArgsConstructor
public class ExpedienteAsociadoController {

    private final ExpedienteAsociadoService service;

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
        return service.consultar(idDatosPersonal);
    }
}