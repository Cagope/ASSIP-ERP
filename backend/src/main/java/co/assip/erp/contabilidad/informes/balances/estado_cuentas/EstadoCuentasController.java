package co.assip.erp.contabilidad.informes.balances.estado_cuentas;

import co.assip.erp.contabilidad.informes.balances.estado_cuentas.dto.EstadoCuentasFiltroDTO;
import co.assip.erp.contabilidad.informes.balances.estado_cuentas.dto.EstadoCuentasRespuestaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/** Consulta y exportación del Estado de Cuentas sin ejecutar cierres. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/contabilidad/informes/balances/estado-cuentas")
public class EstadoCuentasController {
    private final EstadoCuentasService service;
    private final EstadoCuentasPdfService pdfService;
    private final EstadoCuentasExcelService excelService;

    @PostMapping("/consultar")
    public EstadoCuentasRespuestaDTO consultar(@RequestBody EstadoCuentasFiltroDTO filtro) {
        return service.consultar(filtro);
    }

    public record MovimientosSolicitud(Integer idAgencia, LocalDate fechaCorte,
            String codigoCuenta, Integer pagina, Integer tamanoPagina) {}

    @PostMapping("/movimientos")
    public EstadoCuentasService.MovimientosCuentaRespuesta movimientos(
            @RequestBody MovimientosSolicitud solicitud) {
        return service.consultarMovimientosCuenta(solicitud.idAgencia(),
                solicitud.fechaCorte(), solicitud.codigoCuenta(),
                solicitud.pagina(), solicitud.tamanoPagina());
    }

    @GetMapping("/cuentas-principales")
    public List<EstadoCuentasRepository.CuentaPrincipal> cuentas(
            @RequestParam Integer idAgencia) {
        return service.listarCuentas(idAgencia);
    }

    @PostMapping(value = "/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> pdf(@RequestBody EstadoCuentasFiltroDTO filtro) {
        var resultado = service.consultar(filtro);
        byte[] archivo = pdfService.generar(resultado);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=estado_cuentas_" + resultado.fechaCorte() + ".pdf")
                .contentLength(archivo.length).body(archivo);
    }

    @PostMapping(value = "/excel", produces =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> excel(@RequestBody EstadoCuentasFiltroDTO filtro) {
        var resultado = service.consultar(filtro);
        byte[] archivo = excelService.generar(resultado);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=estado_cuentas_" + resultado.fechaCorte() + ".xlsx")
                .contentLength(archivo.length).body(archivo);
    }
}
