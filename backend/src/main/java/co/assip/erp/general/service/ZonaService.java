package co.assip.erp.general.service;

import co.assip.erp.general.dto.ZonaDTOs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ZonaService {
    Page<ZonaDTOs.ZonaListDTO> list(String q, Pageable pageable);
    ZonaDTOs.ZonaDetailDTO get(Integer idZona);
    Integer create(ZonaDTOs.ZonaCreateRequest req, Integer userId);
    void update(Integer idZona, ZonaDTOs.ZonaUpdateRequest req, Integer userId);
    void delete(Integer idZona);
}
