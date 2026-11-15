
package co.assip.erp.contabilidad.informes.pyg.comparado;

import co.assip.erp.contabilidad.informes.pyg.sin_formato.PyGSinFormatoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Construye el PyG comparado con la estructura histórica ESTADOSPYG. */
@Service
@RequiredArgsConstructor
public class PyGComparadoService {
    private static final BigDecimal CERO = BigDecimal.ZERO;

    private final PyGSinFormatoService sinFormatoService;

    public record FilaPyG(
            String codigo,
            String nombre,
            String tipo,
            BigDecimal periodo1,
            BigDecimal periodo2,
            BigDecimal variacion,
            BigDecimal porcentaje
    ) {}

    public record ResultadoPyG(
            int idAgencia,
            int anoPeriodo1,
            int mesPeriodo1,
            int anoPeriodo2,
            int mesPeriodo2,
            List<FilaPyG> filas
    ) {}

    /**
     * Consulta dos meses independientes y construye sus renglones comparativos.
     * La autorización de agencia corresponde al controlador.
     */
    @Transactional(readOnly = true)
    public ResultadoPyG consultar(
            int idAgencia,
            int anoPeriodo1,
            int mesPeriodo1,
            int anoPeriodo2,
            int mesPeriodo2
    ) {
        var primero = sinFormatoService.consultar(
                idAgencia, anoPeriodo1, mesPeriodo1
        );

        var segundo = sinFormatoService.consultar(
                idAgencia, anoPeriodo2, mesPeriodo2
        );

        Map<String, BigDecimal> saldos1 = saldos(primero);
        Map<String, BigDecimal> saldos2 = saldos(segundo);

        List<FilaPyG> filas = new ArrayList<>();

        agregarGrupo(filas, "41", "INGRESOS OPERACIONALES", saldos1, saldos2);
        agregarGrupo(filas, "61", "COSTO DE VENTAS/OPERACIONAL", saldos1, saldos2);

        BigDecimal t1a = saldo(saldos1, "41").subtract(saldo(saldos1, "61"));
        BigDecimal t1b = saldo(saldos2, "41").subtract(saldo(saldos2, "61"));
        filas.add(fila("T1", "UTILIDAD BRUTA", "TOTAL", t1a, t1b));

        agregarGrupo(filas, "51", "GASTOS DE ADMINISTRACION", saldos1, saldos2);
        agregarGrupo(filas, "52", "GASTOS DE VENTAS", saldos1, saldos2);

        BigDecimal t2a = saldo(saldos1, "51").add(saldo(saldos1, "52"));
        BigDecimal t2b = saldo(saldos2, "51").add(saldo(saldos2, "52"));
        filas.add(fila("T2", "GASTOS OPERACIONALES", "TOTAL", t2a, t2b));

        BigDecimal t3a = t1a.subtract(t2a);
        BigDecimal t3b = t1b.subtract(t2b);
        filas.add(fila("T3", "UTILIDAD OPERACIONAL", "TOTAL", t3a, t3b));

        agregarGrupo(filas, "42", "Ingresos no operacionales", saldos1, saldos2);
        agregarGrupo(filas, "53", "Gastos no operacionales", saldos1, saldos2);
        agregarGrupo(filas, "54", "Utilidad Antes de Impuesto", saldos1, saldos2);

        BigDecimal t4a = saldo(saldos1, "42")
                .subtract(saldo(saldos1, "53"))
                .subtract(saldo(saldos1, "54"));

        BigDecimal t4b = saldo(saldos2, "42")
                .subtract(saldo(saldos2, "53"))
                .subtract(saldo(saldos2, "54"));

        filas.add(fila("T4", "UTILIDAD NO OPERACIONAL", "TOTAL", t4a, t4b));

        filas.add(fila(
                "TT",
                "UTILIDAD O PERDIDA DEL EJERCICIO",
                "RESULTADO",
                t3a.add(t4a),
                t3b.add(t4b)
        ));

        return new ResultadoPyG(
                idAgencia,
                anoPeriodo1,
                mesPeriodo1,
                anoPeriodo2,
                mesPeriodo2,
                List.copyOf(filas)
        );
    }

    /**
     * Obtiene los saldos de las cuentas agrupadoras.
     * No suma cuentas hijas sobre sus respectivas cuentas principales.
     */
    private Map<String, BigDecimal> saldos(
            PyGSinFormatoService.ResultadoPyG resultado
    ) {
        Map<String, BigDecimal> valores = new LinkedHashMap<>();

        for (var cuenta : resultado.cuentas()) {
            valores.put(
                    cuenta.codigoCuenta(),
                    cuenta.saldo() == null ? CERO : cuenta.saldo()
            );
        }

        return valores;
    }

    /** Obtiene el saldo de una cuenta; las cuentas inexistentes valen cero. */
    private BigDecimal saldo(Map<String, BigDecimal> saldos, String codigo) {
        return saldos.getOrDefault(codigo, CERO);
    }

    /**
     * Agrega una agrupación y sus cuentas de nivel 3 con movimiento.
     * Conserva el renglón principal aunque su saldo sea cero.
     */
    private void agregarGrupo(
            List<FilaPyG> filas,
            String codigo,
            String nombre,
            Map<String, BigDecimal> saldos1,
            Map<String, BigDecimal> saldos2
    ) {
        filas.add(fila(
                codigo,
                nombre,
                "GRUPO",
                saldo(saldos1, codigo),
                saldo(saldos2, codigo)
        ));
    }

    /** Calcula la variación absoluta y porcentual de un renglón. */
    private FilaPyG fila(
            String codigo,
            String nombre,
            String tipo,
            BigDecimal primero,
            BigDecimal segundo
    ) {
        BigDecimal variacion = primero.subtract(segundo);

        BigDecimal porcentaje = segundo.signum() == 0
                ? null
                : variacion.multiply(BigDecimal.valueOf(100))
                .divide(segundo.abs(), 2, RoundingMode.HALF_UP);

        return new FilaPyG(
                codigo,
                nombre,
                tipo,
                primero,
                segundo,
                variacion,
                porcentaje
        );
    }
}
