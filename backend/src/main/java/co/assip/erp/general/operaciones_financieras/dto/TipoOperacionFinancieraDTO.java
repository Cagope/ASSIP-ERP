
package co.assip.erp.general.operaciones_financieras.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TipoOperacionFinancieraDTO {

    private Long idTipoOperacion;
    private String moduloOrigen;
    private String procesoOrigen;
    private String tablaOrigen;
    private String codigoOperacionFinanciera;
    private String nombreOperacion;
    private String naturaleza;
    private Boolean permiteCaja;
    private Boolean permiteTesoreria;
    private Boolean permiteParciales;
    private String perfilEjecucion;
    private Boolean activo;
}
