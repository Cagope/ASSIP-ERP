package co.assip.erp.shared.referencias.catalogos.estado_ahorro;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EstadoAhorro {

    private String codigoEstadoAhorro;
    private String descripcionEstadoAhorro;
    private String observacionesEstadoAhorro;
    private Boolean operativo;
}
