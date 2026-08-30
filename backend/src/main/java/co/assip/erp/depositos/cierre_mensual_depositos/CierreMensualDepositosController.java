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


    // =========================================================
    // PREVIEW
    //
    // No persiste información.
    // Calcula el cierre para toda la entidad.
    // =========================================================

    @PostMapping("/preview")
    public CierreMensualDepositosPreviewDTO preview(
            @RequestBody CierreMensualDepositosRequestDTO request
    ) {

        return service.preview(
                request
        );
    }


    // =========================================================
    // GENERAR FOTOGRAFÍA
    //
    // Genera una sola fotografía mensual para toda la entidad.
    //
    // Estado resultante:
    // P = En proceso / fotografía abierta
    // =========================================================

    @PostMapping("/generar")
    public CierreMensualDepositosApplyResponseDTO generar(
            @RequestBody CierreMensualDepositosRequestDTO request
    ) {

        return service.generar(
                request
        );
    }


    // =========================================================
    // REGENERAR FOTOGRAFÍA
    //
    // Solamente estado P.
    // Conserva id_cierre_mensual.
    // Incluye nuevamente todas las agencias.
    // =========================================================

    @PostMapping("/{idCierre}/regenerar")
    public CierreMensualDepositosApplyResponseDTO regenerar(
            @PathVariable Long idCierre
    ) {

        return service.regenerar(
                idCierre
        );
    }


    // =========================================================
    // CERRAR FOTOGRAFÍA EN FIRME
    //
    // P -> C
    //
    // No recalcula.
    // =========================================================

    @PostMapping("/{idCierre}/cerrar")
    public CierreMensualDepositosApplyResponseDTO cerrarFotografia(
            @PathVariable Long idCierre
    ) {

        return service.cerrarFotografia(
                idCierre
        );
    }


    // =========================================================
    // LISTAR CIERRES
    // =========================================================

    @GetMapping
    public List<CierreMensualDepositosPreviewDTO> listar() {

        return service.listar();
    }


    // =========================================================
    // OBTENER CIERRE POR ID
    // =========================================================

    @GetMapping("/{idCierre}")
    public CierreMensualDepositosPreviewDTO obtenerPorId(
            @PathVariable Long idCierre
    ) {

        return service.obtenerPorId(
                idCierre
        );
    }


    // =========================================================
    // ELIMINAR PRECierre
    //
    // Solamente estado P.
    // =========================================================

    @DeleteMapping("/{idCierre}")
    public void eliminar(
            @PathVariable Long idCierre
    ) {

        service.eliminar(
                idCierre
        );
    }
}