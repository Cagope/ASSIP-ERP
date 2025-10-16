package co.assip.erp.hojavida.service;

import co.assip.erp.hojavida.dto.SarlaftDTOs.SarlaftRequest;
import co.assip.erp.hojavida.dto.SarlaftDTOs.SarlaftResponse;

import java.util.Optional;

public interface SarlaftService {
    Optional<SarlaftResponse> getByPersona(Integer idDatosPersonal);
    Integer createForPersona(Integer idDatosPersonal, Integer idUsuario, SarlaftRequest req);
    void updateForPersona(Integer idDatosPersonal, Integer idSarlaft, Integer idUsuario, SarlaftRequest req);
    void deleteForPersona(Integer idDatosPersonal, Integer idSarlaft);
}
