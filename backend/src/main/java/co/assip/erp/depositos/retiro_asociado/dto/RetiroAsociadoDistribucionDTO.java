
package co.assip.erp.depositos.retiro_asociado.dto;

import java.math.BigDecimal;

/**
 * Distribución del saldo de una cuenta durante
 * el retiro integral de un asociado.
 *
 * Una cuenta puede distribuirse entre Caja y CXP,
 * pero la suma debe coincidir con su saldo.
 */
public record RetiroAsociadoDistribucionDTO(
        Integer idCuentaAhorro,
        BigDecimal valorCaja,
        BigDecimal valorCxp
) {

    public RetiroAsociadoDistribucionDTO {
        if (idCuentaAhorro == null || idCuentaAhorro <= 0) {
            throw new IllegalArgumentException(
                    "La cuenta de ahorro es obligatoria."
            );
        }

        if (valorCaja == null || valorCxp == null) {
            throw new IllegalArgumentException(
                    "Los valores de distribución son obligatorios."
            );
        }

        if (valorCaja.signum() < 0 || valorCxp.signum() < 0) {
            throw new IllegalArgumentException(
                    "Los valores no pueden ser negativos."
            );
        }
    }

    public BigDecimal total() {
        return valorCaja.add(valorCxp);
    }

    public boolean tienePagoCaja() {
        return valorCaja.signum() > 0;
    }

    public boolean tienePagoCxp() {
        return valorCxp.signum() > 0;
    }
}
