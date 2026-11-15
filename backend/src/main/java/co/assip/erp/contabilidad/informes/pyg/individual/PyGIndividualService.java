
package co.assip.erp.contabilidad.informes.pyg.individual;

import co.assip.erp.contabilidad.informes.pyg.comparado.PyGComparadoService;
import co.assip.erp.contabilidad.informes.pyg.sin_formato.PyGSinFormatoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Construye el Estado de Excedentes o Pérdidas con formato individual.
 * Consulta un período y permite incluir cuentas de mayor detalle.
 */
@Service
@RequiredArgsConstructor
public class PyGIndividualService {
    private static final BigDecimal CERO = BigDecimal.ZERO;

    private final PyGSinFormatoRepository repository;
    private final PyGComparadoService comparadoService;

    public record FilaPyG(
            String codigo,
            String nombre,
            String tipo,
            int nivel,
            BigDecimal valor
    ) {}

    public record ResultadoPyG(
            int idAgencia,
            int ano,
            int mes,
            boolean incluirDetalle,
            List<FilaPyG> filas
    ) {}

    /**
     * Consulta el período y construye el formato individual.
     * La autorización de agencia corresponde al controlador.
     */
    @Transactional(readOnly = true)
    public ResultadoPyG consultar(
            int idAgencia,
            int ano,
            int mes,
            boolean incluirDetalle
    ) {
        validarParametros(idAgencia, ano, mes);

        int nivelMaximo = incluirDetalle ? 6 : 3;

        List<PyGSinFormatoRepository.CuentaResultado> cuentas =
                repository.consultarCuentas(
                        List.of(idAgencia),
                        ano,
                        mes,
                        nivelMaximo
                );

        /*
         * Reutiliza los resultados y fórmulas ya implementados.
         * Ambos períodos son iguales porque el informe es individual.
         */
        PyGComparadoService.ResultadoPyG formato =
                comparadoService.consultar(
                        idAgencia,
                        ano,
                        mes,
                        ano,
                        mes
                );

        Map<String, PyGComparadoService.FilaPyG> filasFormato =
                formato.filas().stream()
                        .collect(Collectors.toMap(
                                PyGComparadoService.FilaPyG::codigo,
                                fila -> fila
                        ));

        List<FilaPyG> filas = new ArrayList<>();

        agregarGrupo(
                filas,
                cuentas,
                filasFormato,
                "41"
        );

        agregarGrupo(
                filas,
                cuentas,
                filasFormato,
                "61"
        );

        agregarTotal(filas, filasFormato, "T1");

        agregarGrupo(
                filas,
                cuentas,
                filasFormato,
                "51"
        );

        agregarGrupo(
                filas,
                cuentas,
                filasFormato,
                "52"
        );

        agregarTotal(filas, filasFormato, "T2");
        agregarTotal(filas, filasFormato, "T3");

        agregarGrupo(
                filas,
                cuentas,
                filasFormato,
                "42"
        );

        agregarGrupo(
                filas,
                cuentas,
                filasFormato,
                "53"
        );

        agregarGrupo(
                filas,
                cuentas,
                filasFormato,
                "54"
        );

        agregarTotal(filas, filasFormato, "T4");
        agregarTotal(filas, filasFormato, "TT");

        return new ResultadoPyG(
                idAgencia,
                ano,
                mes,
                incluirDetalle,
                List.copyOf(filas)
        );
    }

    /**
     * Valida agencia y período antes de consultar.
     * Impide fechas inválidas y agencias sin identificar.
     */
    private void validarParametros(
            int idAgencia,
            int ano,
            int mes
    ) {
        if (idAgencia <= 0) {
            throw new IllegalArgumentException(
                    "La agencia es obligatoria."
            );
        }

        if (ano < 1900 || ano > 9999) {
            throw new IllegalArgumentException(
                    "El año consultado no es válido."
            );
        }

        if (mes < 1 || mes > 12) {
            throw new IllegalArgumentException(
                    "El mes debe estar entre 1 y 12."
            );
        }
    }

    /**
     * Agrega el grupo principal y sus cuentas subordinadas.
     * Los saldos de cada cuenta se calculan independientemente.
     */
    private void agregarGrupo(
            List<FilaPyG> destino,
            List<PyGSinFormatoRepository.CuentaResultado> cuentas,
            Map<String, PyGComparadoService.FilaPyG> formato,
            String codigoGrupo
    ) {
        PyGComparadoService.FilaPyG grupo =
                formato.get(codigoGrupo);

        if (grupo == null) {
            throw new IllegalStateException(
                    "No existe la agrupación PyG " + codigoGrupo
            );
        }

        destino.add(new FilaPyG(
                grupo.codigo(),
                grupo.nombre(),
                "GRUPO",
                2,
                valor(grupo.periodo1())
        ));

        /*
         * Las cuentas subordinadas se muestran como detalle.
         * No se suman nuevamente al saldo del grupo principal.
         */
        cuentas.stream()
                .filter(cuenta -> cuenta.codigoCuenta() != null)
                .filter(cuenta ->
                        cuenta.codigoCuenta().startsWith(codigoGrupo)
                )
                .filter(cuenta ->
                        !cuenta.codigoCuenta().equals(codigoGrupo)
                )
                .filter(cuenta -> cuenta.nivelCuenta() > 2)
                .filter(cuenta -> saldo(cuenta).signum() != 0)
                .sorted((a, b) ->
                        a.codigoCuenta().compareTo(b.codigoCuenta())
                )
                .forEach(cuenta -> destino.add(new FilaPyG(
                        cuenta.codigoCuenta(),
                        cuenta.nombreCuenta(),
                        "CUENTA",
                        cuenta.nivelCuenta(),
                        saldo(cuenta)
                )));
    }

    /**
     * Incorpora los resultados T1, T2, T3, T4 y TT.
     * Utiliza los valores calculados por el PyG comparado.
     */
    private void agregarTotal(
            List<FilaPyG> destino,
            Map<String, PyGComparadoService.FilaPyG> formato,
            String codigo
    ) {
        PyGComparadoService.FilaPyG total =
                formato.get(codigo);

        if (total == null) {
            throw new IllegalStateException(
                    "No existe el resultado PyG " + codigo
            );
        }

        destino.add(new FilaPyG(
                total.codigo(),
                total.nombre(),
                total.tipo(),
                0,
                valor(total.periodo1())
        ));
    }

    /**
     * Calcula el saldo según la naturaleza contable.
     * Conserva el signo para reflejar movimientos contrarios.
     */
    private BigDecimal saldo(
            PyGSinFormatoRepository.CuentaResultado cuenta
    ) {
        BigDecimal debitos = valor(cuenta.debitos());
        BigDecimal creditos = valor(cuenta.creditos());

        return "D".equalsIgnoreCase(cuenta.naturalezaCuenta())
                ? debitos.subtract(creditos)
                : creditos.subtract(debitos);
    }

    /** Normaliza importes nulos sin alterar su signo. */
    private BigDecimal valor(BigDecimal importe) {
        return importe == null ? CERO : importe;
    }
}
