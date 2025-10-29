package co.assip.erp.shared.referencias.catalogos.actividad_economica_ses;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ActividadEconomicaSesService {

    private final ActividadEconomicaSesRepository repository;

    public ActividadEconomicaSesService(ActividadEconomicaSesRepository repository) {
        this.repository = repository;
    }

    public List<ActividadEconomicaSes> listar() {
        return repository.findAll();
    }
}
