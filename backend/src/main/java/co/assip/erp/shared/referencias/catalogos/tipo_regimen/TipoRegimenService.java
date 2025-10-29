package co.assip.erp.shared.referencias.catalogos.tipo_regimen;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TipoRegimenService {

    private final TipoRegimenRepository repository;

    public TipoRegimenService(TipoRegimenRepository repository) {
        this.repository = repository;
    }

    public List<TipoRegimen> listar() {
        return repository.findAll();
    }
}
