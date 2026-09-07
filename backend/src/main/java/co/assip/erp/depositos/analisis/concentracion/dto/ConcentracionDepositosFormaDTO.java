package co.assip.erp.depositos.analisis.concentracion.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class ConcentracionDepositosFormaDTO {

    private Integer idFormaAhorro;
    private String codigoForma;
    private String nombreForma;

    /*
     * Tipo de captación de la forma de ahorro.
     * Permite distinguir, entre otros,
     * los aportes sociales.
     */
    private String tipoCaptacionForma;

    private Integer cantidadCuentas;
    private Integer cantidadAsociados;

    /*
     * Saldo total de la forma de ahorro
     * a la fecha de corte.
     */
    private BigDecimal saldo;

    /*
     * Participación de la forma sobre el total
     * de la población analizada.
     */
    private BigDecimal porcentajeParticipacion;
}