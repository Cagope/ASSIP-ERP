package co.assip.erp.general.subzona;

import co.assip.erp.general.zona.Zona;
import co.assip.erp.general.zona.ZonaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubZonaService {

    private final SubZonaRepository repository;
    private final ZonaRepository zonaRepository;

    public SubZonaService(SubZonaRepository repository, ZonaRepository zonaRepository) {
        this.repository = repository;
        this.zonaRepository = zonaRepository;
    }

    public List<SubZona> listar() {
        return repository.findAll();
    }

    public SubZona obtenerPorId(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("SubZona no encontrada con id: " + id));
    }

    public SubZona guardar(SubZona subZona) {
        validarZona(subZona.getZona().getIdZona());
        return repository.save(subZona);
    }

    public SubZona actualizar(Integer id, SubZona entrada) {
        SubZona actual = obtenerPorId(id);

        if (entrada.getZona() != null) {
            validarZona(entrada.getZona().getIdZona());
            actual.setZona(entrada.getZona());
        }

        actual.setCodigoSubZona(entrada.getCodigoSubZona());
        actual.setNombreSubZona(entrada.getNombreSubZona());
        actual.setComentarioSubZona(entrada.getComentarioSubZona());

        return repository.save(actual);
    }

    public void eliminar(Integer id) {
        repository.deleteById(id);
    }

    private void validarZona(Integer idZona) {
        Zona zona = zonaRepository.findById(idZona)
                .orElseThrow(() -> new RuntimeException("Zona no encontrada con id: " + idZona));
    }
}
