package co.assip.erp.cajas.cierre_recaudos_convenios.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CierreRecaudosConveniosPreviewDTO {

    private Long idProvision;
    private Long idCaja;
    private Integer idAgencia;
    private LocalDate fechaContable;

    private Long idConvenio;
    private String codigoConvenio;
    private String nombreConvenio;

    private Long idCuentaAhorro;
    private String codigoCuenta;
    private String documentoTitular;
    private String nombreTitular;

    private String documentoSoporte;

    private Integer cantidadRecaudos;
    private BigDecimal valorTotal;

    private Boolean permiteAplicar;
    private String mensaje;

    @Builder.Default
    private List<String> errores = new ArrayList<>();

    @Builder.Default
    private List<CierreRecaudosConveniosItemDTO> items = new ArrayList<>();

    public Boolean getPermiteAplicar() {
        return permiteAplicar != null && permiteAplicar;
    }
}