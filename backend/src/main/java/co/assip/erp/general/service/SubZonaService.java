package co.assip.erp.general.service;

import co.assip.erp.general.dto.SubZonaDTOs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SubZonaService {
    Page<SubZonaDTOs.SubZonaListDTO> list(Integer idZona, String q, Pageable pageable);
    SubZonaDTOs.SubZonaDetailDTO get(Integer idSubZona);
    Integer create(SubZonaDTOs.SubZonaCreateRequest req, Integer userId);
    void update(Integer idSubZona, SubZonaDTOs.SubZonaUpdateRequest req, Integer userId);
    void delete(Integer idSubZona);
}
