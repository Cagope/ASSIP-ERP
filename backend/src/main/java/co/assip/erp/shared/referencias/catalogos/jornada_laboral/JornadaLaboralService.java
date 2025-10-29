package co.assip.erp.shared.referencias.catalogos.jornada_laboral;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class JornadaLaboralService {

    private final JornadaLaboralRepository repository;

    public JornadaLaboralService(JornadaLaboralRepository repository) {
        this.repository = repository;
    }

    public List<JornadaLaboral> listar() {
        return repository.findAll();
    }
}
