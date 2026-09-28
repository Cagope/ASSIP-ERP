package co.assip.erp.shared.financiero;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Conversión de importes y números a letras para todo ASSIP ERP. */
public final class NumerosEnLetras {

    private static final BigDecimal MIL = BigDecimal.valueOf(1_000L);
    private static final BigDecimal MILLON = BigDecimal.valueOf(1_000_000L);
    private static final BigDecimal BILLON = BigDecimal.valueOf(1_000_000_000_000L);

    private NumerosEnLetras() {
        // Utilidad estática: no instanciable.
    }

    // =========================================================
    // CONVERTIR VALOR DEL PAGARÉ A LETRAS
    //
    // Uso compartido por todos los módulos del ERP.
    // No modifica valores financieros almacenados.
    // =========================================================

    public static String valorEnLetras(
            BigDecimal valor
    ) {

        if (valor == null) {
            return "";
        }

        if (valor.compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "No es posible expresar un valor monetario negativo en letras."
            );
        }

        BigDecimal valorMonetario =
                valor.setScale(
                        2,
                        RoundingMode.HALF_UP
                );

        BigDecimal parteEntera =
                valorMonetario.setScale(
                        0,
                        RoundingMode.DOWN
                );

        int centavos =
                valorMonetario
                        .subtract(parteEntera)
                        .movePointRight(2)
                        .intValueExact();

        String letras =
                numeroEnLetras(
                        parteEntera
                );

        String moneda =
                parteEntera.compareTo(
                        BigDecimal.ONE
                ) == 0
                        ? "PESO"
                        : "PESOS";

        String resultado =
                letras + " " + moneda;

        if (centavos > 0) {

            resultado += " CON "
                    + String.format(
                    "%02d",
                    centavos
            )
                    + "/100";
        }

        return resultado.toUpperCase();
    }

    // =========================================================
    // CONVERSIÓN DE NÚMEROS ENTEROS
    //
    // Hasta 999.999.999.999.999.
    // =========================================================

    public static String numeroEnLetras(
            BigDecimal numero
    ) {

        if (numero.compareTo(BigDecimal.ZERO) == 0) {
            return "CERO";
        }

        if (numero.compareTo(BILLON) >= 0) {

            BigDecimal[] partes =
                    numero.divideAndRemainder(
                            BILLON
                    );

            String billones =
                    partes[0].compareTo(
                            BigDecimal.ONE
                    ) == 0
                            ? "UN BILLÓN"
                            : numeroEnLetras(
                            partes[0]
                    ) + " BILLONES";

            if (partes[1].signum() == 0) {
                return billones + " DE";
            }

            return billones
                    + " "
                    + numeroEnLetras(
                    partes[1]
            );
        }

        if (numero.compareTo(MILLON) >= 0) {

            BigDecimal[] partes =
                    numero.divideAndRemainder(
                            MILLON
                    );

            String millones =
                    partes[0].compareTo(
                            BigDecimal.ONE
                    ) == 0
                            ? "UN MILLÓN"
                            : numeroEnLetras(
                            partes[0]
                    ) + " MILLONES";

            if (partes[1].signum() == 0) {
                return millones + " DE";
            }

            return millones
                    + " "
                    + numeroEnLetras(
                    partes[1]
            );
        }

        if (numero.compareTo(MIL) >= 0) {

            BigDecimal[] partes =
                    numero.divideAndRemainder(
                            MIL
                    );

            String miles =
                    partes[0].compareTo(
                            BigDecimal.ONE
                    ) == 0
                            ? "MIL"
                            : numeroEnLetras(
                            partes[0]
                    ) + " MIL";

            if (partes[1].signum() == 0) {
                return miles;
            }

            return miles
                    + " "
                    + numeroEnLetras(
                    partes[1]
            );
        }

        return convertirCentenas(
                numero.intValueExact()
        );
    }

    // =========================================================
    // CENTENAS, DECENAS Y UNIDADES
    // =========================================================

    private static String convertirCentenas(
            int numero
    ) {

        if (numero == 0) {
            return "";
        }

        if (numero == 100) {
            return "CIEN";
        }

        if (numero >= 100) {

            String[] centenas = {
                    "",
                    "CIENTO",
                    "DOSCIENTOS",
                    "TRESCIENTOS",
                    "CUATROCIENTOS",
                    "QUINIENTOS",
                    "SEISCIENTOS",
                    "SETECIENTOS",
                    "OCHOCIENTOS",
                    "NOVECIENTOS"
            };

            int centena = numero / 100;
            int resto = numero % 100;

            if (resto == 0) {
                return centena == 1 ? "CIEN" : centenas[centena];
            }

            return centenas[centena]
                    + " "
                    + convertirCentenas(resto);
        }

        String[] unidades = {
                "CERO",
                "UN",
                "DOS",
                "TRES",
                "CUATRO",
                "CINCO",
                "SEIS",
                "SIETE",
                "OCHO",
                "NUEVE",
                "DIEZ",
                "ONCE",
                "DOCE",
                "TRECE",
                "CATORCE",
                "QUINCE",
                "DIECISÉIS",
                "DIECISIETE",
                "DIECIOCHO",
                "DIECINUEVE",
                "VEINTE",
                "VEINTIÚN",
                "VEINTIDÓS",
                "VEINTITRÉS",
                "VEINTICUATRO",
                "VEINTICINCO",
                "VEINTISÉIS",
                "VEINTISIETE",
                "VEINTIOCHO",
                "VEINTINUEVE"
        };

        if (numero < 30) {
            return unidades[numero];
        }

        String[] decenas = {
                "",
                "",
                "",
                "TREINTA",
                "CUARENTA",
                "CINCUENTA",
                "SESENTA",
                "SETENTA",
                "OCHENTA",
                "NOVENTA"
        };

        int decena = numero / 10;
        int unidad = numero % 10;

        if (unidad == 0) {
            return decenas[decena];
        }

        return decenas[decena]
                + " Y "
                + unidades[unidad];
    }

}
