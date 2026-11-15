
package co.assip.erp.ses.formato9999.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Formato9999DTO {

    // Identificación del reporte SES
    private String codigoEntidad;
    private String fechaCorte;

    // Identificación del asociado
    private String tipoIdentificacion;
    private String numeroIdentificacion;
    private String digitoVerificacion;

    // Información personal
    private String primerApellido;
    private String segundoApellido;
    private String nombres;
    private String tipoPersona;

    // Vinculación
    private String fechaIngreso;
    private Integer rolAsociado;
    private Integer activo;

    // Contacto
    private String telefono;
    private String celular;
    private String direccion;
    private String codigoMunicipio;
    private String email;

    // Actividad económica
    private String actividadEconomica;
    private Integer sectorEconomico;
    private Integer ocupacion;

    // Información laboral
    private Integer empleado;
    private Integer tipoContrato;
    private Integer jornadaLaboral;

    // Información socioeconómica
    private Integer nivelEscolaridad;
    private Integer estrato;
    private Integer nivelIngresos;

    // Información demográfica
    private String fechaNacimiento;
    private Integer genero;
    private Integer estadoCivil;
    private Integer mujerCabezaFamilia;

    // Retiro y participación
    private String fechaRetiro;
    private Integer asistioAsamblea;

    // Información financiera
    private Double saldoAportes;
}
