
package co.assip.erp.contabilidad.informes.balances.balance_comprobacion;

import co.assip.erp.contabilidad.informes.balances.balance_comprobacion.dto.BalanceComprobacionFiltroDTO;
import co.assip.erp.contabilidad.informes.balances.balance_comprobacion.dto.BalanceComprobacionRespuestaDTO;
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
@RequestMapping("/contabilidad/informes/balances/balance-comprobacion")
public class BalanceComprobacionController {
    private final BalanceComprobacionService service;
    private final BalanceComprobacionPdfService pdfService;
    private final BalanceComprobacionExcelService excelService;

    /** Consulta el balance tradicional o comparativo, validando permisos, agencia y períodos. */
    @PostMapping("/consultar")
    public ResponseEntity<BalanceComprobacionRespuestaDTO> consultar(
            @RequestBody BalanceComprobacionFiltroDTO filtro
    ) {
        return ResponseEntity.ok(service.consultar(filtro));
    }

    /** Consulta movimientos paginados de una cuenta operable con validación de permisos y agencia. */
    @PostMapping("/movimientos")
    public ResponseEntity<BalanceComprobacionService.MovimientosCuentaRespuesta> consultarMovimientos(
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

    /** Agrupa los filtros de consulta sin introducir otro archivo DTO. */
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
            @RequestBody BalanceComprobacionFiltroDTO filtro
    ) {
        BalanceComprobacionRespuestaDTO balance = service.consultar(filtro);
        byte[] documento = pdfService.generar(balance);

        String nombreArchivo = "balance_comprobacion_"
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
            @RequestBody BalanceComprobacionFiltroDTO filtro
    ) {
        BalanceComprobacionRespuestaDTO balance = service.consultar(filtro);
        byte[] documento = excelService.generar(balance);

        String nombreArchivo = "balance_comprobacion_"
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
