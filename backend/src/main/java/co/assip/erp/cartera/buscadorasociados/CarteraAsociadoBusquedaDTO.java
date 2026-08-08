package co.assip.erp.cartera.buscadorasociados;

import lombok.Getter;
import lombok.Setter;

/**
 * Información resumida del asociado encontrada desde
 * los procesos del módulo de Cartera.
 *
 * Este DTO se usa exclusivamente para:
 *
 * - Buscar personas.
 * - Identificar al asociado.
 * - Seleccionar al asociado.
 * - Conocer cuántos créditos tiene registrados.
 *
 * No contiene saldos ni información detallada de créditos.
 */
@Getter
@Setter
public class CarteraAsociadoBusquedaDTO {

    // =========================================================
    // Identificación interna
    // =========================================================

    private Integer idDatosPersonal;

    // =========================================================
    // Documento
    // =========================================================

    private String tipoDocumento;

    private String nombreTipoDocumento;

    private String documento;

    // =========================================================
    // Nombre
    // =========================================================

    private String nombres;

    private String primerApellido;

    private String segundoApellido;

    private String nombreCompleto;

    // =========================================================
    // Resumen de cartera
    // =========================================================

    /**
     * Cantidad total de créditos registrados para el asociado.
     *
     * Incluye créditos vigentes, saldados y cancelados.
     */
    private Integer cantidadCreditos;

}