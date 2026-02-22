package co.assip.erp.nomina.novedades_nomina.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NovedadMasivaRequestDTO {

    private Integer idPeriodo;
    private String codigoConcepto;

    private LocalDate fechaInicial;
    private LocalDate fechaFinal;

    private BigDecimal cantidad;
    private BigDecimal valor; // opcional (si manual)

    private String observacion;      // ✅ NUEVO
    private Integer fkAgencia;       // ✅ NUEVO
}
