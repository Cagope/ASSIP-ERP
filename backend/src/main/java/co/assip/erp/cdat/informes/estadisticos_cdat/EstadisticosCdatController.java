package co.assip.erp.cdat.informes.estadisticos_cdat;

import co.assip.erp.cdat.informes.estadisticos_cdat.dto.EstadisticosCdatRequestDTO;
import co.assip.erp.cdat.informes.estadisticos_cdat.dto.EstadisticosCdatResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import co.assip.erp.cdat.informes.estadisticos_cdat.dto.EstadisticosCdatDetalleDTO;
import co.assip.erp.cdat.informes.estadisticos_cdat.dto.EstadisticosCdatDetalleRequestDTO;

import java.util.List;

@RestController
@RequestMapping("/cdat/informes/estadisticos-cdat")
@RequiredArgsConstructor
public class EstadisticosCdatController {

    private final EstadisticosCdatService service;

    @PostMapping("/consultar")
    public EstadisticosCdatResponseDTO consultar(
            @RequestBody EstadisticosCdatRequestDTO request
    ) {

        return service.consultar(request);
    }

    @PostMapping("/detalle")
    public List<EstadisticosCdatDetalleDTO> detalle(
            @RequestBody EstadisticosCdatDetalleRequestDTO request
    ) {

        return service.detalle(request);
    }

}