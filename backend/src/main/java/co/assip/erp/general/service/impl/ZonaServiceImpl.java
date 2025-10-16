package co.assip.erp.general.service.impl;

import co.assip.erp.general.domain.Zona;
import co.assip.erp.general.dto.ZonaDTOs;
import co.assip.erp.general.mapper.ZonaMapper;
import co.assip.erp.general.repository.ZonaRepository;
import co.assip.erp.general.service.ZonaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Service
public class ZonaServiceImpl implements ZonaService {

    private final ZonaRepository repo;

    public ZonaServiceImpl(ZonaRepository repo) {
        this.repo = repo;
    }

    @Override
    public Page<ZonaDTOs.ZonaListDTO> list(String q, Pageable pageable) {
        Page<Zona> page;
        if (q == null || q.isBlank()) {
            page = repo.findAll(pageable);
        } else {
            // Busca por nombre y por código; prioriza nombre si trae resultados
            page = repo.findByNombreZonaContainingIgnoreCase(q, pageable);
            if (page.isEmpty()) {
                page = repo.findByCodigoZonaContainingIgnoreCase(q, pageable);
            }
        }
        return page.map(ZonaMapper::toListDTO);
    }

    @Override
    public ZonaDTOs.ZonaDetailDTO get(Integer idZona) {
        Zona z = repo.findById(idZona).orElseThrow(() -> new NoSuchElementException("Zona no encontrada"));
        return ZonaMapper.toDetailDTO(z);
    }

    @Override
    public Integer create(ZonaDTOs.ZonaCreateRequest req, Integer userId) {
        validarUnicidadCodigo(req.codigoZona(), null);

        Zona z = new Zona();
        z.setCodigoZona(safe(req.codigoZona(), 3));
        z.setNombreZona(safe(req.nombreZona(), 100));
        z.setComentarioZona(safe(req.comentarioZona(), 100));
        z.setFkSeguridadCreacion(userId);
        z.setFkSeguridadActualizacion(userId);
        z.setFechaCreacion(LocalDateTime.now());
        z.setFechaActualizacion(LocalDateTime.now());

        z = repo.save(z);
        return z.getIdZona();
    }

    @Override
    public void update(Integer idZona, ZonaDTOs.ZonaUpdateRequest req, Integer userId) {
        Zona z = repo.findById(idZona).orElseThrow(() -> new NoSuchElementException("Zona no encontrada"));

        validarUnicidadCodigo(req.codigoZona(), idZona);

        z.setCodigoZona(safe(req.codigoZona(), 3));
        z.setNombreZona(safe(req.nombreZona(), 100));
        z.setComentarioZona(safe(req.comentarioZona(), 100));
        z.setFkSeguridadActualizacion(userId);
        z.setFechaActualizacion(LocalDateTime.now());
        repo.save(z);
    }

    @Override
    public void delete(Integer idZona) {
        if (!repo.existsById(idZona)) {
            throw new NoSuchElementException("Zona no encontrada");
        }
        repo.deleteById(idZona);
    }

    private void validarUnicidadCodigo(String codigo, Integer idZona) {
        if (codigo == null || codigo.isBlank()) return;
        if (idZona == null) {
            if (repo.existsByCodigoZona(codigo)) {
                throw new IllegalArgumentException("El código de zona ya existe");
            }
        } else {
            if (repo.existsByCodigoZonaAndIdZonaNot(codigo, idZona)) {
                throw new IllegalArgumentException("El código de zona ya existe en otro registro");
            }
        }
    }

    private String safe(String v, int max) {
        if (v == null) return null;
        return v.length() > max ? v.substring(0, max) : v.trim();
    }
}
