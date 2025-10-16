package co.assip.erp.general.service.impl;

import co.assip.erp.general.domain.Privilegiado;
import co.assip.erp.general.dto.PrivilegiadoDTOs.*;
import co.assip.erp.general.mapper.PrivilegiadoMapper;
import co.assip.erp.general.repository.PrivilegiadoRepository;
import co.assip.erp.general.repository.DirectivoRepository;
import co.assip.erp.hojavida.repository.DatosPersonalesRepository;
import co.assip.erp.general.service.PrivilegiadoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class PrivilegiadoServiceImpl implements PrivilegiadoService {

    private final PrivilegiadoRepository repo;
    private final DirectivoRepository directivoRepo;
    private final DatosPersonalesRepository personaRepo;

    public PrivilegiadoServiceImpl(
            PrivilegiadoRepository repo,
            DirectivoRepository directivoRepo,
            DatosPersonalesRepository personaRepo
    ) {
        this.repo = repo;
        this.directivoRepo = directivoRepo;
        this.personaRepo = personaRepo;
    }

    // NUEVO: lista completa (sin filtro por directivo)
    @Override
    @Transactional(readOnly = true)
    public List<PrivilegiadoListItemDTO> listAll() {
        return repo.findAllListItems();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrivilegiadoListItemDTO> listByDirectivo(Integer idDirectivo) {
        return repo.findListItemsByDirectivo(idDirectivo);
    }

    @Override
    @Transactional(readOnly = true)
    public PrivilegiadoDetailDTO get(Integer id) {
        var e = repo.findById(id).orElseThrow(() -> notFound(id));
        return PrivilegiadoMapper.toDetail(e);
    }

    @Override
    public Integer create(PrivilegiadoCreateRequest req, Integer userId) {
        var dir = directivoRepo.findById(req.idDirectivo())
                .orElseThrow(() -> new IllegalArgumentException("Directivo no encontrado: " + req.idDirectivo()));
        var per = personaRepo.findById(req.idDatosPersonal())
                .orElseThrow(() -> new IllegalArgumentException("Persona no encontrada: " + req.idDatosPersonal()));

        var e = new Privilegiado();
        var now = LocalDateTime.now();

        PrivilegiadoMapper.applyCreate(e, req, dir, per, userId, now);
        var saved = repo.save(e);
        return saved.getIdPrivilegiado();
    }

    @Override
    public void update(Integer id, PrivilegiadoUpdateRequest req, Integer userId) {
        var e = repo.findById(id).orElseThrow(() -> notFound(id));

        var dir = directivoRepo.findById(req.idDirectivo())
                .orElseThrow(() -> new IllegalArgumentException("Directivo no encontrado: " + req.idDirectivo()));
        var per = personaRepo.findById(req.idDatosPersonal())
                .orElseThrow(() -> new IllegalArgumentException("Persona no encontrada: " + req.idDatosPersonal()));

        var now = LocalDateTime.now();

        PrivilegiadoMapper.applyUpdate(e, req, dir, per, userId, now);
        repo.save(e);
    }

    @Override
    public void delete(Integer id) {
        if (!repo.existsById(id)) throw notFound(id);
        repo.deleteById(id);
    }

    private RuntimeException notFound(Integer id) {
        return new IllegalArgumentException("Privilegiado no encontrado: id=" + id);
    }
}
