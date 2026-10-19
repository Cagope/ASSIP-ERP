
package co.assip.erp.depositos.traslado_cuentas.dto;

import java.math.BigDecimal;
import java.util.List;


// =========================================================
// DTO - RESULTADO DEL TRASLADO INTEGRAL DE CUENTAS
// ENTRE AGENCIAS
// =========================================================

public record TrasladoCuentasResultadoDTO(

        // Identificación del asociado
        Integer idDatosPersonal,

        // Agencias involucradas en el traslado
        Integer idAgenciaOrigen,
        Integer idAgenciaDestino,

        // Resultado financiero del traslado
        BigDecimal totalTrasladado,
        int cantidadCuentas,

        // Relación de cuentas trasladadas
        List<CuentaTrasladada> cuentas,

        // Comprobantes contables generados
        List<ComprobanteGenerado> comprobantes

) {

    // =====================================================
    // DTO - CUENTA TRASLADADA
    // =====================================================

    public record CuentaTrasladada(

            // Identificación de las cuentas
            Integer idCuentaOrigen,
            Integer idCuentaDestino,

            // Código de la cuenta de ahorro
            String codigoCuenta,

            // Valor efectivamente trasladado
            BigDecimal valorTrasladado

    ) {
    }


    // =====================================================
    // DTO - COMPROBANTE CONTABLE GENERADO
    // =====================================================

    public record ComprobanteGenerado(

            // Agencia donde se registró el comprobante
            Integer idAgencia,

            // Tipo de comprobante contable
            String tipoComprobante,

            // Consecutivo definitivo del comprobante
            String numeroComprobante

    ) {
    }

}
