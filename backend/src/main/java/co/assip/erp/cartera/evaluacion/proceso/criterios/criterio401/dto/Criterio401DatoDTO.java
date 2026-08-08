package co.assip.erp.cartera.evaluacion.proceso.criterios.criterio401.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Criterio401DatoDTO {

    // =========================================================
    // Identificación
    // =========================================================

    private Integer idCierreCarteraCredito;

    private Integer idCarteraCredito;

    private Integer idDatosPersonal;

    private String documento;

    // =========================================================
    // Criterio 401 - Servicio de la deuda
    // =========================================================

    /**
     * Cantidad de pagos consolidados del crédito durante
     * el último año evaluado.
     *
     * Cada pago se identifica por:
     *
     * id_cartera_credito
     * + tipo_comprobante
     * + numero_comprobante
     */
    private Integer cantidadPagosUltimoAnio;

    /**
     * Cantidad de pagos consolidados que registraron
     * algún abono a capital.
     *
     * Este campo es informativo y no determina por sí solo
     * si el pago participa en el promedio.
     */
    private Integer cantidadPagosCapitalUltimoAnio;

    /**
     * Cantidad de pagos consolidados que tienen
     * dias_mora informado y participan en el promedio.
     */
    private Integer cantidadPagosEvaluablesUltimoAnio;

    /**
     * Suma de días de mora de los pagos evaluables
     * durante el periodo analizado.
     */
    private BigDecimal sumaDiasMoraUltimoAnio;

    /**
     * Promedio de días de mora del crédito durante
     * el último año.
     *
     * El valor llega redondeado hacia arriba desde SQL.
     */
    private BigDecimal promedioDiasMoraUltimoAnio;
}