package co.assip.erp.cartera.originacion.centralriesgo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudCentralRiesgoDTO {

    // =========================================================
    // SOLICITUD / DEUDOR
    // =========================================================

    private Integer idSolicitudDeudorCentral;
    private Integer idSolicitudDeudor;
    private Integer idSolicitudCredito;

    private Integer idDatosPersonal;

    private String tipoDocumento;
    private String documento;
    private String nombreCompleto;

    private String tipoDeudor;
    private Integer ordenDeudor;


    // =========================================================
    // CENTRAL DE RIESGO
    // =========================================================

    private Integer idCentralRiesgo;
    private String codigoCentral;
    private String nombreCentral;

    private LocalDate fechaConsulta;
    private LocalDateTime fechaFotografia;


    // =========================================================
    // RESULTADO PRINCIPAL
    // =========================================================

    private BigDecimal saldoActualObligaciones;
    private BigDecimal valorCuotasMensuales;

    private BigDecimal puntajeCentral;

    /**
     * Estado técnico:
     *
     * CON_HISTORIAL
     * SIN_HISTORIAL
     */
    private String calificacionCentral;

    /**
     * Clasificación cualitativa para el modelo:
     *
     * CON HISTORIAL - OK
     * CON PERMANENCIAS
     * CON REPORTES
     * SIN HISTORIAL
     */
    private String calificacionCualitativa;


    // =========================================================
    // CONTROL
    // =========================================================

    private Boolean activo;
}