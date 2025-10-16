package co.assip.erp.hojavida.service;

import co.assip.erp.hojavida.dto.DatosFamiliaresDTOs.FamiliarListItem;
import co.assip.erp.hojavida.dto.DatosFamiliaresDTOs.FamiliarDetail;
import co.assip.erp.hojavida.dto.DatosFamiliaresDTOs.FamiliarCreate;
import co.assip.erp.hojavida.dto.DatosFamiliaresDTOs.FamiliarUpdate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DatosFamiliaresService {

    Page<FamiliarListItem> list(Integer idPersona, String q, Pageable pageable);

    FamiliarDetail get(Integer idPersona, Integer idFamiliar);

    Integer create(Integer idPersona, FamiliarCreate req, Integer userId);

    void update(Integer idPersona, Integer idFamiliar, FamiliarUpdate req, Integer userId);

    void delete(Integer idPersona, Integer idFamiliar);
}
