package co.assip.erp.cdat.cierre_mensual_cdat;

import co.assip.erp.seguridad.service.UsuarioSesionService;
import co.assip.erp.cdat.cierre_mensual_cdat.dto.CdatCierreMensualEntradaDTO;
import co.assip.erp.cdat.cierre_mensual_cdat.dto.CdatCierreMensualPreviewDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cdat/cierre-mensual-cdat")
@RequiredArgsConstructor
public class CdatCierreMensualController {

    private final CdatCierreMensualService service;
    private final UsuarioSesionService usuarioSesionService;

    @PostMapping("/preview")
    public CdatCierreMensualPreviewDTO preview(@RequestBody CdatCierreMensualEntradaDTO dto) {
        return service.preview(dto);
    }

    @PostMapping("/aplicar")
    public CdatCierreMensualPreviewDTO aplicar(
            @RequestBody CdatCierreMensualEntradaDTO dto
    ) {

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        return service.aplicar(dto, idUsuario);
    }

    @GetMapping("/listar")
    public List<CdatCierreMensualPreviewDTO> listar() {
        return service.listar();
    }

    @GetMapping("/{idCierre}")
    public CdatCierreMensualPreviewDTO obtenerPorId(@PathVariable Long idCierre) {
        return service.obtenerPorId(idCierre);
    }

    @DeleteMapping("/{idCierre}")
    public void eliminar(@PathVariable Long idCierre) {
        service.eliminar(idCierre);
    }
}