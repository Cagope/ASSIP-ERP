package co.assip.erp.ses.formato9026.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class Formato9026DTO {

    // Identificación del reporte y del titular.
    private String codigoEntidad;
    private String fechaCorte;
    private Long consecutivo;
    private String tipoIdentificacion;
    private String nit;

    // Clasificación y condiciones del depósito.
    private String codigoContable;
    private String nombreDeposito;
    private Integer tipoAhorro;
    private String amortizacion;
    private String fechaApertura;
    private Integer plazo;
    private String fechaVencimiento;
    private String modalidad;
    private BigDecimal tasaInteresNominal;
    private BigDecimal tasaInteresEfectiva;
    private BigDecimal interesesCausados;
    private BigDecimal saldo;
    private BigDecimal depositoInicial;
    private String numeroCuenta;

    // GMF, estado y características de la cuenta.
    private String exentaGmf;
    private String fechaAceptacionGmf;
    private String estado;
    private String ctaBajoMonto;
    private Integer cotitulares;
    private String conjuntaColectivo;
    private String paso;

    // Datos complementarios para pantalla y conciliación.
    // No se incluyen en el TXT oficial.
    private String moduloOrigen;
    private Integer idAgencia;
    private String nombreTitular;
}