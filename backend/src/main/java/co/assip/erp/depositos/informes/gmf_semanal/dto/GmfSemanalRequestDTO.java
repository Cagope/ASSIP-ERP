package co.assip.erp.depositos.informes.gmf_semanal.dto;

import lombok.Data;

@Data
public class GmfSemanalRequestDTO {

    private String fechaInicial;
    private String fechaFinal;

    private Integer idAgencia;

    private Integer numeroSemana;

    /**
     * 0 = todas las formas.
     * Ejemplo: "02", "03", "04"
     */
    private String codigoForma;

}