package co.assip.erp.shared.referencias.catalogos.tipo_directivo;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TipoDirectivoService {

    private final TipoDirectivoRepository repository;

    public TipoDirectivoService(TipoDirectivoRepository repository) {
        this.repository = repository;
    }

    public List<TipoDirectivo> listar() {
        return repository.findAll();
    }
}
