package co.assip.erp.shared.referencias.catalogos.genero;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class GeneroService {

    private final GeneroRepository repository;

    public GeneroService(GeneroRepository repository) {
        this.repository = repository;
    }

    public List<Genero> listar() {
        return repository.findAll();
    }
}
