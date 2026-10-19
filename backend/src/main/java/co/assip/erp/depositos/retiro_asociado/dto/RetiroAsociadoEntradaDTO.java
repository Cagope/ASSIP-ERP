
package co.assip.erp.depositos.retiro_asociado.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Solicitud de retiro integral del asociado.
 *
 * Incluye la información administrativa del retiro
 * y la distribución de saldos entre Caja y CXP.
 *
 * Los saldos reales siempre se consultan en PostgreSQL.
 */
public record RetiroAsociadoEntradaDTO(

        Integer idDatosPersonal,

        Integer idAgenciaOperacion,

        Integer idCaja,

        LocalDate fechaProceso,

        String estadoFinal,

        String comentarioCuentas,

        String numeroActa,

        LocalDate fechaActa,

        List<MedioPago> mediosPago,

        List<RetiroAsociadoDistribucionDTO> distribuciones

) {

    /**
     * Totales solicitados por medio de pago.
     *
     * CAJA: efectivo pendiente de entregar.
     * CXP: obligación pendiente en Tesorería.
     */
    public record MedioPago(
            String destino,
            BigDecimal valor
    ) {
    }
}
