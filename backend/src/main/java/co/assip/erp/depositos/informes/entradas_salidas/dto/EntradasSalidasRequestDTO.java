package co.assip.erp.depositos.informes.entradas_salidas.dto;

import lombok.Data;

@Data
public class EntradasSalidasRequestDTO {

    private String fechaInicial;
    private String fechaFinal;

    private Integer idAgencia;

    private String codigoForma;

}