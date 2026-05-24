package co.assip.erp.depositos.informes.movimientos_por_meses.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class MovimientosPorMesesResponseDTO {

    private List<String> meses;

    private List<MovimientosPorMesesResumenDTO> resumen;

    private List<MovimientosPorMesesAsociadoDTO> detalleAsociado;

    private Integer totalMovimientos;

    private BigDecimal totalEntradas;

    private BigDecimal totalSalidas;

}