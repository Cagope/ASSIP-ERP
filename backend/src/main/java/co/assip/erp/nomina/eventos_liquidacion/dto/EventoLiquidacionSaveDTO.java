package co.assip.erp.nomina.eventos_liquidacion.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventoLiquidacionSaveDTO {

    private Long idEventoLiquidacion;

    private Integer idContrato;

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
}