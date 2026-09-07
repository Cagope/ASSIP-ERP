package co.assip.erp.gerencia.dashboard_cdat.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DashboardCdatResponseDTO {

    private DashboardCdatResumenDTO resumen;

    private List<DashboardCdatAgenciaDTO> agencias;

    private List<DashboardCdatGrupoDTO> plazos;

    private List<DashboardCdatGrupoDTO> tasas;

    private List<DashboardCdatVencimientoDTO> vencimientos;

    private List<DashboardCdatTendenciaDTO> tendencia;
}