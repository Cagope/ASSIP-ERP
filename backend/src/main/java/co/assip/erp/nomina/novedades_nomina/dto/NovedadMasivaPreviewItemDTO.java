package co.assip.erp.nomina.novedades_nomina.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NovedadMasivaPreviewItemDTO {

    // =========================
    // Empleado
    // =========================
    private Integer idEmpleado;
    private String documentoEmpleado;
    private String nombreEmpleado;

    // =========================
    // Contrato
    // =========================
    private Integer idContrato;

    // =========================
    // Cálculo
    // =========================
    private BigDecimal cantidad;
    private BigDecimal valorCalculado;

    // =========================
    // Control
    // =========================
    private Boolean yaExiste;   // true = omitido
}
