package co.assip.erp.depositos.informes.extracto_cuenta;

import co.assip.erp.depositos.informes.extracto_cuenta.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/depositos/informes/extracto_cuenta")
@RequiredArgsConstructor
public class ExtractoCuentaController {

    private final ExtractoCuentaService service;
    private final ExtractoCuentaPdfService pdfService;

    @GetMapping("/buscar-cuentas")
    public List<ExtractoCuentaBusquedaDTO> buscarCuentas(
            @RequestParam(required = false) String documento,
            @RequestParam(required = false) String nombres,
            @RequestParam(required = false) String primerApellido,
            @RequestParam(required = false) String segundoApellido,
            @RequestParam(required = false) String codigoCuenta
    ) {
        return service.buscarCuentas(
                documento,
                nombres,
                primerApellido,
                segundoApellido,
                codigoCuenta
        );
    }

    @PostMapping
    public ExtractoCuentaResponseDTO consultar(
            @RequestBody ExtractoCuentaRequestDTO request
    ) {
        return service.consultar(request);
    }

    @PostMapping("/pdf")
    public ResponseEntity<byte[]> pdf(
            @RequestBody ExtractoCuentaRequestDTO request
    ) {
        return pdfService.generarPdfResponse(
                service.consultar(request)
        );
    }

}