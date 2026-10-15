package co.assip.erp.cajas.salida_cheques.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CajasSalidaChequesResponseDTO {

    private Long idCajaOrigen;
    private Long idCajaControl;
    private LocalDate fechaContable;
    private Integer cantidadCheques;
    private BigDecimal valorTotal;
    private String tipoComprobante;
    private String numeroComprobante;

    @Builder.Default
    private List<Long> idsMovimientosCaja = new ArrayList<>();

    private String mensaje;
}