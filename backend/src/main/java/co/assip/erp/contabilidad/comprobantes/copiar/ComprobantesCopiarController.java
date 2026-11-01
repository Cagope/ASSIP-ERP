package co.assip.erp.contabilidad.comprobantes.copiar;

import co.assip.erp.contabilidad.comprobantes.copiar.dto.ComprobanteCopiarRequestDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Expone exclusivamente la copia de comprobantes contables. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/contabilidad/comprobantes/copiar")
public class ComprobantesCopiarController {
    private final ComprobantesCopiarService service;

    /** Copia el origen a un nuevo comprobante manual validado en destino. */
    @PostMapping
    public ResponseEntity<ComprobanteCopiadoResponse> copiar(
            @RequestBody ComprobanteCopiarRequestDTO solicitud,
            HttpServletRequest request
    ) {
        String numero = service.copiar(solicitud, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ComprobanteCopiadoResponse(
                        solicitud.idAgenciaDestino(),
                        solicitud.tipoComprobanteDestino().trim().toUpperCase(),
                        numero,
                        "Comprobante copiado correctamente."
                )
        );
    }

    /** Identificación definitiva del comprobante creado por copia. */
    public record ComprobanteCopiadoResponse(
            Integer idAgencia,
            String tipoComprobante,
            String numeroComprobante,
            String mensaje
    ) {}
}
