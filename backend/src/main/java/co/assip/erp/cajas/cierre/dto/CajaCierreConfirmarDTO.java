package co.assip.erp.cajas.cierre.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class CajaCierreConfirmarDTO {

    private Long idProvision;
    private String observacion;

    private List<CajaCierreEfectivoDetalleDTO> efectivoDetalle;
}