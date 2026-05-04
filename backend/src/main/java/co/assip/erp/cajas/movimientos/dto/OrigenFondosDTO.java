package co.assip.erp.cajas.movimientos.dto;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
public class OrigenFondosDTO {

    private Long idCaja;

    private BigDecimal valorEfectivo;
    private BigDecimal valorCheques;
    private BigDecimal valorAhorros;
    private BigDecimal valorBancos;

    private List<ChequeRecibidoDTO> cheques;

}