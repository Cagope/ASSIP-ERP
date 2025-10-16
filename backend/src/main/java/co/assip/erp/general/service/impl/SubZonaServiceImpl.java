package co.assip.erp.general.service.impl;

import co.assip.erp.general.domain.SubZona;
import co.assip.erp.general.domain.Zona;
import co.assip.erp.general.dto.SubZonaDTOs;
import co.assip.erp.general.mapper.SubZonaMapper;
import co.assip.erp.general.repository.SubZonaRepository;
import co.assip.erp.general.repository.ZonaRepository;
import co.assip.erp.general.service.SubZonaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Service
public class SubZonaServiceImpl implements SubZonaService {

    private final SubZonaRepository repo;
    private final ZonaRepository zonaRepo;

    public SubZonaServiceImpl(SubZonaRepository repo, ZonaRepository zonaRepo) {
        this.repo = repo;
        this.zonaRepo = zonaRepo;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SubZonaDTOs.SubZonaListDTO> list(Integer idZona, String q, Pageable pageable) {
        Page<SubZona> page;
        if (idZona != null) {
            Zona z = zonaRepo.findById(idZona).orElseThrow(() -> new NoSuchElementException("Zona no encontrada"));
            if (q == null || q.isBlank()) {
                page = repo.findByZona(z, pageable);
            } else {
                page = repo.findByZonaAndNombreSubZonaContainingIgnoreCase(z, q, pageable);
                if (page.isEmpty()) {
                    page = repo.findByZonaAndCodigoSubZonaContainingIgnoreCase(z, q, pageable);
                }
            }
        } else {
            if (q == null || q.isBlank()) {
                page = repo.findAll(pageable);
            } else {
                page = repo.findByNombreSubZonaContainingIgnoreCase(q, pageable);
                if (page.isEmpty()) {
                    page = repo.findByCodigoSubZonaContainingIgnoreCase(q, pageable);
                }
            }
        }
        // Se mapea dentro de la transacción para resolver LAZY (zona.nombreZona)
        return page.map(SubZonaMapper::toListDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public SubZonaDTOs.SubZonaDetailDTO get(Integer idSubZona) {
        SubZona s = repo.findById(idSubZona).orElseThrow(() -> new NoSuchElementException("Sub zona no encontrada"));
        // Se mapea dentro de la transacción para resolver LAZY
        return SubZonaMapper.toDetailDTO(s);
    }

    @Override
    @Transactional
    public Integer create(SubZonaDTOs.SubZonaCreateRequest req, Integer userId) {
        Zona z = zonaRepo.findById(req.idZona())
                .orElseThrow(() -> new NoSuchElementException("Zona no encontrada"));

        validarUnicidadCodigo(z, req.codigoSubZona(), null);

        SubZona s = new SubZona();
        s.setZona(z);
        s.setCodigoSubZona(safe(req.codigoSubZona(), 3));
        s.setNombreSubZona(safe(req.nombreSubZona(), 100));
        s.setComentarioSubZona(safe(req.comentarioSubZona(), 100));
        s.setFkSeguridadCreacion(userId);
        s.setFkSeguridadActualizacion(userId);
        s.setFechaCreacion(LocalDateTime.now());
        s.setFechaActualizacion(LocalDateTime.now());

        s = repo.save(s);
        return s.getIdSubZona();
    }

    @Override
    @Transactional
    public void update(Integer idSubZona, SubZonaDTOs.SubZonaUpdateRequest req, Integer userId) {
        SubZona s = repo.findById(idSubZona)
                .orElseThrow(() -> new NoSuchElementException("Sub zona no encontrada"));

        Zona z = zonaRepo.findById(req.idZona())
                .orElseThrow(() -> new NoSuchElementException("Zona no encontrada"));

        validarUnicidadCodigo(z, req.codigoSubZona(), idSubZona);

        s.setZona(z);
        s.setCodigoSubZona(safe(req.codigoSubZona(), 3));
        s.setNombreSubZona(safe(req.nombreSubZona(), 100));
        s.setComentarioSubZona(safe(req.comentarioSubZona(), 100));
        s.setFkSeguridadActualizacion(userId);
        s.setFechaActualizacion(LocalDateTime.now());

        repo.save(s);
    }

    @Override
    @Transactional
    public void delete(Integer idSubZona) {
        if (!repo.existsById(idSubZona)) {
            throw new NoSuchElementException("Sub zona no encontrada");
        }
        repo.deleteById(idSubZona);
    }

    private void validarUnicidadCodigo(Zona z, String codigo, Integer idSubZona) {
        if (codigo == null || codigo.isBlank()) return;
        if (idSubZona == null) {
            if (repo.existsByZonaAndCodigoSubZona(z, codigo)) {
                throw new IllegalArgumentException("El código ya existe para esta zona");
            }
        } else {
            if (repo.existsByZonaAndCodigoSubZonaAndIdSubZonaNot(z, codigo, idSubZona)) {
                throw new IllegalArgumentException("El código ya existe en otro registro de esta zona");
            }
        }
    }

    private String safe(String v, int max) {
        if (v == null) return null;
        String t = v.trim();
        return t.length() > max ? t.substring(0, max) : t;
    }
}
