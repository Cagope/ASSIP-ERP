package co.assip.erp.nomina.eventos_liquidacion.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventoLiquidacionListDTO {

    private Long idEventoLiquidacion;

    private Integer idContrato;
    private Integer idEmpleado;
    private Integer idAgencia;

    private String nombreEmpleado;
    private String documentoEmpleado;

    private LocalDate fechaDocumento;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Integer totalDias;

    private String tipoEvento;
    private String numeroSoporte;
    private String responsablePago;

    private Double porcentajeResponsable;
    private Double porcentajeEmpresa;
    private Integer diasEmpresa100;

    private Boolean generaCxc;
    private Boolean liquidaArl;
    private Boolean esRemunerado;

    private String observacion;
    private String estado;

    private Integer fkSeguridadCreacion;
    private LocalDateTime fechaCreacion;
    private Integer fkSeguridadEdicion;
    private LocalDateTime fechaEdicion;
}