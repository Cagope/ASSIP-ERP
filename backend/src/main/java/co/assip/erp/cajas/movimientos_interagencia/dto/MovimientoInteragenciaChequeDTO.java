package co.assip.erp.cajas.movimientos_interagencia.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoInteragenciaChequeDTO {

    private String codigoBanco;
    private String numeroCheque;
    private BigDecimal valorCheque;
    private String observacion;
}