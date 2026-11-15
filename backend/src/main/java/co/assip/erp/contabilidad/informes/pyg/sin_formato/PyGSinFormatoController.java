
package co.assip.erp.contabilidad.informes.pyg.sin_formato;

import co.assip.erp.contabilidad.informes.balances.balance_detallado_mensual.BalanceDetalladoMensualRepository;
import co.assip.erp.contabilidad.informes.balances.balance_detallado_mensual.dto.BalanceDetalladoMensualRespuestaDTO.FirmasDTO;
import co.assip.erp.seguridad.sesion.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

/**
 * Expone la consulta y exportación del Estado de Excedentes o Pérdidas sin formato.
 * Valida permisos y agencias antes de consultar o generar documentos.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/contabilidad/informes/pyg/sin-formato")
public class PyGSinFormatoController {

    private static final String EXCEL_MEDIA_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final PyGSinFormatoService service;
    private final PyGSinFormatoPdfService pdfService;
    private final PyGSinFormatoExcelService excelService;
    private final BalanceDetalladoMensualRepository balanceRepository;

    /**
     * Consulta el resultado contable del mes solicitado.
     * Solo permite agencias incluidas en la sesión autenticada.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('CONTABILIDAD_VIEW')")
    public ResponseEntity<PyGSinFormatoService.ResultadoPyG> consultar(
            @RequestParam Integer idAgencia,
            @RequestParam Integer ano,
            @RequestParam Integer mes
    ) {
        validarAgencia(idAgencia);
        return ResponseEntity.ok(service.consultar(idAgencia, ano, mes));
    }

    /**
     * Genera el PDF con los resultados autorizados y las firmas opcionales.
     * Conserva los mismos filtros utilizados por la consulta.
     */
    @GetMapping(value = "/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAuthority('CONTABILIDAD_VIEW')")
    public ResponseEntity<byte[]> exportarPdf(
            @RequestParam Integer idAgencia,
            @RequestParam Integer ano,
            @RequestParam Integer mes,
            @RequestParam(defaultValue = "true") boolean incluirFirmas
    ) {
        validarAgencia(idAgencia);

        var agencia = consultarAgencia(idAgencia);
        var resultado = service.consultar(idAgencia, ano, mes);

        FirmasDTO firmas = incluirFirmas
                ? balanceRepository.consultarFirmas(idAgencia)
                : null;

        byte[] documento = pdfService.generar(
                resultado,
                agencia.codigo(),
                agencia.nombre(),
                firmas
        );

        String nombreArchivo = nombreArchivo(
                "pdf", agencia.codigo(), resultado.ano(), resultado.mes()
        );

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + nombreArchivo + "\""
                )
                .contentLength(documento.length)
                .body(documento);
    }

    /**
     * Genera el Excel con valores numéricos y las firmas opcionales.
     * Reutiliza los cálculos contables y la parametrización de Balances.
     */
    @GetMapping(value = "/excel", produces = EXCEL_MEDIA_TYPE)
    @PreAuthorize("hasAuthority('CONTABILIDAD_VIEW')")
    public ResponseEntity<byte[]> exportarExcel(
            @RequestParam Integer idAgencia,
            @RequestParam Integer ano,
            @RequestParam Integer mes,
            @RequestParam(defaultValue = "true") boolean incluirFirmas
    ) {
        validarAgencia(idAgencia);

        var agencia = consultarAgencia(idAgencia);
        var resultado = service.consultar(idAgencia, ano, mes);

        FirmasDTO firmas = incluirFirmas
                ? balanceRepository.consultarFirmas(idAgencia)
                : null;

        byte[] documento = excelService.generar(
                resultado,
                agencia.codigo(),
                agencia.nombre(),
                firmas
        );

        String nombreArchivo = nombreArchivo(
                "xlsx", agencia.codigo(), resultado.ano(), resultado.mes()
        );

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(EXCEL_MEDIA_TYPE))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + nombreArchivo + "\""
                )
                .contentLength(documento.length)
                .body(documento);
    }

    /**
     * Verifica que la agencia solicitada pertenezca a la sesión.
     * Se ejecuta en consultas y exportaciones.
     */
    private void validarAgencia(Integer idAgencia) {
        if (idAgencia == null || idAgencia <= 0) {
            throw new IllegalArgumentException(
                    "Debe seleccionar una agencia válida."
            );
        }

        List<Integer> agenciasPermitidas = SecurityUtils.getAgencias();

        if (agenciasPermitidas == null
                || !agenciasPermitidas.contains(idAgencia)) {
            throw new AccessDeniedException(
                    "No tiene autorización para consultar la agencia seleccionada."
            );
        }
    }

    /**
     * Recupera el código y nombre reales de la agencia.
     * Reutiliza el catálogo consultado por Balance detallado mensual.
     */
    private BalanceDetalladoMensualRepository.Agencia consultarAgencia(
            Integer idAgencia
    ) {
        return balanceRepository.listarAgencias()
                .stream()
                .filter(agencia ->
                        Objects.equals(agencia.idAgencia(), idAgencia)
                )
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "La agencia seleccionada no existe."
                ));
    }

    /** Construye el nombre del archivo con agencia y período contable. */
    private String nombreArchivo(
            String extension,
            String codigoAgencia,
            int ano,
            int mes
    ) {
        return "pyg_sin_formato_"
                + codigoAgencia + "_"
                + ano + "_"
                + String.format("%02d", mes)
                + "." + extension;
    }
}
