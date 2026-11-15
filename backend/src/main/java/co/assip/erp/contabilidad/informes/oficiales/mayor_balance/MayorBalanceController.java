package co.assip.erp.contabilidad.informes.oficiales.mayor_balance;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** API de consulta y exportación del libro oficial Mayor y Balance. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/contabilidad/informes/oficiales/mayor-balance")
@PreAuthorize("hasAuthority('CONTABILIDAD_VIEW')")
public class MayorBalanceController {
    private final MayorBalanceService service;
    private final MayorBalancePdfService pdf;
    private final MayorBalanceExcelService excel;
    public record SolicitudPdf(MayorBalanceService.Filtro filtro, String formato) {}

    @PostMapping("/consultar")
    public MayorBalanceService.Resultado consultar(@RequestBody MayorBalanceService.Filtro filtro) {
        return service.consultar(filtro);
    }
    @PostMapping(value="/pdf", produces=MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> pdf(@RequestBody SolicitudPdf solicitud) {
        var r = service.consultar(solicitud.filtro());
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"mayor_balance_" + r.ano() +
                (r.acumuladoAnual() ? "_anual" : "_" + r.mes()) + ".pdf\"")
            .body(pdf.generar(r, solicitud.formato()));
    }
    @PostMapping(value="/excel", produces="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> excel(@RequestBody MayorBalanceService.Filtro filtro) {
        var r = service.consultar(filtro);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"mayor_balance_" + r.ano() +
                (r.acumuladoAnual() ? "_anual" : "_" + r.mes()) + ".xlsx\"")
            .body(excel.generar(r));
    }
}
