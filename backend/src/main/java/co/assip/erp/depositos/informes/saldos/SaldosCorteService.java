package co.assip.erp.depositos.informes.saldos;

import co.assip.erp.depositos.informes.saldos.dto.SaldosCorteItemDTO;
import co.assip.erp.depositos.informes.saldos.dto.SaldosCorteResumenDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SaldosCorteService {

    private final SaldosCorteRepository repository;

    public SaldosCorteService(
            SaldosCorteRepository repository
    ) {
        this.repository = repository;
    }

    public List<SaldosCorteItemDTO> consultar(
            String fechaCorte,
            String agencia
    ) {
        return repository.consultar(
                fechaCorte,
                agencia
        );
    }

    public SaldosCorteResumenDTO resumen(
            List<SaldosCorteItemDTO> lista
    ) {

        SaldosCorteResumenDTO r =
                new SaldosCorteResumenDTO();

        double totalDebitos =
                lista.stream()
                        .mapToDouble(x -> x.getTotalDebitos() == null ? 0 : x.getTotalDebitos())
                        .sum();

        double totalCreditos =
                lista.stream()
                        .mapToDouble(x -> x.getTotalCreditos() == null ? 0 : x.getTotalCreditos())
                        .sum();

        double totalSaldos =
                lista.stream()
                        .mapToDouble(x -> x.getSaldoCorte() == null ? 0 : x.getSaldoCorte())
                        .sum();

        r.setTotalCuentas(lista.size());
        r.setTotalDebitos(totalDebitos);
        r.setTotalCreditos(totalCreditos);
        r.setTotalSaldos(totalSaldos);

        r.setSaldoPromedio(
                lista.isEmpty()
                        ? 0
                        : totalSaldos / lista.size()
        );

        return r;
    }

    public List<Map<String, Object>> resumenPorAgencia(
            String fechaCorte,
            String agencia
    ) {
        return repository.resumenPorAgencia(
                fechaCorte,
                agencia
        );
    }

}