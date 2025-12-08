package co.assip.erp.depositos.habilidad_asociado.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 🎯 HabilidadAsociadoItemDTO
 * ----------------------------------------------------
 * Resultado técnico de la evaluación:
 *  - zona / subzona
 *  - documento, nombre
 *  - tipo persona, edad
 *  - estado de la cuenta
 *  - saldo actual
 *  - total aportes
 *  - resultado: HÁBIL / INHÁBIL (...)
 */
@Data
public class HabilidadAsociadoItemDTO {

    private Integer idCuentaAhorro;
    private String codigoCuenta;

    private String zona;
    private String subzona;

    private String documento;
    private String nombre;

    private String tipoPersona;
    private Integer edad;

    private String estado;

    private BigDecimal saldoHoy;
    private BigDecimal totalAportes;

    private String resultado;
}
