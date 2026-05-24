package co.assip.erp.depositos.informes.extracto_por_valor.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ExtractoPorValorRequestDTO {

    private String fechaInicial;
    private String fechaFinal;

    private Integer idAgencia;

    /**
     * "0" = todas.
     */
    private String codigoForma;

    /**
     * Valor buscado.
     */
    private BigDecimal valor;

    /**
     * D = Débitos
     * C = Créditos
     * A = Ambos
     */
    private String tipoBusqueda;

}