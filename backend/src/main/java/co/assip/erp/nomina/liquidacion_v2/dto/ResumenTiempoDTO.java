package co.assip.erp.nomina.liquidacion_v2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumenTiempoDTO {

    /**
     * Días calendario del período de nómina.
     * Ejemplo: quincena = 15, mensual = 30 o 31 según fechas reales.
     */
    private Integer diasPeriodo;

    /**
     * Días clasificados por novedades de tiempo.
     */
    private BigDecimal diasVacaciones;
    private BigDecimal diasIncapacidad;
    private BigDecimal diasPermisoRemunerado;
    private BigDecimal diasPermisoNoRemunerado;

    /**
     * Días efectivamente liquidados como básico.
     *
     * Fórmula esperada:
     * diasPeriodo
     * - diasVacaciones
     * - diasIncapacidad
     * - diasPermisoRemunerado
     * - diasPermisoNoRemunerado
     */
    private BigDecimal diasLaborados;

    /**
     * Normaliza nulls a cero.
     * Útil para evitar validaciones repetidas en calculadores.
     */
    public BigDecimal getDiasVacacionesSafe() {
        return diasVacaciones != null ? diasVacaciones : BigDecimal.ZERO;
    }

    public BigDecimal getDiasIncapacidadSafe() {
        return diasIncapacidad != null ? diasIncapacidad : BigDecimal.ZERO;
    }

    public BigDecimal getDiasPermisoRemuneradoSafe() {
        return diasPermisoRemunerado != null ? diasPermisoRemunerado : BigDecimal.ZERO;
    }

    public BigDecimal getDiasPermisoNoRemuneradoSafe() {
        return diasPermisoNoRemunerado != null ? diasPermisoNoRemunerado : BigDecimal.ZERO;
    }

    public BigDecimal getDiasLaboradosSafe() {
        return diasLaborados != null ? diasLaborados : BigDecimal.ZERO;
    }
}