package co.assip.erp.sarlaft.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 🎯 ReglasInput
 * ------------------------------------------------------------
 * Contrato de entrada del motor SARLAFT.
 * Este DTO es estable y permite extender el sistema sin romper reglas previas.
 */
@Getter
@Setter
public class ReglasInput {

    /** ID de la persona involucrada en la operación */
    private Long idDatosPersonal;

    /** Agencia donde se realiza la acción */
    private Integer idAgencia;

    /** Código del módulo origen (02=Depósitos, HV, 03=Cartera…) */
    private String codigoModulo;

    /** Acción a evaluar (CONSULTA, APERTURA, CONSIGNACION…) */
    private String accion;

    /** Monto de la operación (si aplica) */
    private Double monto;

    /** Fecha de última actualización (Regla 001) */
    private java.sql.Timestamp fechaUltimaActualizacion;

    // ==========================================
    // 🔥 Campos adicionales para reglas 002 y 003
    // ==========================================

    /** Fecha de nacimiento (coherencia edad/documento, edad/reglas) */
    private java.sql.Date fechaNacimiento;

    /** Tipo de documento (CC, TI, CE…) */
    private String tipoDocumento;

    /** Código de forma de ahorro (para reglas específicas de depósitos) */
    private String codigoFormaAhorro;

    // ==========================================
    // 🔥 Campos opcionales para reglas avanzadas
    //    NO afectan reglas existentes
    // ==========================================

    /** Ingresos mensuales del asociado */
    private Double ingresosMensuales;

    /** Egresos mensuales del asociado */
    private Double egresosMensuales;

    /** Total de activos del asociado (sirve para CDTAs y patrimonio) */
    private Double totalActivos;

    /** Total de pasivos del asociado */
    private Double totalPasivos;

}
