package co.assip.erp.shared.referencias.catalogos.tipo_peps;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TipoPepsService {

    private final TipoPepsRepository repository;

    public TipoPepsService(TipoPepsRepository repository) {
        this.repository = repository;
    }

    public List<TipoPeps> listar() {
        return repository.findAll();
    }
}
