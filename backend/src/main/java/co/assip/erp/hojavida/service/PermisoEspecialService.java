package co.assip.erp.hojavida.service;

import co.assip.erp.hojavida.dto.PermisosEspecialesDTOs.PermisoEspecialRequest;
import co.assip.erp.hojavida.dto.PermisosEspecialesDTOs.PermisoEspecialResponse;

import java.util.Optional;

public interface PermisoEspecialService {
    Optional<PermisoEspecialResponse> getByPersona(Integer idDatosPersonal);
    Integer createForPersona(Integer idDatosPersonal, Integer idUsuario, PermisoEspecialRequest req);
    void updateForPersona(Integer idDatosPersonal, Integer idPermiso, Integer idUsuario, PermisoEspecialRequest req);
    void deleteForPersona(Integer idDatosPersonal, Integer idPermiso);
}
