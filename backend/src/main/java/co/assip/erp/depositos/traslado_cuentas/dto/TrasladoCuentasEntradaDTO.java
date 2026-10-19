
package co.assip.erp.depositos.traslado_cuentas.dto;

import java.time.LocalDate;


// =========================================================
// DTO - ENTRADA DEL TRASLADO INTEGRAL DE CUENTAS
// ENTRE AGENCIAS
// =========================================================

public record TrasladoCuentasEntradaDTO(

        // Identificación del asociado
        Integer idDatosPersonal,

        // Agencia de origen
        Integer idAgenciaOrigen,

        // Agencia de destino
        Integer idAgenciaDestino,

        // Fecha de ejecución del traslado
        LocalDate fechaProceso,

        // Comentario o justificación del traslado
        String comentario

) {
}
