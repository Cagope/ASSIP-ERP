package co.assip.erp.nomina.periodos_nomina.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PeriodoNominaListDTO {

    private Integer idPeriodo;

    private Integer idAgencia;

    // ✅ NUEVO — agencia decodificada
    private String nombreAgencia;

    private Integer anio;
    private Integer mes;

    private String tipoPeriodo;       // MENSUAL / QUINCENAL
    private Integer numeroPeriodo;    // 1,2 (quincenal)
    private String descripcion;

    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    private String estado;

    private LocalDateTime fechaLiquidacion;
    private Integer fkSeguridadLiquidacion;

    private LocalDateTime fechaContabiliza;
    private Integer fkSeguridadContabiliza;
}
