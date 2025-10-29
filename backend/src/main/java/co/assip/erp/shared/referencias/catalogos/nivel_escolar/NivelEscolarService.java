package co.assip.erp.shared.referencias.catalogos.nivel_escolar;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class NivelEscolarService {

    private final NivelEscolarRepository repository;

    public NivelEscolarService(NivelEscolarRepository repository) {
        this.repository = repository;
    }

    public List<NivelEscolar> listar() {
        return repository.findAll();
    }
}
