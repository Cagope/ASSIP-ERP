package co.assip.erp.contabilidad.comprobantes.consulta;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * Expone la búsqueda y recuperación de comprobantes contables manuales.
 * Las validaciones de permiso y agencia se realizan en el servicio.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/contabilidad/comprobantes")
public class ComprobantesConsultaController {

    private final ComprobantesConsultaService service;

    /**
     * Busca comprobantes manuales mediante filtros opcionales.
     * El servicio restringe los resultados a las agencias autorizadas.
     */
    @GetMapping("/buscar")
    public List<ComprobantesConsultaRepository.ResumenComprobante> buscar(
            @RequestParam(required = false) Integer idAgencia,
            @RequestParam(required = false) String tipoComprobante,
            @RequestParam(required = false) String numeroComprobante,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) String concepto,
            @RequestParam(required = false) Integer idDatosPersonal
    ) {
        return service.buscar(idAgencia, tipoComprobante, numeroComprobante,
                fechaDesde, fechaHasta, concepto, idDatosPersonal);
    }

    /**
     * Recupera la cabecera, los movimientos y sus controles documentales.
     * La consulta no modifica ni abre períodos contables.
     */
    @GetMapping("/{idAgencia}/{tipoComprobante}/{numeroComprobante}")
    public ComprobantesConsultaService.ComprobanteCompleto consultar(
            @PathVariable Integer idAgencia,
            @PathVariable String tipoComprobante,
            @PathVariable String numeroComprobante
    ) {
        return service.consultar(idAgencia, tipoComprobante, numeroComprobante);
    }
}
