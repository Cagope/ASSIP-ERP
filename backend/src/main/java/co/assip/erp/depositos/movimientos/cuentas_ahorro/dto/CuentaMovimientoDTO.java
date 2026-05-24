package co.assip.erp.depositos.movimientos.cuentasahorro.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CuentaMovimientoDTO {

    private Integer idCuentaAhorro;

    private Integer idAgencia;
    private String codigoAgencia;
    private String nombreAgencia;

    private Integer idFormaAhorro;
    private String codigoForma;
    private String nombreForma;

    private String codigoCuenta;

    private Integer idDatosPersonal;
    private String documento;
    private String nombreAsociado;

    private BigDecimal saldoActualCuenta;

    private String estadoCuenta;
    private String cuentaActiva;
    private String gmfCuenta;

    private LocalDate fechaAperturaCuenta;

    private String descripcionEstadoCuenta;
    private Boolean estadoOperativo;
    private String mensajeOperativo;

    private String numeroInicialLibreta;
    private String numeroFinalLibreta;
    private String tipoDocumentoSoporte;

    private String documentoPoder;
    private String nombrePoder;
    private String telefonoPoder;
    private String celularPoder;

    private Boolean cuentaConjuntaReal;
    private String conjuntos;
    private BigDecimal valorEnCanje;

}