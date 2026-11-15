package co.assip.erp.ses.formato9026;

import co.assip.erp.ses.formato9026.dto.Formato9026DTO;
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
@RequestMapping("/ses/formatos/9026")
@RequiredArgsConstructor
public class Formato9026Controller {

    private final Formato9026Service formato9026Service;

    /**
     * Consulta el Formato SES 9026 - Ahorros.
     *
     * Recibe la fecha de corte en formato yyyy-MM-dd.
     *
     * El servicio valida la fecha y la existencia de
     * los cierres mensuales de Depósitos y CDAT.
     *
     * La información procede de los detalles históricos
     * de los cierres y consolida las agencias.
     *
     * Excluye Aportes Sociales, que corresponden
     * al Formato SES 9013.
     *
     * Esta operación es exclusivamente de consulta.
     * No modifica saldos, cuentas ni registros financieros.
     *
     * Permiso requerido: SES_FORMATOS_VIEW.
     *
     * @param fechaCorte Fecha de corte del informe.
     * @return Registros consolidados del Formato SES 9026.
     */
    @PreAuthorize("hasAuthority('SES_FORMATOS_VIEW')")
    @GetMapping
    public ResponseEntity<List<Formato9026DTO>> consultar(
            @RequestParam("fechaCorte")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaCorte
    ) {

        List<Formato9026DTO> resultado =
                formato9026Service.consultar(
                        fechaCorte.toString()
                );

        return ResponseEntity.ok(resultado);
    }
}