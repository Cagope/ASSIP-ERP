package co.assip.erp.cartera.originacion.financiero.dto;

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
public class SolicitudFinancieroDetalleDTO {

    // =========================================================
    // IDENTIFICACIÓN DEL REGISTRO
    // =========================================================

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


    // =========================================================
    // ACTIVIDAD ECONÓMICA
    // =========================================================

    private String codigoOcupacion;
    private String codigoSectorEconomico;
    private String codigoActividadSes;
    private String codigoActividadDian;


    // =========================================================
    // PERSONA NATURAL - INGRESOS
    // =========================================================

    private BigDecimal valorSalario;
    private BigDecimal valorPension;
    private BigDecimal ingresoIndependiente;
    private BigDecimal ingresosArriendo;
    private BigDecimal ingresosComisiones;
    private BigDecimal otrosIngresos;

    private String comentarioOtrosIngresos;

    private BigDecimal ingresosTotalesNatural;


    // =========================================================
    // PERSONA NATURAL - EGRESOS
    // =========================================================

    private BigDecimal egresosFamiliares;
    private BigDecimal egresosArriendo;
    private BigDecimal egresosCredito;
    private BigDecimal otrosEgresos;

    private String comentarioOtrosEgresos;

    private BigDecimal egresosTotalesNatural;


    // =========================================================
    // DECLARACIÓN DE RENTA
    // =========================================================

    private Boolean declaraRenta;
    private Integer anioDeclaracion;
    private LocalDate fechaPresentacionDeclaracion;


    // =========================================================
    // PERSONA JURÍDICA
    // =========================================================

    private BigDecimal ingresosOperacionales;
    private BigDecimal ingresosNoOperacionales;
    private BigDecimal ingresosTotalesJuridica;

    private BigDecimal costos;
    private BigDecimal gastosOperacionales;
    private BigDecimal gastosFinancieros;
    private BigDecimal otrosGastos;

    private BigDecimal egresosTotalesJuridica;


    // =========================================================
    // BALANCE
    // =========================================================

    private BigDecimal activoCorriente;
    private BigDecimal pasivoCorriente;

    private BigDecimal utilidadOperacional;
    private BigDecimal utilidadNeta;

    private BigDecimal totalActivos;
    private BigDecimal totalPasivos;
    private BigDecimal patrimonioTotal;


    // =========================================================
    // INFORMACIÓN COMPLEMENTARIA
    // =========================================================

    private String origenFondos;
    private String relacionFinanciera;
    private BigDecimal deudaRelacionFinanciera;


    // =========================================================
    // SINCRONIZACIÓN HOJA DE VIDA
    // =========================================================

    private Boolean sincronizadoHojaVida;


    // =========================================================
    // CONTROL
    // =========================================================

    private Boolean activo;

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaEdicion;
}