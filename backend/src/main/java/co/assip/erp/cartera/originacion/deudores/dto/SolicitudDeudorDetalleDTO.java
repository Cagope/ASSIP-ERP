package co.assip.erp.cartera.originacion.deudores.dto;

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
public class SolicitudDeudorDetalleDTO {

    // =========================================================
    // DEUDOR EN LA SOLICITUD
    // =========================================================

    private Integer idSolicitudDeudor;
    private Integer idSolicitudCredito;
    private Integer idDatosPersonal;

    private String tipoDeudor;
    private Integer ordenDeudor;


    // =========================================================
    // IDENTIFICACIÓN
    // =========================================================

    private String tipoDocumento;
    private String documento;
    private String nombreCompleto;


    // =========================================================
    // CARTERA AL INICIO
    // =========================================================

    private BigDecimal saldoCarteraInicio;
    private Integer diasMoraInicio;
    private Boolean cumpleMoraInicio;


    // =========================================================
    // CARTERA EN VALIDACIÓN
    // =========================================================

    private BigDecimal saldoCarteraValidacion;
    private Integer diasMoraValidacion;
    private Boolean cumpleMoraValidacion;


    // =========================================================
    // CONTROL
    // =========================================================

    private Boolean activo;

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaEdicion;
}