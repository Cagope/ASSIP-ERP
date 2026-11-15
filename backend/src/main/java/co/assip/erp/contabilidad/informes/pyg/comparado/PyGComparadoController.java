
package co.assip.erp.contabilidad.informes.pyg.comparado;

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
 * Expone el Estado de Excedentes o Pérdidas con formato comparado.
 * Controla permisos, agencias y exportaciones de los dos períodos.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/contabilidad/informes/pyg/comparado")
public class PyGComparadoController {

    private static final String EXCEL_MEDIA_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final PyGComparadoService service;
    private final PyGComparadoPdfService pdfService;
    private final PyGComparadoExcelService excelService;
    private final BalanceDetalladoMensualRepository balanceRepository;

    /**
     * Consulta los dos períodos seleccionados.
     * Restringe la información a las agencias autorizadas en la sesión.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('CONTABILIDAD_VIEW')")
    public ResponseEntity<PyGComparadoService.ResultadoPyG> consultar(
            @RequestParam Integer idAgencia,
            @RequestParam Integer anoPeriodo1,
            @RequestParam Integer mesPeriodo1,
            @RequestParam Integer anoPeriodo2,
            @RequestParam Integer mesPeriodo2
    ) {
        validarAgencia(idAgencia);

        return ResponseEntity.ok(service.consultar(
                idAgencia,
                anoPeriodo1,
                mesPeriodo1,
                anoPeriodo2,
                mesPeriodo2
        ));
    }

    /**
     * Genera el informe PDF de los dos períodos.
     * Incorpora las firmas institucionales cuando se solicitan.
     */
    @GetMapping(value = "/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAuthority('CONTABILIDAD_VIEW')")
    public ResponseEntity<byte[]> exportarPdf(
            @RequestParam Integer idAgencia,
            @RequestParam Integer anoPeriodo1,
            @RequestParam Integer mesPeriodo1,
            @RequestParam Integer anoPeriodo2,
            @RequestParam Integer mesPeriodo2,
            @RequestParam(defaultValue = "true") boolean incluirFirmas
    ) {
        validarAgencia(idAgencia);

        var agencia = consultarAgencia(idAgencia);
        var resultado = service.consultar(
                idAgencia,
                anoPeriodo1,
                mesPeriodo1,
                anoPeriodo2,
                mesPeriodo2
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
                "pdf", agencia.codigo(), resultado
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
     * Genera el Excel comparativo con importes numéricos.
     * Conserva los mismos períodos, resultados y firmas opcionales.
     */
    @GetMapping(value = "/excel", produces = EXCEL_MEDIA_TYPE)
    @PreAuthorize("hasAuthority('CONTABILIDAD_VIEW')")
    public ResponseEntity<byte[]> exportarExcel(
            @RequestParam Integer idAgencia,
            @RequestParam Integer anoPeriodo1,
            @RequestParam Integer mesPeriodo1,
            @RequestParam Integer anoPeriodo2,
            @RequestParam Integer mesPeriodo2,
            @RequestParam(defaultValue = "true") boolean incluirFirmas
    ) {
        validarAgencia(idAgencia);

        var agencia = consultarAgencia(idAgencia);
        var resultado = service.consultar(
                idAgencia,
                anoPeriodo1,
                mesPeriodo1,
                anoPeriodo2,
                mesPeriodo2
        );

        FirmasDTO firmas = incluirFirmas
                ? balanceRepository.consultarFirmas(idAgencia)
                : null;

        byte[] documento = excelService.generar(
                resultado,
                agencia.codigo(),
                agencia.nombre(),
                firmas
        );

        String archivo = nombreArchivo(
                "xlsx", agencia.codigo(), resultado
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
     * Verifica la agencia solicitada contra las agencias del JWT.
     * No permite consultas ni exportaciones fuera de ese alcance.
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

    /** Obtiene el código y nombre institucional de la agencia. */
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

    /** Identifica la agencia y ambos períodos en el archivo exportado. */
    private String nombreArchivo(
            String extension,
            String codigoAgencia,
            PyGComparadoService.ResultadoPyG resultado
    ) {
        return "pyg_comparado_"
                + codigoAgencia + "_"
                + resultado.anoPeriodo1() + "-"
                + String.format("%02d", resultado.mesPeriodo1()) + "_"
                + resultado.anoPeriodo2() + "-"
                + String.format("%02d", resultado.mesPeriodo2())
                + "." + extension;
    }
}
