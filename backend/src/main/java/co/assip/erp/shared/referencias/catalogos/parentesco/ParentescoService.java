package co.assip.erp.shared.referencias.catalogos.parentesco;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ParentescoService {

    private final ParentescoRepository repository;

    public ParentescoService(ParentescoRepository repository) {
        this.repository = repository;
    }

    public List<Parentesco> listar() {
        return repository.findAll();
    }
}
