package co.assip.erp.nomina.desprendible.dto;

import java.math.BigDecimal;

public class DesprendibleTotalesDTO {

    private BigDecimal totalDevengados;
    private BigDecimal totalDeducciones;
    private BigDecimal netoPagar;

    public DesprendibleTotalesDTO(
            BigDecimal totalDevengados,
            BigDecimal totalDeducciones,
            BigDecimal netoPagar
    ) {
        this.totalDevengados = totalDevengados;
        this.totalDeducciones = totalDeducciones;
        this.netoPagar = netoPagar;
    }

    public BigDecimal getTotalDevengados() { return totalDevengados; }
    public BigDecimal getTotalDeducciones() { return totalDeducciones; }
    public BigDecimal getNetoPagar() { return netoPagar; }
}