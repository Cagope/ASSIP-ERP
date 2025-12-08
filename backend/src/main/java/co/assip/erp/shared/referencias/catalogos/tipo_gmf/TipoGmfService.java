package co.assip.erp.shared.referencias.catalogos.tipo_gmf;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TipoGmfService {

    private final TipoGmfRepository repository;

    public List<TipoGmf> listar() {
        return repository.listar();
    }
}
