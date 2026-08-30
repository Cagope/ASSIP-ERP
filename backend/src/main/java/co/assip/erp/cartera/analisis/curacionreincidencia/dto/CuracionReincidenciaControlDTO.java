package co.assip.erp.cartera.analisis.curacionreincidencia.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CuracionReincidenciaControlDTO {

    private LocalDate primerCorteDisponible;
    private LocalDate ultimoCorteDisponible;

    private LocalDate periodoDesdeSugerido;
    private LocalDate periodoHastaSugerido;

    private Integer mesesSeguimientoReincidencia;
}
