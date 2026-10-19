
package co.assip.erp.depositos.traslado_cuentas.dto;

import java.math.BigDecimal;
import java.util.List;


// =========================================================
// DTO - VISTA PREVIA DEL TRASLADO INTEGRAL DE CUENTAS
// ENTRE AGENCIAS
// =========================================================

public record TrasladoCuentasPreviewDTO(

        // Identificación del asociado
        Integer idDatosPersonal,
        String documento,
        String nombreCompleto,

        // Agencias involucradas
        Integer idAgenciaOrigen,
        Integer idAgenciaDestino,

        // Cuentas de ahorro que serán trasladadas
        List<TrasladoCuentasCuentaDTO> cuentas,

        // Valor total del traslado
        BigDecimal totalTraslado,

        // Resultado de las validaciones
        TrasladoCuentasValidacionDTO validacion,

        // Vista previa de los comprobantes contables
        List<ComprobantePreview> comprobantes

) {

    // =====================================================
    // DTO - COMPROBANTE CONTABLE PRELIMINAR
    // =====================================================

    public record ComprobantePreview(

            // Agencia responsable del comprobante
            Integer idAgencia,
            String nombreAgencia,

            // Identificación y concepto del comprobante
            String tipoComprobante,
            String concepto,

            // Movimientos contables del comprobante
            List<MovimientoPreview> movimientos,

            // Totales para validar el cuadre contable
            BigDecimal totalDebito,
            BigDecimal totalCredito

    ) {
    }


    // =====================================================
    // DTO - MOVIMIENTO CONTABLE PRELIMINAR
    // =====================================================

    public record MovimientoPreview(

            // Identificación de la cuenta contable
            String codigoCuenta,
            String nombreCuenta,

            // Descripción del movimiento
            String detalle,

            // Valores del movimiento
            BigDecimal valorDebito,
            BigDecimal valorCredito

    ) {
    }

}
