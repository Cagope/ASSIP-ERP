package co.assip.erp.shared.referencias.catalogos.sector_economico;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SectorEconomicoService {

    private final SectorEconomicoRepository repository;

    public SectorEconomicoService(SectorEconomicoRepository repository) {
        this.repository = repository;
    }

    public List<SectorEconomico> listar() {
        return repository.findAll();
    }
}
