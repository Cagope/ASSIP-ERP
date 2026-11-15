package co.assip.erp.contabilidad.informes.oficiales.caja_diario;

import co.assip.erp.seguridad.sesion.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/** Prepara el libro y su resumen usando exactamente el mismo conjunto de movimientos. */
@Service
@RequiredArgsConstructor
public class CajaDiarioService {
    private final CajaDiarioRepository repository;
    public record Filtro(Integer idAgencia, Integer ano, Integer mes, Integer nivel, Boolean acumuladoAnual) {}
    public record Fila(LocalDate fecha, String codigo, String nombre, BigDecimal debito, BigDecimal credito) {}
    public record TotalDia(LocalDate fecha, BigDecimal debito, BigDecimal credito) {}
    public record Resultado(int idAgencia, String codigoAgencia, String nombreAgencia, int ano, int mes,
                            int nivel, boolean acumuladoAnual, LocalDate desde, LocalDate hasta,
                            List<Fila> diario, List<TotalDia> totalesDias, List<Fila> resumen,
                            BigDecimal totalDebito, BigDecimal totalCredito, BigDecimal diferencia) {}
    /** Valida agencia, fechas y nivel antes de consultar los movimientos vigentes. */
    @Transactional(readOnly = true)
    public Resultado consultar(Filtro filtro) {
        if (filtro == null || filtro.idAgencia() == null || filtro.idAgencia() <= 0 || filtro.idAgencia() == 1)
            throw new IllegalArgumentException("Seleccione una agencia operativa válida.");
        if (SecurityUtils.getAgencias() == null || !SecurityUtils.getAgencias().contains(filtro.idAgencia()))
            throw new AccessDeniedException("No tiene autorización para esta agencia.");
        if (filtro.ano() == null || filtro.ano() < 1900 || filtro.ano() > 9999 ||
            filtro.mes() == null || filtro.mes() < 1 || filtro.mes() > 12 ||
            filtro.nivel() == null || filtro.nivel() < 1 || filtro.nivel() > 6)
            throw new IllegalArgumentException("Año, mes o nivel contable inválido.");
        boolean anual = Boolean.TRUE.equals(filtro.acumuladoAnual());
        if (anual && filtro.ano() == 9999)
            throw new IllegalArgumentException("El año 9999 no admite consulta anual.");
        LocalDate inicioMes = LocalDate.of(filtro.ano(), filtro.mes(), 1);
        LocalDate desde = anual ? LocalDate.of(filtro.ano(), 1, 1) : inicioMes;
        LocalDate finExclusivo = anual ? desde.plusYears(1) : inicioMes.plusMonths(1);
        int longitud = switch (filtro.nivel()) {case 1 -> 1; case 2 -> 2; case 3 -> 4;
            case 4 -> 6; case 5 -> 8; default -> 11;};
        var agencia = repository.agencia(filtro.idAgencia());
        var movimientos = repository.consultar(filtro.idAgencia(), desde, finExclusivo, longitud);
        List<Fila> diario = new ArrayList<>();
        LinkedHashMap<LocalDate, BigDecimal[]> dias = new LinkedHashMap<>();
        LinkedHashMap<String, Fila> cuentas = new LinkedHashMap<>();
        BigDecimal debito = BigDecimal.ZERO, credito = BigDecimal.ZERO;
        for (var m : movimientos) {
            diario.add(new Fila(m.fecha(), m.codigo(), m.nombre(), m.debito(), m.credito()));
            var d = dias.computeIfAbsent(m.fecha(), k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            d[0] = d[0].add(m.debito()); d[1] = d[1].add(m.credito());
            var anterior = cuentas.get(m.codigo());
            cuentas.put(m.codigo(), anterior == null
                ? new Fila(null, m.codigo(), m.nombre(), m.debito(), m.credito())
                : new Fila(null, m.codigo(), m.nombre(), anterior.debito().add(m.debito()),
                           anterior.credito().add(m.credito())));
            debito = debito.add(m.debito()); credito = credito.add(m.credito());
        }
        List<TotalDia> totales = dias.entrySet().stream()
            .map(e -> new TotalDia(e.getKey(), e.getValue()[0], e.getValue()[1])).toList();
        List<Fila> resumen = cuentas.values().stream()
            .sorted(java.util.Comparator.comparing(Fila::codigo)).toList();
        return new Resultado(filtro.idAgencia(), agencia.codigo(), agencia.nombre(), filtro.ano(),
            filtro.mes(), filtro.nivel(), anual, desde, finExclusivo.minusDays(1),
            List.copyOf(diario), totales, resumen, debito, credito, debito.subtract(credito));
    }
}
