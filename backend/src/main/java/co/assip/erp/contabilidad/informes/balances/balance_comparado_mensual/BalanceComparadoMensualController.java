
package co.assip.erp.contabilidad.informes.balances.balance_comparado_mensual;

import co.assip.erp.contabilidad.informes.balances.balance_comparado_mensual.dto.BalanceComparadoMensualFiltroDTO;
import co.assip.erp.contabilidad.informes.balances.balance_comparado_mensual.dto.BalanceComparadoMensualRespuestaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Expone la consulta y exportación del balance comparado entre dos períodos. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/contabilidad/informes/balances/balance-comparado-mensual")
public class BalanceComparadoMensualController {
    private final BalanceComparadoMensualService service;
    private final BalanceComparadoMensualPdfService pdfService;
    private final BalanceComparadoMensualExcelService excelService;

    /** Consulta el balance validando permisos, agencia y ambos períodos. */
    @PostMapping("/consultar")
    public ResponseEntity<BalanceComparadoMensualRespuestaDTO> consultar(
            @RequestBody BalanceComparadoMensualFiltroDTO filtro
    ) {
        return ResponseEntity.ok(service.consultar(filtro));
    }

    /** Exporta el PDF identificando los dos períodos seleccionados. */
    @PostMapping(value = "/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> exportarPdf(
            @RequestBody BalanceComparadoMensualFiltroDTO filtro
    ) {
        BalanceComparadoMensualRespuestaDTO balance = service.consultar(filtro);
        byte[] documento = pdfService.generar(balance);

        String nombreArchivo = nombreArchivo(balance, "pdf");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + nombreArchivo + "\""
                )
                .contentLength(documento.length)
                .body(documento);
    }

    /** Exporta el Excel con los mismos saldos y períodos del informe. */
    @PostMapping(
            value = "/excel",
            produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    )
    public ResponseEntity<byte[]> exportarExcel(
            @RequestBody BalanceComparadoMensualFiltroDTO filtro
    ) {
        BalanceComparadoMensualRespuestaDTO balance = service.consultar(filtro);
        byte[] documento = excelService.generar(balance);

        String nombreArchivo = nombreArchivo(balance, "xlsx");

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                ))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + nombreArchivo + "\""
                )
                .contentLength(documento.length)
                .body(documento);
    }

    /** Construye un nombre de archivo con agencia y ambos períodos. */
    private String nombreArchivo(
            BalanceComparadoMensualRespuestaDTO balance,
            String extension
    ) {
        return "balance_comparado_mensual_"
                + balance.codigoAgencia() + "_"
                + balance.anoPeriodo1() + "-"
                + String.format("%02d", balance.mesPeriodo1()) + "_"
                + balance.anoPeriodo2() + "-"
                + String.format("%02d", balance.mesPeriodo2()) + "."
                + extension;
    }
}
