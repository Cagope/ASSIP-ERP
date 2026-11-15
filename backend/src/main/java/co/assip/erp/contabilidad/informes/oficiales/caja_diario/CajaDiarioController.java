
package co.assip.erp.contabilidad.informes.oficiales.caja_diario;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Consulta y exporta el libro Caja - Diario por agencia autorizada.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/contabilidad/informes/oficiales/caja-diario")
@PreAuthorize("hasAuthority('CONTABILIDAD_VIEW')")
public class CajaDiarioController {

    private final CajaDiarioService service;
    private final CajaDiarioPdfService pdf;
    private final CajaDiarioExcelService excel;

    /**
     * Devuelve libro diario, subtotales por fecha y resumen por cuenta.
     */
    @PostMapping("/consultar")
    public CajaDiarioService.Resultado consultar(
            @RequestBody CajaDiarioService.Filtro filtro) {

        return service.consultar(filtro);
    }

    /**
     * Genera las dos secciones en tamaño carta,
     * con presentación interna u oficial.
     */
    @PostMapping(
            value = "/pdf",
            produces = MediaType.APPLICATION_PDF_VALUE
    )
    public ResponseEntity<byte[]> pdf(
            @RequestBody SolicitudPdf solicitud) {

        var resultado = service.consultar(solicitud.filtro());

        byte[] bytes = pdf.generar(
                resultado,
                solicitud.formato()
        );

        String nombreArchivo =
                "caja_diario_" +
                        resultado.ano() + "_" +
                        (resultado.acumuladoAnual() ? "anual" : String.format("%02d", resultado.mes())) + ".pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + nombreArchivo + "\""
                )
                .body(bytes);
    }

    /**
     * Exporta las dos secciones con importes numéricos
     * compatibles con Excel.
     */
    @PostMapping(
            value = "/excel",
            produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    )
    public ResponseEntity<byte[]> excel(
            @RequestBody CajaDiarioService.Filtro filtro) {

        var resultado = service.consultar(filtro);

        String nombreArchivo =
                "caja_diario_" +
                        resultado.ano() + "_" +
                        (resultado.acumuladoAnual() ? "anual" : String.format("%02d", resultado.mes())) + ".xlsx";

        return ResponseEntity.ok()
                .contentType(
                        MediaType.parseMediaType(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        )
                )
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + nombreArchivo + "\""
                )
                .body(excel.generar(resultado));
    }

    /**
     * Parámetros de exportación PDF.
     */
    public record SolicitudPdf(
            CajaDiarioService.Filtro filtro,
            String formato
    ) {
    }
}
