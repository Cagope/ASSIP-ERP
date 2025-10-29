package co.assip.erp.shared.referencias.catalogos.estado_civil;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class EstadoCivilService {

    private final EstadoCivilRepository repository;

    public EstadoCivilService(EstadoCivilRepository repository) {
        this.repository = repository;
    }

    public List<EstadoCivil> listar() {
        return repository.findAll();
    }
}
