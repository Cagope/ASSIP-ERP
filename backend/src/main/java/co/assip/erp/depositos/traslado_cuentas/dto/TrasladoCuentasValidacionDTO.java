
package co.assip.erp.depositos.traslado_cuentas.dto;

import java.util.List;


// =========================================================
// DTO - VALIDACIONES DEL TRASLADO INTEGRAL DE CUENTAS
// ENTRE AGENCIAS
// =========================================================

public record TrasladoCuentasValidacionDTO(

        // Resultado general de las validaciones
        boolean permitido,

        // Restricciones por productos del asociado
        boolean tieneCdatBloqueante,
        boolean tieneCarteraBloqueante,

        // Validación de cuentas existentes en destino
        boolean tieneCuentasDuplicadasDestino,

        // Mensajes de validación y restricciones encontradas
        List<String> mensajes

) {
}
