package co.assip.erp.contabilidad.comprobantes.importar.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Resultado de la lectura y validación de un Excel contable.
 * No genera comprobantes ni modifica registros financieros.
 */
public record ComprobanteImportacionResponseDTO(
        String nombreArchivo,
        Integer idAgencia,
        Integer totalFilas,
        Integer filasValidas,
        Integer filasConErrores,
        BigDecimal totalDebitos,
        BigDecimal totalCreditos,
        BigDecimal diferencia,
        Boolean puedeGrabar,
        List<Movimiento> movimientos,
        List<ErrorValidacion> errores
) {

    /**
     * Movimiento interpretado y validado desde el Excel.
     * Conserva la fila original para identificar errores.
     */
    public record Movimiento(
            Integer numeroFila,
            String codigoCuenta,
            Integer idCatalogoCuenta,
            String documentoTercero,
            Integer idDatosPersonal,
            String detalleMovimiento,
            BigDecimal valorDebito,
            BigDecimal valorCredito,
            BigDecimal valorBase,
            Integer idTiposControl,
            String documentoSoporte
    ) {
    }

    /**
     * Error de validación asociado a una fila y columna.
     * Permite corregir el archivo antes de contabilizar.
     */
    public record ErrorValidacion(
            Integer numeroFila,
            String columna,
            String mensaje
    ) {
    }
}