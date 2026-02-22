package co.assip.erp.nomina.liquidacion.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO de detalle de liquidación de nómina.
 * Se usa exclusivamente entre calculators y repositories.
 */
@Getter
@Setter
public class LiquidacionDetalleDTO {

    // =========================
    // RELACIÓN
    // =========================
    private String codigoConcepto; // ← CLAVE REAL (VARCHAR)

    // =========================
    // CLASIFICACIÓN
    // =========================
    private String tipo;      // DEVENGADO | DEDUCCION | PROVISION
    private String origen;    // AUTOMATICO | NOVEDAD

    // =========================
    // CÁLCULO
    // =========================
    private BigDecimal cantidad;
    private BigDecimal valorUnitario;
    private BigDecimal valorTotal;
    private BigDecimal baseCalculo;

    // =========================
    // MOTOR DE CÁLCULO
    // =========================
    private String tipoCalculo;        // POR_DIAS | POR_HORAS | POR_PORCENTAJE | MANUAL
    private BigDecimal multiplicador;  // tarifa o factor

    // =========================
    // TRAZABILIDAD
    // =========================
    private Integer idNovedadNomina;

    // =========================================================
    // 🔒 CONSTRUCTOR PRIVADO
    // =========================================================
    private LiquidacionDetalleDTO() {
        this.tipoCalculo = "MANUAL";
        this.multiplicador = BigDecimal.ONE;
    }

    // =========================================================
    // 🟢 FACTORIES – DEVENGADOS
    // =========================================================
    public static LiquidacionDetalleDTO devengadoAutomatico(
            String codigoConcepto,
            BigDecimal cantidad,
            BigDecimal valorUnitario,
            BigDecimal valorTotal,
            BigDecimal baseCalculo
    ) {
        LiquidacionDetalleDTO dto = new LiquidacionDetalleDTO();
        dto.codigoConcepto = codigoConcepto;
        dto.tipo = "DEVENGADO";
        dto.origen = "AUTOMATICO";
        dto.cantidad = cantidad;
        dto.valorUnitario = valorUnitario;
        dto.valorTotal = valorTotal;
        dto.baseCalculo = baseCalculo;
        return dto;
    }

    public static LiquidacionDetalleDTO devengadoNovedad(
            String codigoConcepto,
            BigDecimal cantidad,
            BigDecimal valorUnitario,
            BigDecimal valorTotal,
            BigDecimal baseCalculo,
            Integer idNovedadNomina
    ) {
        LiquidacionDetalleDTO dto = new LiquidacionDetalleDTO();
        dto.codigoConcepto = codigoConcepto;
        dto.tipo = "DEVENGADO";
        dto.origen = "NOVEDAD";
        dto.cantidad = cantidad;
        dto.valorUnitario = valorUnitario;
        dto.valorTotal = valorTotal;
        dto.baseCalculo = baseCalculo;
        dto.idNovedadNomina = idNovedadNomina;
        return dto;
    }

    // =========================================================
    // 🔴 FACTORIES – DEDUCCIONES
    // =========================================================
    public static LiquidacionDetalleDTO deduccionAutomatica(
            String codigoConcepto,
            BigDecimal cantidad,
            BigDecimal valorUnitario,
            BigDecimal valorTotal,
            BigDecimal baseCalculo
    ) {
        LiquidacionDetalleDTO dto = new LiquidacionDetalleDTO();
        dto.codigoConcepto = codigoConcepto;
        dto.tipo = "DEDUCCION";
        dto.origen = "AUTOMATICO";
        dto.cantidad = cantidad;
        dto.valorUnitario = valorUnitario;
        dto.valorTotal = valorTotal;
        dto.baseCalculo = baseCalculo;
        return dto;
    }

    public static LiquidacionDetalleDTO deduccionNovedad(
            String codigoConcepto,
            BigDecimal cantidad,
            BigDecimal valorUnitario,
            BigDecimal valorTotal,
            BigDecimal baseCalculo,
            Integer idNovedadNomina
    ) {
        LiquidacionDetalleDTO dto = new LiquidacionDetalleDTO();
        dto.codigoConcepto = codigoConcepto;
        dto.tipo = "DEDUCCION";
        dto.origen = "NOVEDAD";
        dto.cantidad = cantidad;
        dto.valorUnitario = valorUnitario;
        dto.valorTotal = valorTotal;
        dto.baseCalculo = baseCalculo;
        dto.idNovedadNomina = idNovedadNomina;
        return dto;
    }

    // =========================================================
    // 🔵 FACTORIES – PROVISIONES
    // =========================================================
    public static LiquidacionDetalleDTO provisionAutomatica(
            String codigoConcepto,
            BigDecimal cantidad,
            BigDecimal valorUnitario,
            BigDecimal valorTotal,
            BigDecimal baseCalculo
    ) {
        LiquidacionDetalleDTO dto = new LiquidacionDetalleDTO();
        dto.codigoConcepto = codigoConcepto;
        dto.tipo = "PROVISION";
        dto.origen = "AUTOMATICO";
        dto.cantidad = cantidad;
        dto.valorUnitario = valorUnitario;
        dto.valorTotal = valorTotal;
        dto.baseCalculo = baseCalculo;
        return dto;
    }

    public static LiquidacionDetalleDTO provisionNovedad(
            String codigoConcepto,
            BigDecimal cantidad,
            BigDecimal valorUnitario,
            BigDecimal valorTotal,
            BigDecimal baseCalculo,
            Integer idNovedadNomina
    ) {
        LiquidacionDetalleDTO dto = new LiquidacionDetalleDTO();
        dto.codigoConcepto = codigoConcepto;
        dto.tipo = "PROVISION";
        dto.origen = "NOVEDAD";
        dto.cantidad = cantidad;
        dto.valorUnitario = valorUnitario;
        dto.valorTotal = valorTotal;
        dto.baseCalculo = baseCalculo;
        dto.idNovedadNomina = idNovedadNomina;
        return dto;
    }
}
