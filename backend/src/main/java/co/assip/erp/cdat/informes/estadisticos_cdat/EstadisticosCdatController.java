package co.assip.erp.cdat.informes.estadisticos_cdat;

import co.assip.erp.cdat.informes.estadisticos_cdat.dto.EstadisticosCdatDetalleDTO;
import co.assip.erp.cdat.informes.estadisticos_cdat.dto.EstadisticosCdatDetalleRequestDTO;
import co.assip.erp.cdat.informes.estadisticos_cdat.dto.EstadisticosCdatRequestDTO;
import co.assip.erp.cdat.informes.estadisticos_cdat.dto.EstadisticosCdatResponseDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cdat/informes/estadisticos-cdat")
@RequiredArgsConstructor
public class EstadisticosCdatController {

    private static final String PERMISO =
            "CDAT_INFORMES_VIEW";

    private final EstadisticosCdatService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    @PostMapping("/consultar")
    public EstadisticosCdatResponseDTO consultar(
            @RequestBody EstadisticosCdatRequestDTO request
    ) {

        validarAcceso();

        return service.consultar(request);
    }

    @PostMapping("/detalle")
    public List<EstadisticosCdatDetalleDTO> detalle(
            @RequestBody EstadisticosCdatDetalleRequestDTO request
    ) {

        validarAcceso();

        return service.detalle(request);
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