package co.assip.erp.general.service;

import co.assip.erp.general.dto.PrivilegiadoDTOs.*;

import java.util.List;

public interface PrivilegiadoService {
    // Nuevo: lista completa (sin filtro por directivo)
    List<PrivilegiadoListItemDTO> listAll();

    // Existente: lista por directivo
    List<PrivilegiadoListItemDTO> listByDirectivo(Integer idDirectivo);

    PrivilegiadoDetailDTO get(Integer id);
    Integer create(PrivilegiadoCreateRequest req, Integer userId);
    void update(Integer id, PrivilegiadoUpdateRequest req, Integer userId);
    void delete(Integer id);
}
