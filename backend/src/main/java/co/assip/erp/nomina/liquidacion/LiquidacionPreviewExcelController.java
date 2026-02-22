package co.assip.erp.nomina.liquidacion;

import co.assip.erp.nomina.liquidacion.dto.LiquidacionPreviewExcelDTO;
import co.assip.erp.nomina.liquidacion.dto.LiquidacionRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador de exportación EXCEL para PREVIEW de liquidación de nómina.
 * ⚠️ No persiste información.
 * ⚠️ No modifica estados.
 * ⚠️ Usa exactamente el mismo cálculo que el preview normal.
 */
@RestController
@RequestMapping("/nomina/liquidacion/preview/excel")
@RequiredArgsConstructor
public class LiquidacionPreviewExcelController {

    private final LiquidacionPreviewExcelService excelService;

    // =========================================================
    // 📊 EXPORTAR PREVIEW A EXCEL
    // =========================================================
    @PostMapping
    public ResponseEntity<List<LiquidacionPreviewExcelDTO>> exportarPreviewExcel(
            @RequestBody LiquidacionRequestDTO request
    ) {

        List<LiquidacionPreviewExcelDTO> data =
                excelService.generarPreviewExcel(request);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=preview_liquidacion_nomina.xlsx"
                )
                .contentType(MediaType.APPLICATION_JSON)
                .body(data);
    }
}
