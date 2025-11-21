package co.assip.erp.superintendencia.aportes.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AportesDTO {

    // Identificación
    private String tipoIdentificacion;
    private String numeroIdentificacion;
    private String primerApellido;
    private String segundoApellido;
    private String nombres;
    private String nombreCompleto;

    // Cuenta
    private String codigoCuenta;
    private String fechaIngreso;   // fecha_apertura o fecha_ingreso

    // Saldos
    private Double saldoAportes;
    private Double valorAporteMensual;
    private Double valorRevalorizacion;
    private Double aportesOrdinarios;
    private Double aportesExtraordinarios;

    private Double promedioDiaAnual;

    private String fechaUltimoPago;
}
