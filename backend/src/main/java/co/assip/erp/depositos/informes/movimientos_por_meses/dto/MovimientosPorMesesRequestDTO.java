package co.assip.erp.depositos.informes.movimientos_por_meses.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class MovimientosPorMesesRequestDTO {

    private LocalDate fechaCorte;

    private Integer idAgencia;

    private String codigoForma;

    private Integer meses;

    private String tipoInforme;

}