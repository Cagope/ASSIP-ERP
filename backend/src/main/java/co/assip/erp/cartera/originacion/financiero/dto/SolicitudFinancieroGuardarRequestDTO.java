package co.assip.erp.cartera.originacion.financiero.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudFinancieroGuardarRequestDTO {

    // =========================================================
    // DEUDOR
    // =========================================================

    private Integer idSolicitudDeudor;


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


    // =========================================================
    // PERSONA NATURAL - EGRESOS
    // =========================================================

    private BigDecimal egresosFamiliares;
    private BigDecimal egresosArriendo;
    private BigDecimal egresosCredito;
    private BigDecimal otrosEgresos;

    private String comentarioOtrosEgresos;


    // =========================================================
    // DECLARACIÓN DE RENTA
    // =========================================================

    private Boolean declaraRenta;
    private Integer anioDeclaracion;
    private LocalDate fechaPresentacionDeclaracion;


    // =========================================================
    // PERSONA JURÍDICA - INGRESOS
    // =========================================================

    private BigDecimal ingresosOperacionales;
    private BigDecimal ingresosNoOperacionales;


    // =========================================================
    // PERSONA JURÍDICA - EGRESOS
    // =========================================================

    private BigDecimal costos;
    private BigDecimal gastosOperacionales;
    private BigDecimal gastosFinancieros;
    private BigDecimal otrosGastos;


    // =========================================================
    // INDICADORES / BALANCE
    // =========================================================

    private BigDecimal activoCorriente;
    private BigDecimal pasivoCorriente;

    private BigDecimal utilidadOperacional;
    private BigDecimal utilidadNeta;

    private BigDecimal totalActivos;
    private BigDecimal totalPasivos;


    // =========================================================
    // INFORMACIÓN COMPLEMENTARIA
    // =========================================================

    private String origenFondos;
    private String relacionFinanciera;
    private BigDecimal deudaRelacionFinanciera;
}