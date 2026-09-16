package co.assip.erp.cartera.originacion.asociados;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OriginacionAsociadoDTO {

    private Integer idDatosPersonal;

    private String tipoDocumento;
    private String nombreTipoDocumento;
    private String documento;

    private String tipoPersona;

    private String nombres;
    private String primerApellido;
    private String segundoApellido;

    private String nombreCompleto;

    private String celularUno;
    private String telefono;
    private String correoPersonal;

    private String ciudadResidencia;

    private LocalDate fechaApertura;
    private LocalDate fechaActualizacion;

    private Integer diasSinActualizacion;

    private Integer diasMaximoActualizacion;
    private Boolean informacionActualizada;
}