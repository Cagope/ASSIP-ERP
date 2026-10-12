package co.assip.erp.cajas.captura_depositos.dto;

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
public class CajaCapturaDepositosRequestDTO {

    private Long idCaja;
    private Integer idAgencia;
    private LocalDate fechaContable;

    private Long idCuentaAhorro;

    // Operación de caja
    private String codigoOperacion;

    // Tipo movimiento depósitos
    private String tipoMovimiento;

    // CJ = documento propio de la cuenta (libreta/orden)
    // FJ = formato físico prenumerado de consignación
    private String tipoComprobante;

    // Número del documento soporte utilizado
    private String numeroComprobante;

    private BigDecimal valorEfectivo;
    private BigDecimal valorCheques;
    private BigDecimal valorTransferencias;

    private String concepto;
    private String observacion;

    @Builder.Default
    private List<CajaCapturaDepositosChequeDTO> cheques = new ArrayList<>();
}