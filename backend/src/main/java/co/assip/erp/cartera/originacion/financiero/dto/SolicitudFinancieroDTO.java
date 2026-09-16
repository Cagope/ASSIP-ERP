package co.assip.erp.cartera.originacion.financiero.dto;

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
public class SolicitudFinancieroDTO {

    private Integer idSolicitudDeudorFinanciero;
    private Integer idSolicitudDeudor;
    private Integer idSolicitudCredito;

    private Integer idDatosPersonal;
    private String tipoDocumento;
    private String documento;
    private String nombreCompleto;

    private String tipoDeudor;
    private Integer ordenDeudor;

    private String tipoPersona;
    private LocalDateTime fechaFotografia;

    private BigDecimal ingresosTotalesNatural;
    private BigDecimal egresosTotalesNatural;

    private BigDecimal ingresosTotalesJuridica;
    private BigDecimal egresosTotalesJuridica;

    private BigDecimal totalActivos;
    private BigDecimal totalPasivos;
    private BigDecimal patrimonioTotal;

    private Boolean activo;
}