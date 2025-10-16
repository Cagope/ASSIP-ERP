package co.assip.erp.general.service;

import co.assip.erp.general.dto.AgenciaDTOs.*;

import java.util.List;

public interface AgenciaService {
    List<AgenciaListItemDTO> list(String q);
    AgenciaDetailDTO get(Integer id);
    Integer create(AgenciaCreateRequest req, Integer userId);
    void update(Integer id, AgenciaUpdateRequest req, Integer userId);
    void delete(Integer id);
}
