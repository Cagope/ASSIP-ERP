package co.assip.erp.cdat.cierre_mensual_cdat;

import co.assip.erp.cdat.cierre_mensual_cdat.dto.CdatCierreMensualEntradaDTO;
import co.assip.erp.cdat.cierre_mensual_cdat.dto.CdatCierreMensualPreviewDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cdat/cierre-mensual-cdat")
@RequiredArgsConstructor
public class CdatCierreMensualController {

    private static final String PERMISO =
            "CDAT_CIERRE_MENSUAL_EXECUTE";

    private final CdatCierreMensualService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    @PostMapping("/preview")
    public CdatCierreMensualPreviewDTO preview(
            @RequestBody CdatCierreMensualEntradaDTO dto
    ) {

        validarAcceso();

        return service.preview(dto);
    }

    @PostMapping("/aplicar")
    public CdatCierreMensualPreviewDTO aplicar(
            @RequestBody CdatCierreMensualEntradaDTO dto
    ) {

        validarAcceso();

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        return service.aplicar(
                dto,
                idUsuario
        );
    }

    @GetMapping("/listar")
    public List<CdatCierreMensualPreviewDTO> listar() {

        validarAcceso();

        return service.listar();
    }

    @GetMapping("/{idCierre}")
    public CdatCierreMensualPreviewDTO obtenerPorId(
            @PathVariable Long idCierre
    ) {

        validarAcceso();

        return service.obtenerPorId(idCierre);
    }

    @DeleteMapping("/{idCierre}")
    public void eliminar(
            @PathVariable Long idCierre
    ) {

        validarAcceso();

        service.eliminar(idCierre);
    }

    private void validarAcceso() {

        Usuario usuarioActual = new Usuario();
        usuarioActual.setIdUsuario(
                usuarioSesionService.idUsuario()
        );

        accessValidator.validarAcceso(
                usuarioActual,
                PERMISO
        );
    }
}