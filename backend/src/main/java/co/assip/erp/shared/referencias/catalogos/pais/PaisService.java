package co.assip.erp.shared.referencias.catalogos.pais;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PaisService {
    private final PaisRepository repository;

    public PaisService(PaisRepository repository) {
        this.repository = repository;
    }

    public List<Pais> listar() {
        return repository.findAll();
    }
}
