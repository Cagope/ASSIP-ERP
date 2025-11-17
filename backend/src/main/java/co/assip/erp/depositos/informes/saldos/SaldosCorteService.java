package co.assip.erp.depositos.informes.saldos;

import co.assip.erp.depositos.informes.saldos.dto.SaldosCorteItemDTO;
import co.assip.erp.depositos.informes.saldos.dto.SaldosCorteResumenDTO;
import co.assip.erp.depositos.informes.saldos.repository.SaldosCorteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SaldosCorteService {

    private final SaldosCorteRepository repository;

    public SaldosCorteService(SaldosCorteRepository repository) {
        this.repository = repository;
    }

    // ============================================================
    // 🔍 CONSULTA DETALLADA (YA EXISTENTE)
    // ============================================================
    public List<SaldosCorteItemDTO> consultar(String fechaCorte, String agencia) {
        return repository.consultar(fechaCorte, agencia);
    }

    // ============================================================
    // 📊 RESUMEN GENERAL (YA EXISTENTE)
    // ============================================================
    public SaldosCorteResumenDTO resumen(List<SaldosCorteItemDTO> lista) {
        SaldosCorteResumenDTO r = new SaldosCorteResumenDTO();

        r.setTotalCuentas(lista.size());
        r.setTotalSaldos(
                lista.stream().mapToDouble(SaldosCorteItemDTO::getSaldoCorte).sum()
        );

        return r;
    }

    // ============================================================
    // 🆕 Resumen agrupado por agencia y forma de ahorro
    // ============================================================
    public List<Map<String, Object>> resumenPorAgencia(String fechaCorte) {
        return repository.resumenPorAgencia(fechaCorte);
    }
}
