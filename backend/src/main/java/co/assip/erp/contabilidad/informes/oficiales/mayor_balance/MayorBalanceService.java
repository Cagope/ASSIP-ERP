package co.assip.erp.contabilidad.informes.oficiales.mayor_balance;

import co.assip.erp.seguridad.sesion.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Construye el Mayor y Balance con saldos iniciales, movimientos y saldos finales. */
@Service
@RequiredArgsConstructor
public class MayorBalanceService {
    private final MayorBalanceRepository repository;
    private static final BigDecimal CERO = BigDecimal.ZERO;
    public record Filtro(Integer idAgencia, Integer ano, Integer mes, Boolean acumuladoAnual) {}
    public record Fila(String codigo, String nombre, String naturaleza,
                       BigDecimal debitoAnterior, BigDecimal creditoAnterior,
                       BigDecimal debitoMovimiento, BigDecimal creditoMovimiento,
                       BigDecimal debitoSaldo, BigDecimal creditoSaldo) {}
    public record Totales(BigDecimal debitoAnterior, BigDecimal creditoAnterior,
                          BigDecimal debitoMovimiento, BigDecimal creditoMovimiento,
                          BigDecimal debitoSaldo, BigDecimal creditoSaldo) {}
    public record Resultado(int idAgencia, String codigoAgencia, String nombreAgencia,
                            int ano, int mes, boolean acumuladoAnual, int nivel,
                            LocalDate desde, LocalDate hasta, List<Fila> filas, Totales totales,
                            BigDecimal diferenciaMovimiento, BigDecimal diferenciaSaldo) {}

    private static BigDecimal valor(BigDecimal n) { return n == null ? CERO : n; }
    private static BigDecimal[] columnas(String naturaleza, BigDecimal neto) {
        if ("D".equals(naturaleza)) return new BigDecimal[]{neto, CERO};
        if ("C".equals(naturaleza)) return new BigDecimal[]{CERO, neto.negate()};
        throw new IllegalStateException("Naturaleza contable inválida: " + naturaleza);
    }
    @Transactional(readOnly = true)
    public Resultado consultar(Filtro filtro) {
        if (filtro == null || filtro.idAgencia() == null || filtro.idAgencia() <= 0 || filtro.idAgencia() == 1)
            throw new IllegalArgumentException("Seleccione una agencia operativa válida.");
        if (SecurityUtils.getAgencias() == null || !SecurityUtils.getAgencias().contains(filtro.idAgencia()))
            throw new AccessDeniedException("No tiene autorización para esta agencia.");
        if (filtro.ano() == null || filtro.ano() < 1901 || filtro.ano() > 9998 ||
            filtro.mes() == null || filtro.mes() < 1 || filtro.mes() > 12)
            throw new IllegalArgumentException("Año o mes inválido.");
        boolean anual = Boolean.TRUE.equals(filtro.acumuladoAnual());
        int mesFinal = anual ? 12 : filtro.mes();
        if (!repository.existeCierreMensual(filtro.idAgencia(), filtro.ano(), mesFinal))
            throw new IllegalStateException("El período contable solicitado no tiene cierre mensual consolidado.");
        if (anual || mesFinal == 1) {
            if (!repository.existeCierreAnual(filtro.idAgencia(), filtro.ano() - 1))
                throw new IllegalStateException("No existe el cierre anual anterior requerido para el saldo inicial.");
        } else if (!repository.existeCierreMensual(filtro.idAgencia(), filtro.ano(), mesFinal - 1)) {
            throw new IllegalStateException("No existe el cierre del mes anterior requerido para el saldo inicial.");
        }
        var agencia = repository.agencia(filtro.idAgencia());
        List<Fila> filas = new ArrayList<>();
        BigDecimal[] totales = {CERO,CERO,CERO,CERO,CERO,CERO};
        for (var c : repository.consultar(filtro.idAgencia(), filtro.ano(), mesFinal, anual)) {
            BigDecimal inicialNeto = valor(c.debitoInicial()).subtract(valor(c.creditoInicial()));
            BigDecimal finalNeto = valor(c.debitoFinal()).subtract(valor(c.creditoFinal()));
            BigDecimal debitoMov = valor(c.debitoFinal()).subtract(valor(c.debitoInicial()));
            BigDecimal creditoMov = valor(c.creditoFinal()).subtract(valor(c.creditoInicial()));
            if (inicialNeto.signum() == 0 && finalNeto.signum() == 0 &&
                debitoMov.signum() == 0 && creditoMov.signum() == 0) continue;
            var inicial = columnas(c.naturaleza(), inicialNeto);
            var finalSaldo = columnas(c.naturaleza(), finalNeto);
            BigDecimal[] v = {inicial[0], inicial[1], debitoMov, creditoMov, finalSaldo[0], finalSaldo[1]};
            for (int j=0; j<6; j++) totales[j] = totales[j].add(v[j]);
            filas.add(new Fila(c.codigo(), c.nombre(), c.naturaleza(),
                v[0],v[1],v[2],v[3],v[4],v[5]));
        }
        LocalDate desde = LocalDate.of(filtro.ano(), anual ? 1 : mesFinal, 1);
        LocalDate hasta = LocalDate.of(filtro.ano(), mesFinal, 1).plusMonths(1).minusDays(1);
        Totales t = new Totales(totales[0],totales[1],totales[2],totales[3],totales[4],totales[5]);
        return new Resultado(filtro.idAgencia(), agencia.codigo(), agencia.nombre(),
            filtro.ano(), filtro.mes(), anual, 3, desde, hasta, List.copyOf(filas), t,
            totales[2].subtract(totales[3]), totales[4].subtract(totales[5]));
    }
}
