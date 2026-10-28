
package co.assip.erp.contabilidad.comprobantes.crear.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Solicitud para crear un comprobante contable manual.
 *
 * El backend asigna el consecutivo definitivo, el usuario
 * responsable y la trazabilidad del origen.
 */
public record ComprobanteCrearRequestDTO(

        Integer idAgencia,

        String tipoComprobante,

        LocalDate fechaContable,

        String conceptoComprobante,

        List<Detalle> movimientos

) {

    /**
     * Representa una línea del comprobante manual.
     *
     * Cada línea tiene un tercero obligatorio.
     * El tipo de control y el documento se utilizan
     * cuando la cuenta maneja entrada o salida.
     *
     * La base permite registrar operaciones como
     * IVA y retenciones.
     */
    public record Detalle(

            Integer idCatalogoCuenta,

            Integer idDatosPersonal,

            String detalleMovimiento,

            BigDecimal valorDebito,

            BigDecimal valorCredito,

            BigDecimal valorBase,

            Integer idTiposControl,

            String documentoSoporte

    ) {
    }
}
