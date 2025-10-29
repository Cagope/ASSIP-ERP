package co.assip.erp.shared.referencias.catalogos.tipo_documento;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TipoDocumentoService {

    private final TipoDocumentoRepository repository;

    public TipoDocumentoService(TipoDocumentoRepository repository) {
        this.repository = repository;
    }

    public List<TipoDocumento> listar() {
        return repository.findAll();
    }
}
