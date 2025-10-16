package co.assip.erp.general.service.impl;

import co.assip.erp.general.domain.DatosAgencia;
import co.assip.erp.general.dto.AgenciaDTOs.*;
import co.assip.erp.general.mapper.AgenciaMapper;
import co.assip.erp.general.repository.DatosAgenciaRepository;
import co.assip.erp.general.service.AgenciaService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AgenciaServiceImpl implements AgenciaService {

    private final DatosAgenciaRepository repo;

    public AgenciaServiceImpl(DatosAgenciaRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AgenciaListItemDTO> list(String q) {
        if (q == null || q.isBlank()) {
            // Devuelve DTO ya armado (sigla correcta)
            return repo.findListItems();
        }
        // Búsqueda en DTO (evita mapear manual y mantiene sigla correcta)
        return repo.searchListItemsByNombre(q.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public AgenciaDetailDTO get(Integer id) {
        var e = repo.findById(id).orElseThrow(() -> notFound(id));
        return AgenciaMapper.toDetail(e);
    }

    @Override
    public Integer create(AgenciaCreateRequest req, Integer userId) {
        if (repo.existsByCodigoAgenciaIgnoreCase(req.codigoAgencia())) {
            throw new DataIntegrityViolationException("El código de agencia ya existe: " + req.codigoAgencia());
        }
        var e = new DatosAgencia();
        AgenciaMapper.applyCreate(e, req);

        // Auditoría
        e.setFkSeguridadCreacion(userId);
        e.setFkSeguridadActualizacion(userId);
        e.setFechaCreacion(LocalDateTime.now());
        e.setFechaActualizacion(LocalDateTime.now());

        var saved = repo.save(e);
        return saved.getIdAgencia();
    }

    @Override
    public void update(Integer id, AgenciaUpdateRequest req, Integer userId) {
        var e = repo.findById(id).orElseThrow(() -> notFound(id));

        if (!e.getCodigoAgencia().equalsIgnoreCase(req.codigoAgencia())
                && repo.existsByCodigoAgenciaIgnoreCase(req.codigoAgencia())) {
            throw new DataIntegrityViolationException("El código de agencia ya existe: " + req.codigoAgencia());
        }

        AgenciaMapper.applyUpdate(e, req);

        // Auditoría
        e.setFkSeguridadActualizacion(userId);
        e.setFechaActualizacion(LocalDateTime.now());

        repo.save(e);
    }

    @Override
    public void delete(Integer id) {
        if (!repo.existsById(id)) throw notFound(id);
        repo.deleteById(id);
    }

    private RuntimeException notFound(Integer id) {
        return new IllegalArgumentException("Agencia no encontrada: id=" + id);
    }
}
