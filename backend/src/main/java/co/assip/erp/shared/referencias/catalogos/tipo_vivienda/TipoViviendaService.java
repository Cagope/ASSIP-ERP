package co.assip.erp.shared.referencias.catalogos.tipo_vivienda;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TipoViviendaService {

    private final TipoViviendaRepository repository;

    public TipoViviendaService(TipoViviendaRepository repository) {
        this.repository = repository;
    }

    public List<TipoVivienda> listar() {
        return repository.findAll();
    }
}
