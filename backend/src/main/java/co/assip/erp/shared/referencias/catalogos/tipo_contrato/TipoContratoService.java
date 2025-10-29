package co.assip.erp.shared.referencias.catalogos.tipo_contrato;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TipoContratoService {

    private final TipoContratoRepository repository;

    public TipoContratoService(TipoContratoRepository repository) {
        this.repository = repository;
    }

    public List<TipoContrato> listar() {
        return repository.findAll();
    }
}
