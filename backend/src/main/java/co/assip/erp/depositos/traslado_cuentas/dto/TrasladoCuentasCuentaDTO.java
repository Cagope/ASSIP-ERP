
package co.assip.erp.depositos.traslado_cuentas.dto;

import java.math.BigDecimal;


// =========================================================
// DTO - CUENTA DE AHORRO
// TRASLADO INTEGRAL ENTRE AGENCIAS
// =========================================================

public record TrasladoCuentasCuentaDTO(

        // Identificación de la cuenta
        Integer idCuentaAhorro,

        // Agencia y forma de ahorro
        Integer idAgencia,
        Integer idFormaAhorro,

        // Información de la cuenta
        String codigoCuenta,
        String codigoForma,
        String nombreForma,
        String estadoCuenta,

        // Información financiera
        BigDecimal saldoActual,

        // Validación del traslado
        boolean trasladable,
        String observacion

) {
}
