package co.assip.erp.hojavida.service;

import co.assip.erp.hojavida.dto.ReferenciasPersonalesDTOs.ReferenciaPersonalRequest;
import co.assip.erp.hojavida.dto.ReferenciasPersonalesDTOs.ReferenciaPersonalResponse;

import java.util.Optional;

public interface ReferenciaPersonalService {
    Optional<ReferenciaPersonalResponse> getByPersona(Integer idDatosPersonal);
    Integer createForPersona(Integer idDatosPersonal, Integer idUsuario, ReferenciaPersonalRequest req);
    void updateForPersona(Integer idDatosPersonal, Integer idReferencia, Integer idUsuario, ReferenciaPersonalRequest req);
    void deleteForPersona(Integer idDatosPersonal, Integer idReferencia);
}
