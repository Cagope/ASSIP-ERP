package co.assip.erp.tesoreria.operaciones_pendientes;

import java.util.Map;

/** Contrato de validación del origen para recaudos bancarios de Tesorería. */
public interface TesoreriaOrigenFinancieroValidator {
    /** Identifica de forma inequívoca el módulo, proceso y tabla del origen. */
    boolean soporta(Map<String, Object> operacion);

    /** Comprueba la vigencia del origen para una vista previa sin bloquear registros. */
    void validarVistaPrevia(Map<String, Object> operacion);

    /** Valida y bloquea el origen dentro de la transacción de confirmación. */
    void validarConfirmacion(Map<String, Object> operacion);

    /** Obtiene la cuenta CxC efectivamente debitada en el comprobante original. */
    Integer obtenerCuentaCxCOriginal(Map<String, Object> operacion);
}
