package co.assip.erp.superintendencia.aportes;

import co.assip.erp.superintendencia.aportes.dto.AportesDTO;
import co.assip.erp.superintendencia.aportes.repository.AportesRepositoryTemp;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/super/aportes")
public class AportesController {

    private final AportesService service;
    private final AportesRepositoryTemp tempRepo;

    public AportesController(AportesService service,
                             AportesRepositoryTemp tempRepo) {
        this.service = service;
        this.tempRepo = tempRepo;
    }

    // ============================================
    // ✅ ENDPOINT REAL (SE VUELVE A ACTIVAR)
    // ============================================
    @GetMapping
    public List<AportesDTO> consultar(@RequestParam String fechaCorte) {
        return service.consultar(fechaCorte);
    }

    // ============================================
    // 🔧 ENDPOINT TEMPORAL (SE MANTIENE PARA PRUEBAS)
    // ============================================
    @GetMapping("/temp")
    public List<Map<String, Object>> consultarTemporal(@RequestParam String fechaCorte) {
        return tempRepo.probar(fechaCorte);
    }
}
