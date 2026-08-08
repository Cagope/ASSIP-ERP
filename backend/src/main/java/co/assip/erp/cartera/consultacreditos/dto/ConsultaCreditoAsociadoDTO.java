package co.assip.erp.cartera.consultacreditos.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Asociado encontrado en la búsqueda previa
 * a la consulta de créditos.
 *
 * Este DTO contiene únicamente la información necesaria
 * para identificar y seleccionar al asociado.
 *
 * No incluye información financiera ni detalle de créditos.
 */
@Getter
@Setter
public class ConsultaCreditoAsociadoDTO {

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
    // Nombres
    // =========================================================

    private String nombres;
    private String primerApellido;
    private String segundoApellido;
    private String nombreCompleto;

    // =========================================================
    // Agencia
    // =========================================================

    private Integer idAgencia;
    private String codigoAgencia;
    private String nombreAgencia;

    // =========================================================
    // Estado del asociado
    // =========================================================

    private String estadoPersona;
    private Boolean activo;

    // =========================================================
    // Resumen de créditos
    // =========================================================

    /**
     * Cantidad total de créditos registrados para el asociado.
     *
     * Incluye créditos vigentes, saldados y cancelados.
     */
    private Integer cantidadCreditos;

    /**
     * Indica si el asociado tiene al menos un crédito registrado.
     */
    private Boolean tieneCreditos;

}