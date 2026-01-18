package co.assip.erp.activosfijos.ingreso.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class IngresoActivosRequestDTO {

    @Valid
    @NotNull
    private IngresoActivoHeaderDTO header;

    @Valid
    @NotEmpty
    private List<IngresoActivoItemDTO> items;

    public IngresoActivoHeaderDTO getHeader() { return header; }
    public void setHeader(IngresoActivoHeaderDTO header) { this.header = header; }

    public List<IngresoActivoItemDTO> getItems() { return items; }
    public void setItems(List<IngresoActivoItemDTO> items) { this.items = items; }
}
