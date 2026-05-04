package co.assip.erp.cdat.cdats.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CdatSaveResponseDTO {

    private Long idCuentaCdat;
    private String codigoCdat;

    private String tipoComprobante;
    private String numeroComprobante;

    private String mensaje;
}