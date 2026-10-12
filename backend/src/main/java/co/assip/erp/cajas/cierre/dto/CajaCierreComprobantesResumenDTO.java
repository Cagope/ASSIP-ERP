package co.assip.erp.cajas.cierre.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CajaCierreComprobantesResumenDTO {

    private Long idDepartamentoOperativo;
    private String codigoDepartamento;
    private String nombreDepartamento;

    private List<CajaCierreComprobanteDTO> documentosOrigen;
    private List<CajaCierreComprobanteDTO> comprobantesCaja;
}