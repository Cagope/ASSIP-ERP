
package co.assip.erp.contabilidad.informes.pyg.sin_formato;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Calcula el Estado de Excedentes o Pérdidas sin formato.
 * Trabaja exclusivamente con el mes seleccionado y no modifica contabilidad.
 */
@Service
@RequiredArgsConstructor
public class PyGSinFormatoService {

    private final PyGSinFormatoRepository repository;

    public record CuentaPyG(
            String codigoCuenta,
            String nombreCuenta,
            int nivelCuenta,
            String naturalezaCuenta,
            BigDecimal debitos,
            BigDecimal creditos,
            BigDecimal saldo
    ) {}

    public record ResultadoPyG(
            int idAgencia,
            int ano,
            int mes,
            List<CuentaPyG> cuentas,
            BigDecimal totalIngresos,
            BigDecimal totalCostosGastos,
            BigDecimal excedentePerdida
    ) {}

    /**
     * Consulta un mes de una agencia y calcula sus resultados.
     * La autorización de la agencia debe validarse antes de invocar este método.
     */
    @Transactional(readOnly = true)
    public ResultadoPyG consultar(int idAgencia, int ano, int mes) {
        if (idAgencia <= 0) {
            throw new IllegalArgumentException("La agencia es obligatoria.");
        }
        if (ano < 1900 || ano > 9999) {
            throw new IllegalArgumentException("El año consultado no es válido.");
        }
        if (mes < 1 || mes > 12) {
            throw new IllegalArgumentException("El mes debe estar entre 1 y 12.");
        }

        List<PyGSinFormatoRepository.CuentaResultado> registros =
                repository.consultarCuentas(List.of(idAgencia), ano, mes);

        List<CuentaPyG> cuentas = new ArrayList<>();

        for (PyGSinFormatoRepository.CuentaResultado registro : registros) {
            BigDecimal debitos = valor(registro.debitos());
            BigDecimal creditos = valor(registro.creditos());

            BigDecimal saldo = "D".equalsIgnoreCase(registro.naturalezaCuenta())
                    ? debitos.subtract(creditos)
                    : creditos.subtract(debitos);

            cuentas.add(new CuentaPyG(
                    registro.codigoCuenta(),
                    registro.nombreCuenta(),
                    registro.nivelCuenta(),
                    registro.naturalezaCuenta(),
                    debitos,
                    creditos,
                    saldo
            ));
        }

        cuentas.sort(Comparator.comparing(CuentaPyG::codigoCuenta));

        BigDecimal ingresos = totalClase(cuentas, "4");

        BigDecimal gastos = totalClase(cuentas, "5")
                .add(totalClase(cuentas, "6"))
                .add(totalClase(cuentas, "7"));

        BigDecimal resultado = ingresos.subtract(gastos);

        return new ResultadoPyG(
                idAgencia,
                ano,
                mes,
                List.copyOf(cuentas),
                ingresos,
                gastos,
                resultado
        );
    }

    /**
     * Totaliza una clase contable usando exclusivamente su nivel más alto.
     * Evita sumar simultáneamente cuentas principales y sus subcuentas.
     */
    private BigDecimal totalClase(List<CuentaPyG> cuentas, String clase) {
        List<CuentaPyG> cuentasClase = cuentas.stream()
                .filter(c -> c.codigoCuenta() != null)
                .filter(c -> c.codigoCuenta().startsWith(clase))
                .toList();

        if (cuentasClase.isEmpty()) {
            return BigDecimal.ZERO;
        }

        int nivelPrincipal = cuentasClase.stream()
                .mapToInt(CuentaPyG::nivelCuenta)
                .min()
                .orElse(1);

        return cuentasClase.stream()
                .filter(c -> c.nivelCuenta() == nivelPrincipal)
                .map(CuentaPyG::saldo)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Normaliza importes nulos provenientes de la consulta contable. */
    private BigDecimal valor(BigDecimal importe) {
        return importe == null ? BigDecimal.ZERO : importe;
    }
}
