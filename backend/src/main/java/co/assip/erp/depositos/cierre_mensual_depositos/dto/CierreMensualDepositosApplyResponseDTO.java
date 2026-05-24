package co.assip.erp.depositos.cierre_mensual_depositos.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CierreMensualDepositosApplyResponseDTO {

    private Long idCierreMensual;

    private String mensaje;

}