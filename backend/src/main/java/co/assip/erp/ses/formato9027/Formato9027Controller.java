package co.assip.erp.ses.formato9027;

import co.assip.erp.ses.formato9027.dto.Formato9027DTO;
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
@RequestMapping("/ses/formatos/9027")
@RequiredArgsConstructor
public class Formato9027Controller {
    private final Formato9027Service service;

    @GetMapping
    @PreAuthorize("hasAuthority('SES_FORMATOS_VIEW')")
    public ResponseEntity<List<Formato9027DTO>> consultar(
            @RequestParam("fechaCorte")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaCorte) {
        return ResponseEntity.ok(service.consultar(fechaCorte));
    }
}
