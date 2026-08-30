package co.assip.erp.cartera.analisis.cosechas.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CosechaFilaDTO {

    // =========================================================
    // COSECHA
    // =========================================================

    private LocalDate cosecha;


    // =========================================================
    // ORIGINACIÓN
    // =========================================================

    private Integer cantidadOriginada;

    private BigDecimal valorInicialOriginal;

    private BigDecimal valorDesembolsadoOriginal;


    // =========================================================
    // SEGUIMIENTO
    // =========================================================

    private Integer maxMob;

    private List<CosechaCeldaDTO> celdas =
            new ArrayList<>();
}