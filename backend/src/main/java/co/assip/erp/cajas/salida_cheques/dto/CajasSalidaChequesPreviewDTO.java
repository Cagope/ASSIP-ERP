
package co.assip.erp.cajas.salida_cheques.dto;

import co.assip.erp.contabilidad.auxiliares_contables.dto.MovimientoContablePreviewDTO;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class CajasSalidaChequesPreviewDTO {

    private Integer idAgencia;

    private Long idCajaOrigen;
    private Long idCajaControl;

    private LocalDate fechaContable;

    private Integer cantidadCheques;
    private BigDecimal valorTotal;

    private String tipoComprobante;
    private String conceptoComprobante;

    private List<MovimientoContablePreviewDTO> movimientos;

    private BigDecimal totalDebito;
    private BigDecimal totalCredito;
    private BigDecimal diferencia;
    private Boolean cuadrado;
}
