
package co.assip.erp.contabilidad.informes.balances.balance_detallado_mensual;

import co.assip.erp.contabilidad.informes.balances.balance_detallado_mensual.dto.BalanceDetalladoMensualFiltroDTO;
import co.assip.erp.contabilidad.informes.balances.balance_detallado_mensual.dto.BalanceDetalladoMensualRespuestaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Expone la consulta y exportación del balance, respetando permisos y agencias autorizadas. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/contabilidad/informes/balances/balance-detallado-mensual")
public class BalanceDetalladoMensualController {
    private final BalanceDetalladoMensualService service;
    private final BalanceDetalladoMensualPdfService pdfService;
    private final BalanceDetalladoMensualExcelService excelService;

    /** Consulta el balance mensual o comparativo, validando permisos, agencia y períodos. */
    @PostMapping("/consultar")
    public ResponseEntity<BalanceDetalladoMensualRespuestaDTO> consultar(
            @RequestBody BalanceDetalladoMensualFiltroDTO filtro
    ) {
        return ResponseEntity.ok(service.consultar(filtro));
    }

    /**
     * Consulta los movimientos de una cuenta auxiliar en un período cerrado.
     * El servicio valida permisos, agencias, cuenta operable y paginación.
     */
    @PostMapping("/movimientos")
    public ResponseEntity<BalanceDetalladoMensualService.MovimientosCuentaRespuesta> consultarMovimientos(
            @RequestBody MovimientosCuentaSolicitud solicitud
    ) {
        return ResponseEntity.ok(service.consultarMovimientosCuenta(
                solicitud.idAgencia(),
                solicitud.ano(),
                solicitud.mes(),
                solicitud.codigoCuenta(),
                solicitud.pagina(),
                solicitud.tamanoPagina()
        ));
    }

    /** Recibe los filtros de movimientos sin crear un DTO adicional. */
    public record MovimientosCuentaSolicitud(
            Integer idAgencia,
            Integer ano,
            Integer mes,
            String codigoCuenta,
            Integer pagina,
            Integer tamanoPagina
    ) {}

    /** Genera el informe PDF a partir de los datos autorizados de la consulta. */
    @PostMapping(value = "/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> exportarPdf(
            @RequestBody BalanceDetalladoMensualFiltroDTO filtro
    ) {
        BalanceDetalladoMensualRespuestaDTO balance = service.consultar(filtro);
        byte[] documento = pdfService.generar(balance);

        String nombreArchivo = "balance_detallado_mensual_"
                + balance.codigoAgencia() + "_"
                + balance.ano() + "_"
                + String.format("%02d", balance.mes()) + ".pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + nombreArchivo + "\""
                )
                .contentLength(documento.length)
                .body(documento);
    }

    /** Genera el informe Excel con los mismos filtros y validaciones de la consulta. */
    @PostMapping(
            value = "/excel",
            produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    )
    public ResponseEntity<byte[]> exportarExcel(
            @RequestBody BalanceDetalladoMensualFiltroDTO filtro
    ) {
        BalanceDetalladoMensualRespuestaDTO balance = service.consultar(filtro);
        byte[] documento = excelService.generar(balance);

        String nombreArchivo = "balance_detallado_mensual_"
                + balance.codigoAgencia() + "_"
                + balance.ano() + "_"
                + String.format("%02d", balance.mes()) + ".xlsx";

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
}
