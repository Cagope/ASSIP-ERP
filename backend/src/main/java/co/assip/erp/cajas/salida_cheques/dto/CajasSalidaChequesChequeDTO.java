package co.assip.erp.cajas.salida_cheques.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CajasSalidaChequesChequeDTO {

    private Long idChequeRecibido;
    private Long idCaja;
    private Integer codigoBanco;
    private String numeroCheque;
    private BigDecimal valorCheque;
}