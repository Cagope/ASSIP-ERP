package co.assip.erp.cartera.originacion.solicitudes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudCreditoCrearRequestDTO {

    // =========================================================
    // SOLICITANTE
    // =========================================================

    private Integer idAgencia;

    private Integer idDatosPersonal;


    // =========================================================
    // LÍNEA Y CLASIFICACIÓN
    // =========================================================

    private Integer idLineaCredito;

    private String codigoClasificacionCredito;

    private String codigoDestinoEconomico;


    // =========================================================
    // GARANTÍA
    // =========================================================

    private String codigoGarantiaCredito;

    /**
     * Puede ser null cuando la garantía seleccionada
     * no requiere subgarantía.
     */
    private String codigoSubgarantia;


    // =========================================================
    // FONDO DE GARANTÍAS
    // =========================================================

    /**
     * Fondo de garantías seleccionado.
     *
     * Es obligatorio seleccionar una opción del catálogo,
     * incluyendo "SIN FONDO DE GARANTÍAS".
     *
     * El porcentaje y el valor aplicados son determinados
     * por el backend.
     */
    private Integer idFondoGarantia;


    // =========================================================
    // FORMA DE PAGO E INTERESES
    // =========================================================

    private String codigoFormaPago;

    private String periodoCodigoInteres;

    private String tipoModalidadInteres;

    /**
     * Periodicidad de amortización del capital,
     * expresada en meses.
     */
    private Integer amortizacionCapital;

    private String codigoTipoCuota;


    // =========================================================
    // PLAZO
    // =========================================================

    private Integer plazoSolicitado;

    /**
     * Puede enviarse null.
     * PostgreSQL lo normaliza a cero al guardar.
     */
    private Integer mesesGraciaCapital;

    /**
     * Puede enviarse null.
     * PostgreSQL lo normaliza a cero al guardar.
     */
    private Integer mesesGraciaInteres;


    // =========================================================
    // VALOR
    // =========================================================

    private BigDecimal valorSolicitado;


    // =========================================================
    // LIBRANZA
    // =========================================================

    /**
     * Solo se informa cuando la operación
     * corresponde a una empresa de libranza.
     */
    private Integer idEmpresaLibranza;


    // =========================================================
    // OBSERVACIÓN
    // =========================================================

    private String observacionAsesor;
}