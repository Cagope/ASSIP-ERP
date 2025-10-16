package co.assip.erp.general.service.impl;

import co.assip.erp.general.domain.Directivo;
import co.assip.erp.general.dto.DirectivoDTOs.*;
import co.assip.erp.general.mapper.DirectivoMapper;
import co.assip.erp.general.repository.DirectivoRepository;
import co.assip.erp.general.service.DirectivoService;
import co.assip.erp.hojavida.repository.DatosPersonalesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class DirectivoServiceImpl implements DirectivoService {

    private final DirectivoRepository repo;
    private final DatosPersonalesRepository personasRepo;

    public DirectivoServiceImpl(DirectivoRepository repo, DatosPersonalesRepository personasRepo) {
        this.repo = repo;
        this.personasRepo = personasRepo;
    }

    @Override @Transactional(readOnly = true)
    public List<DirectivoListItemDTO> list(String q) {
        if (q == null || q.isBlank()) return repo.findListItems();
        return repo.searchListItemsByPersona(q.trim());
    }

    @Override @Transactional(readOnly = true)
    public DirectivoDetailDTO get(Integer id) {
        var e = repo.findById(id).orElseThrow(() -> notFound(id));
        return DirectivoMapper.toDetail(e);
    }

    @Override
    public Integer create(DirectivoCreateRequest req, Integer userId) {
        var persona = personasRepo.findById(req.idDatosPersonal())
                .orElseThrow(() -> new IllegalArgumentException("Persona no encontrada: id=" + req.idDatosPersonal()));

        var e = new Directivo();
        e.setPersona(persona);
        e.setCodigoTipoDirectivo(req.codigoTipoDirectivo());
        e.setCalidadDirectivo(req.calidadDirectivo());
        e.setEstadoDirectivo(req.estadoDirectivo());
        e.setActaAsamblea(req.actaAsamblea());
        e.setFechaAsamblea(req.fechaAsamblea());
        e.setResolucionSes(req.resolucionSes());
        e.setFechaResolucion(req.fechaResolucion());
        e.setFechaRetiro(req.fechaRetiro());
        e.setPeriodosVigencia(req.periodosVigencia() == null ? 0 : req.periodosVigencia());

        // Reglas simples
        if (!"1".equals(e.getEstadoDirectivo())) {
            // si no está nombrado, puede o no tener fechaRetiro; no forzamos aquí
        }

        // Auditoría
        e.setFkSeguridadCreacion(userId);
        e.setFkSeguridadActualizacion(userId);
        e.setFechaCreacion(LocalDateTime.now());
        e.setFechaActualizacion(LocalDateTime.now());

        var saved = repo.save(e);
        return saved.getIdDirectivo();
    }

    @Override
    public void update(Integer id, DirectivoUpdateRequest req, Integer userId) {
        var e = repo.findById(id).orElseThrow(() -> notFound(id));

        if (!e.getPersona().getIdDatosPersonal().equals(req.idDatosPersonal())) {
            var persona = personasRepo.findById(req.idDatosPersonal())
                    .orElseThrow(() -> new IllegalArgumentException("Persona no encontrada: id=" + req.idDatosPersonal()));
            e.setPersona(persona);
        }

        e.setCodigoTipoDirectivo(req.codigoTipoDirectivo());
        e.setCalidadDirectivo(req.calidadDirectivo());
        e.setEstadoDirectivo(req.estadoDirectivo());
        e.setActaAsamblea(req.actaAsamblea());
        e.setFechaAsamblea(req.fechaAsamblea());
        e.setResolucionSes(req.resolucionSes());
        e.setFechaResolucion(req.fechaResolucion());
        e.setFechaRetiro(req.fechaRetiro());
        e.setPeriodosVigencia(req.periodosVigencia() == null ? 0 : req.periodosVigencia());

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
        return new IllegalArgumentException("Directivo no encontrado: id=" + id);
    }
}
