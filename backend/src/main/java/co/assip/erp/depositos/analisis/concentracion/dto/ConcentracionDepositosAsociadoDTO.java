package co.assip.erp.depositos.analisis.concentracion.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class ConcentracionDepositosAsociadoDTO {

    private Integer posicion;

    private Integer idDatosPersonal;
    private String tipoDocumento;
    private String documento;
    private String nombreCompleto;

    private Integer cantidadCuentas;

    /*
     * Saldo consolidado de todas las cuentas del asociado
     * dentro de la población seleccionada.
     */
    private BigDecimal saldo;

    /*
     * Participación del asociado sobre el saldo total
     * de la población analizada.
     */
    private BigDecimal porcentajeParticipacion;

    /*
     * Participación acumulada desde el asociado
     * con mayor saldo hasta la posición actual.
     */
    private BigDecimal porcentajeAcumulado;
}