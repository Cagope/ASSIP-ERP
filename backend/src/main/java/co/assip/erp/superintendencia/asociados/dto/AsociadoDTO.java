package co.assip.erp.superintendencia.asociados.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AsociadoDTO {

    private String tipoIdentificacion;
    private String numeroIdentificacion;
    private String digitoVerificacion;

    private String primerApellido;
    private String segundoApellido;
    private String nombres;

    private String tipoPersona;  // 🔹 AGREGADO — solo para reglas SES

    private String fechaIngreso;

    private String telefono;
    private String celular;
    private String direccion;

    private Integer rolAsociado;
    private Integer activo;

    private String actividadEconomica;

    private String codigoMunicipio;
    private String email;

    private Integer genero;
    private Integer empleado;
    private Integer tipoContrato;

    private Integer nivelEscolaridad;
    private Integer estrato;
    private Integer nivelIngresos;

    private String fechaNacimiento;

    private Integer estadoCivil;
    private Integer mujerCabezaFamilia;
    private Integer ocupacion;
    private Integer sectorEconomico;
    private Integer jornadaLaboral;

    private String fechaRetiro;
    private Integer asistioAsamblea;

    private Double saldoAportes;
}
