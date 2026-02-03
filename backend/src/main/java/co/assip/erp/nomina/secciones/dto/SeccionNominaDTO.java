package co.assip.erp.nomina.secciones.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SeccionNominaDTO {

    private Integer idSeccion;
    private String codigo;
    private String nombreSeccion;
    private Boolean activo;

    // auditoría (opcional mostrar, pero útil)
    private Integer fkSeguridadCreacion;
    private LocalDateTime fechaCreacion;
    private Integer fkSeguridadEdicion;
    private LocalDateTime fechaEdicion;
}
