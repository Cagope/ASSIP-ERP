package co.assip.erp.shared.referencias.catalogos.tipo_bien;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TipoBienService {

    private final TipoBienRepository repository;

    public TipoBienService(TipoBienRepository repository) {
        this.repository = repository;
    }

    public List<TipoBien> listar() {
        return repository.findAll();
    }
}
