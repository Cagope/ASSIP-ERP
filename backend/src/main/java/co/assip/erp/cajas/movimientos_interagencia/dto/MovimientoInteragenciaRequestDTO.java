package co.assip.erp.cajas.movimientos_interagencia.dto;

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
public class MovimientoInteragenciaRequestDTO {

    private Long idCaja;
    private Long idProvision;
    private Integer idAgenciaCaja;
    private LocalDate fechaContable;

    private Long idCuentaAhorro;

    // Operación de caja:
    // CONSIGNACION_AHORRO / RETIRO_AHORRO
    private String codigoOperacion;

    // Tipo movimiento depósitos:
    // 001 / 551
    private String tipoMovimiento;

    // Normalmente "CJ"
    private String tipoComprobante;

    // Desprendible / libreta / orden
    private String numeroComprobante;

    private BigDecimal valorEfectivo;
    private BigDecimal valorCheques;

    @Builder.Default
    private List<MovimientoInteragenciaChequeDTO> cheques = new ArrayList<>();
}