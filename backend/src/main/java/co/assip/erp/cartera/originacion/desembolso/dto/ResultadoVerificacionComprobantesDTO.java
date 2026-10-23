
package co.assip.erp.cartera.originacion.desembolso.dto;

import java.util.List;

public record ResultadoVerificacionComprobantesDTO(
        boolean distribucionCuadra,
        boolean comprobantesCuadran,
        String mensaje,
        List<ComprobanteDesembolsoPreviewDTO> comprobantes
) {
}
