
package co.assip.erp.contabilidad.informes.pyg.individual;

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
 * Expone el Estado de Excedentes o Pérdidas con formato individual.
 * Valida permisos y agencias en consultas y exportaciones.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/contabilidad/informes/pyg/individual")
public class PyGIndividualController {

    private static final String EXCEL_MEDIA_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final PyGIndividualService service;
    private final PyGIndividualPdfService pdfService;
    private final PyGIndividualExcelService excelService;
    private final BalanceDetalladoMensualRepository balanceRepository;

    /**
     * Consulta un período contable con detalle opcional.
     * Restringe los resultados a las agencias autorizadas.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('CONTABILIDAD_VIEW')")
    public ResponseEntity<PyGIndividualService.ResultadoPyG> consultar(
            @RequestParam Integer idAgencia,
            @RequestParam Integer ano,
            @RequestParam Integer mes,
            @RequestParam(defaultValue = "false") boolean incluirDetalle
    ) {
        validarAgencia(idAgencia);

        return ResponseEntity.ok(service.consultar(
                idAgencia,
                ano,
                mes,
                incluirDetalle
        ));
    }

    /**
     * Genera el PDF del período solicitado.
     * Incorpora firmas institucionales únicamente si se solicitan.
     */
    @GetMapping(value = "/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAuthority('CONTABILIDAD_VIEW')")
    public ResponseEntity<byte[]> exportarPdf(
            @RequestParam Integer idAgencia,
            @RequestParam Integer ano,
            @RequestParam Integer mes,
            @RequestParam(defaultValue = "false") boolean incluirDetalle,
            @RequestParam(defaultValue = "true") boolean incluirFirmas
    ) {
        validarAgencia(idAgencia);

        var agencia = consultarAgencia(idAgencia);
        var resultado = service.consultar(
                idAgencia,
                ano,
                mes,
                incluirDetalle
        );

        FirmasDTO firmas = incluirFirmas
                ? balanceRepository.consultarFirmas(idAgencia)
                : null;

        byte[] documento = pdfService.generar(
                resultado,
                agencia.codigo(),
                agencia.nombre(),
                firmas
        );

        String archivo = nombreArchivo(
                "pdf",
                agencia.codigo(),
                resultado
        );

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + archivo + "\""
                )
                .contentLength(documento.length)
                .body(documento);
    }

    /**
     * Genera el Excel del período con valores numéricos.
     * No consulta ni incorpora firmas institucionales.
     */
    @GetMapping(value = "/excel", produces = EXCEL_MEDIA_TYPE)
    @PreAuthorize("hasAuthority('CONTABILIDAD_VIEW')")
    public ResponseEntity<byte[]> exportarExcel(
            @RequestParam Integer idAgencia,
            @RequestParam Integer ano,
            @RequestParam Integer mes,
            @RequestParam(defaultValue = "false") boolean incluirDetalle
    ) {
        validarAgencia(idAgencia);

        var agencia = consultarAgencia(idAgencia);
        var resultado = service.consultar(
                idAgencia,
                ano,
                mes,
                incluirDetalle
        );

        byte[] documento = excelService.generar(
                resultado,
                agencia.codigo(),
                agencia.nombre()
        );

        String archivo = nombreArchivo(
                "xlsx",
                agencia.codigo(),
                resultado
        );

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(EXCEL_MEDIA_TYPE))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + archivo + "\""
                )
                .contentLength(documento.length)
                .body(documento);
    }

    /**
     * Comprueba que la agencia esté incluida en el JWT.
     * Impide consultar o exportar información no autorizada.
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
     * Obtiene el código y nombre de la agencia seleccionada.
     * Reutiliza el catálogo del Balance detallado mensual.
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

    /**
     * Construye el nombre de descarga con agencia, período y detalle.
     * Diferencia las exportaciones resumidas de las detalladas.
     */
    private String nombreArchivo(
            String extension,
            String codigoAgencia,
            PyGIndividualService.ResultadoPyG resultado
    ) {
        return "pyg_individual_"
                + codigoAgencia + "_"
                + resultado.ano() + "_"
                + String.format("%02d", resultado.mes())
                + (resultado.incluirDetalle() ? "_detallado" : "")
                + "." + extension;
    }
}
