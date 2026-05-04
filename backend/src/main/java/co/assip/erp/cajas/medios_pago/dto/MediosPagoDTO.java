package co.assip.erp.cajas.medios_pago.dto;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
public class MediosPagoDTO {

    private Long idCaja;

    private BigDecimal valorEfectivo;
    private BigDecimal valorCheques;
    private BigDecimal valorDepositos;
    private BigDecimal valorBancos;
    private BigDecimal valorTrasladosAgencias;

    private List<MedioPagoChequeDTO> cheques = List.of();
    private List<MedioPagoDepositoDTO> depositos = List.of();
    private List<MedioPagoBancoDTO> bancos = List.of();
    private List<MedioPagoTrasladoAgenciaDTO> trasladosAgencias = List.of();

}