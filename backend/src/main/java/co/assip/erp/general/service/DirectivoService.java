package co.assip.erp.general.service;

import co.assip.erp.general.dto.DirectivoDTOs.*;

import java.util.List;

public interface DirectivoService {
    List<DirectivoListItemDTO> list(String q);
    DirectivoDetailDTO get(Integer id);
    Integer create(DirectivoCreateRequest req, Integer userId);
    void update(Integer id, DirectivoUpdateRequest req, Integer userId);
    void delete(Integer id);
}
