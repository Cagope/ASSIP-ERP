package co.assip.erp.general.service.impl;

import co.assip.erp.general.domain.DatosAgencia;
import co.assip.erp.general.domain.Parametro;
import co.assip.erp.general.dto.ParametroDTOs;
import co.assip.erp.general.mapper.ParametroMapper;
import co.assip.erp.general.repository.DatosAgenciaRepository;
import co.assip.erp.general.repository.ParametroRepository;
import co.assip.erp.general.service.ParametroService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Service
public class ParametroServiceImpl implements ParametroService {

    private final ParametroRepository repo;
    private final DatosAgenciaRepository agenciaRepo;

    public ParametroServiceImpl(ParametroRepository repo, DatosAgenciaRepository agenciaRepo) {
        this.repo = repo;
        this.agenciaRepo = agenciaRepo;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ParametroDTOs.ParametroListDTO> list(Integer idAgencia, String q, Integer codigo, Pageable pageable) {
        Page<Parametro> page;

        if (idAgencia != null) {
            DatosAgencia ag = agenciaRepo.findById(idAgencia)
                    .orElseThrow(() -> new NoSuchElementException("Agencia no encontrada"));

            if (codigo != null) {
                page = repo.findByAgenciaAndCodigoParametro(ag, codigo, pageable);
            } else if (q != null && !q.isBlank()) {
                page = repo.findByAgenciaAndNombreParametroContainingIgnoreCase(ag, q, pageable);
            } else {
                page = repo.findByAgencia(ag, pageable);
            }

        } else {
            if (codigo != null) {
                page = repo.findByCodigoParametro(codigo, pageable);
            } else if (q != null && !q.isBlank()) {
                page = repo.findByNombreParametroContainingIgnoreCase(q, pageable);
            } else {
                page = repo.findAll(pageable);
            }
        }

        return page.map(ParametroMapper::toListDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public ParametroDTOs.ParametroDetailDTO get(Integer idParametro) {
        Parametro p = repo.findById(idParametro)
                .orElseThrow(() -> new NoSuchElementException("Parámetro no encontrado"));
        return ParametroMapper.toDetailDTO(p);
    }

    @Override
    @Transactional
    public Integer create(ParametroDTOs.ParametroCreateRequest req, Integer userId) {
        DatosAgencia ag = agenciaRepo.findById(req.idAgencia())
                .orElseThrow(() -> new NoSuchElementException("Agencia no encontrada"));

        validarUnicidad(ag, req.codigoParametro(), null);

        Parametro p = new Parametro();
        p.setAgencia(ag);
        p.setCodigoParametro(req.codigoParametro());
        p.setNombreParametro(safe(req.nombreParametro(), 100));
        p.setValorParametro(req.valorParametro());
        p.setTipoValor(req.tipoValor());
        p.setFkSeguridadCreacion(userId);
        p.setFkSeguridadActualizacion(userId);
        p.setFechaCreacion(LocalDateTime.now());
        p.setFechaActualizacion(LocalDateTime.now());

        p = repo.save(p);
        return p.getIdParametro();
    }

    @Override
    @Transactional
    public void update(Integer idParametro, ParametroDTOs.ParametroUpdateRequest req, Integer userId) {
        Parametro p = repo.findById(idParametro)
                .orElseThrow(() -> new NoSuchElementException("Parámetro no encontrado"));

        DatosAgencia ag = agenciaRepo.findById(req.idAgencia())
                .orElseThrow(() -> new NoSuchElementException("Agencia no encontrada"));

        validarUnicidad(ag, req.codigoParametro(), idParametro);

        p.setAgencia(ag);
        p.setCodigoParametro(req.codigoParametro());
        p.setNombreParametro(safe(req.nombreParametro(), 100));
        p.setValorParametro(req.valorParametro());
        p.setTipoValor(req.tipoValor());
        p.setFkSeguridadActualizacion(userId);
        p.setFechaActualizacion(LocalDateTime.now());

        repo.save(p);
    }

    @Override
    @Transactional
    public void delete(Integer idParametro) {
        if (!repo.existsById(idParametro)) {
            throw new NoSuchElementException("Parámetro no encontrado");
        }
        repo.deleteById(idParametro);
    }

    private void validarUnicidad(DatosAgencia ag, Integer codigo, Integer idParametro) {
        if (idParametro == null) {
            if (repo.existsByAgenciaAndCodigoParametro(ag, codigo)) {
                throw new IllegalArgumentException("El código ya existe para esta agencia");
            }
        } else {
            if (repo.existsByAgenciaAndCodigoParametroAndIdParametroNot(ag, codigo, idParametro)) {
                throw new IllegalArgumentException("El código ya existe en otro registro de esta agencia");
            }
        }
    }

    private String safe(String v, int max) {
        if (v == null) return null;
        String t = v.trim();
        return t.length() > max ? t.substring(0, max) : t;
    }
}
