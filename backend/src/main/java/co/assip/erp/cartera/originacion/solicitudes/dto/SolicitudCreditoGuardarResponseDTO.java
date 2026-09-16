package co.assip.erp.cartera.originacion.solicitudes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudCreditoGuardarResponseDTO {

    // =========================================================
    // SOLICITUD
    // =========================================================

    private Integer idSolicitudCredito;

    private String numeroSolicitud;


    // =========================================================
    // CONDICIÓN INICIAL APLICADA
    // =========================================================

    private Integer idCondicionInicial;

    private Integer idCondicionInicialDetalle;

    /**
     * Indica si se encontró una condición inicial
     * aplicable para las características de la solicitud.
     */
    private Boolean condicionEncontrada;


    // =========================================================
    // PLAZO APLICADO
    // =========================================================

    private Integer plazoMinimoAplicado;

    private Integer plazoMaximoAplicado;


    // =========================================================
    // RANGO SMMLV APLICADO
    // =========================================================

    private BigDecimal cantidadSmmlvMinimoAplicada;

    private BigDecimal cantidadSmmlvMaximoAplicada;

    private BigDecimal valorSmmlvAplicado;

    /**
     * Valor solicitado expresado en SMMLV.
     */
    private BigDecimal cantidadSmmlvSolicitada;


    // =========================================================
    // RECIPROCIDAD DE APORTES
    // =========================================================

    private BigDecimal factorReciprocidadAportesAplicado;

    private BigDecimal valorAportesInicio;

    private BigDecimal cupoMaximoPorAportes;

    private BigDecimal valorAportesRequerido;

    private Boolean cumpleAportesInicio;


    // =========================================================
    // TASA DE COLOCACIÓN
    // =========================================================

    private Integer idTasaColocacionDetalle;

    private BigDecimal tasaColocacionAplicada;

    /**
     * Indica si se encontró una tasa de colocación
     * aplicable para la solicitud.
     */
    private Boolean tasaEncontrada;


    // =========================================================
    // RESULTADO FINANCIERO
    // =========================================================

    private BigDecimal tasaEfectivaAnual;

    private BigDecimal valorCuotaProyectada;


    // =========================================================
    // GESTIÓN
    // =========================================================

    private LocalDateTime fechaUltimaGestion;
}