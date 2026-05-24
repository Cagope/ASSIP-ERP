package co.assip.erp.depositos.cierre_mensual_depositos.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class CierreMensualDepositosDetalleDTO {

    private Integer idAgencia;

    private Integer idFormaAhorro;

    private String codigoForma;

    private String nombreForma;

    private Integer idCuentaAhorro;

    private String codigoCuenta;

    private Integer idDatosPersonal;

    private String documento;

    private String nombreCompleto;

    private String tipoPersona;

    private String codigoGenero;

    private String nombreGenero;

    private String estadoCuenta;

    private LocalDate fechaAperturaCuenta;

    private LocalDate fechaEstadoCuenta;

    private BigDecimal saldoCierre;

    private BigDecimal totalDebitos;

    private BigDecimal totalCreditos;

    private String gmfCuenta;

    private LocalDate fechaGmf;

    private Integer plazo;

    private BigDecimal cuotaMensual;

    private LocalDate fechaFinal;

    private BigDecimal tasa;

    private String cuentaActiva;

    private String cuentaConjunta;

    private String accionConjunta;

}