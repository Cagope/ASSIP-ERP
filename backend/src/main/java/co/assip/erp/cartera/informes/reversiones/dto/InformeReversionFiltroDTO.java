package co.assip.erp.cartera.informes.reversiones.dto;

import java.time.LocalDate;

/** Filtros opcionales del informe, excepto el rango obligatorio de fechas. */
public record InformeReversionFiltroDTO(
        LocalDate fechaInicial,
        LocalDate fechaFinal,
        Integer idAgencia,
        String asociado,
        String pagare,
        String funcionario
) {}
