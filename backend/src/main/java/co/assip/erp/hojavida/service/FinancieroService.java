package co.assip.erp.hojavida.service;

import co.assip.erp.hojavida.dto.FinancierosDTOs.FinancieroRequest;
import co.assip.erp.hojavida.dto.FinancierosDTOs.FinancieroResponse;

import java.util.Optional;

public interface FinancieroService {
    Optional<FinancieroResponse> getByPersona(Integer idDatosPersonal);
    Integer createForPersona(Integer idDatosPersonal, Integer idUsuario, FinancieroRequest req);
    void updateForPersona(Integer idDatosPersonal, Integer idFinanciero, Integer idUsuario, FinancieroRequest req);
    void deleteForPersona(Integer idDatosPersonal, Integer idFinanciero);
}
