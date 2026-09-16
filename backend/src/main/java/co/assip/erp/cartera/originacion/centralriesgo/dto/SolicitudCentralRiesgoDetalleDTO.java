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
public class SolicitudCentralRiesgoDetalleDTO {

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
    // CENTRAL
    // =========================================================

    private Integer idCentralRiesgo;
    private String codigoCentral;
    private String documentoCentral;
    private String nombreCentral;
    private String descripcionCentral;


    // =========================================================
    // CONSULTA / REGISTRO
    // =========================================================

    private LocalDate fechaConsulta;
    private LocalDateTime fechaFotografia;


    // =========================================================
    // OBLIGACIONES
    // =========================================================

    private BigDecimal valorInicialObligaciones;
    private BigDecimal saldoActualObligaciones;
    private BigDecimal valorCuotasMensuales;


    // =========================================================
    // CALIFICACIONES
    // =========================================================

    private Integer cantidadCalificacionA;
    private Integer cantidadCalificacionB;
    private Integer cantidadCalificacionC;
    private Integer cantidadCalificacionD;
    private Integer cantidadCalificacionE;
    private Integer cantidadCalificacionK;


    // =========================================================
    // EVENTOS
    // =========================================================

    private Integer cantidadReestructuraciones;
    private Integer cantidadRefinanciaciones;
    private Integer cantidadCuentasEmbargadas;


    // =========================================================
    // RESULTADO CENTRAL
    // =========================================================

    private BigDecimal puntajeCentral;

    /**
     * Estado técnico de la consulta:
     *
     * CON_HISTORIAL
     * SIN_HISTORIAL
     */
    private String calificacionCentral;

    /**
     * Clasificación cualitativa utilizada
     * por el modelo de análisis:
     *
     * CON HISTORIAL - OK
     * CON PERMANENCIAS
     * CON REPORTES
     * SIN HISTORIAL
     */
    private String calificacionCualitativa;

    private String observacion;


    // =========================================================
    // CONTROL
    // =========================================================

    private Boolean activo;

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaEdicion;
}