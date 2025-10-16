package co.assip.erp.hojavida.service;

import co.assip.erp.hojavida.dto.LaboralDTOs.LaboralCreate;
import co.assip.erp.hojavida.dto.LaboralDTOs.LaboralDetail;
import co.assip.erp.hojavida.dto.LaboralDTOs.LaboralUpdate;

import java.util.Optional;

public interface LaboralesService {

    // GET detalle por persona (único registro por persona) - lanza 404 si no existe
    LaboralDetail getByPersona(Integer idPersona);

    // GET detalle por persona (único registro por persona) - no lanza, Optional.empty() si no existe
    Optional<LaboralDetail> tryGetByPersona(Integer idPersona);

    // POST crear
    Integer create(Integer idPersona, LaboralCreate req, Integer userId);

    // PUT actualizar
    void update(Integer idPersona, Integer idLaboral, LaboralUpdate req, Integer userId);

    // DELETE eliminar
    void delete(Integer idPersona, Integer idLaboral);
}
