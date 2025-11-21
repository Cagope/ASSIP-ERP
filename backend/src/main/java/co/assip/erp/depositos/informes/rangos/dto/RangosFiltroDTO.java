package co.assip.erp.depositos.informes.rangos.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 🎯 RangosFiltroDTO
 * ------------------------------------------------------------
 * Representa un rango individual [desde, hasta] enviado desde
 * el frontend para construir los informes estadísticos.
 *
 * Se utiliza para:
 *   - Informe por EDAD (años)
 *   - Informe por SALDO (pesos)
 *   - Informe por ANTIGUEDAD (años)
 */
@Getter
@Setter
public class RangosFiltroDTO {

    /** Límite inferior del rango (incluido) */
    private Integer desde;

    /** Límite superior del rango (incluido) */
    private Integer hasta;
}
