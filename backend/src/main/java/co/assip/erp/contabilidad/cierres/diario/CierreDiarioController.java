package co.assip.erp.contabilidad.cierres.diario;

import co.assip.erp.contabilidad.cierres.diario.dto.CierreDiarioInformeDTO;
import co.assip.erp.contabilidad.cierres.diario.dto.CierreDiarioMensualDTO;
import co.assip.erp.contabilidad.cierres.diario.dto.CierreDiarioRequestDTO;
import co.assip.erp.contabilidad.cierres.diario.dto.CierreDiarioResumenDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Expone la consulta mensual, consulta diaria, cierre, reapertura
 * e informe contable por agencia y fecha.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/contabilidad/cierres/diario")
public class CierreDiarioController {

    private final CierreDiarioService service;

    /** Consulta los días del mes, sus estados y resultados contables. */
    @GetMapping("/mes")
    public CierreDiarioMensualDTO consultarMes(
            @RequestParam Integer idAgencia,
            @RequestParam int ano,
            @RequestParam int mes
    ) {
        return service.consultarMes(idAgencia, ano, mes);
    }

    /** Consulta el estado y los movimientos contables de una fecha. */
    @GetMapping
    public CierreDiarioResumenDTO consultar(
            @RequestParam Integer idAgencia,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha
    ) {
        return service.consultar(idAgencia, fecha);
    }

    /** Consulta el informe formal del día para impresión y exportación. */
    @GetMapping("/informe")
    public CierreDiarioInformeDTO consultarInforme(
            @RequestParam Integer idAgencia,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha
    ) {
        return service.consultarInforme(idAgencia, fecha);
    }

    /** Ejecuta el cierre sin modificar los estados de los auxiliares. */
    @PostMapping("/cerrar")
    public CierreDiarioResumenDTO cerrar(
            @RequestBody CierreDiarioRequestDTO dto,
            HttpServletRequest request
    ) {
        return service.cerrar(
                dto.idAgencia(),
                dto.fecha(),
                request
        );
    }

    /** Reabre un día con justificación, permisos y validación de períodos. */
    @PostMapping("/reabrir")
    public CierreDiarioResumenDTO reabrir(
            @RequestBody CierreDiarioRequestDTO dto,
            HttpServletRequest request
    ) {
        return service.reabrir(
                dto.idAgencia(),
                dto.fecha(),
                dto.justificacion(),
                request
        );
    }
}
