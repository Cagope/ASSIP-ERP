package co.assip.erp.shared.referencias.catalogos.actividad_economica_dian;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ActividadEconomicaDianService {

    private final ActividadEconomicaDianRepository repository;

    public ActividadEconomicaDianService(ActividadEconomicaDianRepository repository) {
        this.repository = repository;
    }

    public List<ActividadEconomicaDian> listar() {
        return repository.findAll();
    }
}
