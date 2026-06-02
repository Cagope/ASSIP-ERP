package co.assip.erp.cajas.captura_depositos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CajaCapturaDepositosCuentaDTO {

    private Long idCuentaAhorro;
    private Integer idAgencia;
    private String codigoCuenta;

    private Long idDatosPersonal;
    private String tipoDocumento;
    private String documento;
    private String nombreAsociado;

    private Integer idFormaAhorro;
    private String codigoForma;
    private String nombreForma;

    private BigDecimal saldoActual;
    private BigDecimal valorCanje;
    private BigDecimal saldoDisponible;

    private String estadoCuenta;
    private Boolean estadoOperativo;
    private String mensajeOperativo;

    private String firma1;
    private String firma2;

    private String codigoAgencia;
    private String nombreAgencia;

    private String descripcionEstadoCuenta;

    private String tipoDocumentoSoporte;
    private String numeroInicialLibreta;
    private String numeroFinalLibreta;

    private String gmfCuenta;

    private Boolean cuentaConjuntaReal;
    private String conjuntos;

    private String documentoPoder;
    private String nombrePoder;
    private String telefonoPoder;
    private String celularPoder;

    private String fechaAperturaCuenta;




}