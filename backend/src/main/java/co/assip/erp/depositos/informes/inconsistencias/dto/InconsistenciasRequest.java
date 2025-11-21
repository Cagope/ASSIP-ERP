package co.assip.erp.depositos.informes.inconsistencias.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Filtros simples del informe
 */
@Getter
@Setter
public class InconsistenciasRequest {

    private String agencia;     // "0" = todas
    private String fechaCorte;  // YYYY-MM-DD
}
