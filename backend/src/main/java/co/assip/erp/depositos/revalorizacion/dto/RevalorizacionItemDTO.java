package co.assip.erp.depositos.revalorizacion.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 📌 RevalorizacionItemDTO
 * ----------------------------------------------------
 * Resultado técnico del cálculo de revalorización por cuenta.
 *
 * Este DTO se envía al frontend para mostrar el listado:
 *   - Tipo de documento
 *   - Documento
 *   - Nombre completo
 *   - Saldo actual a la fecha contable
 *   - Valor promedio del período
 *   - Valor de la revalorización
 *   - Estado de la cuenta (decodificado)
 */
@Data
public class RevalorizacionItemDTO {

    private String tipoDocumento;
    private String documento;
    private String nombreCompleto;

    /** Saldo actual a la fecha de contabilización */
    private BigDecimal saldoActual;

    /** Promedio ponderado calculado en el período */
    private BigDecimal valorPromedio;

    /** Valor de la revalorización = promedio * tasa */
    private BigDecimal valorRevalorizacion;

    /** Nombre del estado de la cuenta (decodificado) */
    private String estadoCuenta;
}
