package co.assip.erp.general.zona;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ZonaService {

    private final ZonaRepository repository;

    public ZonaService(ZonaRepository repository) {
        this.repository = repository;
    }

    public List<Zona> listar() {
        return repository.findAll();
    }

    public Zona obtenerPorId(Integer id) {
        return repository.findById(id).orElseThrow(
                () -> new RuntimeException("Zona no encontrada con id: " + id)
        );
    }

    public Zona guardar(Zona zona) {
        return repository.save(zona);
    }

    public Zona actualizar(Integer id, Zona entrada) {
        Zona actual = obtenerPorId(id);

        actual.setCodigoZona(entrada.getCodigoZona());
        actual.setNombreZona(entrada.getNombreZona());
        actual.setComentarioZona(entrada.getComentarioZona());

        // Auditoría automática por @PreUpdate (si se usa BaseAudit)
        return repository.save(actual);
    }

    public void eliminar(Integer id) {
        repository.deleteById(id);
    }
}
