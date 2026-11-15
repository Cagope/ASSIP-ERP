
package co.assip.erp.ses.formato9013.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class Formato9013DTO {

    // ============================================
    // IDENTIFICACIÓN DEL REPORTE SES
    // ============================================

    /**
     * Código oficial de la entidad.
     * TXT: CODENTIDAD
     */
    private String codigoEntidad;

    /**
     * Fecha de corte en formato ddMMyyyy.
     * TXT: FECHACORTE
     */
    private String fechaCorte;

    /**
     * Consecutivo del registro, iniciando en 1.
     * TXT: CONSECUTIVO
     */
    private Long consecutivo;

    // ============================================
    // IDENTIFICACIÓN DEL ASOCIADO
    // ============================================

    /**
     * Tipo de identificación según codificación SES.
     * TXT: TIPOIDEN
     */
    private String tipoIdentificacion;

    /**
     * Número de identificación del asociado.
     * TXT: NÚMERO IDENTIFICACION
     */
    private String numeroIdentificacion;

    /**
     * Datos complementarios para consulta Angular.
     * No forman parte de las 13 columnas del TXT.
     */
    private String primerApellido;

    private String segundoApellido;

    private String nombres;

    // ============================================
    // INFORMACIÓN FINANCIERA DE APORTES SOCIALES
    // ============================================

    /**
     * Saldo de aportes sociales al cierre mensual.
     * TXT: saldo
     */
    private BigDecimal saldoAportes;

    /**
     * Valor de la cuota mensual pactada.
     * No corresponde al recaudo del mes.
     * TXT: VRAPORTEMENSUAL
     */
    private BigDecimal valorAporteMensual;

    /**
     * Saldo de aportes ordinarios.
     * TXT: APORTEORDINARIO
     */
    private BigDecimal aportesOrdinarios;

    /**
     * Saldo de aportes extraordinarios.
     * TXT: APORTEEXTRAORDINARIO
     */
    private BigDecimal aportesExtraordinarios;

    /**
     * Valor de revalorización de aportes.
     * TXT: VALORREVALORIZACION
     */
    private BigDecimal valorRevalorizacion;

    /**
     * Monto promedio de aportes.
     * TXT: MONTOPROMEDIO
     */
    private BigDecimal montoPromedio;

    /**
     * Fecha del último aporte en formato ddMMyyyy.
     * TXT: ULTIMAFECHA
     */
    private String ultimaFecha;

    /**
     * Contribuciones según definición del formato SES.
     * Puede permanecer NULL si no aplica.
     * TXT: CONTRIBUCIONES
     */
    private BigDecimal contribuciones;
}
