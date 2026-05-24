package co.assip.erp.depositos.informes.saldos.dto;

import lombok.Data;

@Data
public class SaldosCorteItemDTO {

    private String codigoCuenta;
    private String documento;
    private String nombreCompleto;

    private String codigoAgencia;
    private String agencia;

    private String codigoForma;
    private String forma;

    private String zona;
    private String subZona;

    private String estadoCuentaCodigo;
    private String estadoCuentaNombre;

    private String telefono;
    private String celular;
    private String correo;

    private boolean recibeLlamadas;
    private boolean recibeMsm;
    private boolean recibeEmails;
    private boolean recibeCartas;
    private boolean recibeRedesSociales;

    private String fechaApertura;

    private Double totalDebitos;
    private Double totalCreditos;
    private Double saldoCorte;
    private String direccion;
    private String departamento;
    private String ciudad;
    private String celularDos;
    private String fechaUltimoMovimiento;

}