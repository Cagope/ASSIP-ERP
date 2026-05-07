package co.assip.erp.depositos.revalorizacion.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RevalorizacionItemDTO {

    private Integer idCuentaAhorro;
    private String codigoCuenta;

    private Integer idDatosPersonal;

    private String tipoDocumento;
    private String documento;
    private String nombreCompleto;

    // =====================================================
    // 🔹 Valores base
    // =====================================================

    private BigDecimal saldoActual;
    private BigDecimal valorPromedio;

    // =====================================================
    // 🔹 Revalorización
    // =====================================================

    private BigDecimal valorRevalorizacion;
    private BigDecimal nuevoSaldo;

    // =====================================================
    // 🔹 Parámetros
    // =====================================================

    private BigDecimal tasaRevalorizacion;
    private Integer tiempoLiquidacion;
    private BigDecimal minimoForma;

    // =====================================================
    // 🔹 Estado
    // =====================================================

    private String estadoCuenta;

}