package co.assip.erp.hojavida.service;

import co.assip.erp.hojavida.dto.DatosPersonalesDTOs.DatosPersonalesListItem;
import co.assip.erp.hojavida.dto.DatosPersonalesDTOs.DatosPersonalesDetail;
import co.assip.erp.hojavida.dto.DatosPersonalesDTOs.DatosPersonaCreate;
import co.assip.erp.hojavida.dto.DatosPersonalesDTOs.DatosPersonaUpdate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DatosPersonalesService {

    Page<DatosPersonalesListItem> list(String q, Pageable pageable);

    DatosPersonalesDetail get(Integer id);

    Integer create(DatosPersonaCreate req, Integer userId);

    void update(Integer id, DatosPersonaUpdate req, Integer userId);

    void delete(Integer id);

    /** Nuevo: consulta existencia por tipo+documento */
    boolean exists(String tipoDocumento, String documento);
}
