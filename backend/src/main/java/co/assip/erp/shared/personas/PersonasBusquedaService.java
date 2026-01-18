package co.assip.erp.shared.personas;

import co.assip.erp.shared.personas.dto.PersonaBusquedaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PersonasBusquedaService {

    private final PersonasBusquedaRepository repository;

    // =========================================================
    // 1) AUTOCOMPLETE (YA EXISTENTE)
    // =========================================================
    public List<PersonaBusquedaDTO> buscar(String q) {

        if (q == null) return Collections.emptyList();

        String txt = q.trim();
        if (txt.length() < 2) return Collections.emptyList(); // evita consultas inútiles

        return repository.buscar(txt, 30);
    }

    // =========================================================
    // 2) OBTENER PERSONA POR ID (NUEVO – PARA CARGA FORM)
    // =========================================================
    public PersonaBusquedaDTO obtenerPorId(Long id) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Id de persona no válido.");
        }

        return repository.obtenerPorId(id);
    }
}
