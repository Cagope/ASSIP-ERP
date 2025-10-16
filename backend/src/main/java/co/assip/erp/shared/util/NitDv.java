package co.assip.erp.shared.util;

import java.util.Objects;

/**
 * Cálculo del DV para NIT/Documento según DIAN.
 * Pesos desde el dígito más a la derecha (unidad) hacia la izquierda:
 * 3, 7, 13, 17, 19, 23, 29, 37, 41, 43, 47, 53, 59
 *
 * Fórmula:
 *   s = Σ( digito[i] * peso[i] )  (i desde derecha a izquierda)
 *   r = s % 11
 *   DV = (r > 1) ? (11 - r) : r
 */
public final class NitDv {

    private static final int[] PESOS = {3,7,13,17,19,23,29,37,41,43,47,53,59};

    private NitDv() {}

    public static String calcular(String numero) {
        Objects.requireNonNull(numero, "numero");
        String n = numero.trim();
        if (!n.matches("\\d+")) {
            throw new IllegalArgumentException("El número para DV debe tener solo dígitos");
        }
        int sum = 0;
        int wIndex = 0;
        for (int i = n.length() - 1; i >= 0; i--) {
            int dig = n.charAt(i) - '0';
            int peso = PESOS[wIndex % PESOS.length];
            sum += dig * peso;
            wIndex++;
        }
        int r = sum % 11;
        int dv = (r > 1) ? (11 - r) : r;
        return String.valueOf(dv);
    }
}
