package co.assip.erp.cdat.informes.estadisticos_cdat.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class EstadisticosCdatResponseDTO {

    private EstadisticosCdatResumenDTO resumen;

    private List<EstadisticosCdatGrupoDTO> rangos;

    private List<EstadisticosCdatGrupoDTO> amortizacion;

    private List<EstadisticosCdatGrupoDTO> plazos;

    private List<EstadisticosCdatGrupoDTO> plazosDetalle;

    private List<EstadisticosCdatTasaDTO> tasas;

    private List<EstadisticosCdatTasaDTO> tasasDetalle;
}