package co.assip.erp.gerencia.dashboard_depositos.dto;
import java.time.LocalDate;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DashboardDepositosResponseDTO {

    private DashboardDepositosResumenDTO resumen;

    private List<DashboardDepositosFormaDTO> formas;
    private List<DashboardDepositosGrupoDTO> agencias;
    private List<DashboardDepositosTendenciaDTO> tendencia;

    private LocalDate fechaCorteAnterior;

}