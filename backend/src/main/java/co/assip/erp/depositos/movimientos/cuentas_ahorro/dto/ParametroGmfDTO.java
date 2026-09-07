package co.assip.erp.depositos.movimientos.cuentasahorro.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class ParametroGmfDTO {

    private Long idParametroGmf;
    private LocalDate fechaInicial;
    private LocalDate fechaFinal;
    private BigDecimal porcentajeGmf;
    private BigDecimal valorTopeExencion;
}