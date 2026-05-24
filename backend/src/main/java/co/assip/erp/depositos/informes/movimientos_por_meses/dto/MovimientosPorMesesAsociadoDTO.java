package co.assip.erp.depositos.informes.movimientos_por_meses.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class MovimientosPorMesesAsociadoDTO {

    private String documento;

    private String nombreCompleto;

    private String codigoCuenta;

    private String codigoForma;

    private String nombreForma;

    private String mes;

    private Integer cantidadMovimientos;

    private BigDecimal entradas;

    private BigDecimal salidas;

}