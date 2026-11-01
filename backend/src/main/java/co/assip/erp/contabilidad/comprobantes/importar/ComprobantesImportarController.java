
package co.assip.erp.contabilidad.comprobantes.importar;

import co.assip.erp.contabilidad.comprobantes.importar.dto.ComprobanteImportacionResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

/**
 * Expone la validación y confirmación de comprobantes importados desde Excel.
 * La validación no contabiliza; la confirmación delega el registro definitivo.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/contabilidad/comprobantes/importar")
public class ComprobantesImportarController {

    private final ComprobantesImportarService comprobantesImportarService;

    /**
     * Valida el archivo y devuelve los movimientos y errores
     * para presentar la vista previa sin modificar la contabilidad.
     */
    @PostMapping(
            value = "/validar",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ComprobanteImportacionResponseDTO validar(
            @RequestParam("idAgencia") Integer idAgencia,
            @RequestPart("archivo") MultipartFile archivo
    ) {
        return comprobantesImportarService.validar(
                idAgencia,
                archivo
        );
    }

    /**
     * Revalida el Excel original y contabiliza un único comprobante.
     * Devuelve el número definitivo asignado por Contabilidad.
     */
    @PostMapping(
            value = "/confirmar",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public String confirmar(
            @RequestParam("idAgencia") Integer idAgencia,
            @RequestParam("tipoComprobante") String tipoComprobante,
            @RequestParam("fechaContable") LocalDate fechaContable,
            @RequestParam("conceptoComprobante") String conceptoComprobante,
            @RequestPart("archivo") MultipartFile archivo,
            HttpServletRequest request
    ) {
        return comprobantesImportarService.confirmar(
                idAgencia,
                tipoComprobante,
                fechaContable,
                conceptoComprobante,
                archivo,
                request
        );
    }
}
