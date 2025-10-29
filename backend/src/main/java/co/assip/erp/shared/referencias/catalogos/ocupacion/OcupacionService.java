package co.assip.erp.shared.referencias.catalogos.ocupacion;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class OcupacionService {

    private final OcupacionRepository repository;

    public OcupacionService(OcupacionRepository repository) {
        this.repository = repository;
    }

    public List<Ocupacion> listar() {
        return repository.findAll();
    }
}
