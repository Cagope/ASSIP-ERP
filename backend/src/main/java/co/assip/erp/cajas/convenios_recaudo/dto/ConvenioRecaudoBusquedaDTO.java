package co.assip.erp.cajas.convenios_recaudo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConvenioRecaudoBusquedaDTO {

    private Boolean valido;
    private String mensaje;

    private String documento;
    private String nombreTitular;

    private String codigoCuentaAportes;
    private BigDecimal saldoAportes;
    private String fechaAperturaAportes;

    @Builder.Default
    private List<ConvenioRecaudoCuentaDTO> cuentas = new ArrayList<>();
}