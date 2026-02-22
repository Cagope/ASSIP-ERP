package co.assip.erp.nomina.liquidacion.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO PLANO para exportación Excel de PREVIEW de liquidación.
 * Una fila representa UN concepto liquidado de UN contrato.
 */
@Getter
@Setter
public class LiquidacionPreviewExcelDTO {

    // =========================================================
    // 🧑 EMPLEADO
    // =========================================================
    private Integer idEmpleado;
    private String documentoEmpleado;
    private String nombreEmpleado;

    // =========================================================
    // 📄 CONTRATO
    // =========================================================
    private Integer idContrato;
    private LocalDate fechaInicioContrato;
    private LocalDate fechaFinContrato;
    private Boolean contratoActivo;

    // =========================================================
    // 🏢 ORGANIZACIÓN
    // =========================================================
    private Integer idAgencia;
    private String nombreAgencia;

    private Integer idSeccion;
    private String nombreSeccion;

    private Integer idCargo;
    private String nombreCargo;

    // =========================================================
    // 🗓 PERÍODO DE NÓMINA
    // =========================================================
    private Integer idPeriodoNomina;
    private LocalDate fechaInicioPeriodo;
    private LocalDate fechaFinPeriodo;

    // =========================================================
    // 💼 CONCEPTO DE NÓMINA
    // =========================================================
    private String codigoConcepto;
    private String nombreConcepto;

    // DEVENGADO | DEDUCCION | PROVISION
    private String tipoConcepto;

    // AUTOMATICO | NOVEDAD
    private String origen;

    // =========================================================
    // ⚙️ MOTOR DE CÁLCULO
    // =========================================================
    // MANUAL | POR_DIAS | POR_HORAS | POR_PORCENTAJE | AUX_TRANSPORTE
    private String tipoCalculo;

    // SALARIO_BASE | IBC | DEVENGADOS | NETO | NINGUNA
    private String baseCalculo;

    private BigDecimal multiplicador;

    // =========================================================
    // 🧮 CÁLCULO
    // =========================================================
    private BigDecimal cantidad;
    private BigDecimal valorUnitario;
    private BigDecimal baseCalculoValor;
    private BigDecimal valorTotal;

    // =========================================================
    // 🧾 TRAZABILIDAD (NOVEDADES)
    // =========================================================
    private Integer idNovedadNomina;
    private String observacionNovedad;

    // =========================================================
    // 📊 RESULTADOS DEL CONTRATO (REPETIDOS POR FILA)
    // =========================================================
    private BigDecimal salarioBase;
    private BigDecimal ibc;

    private BigDecimal totalDevengados;
    private BigDecimal totalDeducciones;
    private BigDecimal totalProvisiones;
    private BigDecimal netoPagar;

    // =========================================================
    // 🔎 CONTROL
    // =========================================================
    private String estadoLiquidacion; // PREVIEW
}
