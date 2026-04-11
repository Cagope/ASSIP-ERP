package co.assip.erp.nomina.tipos_contratos.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoContratoDTO {

    private Integer idTipoContrato;
    private String codigo;
    private String nombre;
    private Boolean activo;
    private String codigoSuperintendencia;

    // =========================
    // 🔥 NUEVO: REGLAS DE LIQUIDACIÓN
    // =========================
    private Boolean aplicaSalud;
    private Boolean aplicaPension;
    private Boolean aplicaArl;
    private Boolean aplicaCajaCompensacion;
    private Boolean aplicaCesantias;
    private Boolean aplicaPrima;
    private Boolean aplicaVacaciones;
    private Boolean aplicaParafiscales;
}