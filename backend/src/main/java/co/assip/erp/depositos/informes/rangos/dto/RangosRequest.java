package co.assip.erp.depositos.informes.rangos;

import co.assip.erp.depositos.informes.rangos.dto.RangosFiltroDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 📥 RangosRequest — Informe por Rangos (Versión Final)
 * ----------------------------------------------------------------
 * Request completo para informes estadísticos:
 *
 *  🔸 tipo        → EDAD | SALDO | ANTIGUEDAD
 *  🔸 agencia     → "0" = todas, o id_agencia específica
 *  🔸 fechaCorte  → YYYY-MM-DD (afecta saldo, edad y antigüedad)
 *  🔸 rangos      → lista de 4 rangos {desde, hasta}
 *
 * Notas:
 *  - Informe toma TODAS las formas de ahorro.
 *  - La edad y la antigüedad se calculan A LA FECHA DE CORTE.
 *  - El usuario puede filtrar por agencia o manejarlo en Excel.
 */
@Getter
@Setter
public class RangosRequest {

    /** Tipo de informe: EDAD | SALDO | ANTIGUEDAD */
    private String tipo;

    /** "0" = todas las agencias */
    private String agencia;

    /** Fecha de corte (YYYY-MM-DD) */
    private String fechaCorte;

    /** Lista de rangos enviados desde frontend (4 rangos obligatorios) */
    private List<RangosFiltroDTO> rangos;
}
