
package co.assip.erp.ses.formato9999;

import co.assip.erp.ses.formato9999.dto.Formato9999DTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/ses/asociados")
@RequiredArgsConstructor
public class Formato9999Controller {

    private final Formato9999Service asociadosService;

    /**
     * Consulta los asociados para el reporte SES.
     *
     * La fecha de entrada utiliza yyyy-MM-dd.
     *
     * Los datos devueltos incluyen CODENTIDAD,
     * FECHACORTE y fechas en formato ddMMyyyy.
     *
     * No modifica información de la base de datos.
     */
    @PreAuthorize("hasAuthority('SES_FORMATOS_VIEW')")
    @GetMapping
    public ResponseEntity<List<Formato9999DTO>> consultar(
            @RequestParam("fechaCorte")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaCorte
    ) {

        List<Formato9999DTO> asociados =
                asociadosService.consultar(
                        fechaCorte.toString()
                );

        return ResponseEntity.ok(asociados);
    }
}
