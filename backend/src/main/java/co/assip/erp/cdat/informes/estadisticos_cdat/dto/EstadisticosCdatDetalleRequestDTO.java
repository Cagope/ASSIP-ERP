package co.assip.erp.cdat.informes.estadisticos_cdat.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EstadisticosCdatDetalleRequestDTO {

    private String fechaCorte;

    private String tipoBloque;

    private String concepto;
}