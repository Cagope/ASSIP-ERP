package co.assip.erp.nomina.novedades_nomina.dto;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NovedadMasivaPreviewDTO {

    // =========================
    // Resumen
    // =========================
    private Integer totalContratos;
    private Integer calculables;
    private Integer omitidos;

    // =========================
    // Detalle
    // =========================
    private List<NovedadMasivaPreviewItemDTO> items;
}
