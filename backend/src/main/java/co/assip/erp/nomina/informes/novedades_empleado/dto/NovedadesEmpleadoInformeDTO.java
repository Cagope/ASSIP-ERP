package co.assip.erp.nomina.informes.novedades_empleado.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NovedadesEmpleadoInformeDTO {

    private Integer idNovedad;

    private Integer idPeriodo;
    private Integer anio;
    private Integer mes;
    private Integer numeroPeriodo;
    private String tipoPeriodo;
    private String estadoPeriodo;

    private Integer idEmpleado;
    private Integer idContrato;
    private Integer idDatosPersonal;
    private String documento;
    private String nombreEmpleado;

    private Integer idAgencia;
    private String codigoAgencia;
    private String nombreAgencia;

    private String codigoConcepto;
    private String nombreConcepto;
    private String tipoConcepto;

    private LocalDate fechaInicial;
    private LocalDate fechaFinal;

    private BigDecimal cantidad;
    private BigDecimal valor;

    private String estado;
    private String origen;
    private String observacion;
}