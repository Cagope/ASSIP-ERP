package co.assip.erp.contabilidad.informes.balances.balances_vigencias;

import co.assip.erp.contabilidad.informes.balances.balances_vigencias.dto.BalancesVigenciasFiltroDTO;
import co.assip.erp.contabilidad.informes.balances.balances_vigencias.dto.BalancesVigenciasRespuestaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Consulta y exportación del Balances por Vigencias sin ejecutar cierres. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/contabilidad/informes/balances/balances-vigencias")
public class BalancesVigenciasController {
    private final BalancesVigenciasService service;
    private final BalancesVigenciasPdfService pdfService;
    private final BalancesVigenciasExcelService excelService;

    @PostMapping("/consultar")
    public BalancesVigenciasRespuestaDTO consultar(@RequestBody BalancesVigenciasFiltroDTO filtro) {
        return service.consultar(filtro);
    }

    public record MovimientosSolicitud(Integer idAgencia, Integer ano,
            String codigoCuenta, Integer pagina, Integer tamanoPagina) {}

    @PostMapping("/movimientos")
    public BalancesVigenciasService.MovimientosCuentaRespuesta movimientos(
            @RequestBody MovimientosSolicitud solicitud) {
        return service.consultarMovimientosCuenta(solicitud.idAgencia(),
                solicitud.ano(), solicitud.codigoCuenta(),
                solicitud.pagina(), solicitud.tamanoPagina());
    }

    @GetMapping("/cuentas-principales")
    public List<BalancesVigenciasRepository.CuentaPrincipal> cuentas(
            @RequestParam Integer idAgencia) {
        return service.listarCuentas(idAgencia);
    }

    @PostMapping(value = "/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> pdf(@RequestBody BalancesVigenciasFiltroDTO filtro) {
        var resultado = service.consultar(filtro);
        byte[] archivo = pdfService.generar(resultado);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=balances_vigencias_" + resultado.ano() + ".pdf")
                .contentLength(archivo.length).body(archivo);
    }

    @PostMapping(value = "/excel", produces =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> excel(@RequestBody BalancesVigenciasFiltroDTO filtro) {
        var resultado = service.consultar(filtro);
        byte[] archivo = excelService.generar(resultado);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=balances_vigencias_" + resultado.ano() + ".xlsx")
                .contentLength(archivo.length).body(archivo);
    }
}
