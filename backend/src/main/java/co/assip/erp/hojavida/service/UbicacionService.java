package co.assip.erp.hojavida.service;

import co.assip.erp.hojavida.dto.UbicacionDTOs;

import java.util.Optional;

public interface UbicacionService {
    Optional<UbicacionDTOs.UbicacionResponse> getByPersona(Integer idDatosPersonal);
    Integer createForPersona(Integer idDatosPersonal, Integer userId, UbicacionDTOs.UbicacionRequest req);
    void updateForPersona(Integer idDatosPersonal, Integer idUbicacion, Integer userId, UbicacionDTOs.UbicacionRequest req);
    void deleteForPersona(Integer idDatosPersonal, Integer idUbicacion);
}
