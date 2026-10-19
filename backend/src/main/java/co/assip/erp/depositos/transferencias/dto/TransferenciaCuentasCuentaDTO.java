
package co.assip.erp.depositos.transferencias.dto;

import java.math.BigDecimal;

// =========================================================
// DTO - CUENTA DE AHORRO
// TRANSFERENCIA DE FONDOS ENTRE CUENTAS
// =========================================================

public record TransferenciaCuentasCuentaDTO(

        // =====================================================
        // IDENTIFICACIÓN
        // =====================================================

        Integer idCuentaAhorro,
        Integer idAgencia,
        Integer idDatosPersonal,

        // =====================================================
        // AGENCIA
        // =====================================================

        String codigoAgencia,
        String nombreAgencia,

        // =====================================================
        // TITULAR
        // =====================================================

        String documento,
        String nombreCompleto,

        // =====================================================
        // CUENTA Y PRODUCTO
        // =====================================================

        String codigoCuenta,
        String nombreForma,
        String tipoCaptacion,

        // =====================================================
        // ESTADO
        // =====================================================

        String estadoCuenta,
        String descripcionEstadoCuenta,
        boolean operativa,

        // =====================================================
        // INFORMACIÓN FINANCIERA
        // =====================================================

        BigDecimal saldoActual,
        BigDecimal saldoDisponible,

        // =====================================================
        // VALIDACIÓN DE OPERACIONES
        // =====================================================

        boolean permiteDebito

) {
}
