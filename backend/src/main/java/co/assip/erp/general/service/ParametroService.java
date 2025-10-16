package co.assip.erp.general.service;

import co.assip.erp.general.dto.ParametroDTOs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ParametroService {
    Page<ParametroDTOs.ParametroListDTO> list(Integer idAgencia, String q, Integer codigo, Pageable pageable);
    ParametroDTOs.ParametroDetailDTO get(Integer idParametro);
    Integer create(ParametroDTOs.ParametroCreateRequest req, Integer userId);
    void update(Integer idParametro, ParametroDTOs.ParametroUpdateRequest req, Integer userId);
    void delete(Integer idParametro);
}
