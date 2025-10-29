package co.assip.erp.shared.referencias.catalogos.tipo_empresa;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TipoEmpresaService {

    private final TipoEmpresaRepository repository;

    public TipoEmpresaService(TipoEmpresaRepository repository) {
        this.repository = repository;
    }

    public List<TipoEmpresa> listar() {
        return repository.findAll();
    }
}
