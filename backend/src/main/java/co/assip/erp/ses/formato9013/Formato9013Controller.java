
package co.assip.erp.ses.formato9013;

import co.assip.erp.ses.formato9013.dto.Formato9013DTO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/ses/formatos/9013")
@RequiredArgsConstructor
public class Formato9013Controller {

    private final Formato9013Service formato9013Service;

    /**
     * Consulta el Formato 9013 - Aportes Sociales.
     *
     * La fecha de corte se recibe en formato yyyy-MM-dd.
     *
     * La información debe proceder de los cierres
     * mensuales definitivos de Depósitos, incluyendo
     * las agencias 2 y 3.
     *
     * Esta operación es exclusivamente de consulta.
     */
    @PreAuthorize("hasAuthority('SES_FORMATOS_VIEW')")
    @GetMapping
    public ResponseEntity<List<Formato9013DTO>> consultar(
            @RequestParam("fechaCorte")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaCorte
    ) {

        List<Formato9013DTO> resultado =
                formato9013Service.consultar(
                        fechaCorte.toString()
                );

        return ResponseEntity.ok(resultado);
    }
}
