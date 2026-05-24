package co.assip.erp.depositos.cierre_mensual_depositos;

import co.assip.erp.depositos.cierre_mensual_depositos.dto.CierreMensualDepositosApplyResponseDTO;
import co.assip.erp.depositos.cierre_mensual_depositos.dto.CierreMensualDepositosPreviewDTO;
import co.assip.erp.depositos.cierre_mensual_depositos.dto.CierreMensualDepositosRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/depositos/cierre-mensual-depositos")
@RequiredArgsConstructor
public class CierreMensualDepositosController {

    private final CierreMensualDepositosService service;

    @PostMapping("/preview")
    public CierreMensualDepositosPreviewDTO preview(
            @RequestBody CierreMensualDepositosRequestDTO request
    ) {
        return service.preview(request);
    }

    @PostMapping("/aplicar")
    public CierreMensualDepositosApplyResponseDTO aplicar(
            @RequestBody CierreMensualDepositosRequestDTO request
    ) {
        return service.aplicar(request);
    }
    @GetMapping
    public List<CierreMensualDepositosPreviewDTO> listar() {
        return service.listar();
    }

    @GetMapping("/{idCierre}")
    public CierreMensualDepositosPreviewDTO obtenerPorId(
            @PathVariable Long idCierre
    ) {
        return service.obtenerPorId(idCierre);
    }

    @DeleteMapping("/{idCierre}")
    public void eliminar(
            @PathVariable Long idCierre
    ) {
        service.eliminar(idCierre);
    }

}