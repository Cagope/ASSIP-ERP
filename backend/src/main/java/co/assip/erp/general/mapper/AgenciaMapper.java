package co.assip.erp.general.mapper;

import co.assip.erp.general.domain.DatosAgencia;
import co.assip.erp.general.dto.AgenciaDTOs.*;

public final class AgenciaMapper {
    private AgenciaMapper() {}

    public static AgenciaListItemDTO toListItem(DatosAgencia e) {
        return new AgenciaListItemDTO(
                e.getIdAgencia(),
                e.getCodigoAgencia(),
                e.getNombreAgencia(),
                e.getSiglaAgencia()   // ← SIGLA correcta
        );
    }

    public static AgenciaDetailDTO toDetail(DatosAgencia e) {
        return new AgenciaDetailDTO(
                e.getIdAgencia(),
                e.getCodigoAgencia(),
                e.getNombreAgencia(),
                e.getSiglaAgencia(),  // ← SIGLA correcta
                e.getDireccionAgencia(),
                e.getIdDepartamento(),
                e.getIdCiudad(),
                e.getCorreoAgencia(),
                e.getCelularAgencia(),
                e.getTelefonoAgencia()
        );
    }

    public static void applyCreate(DatosAgencia e, AgenciaCreateRequest req) {
        e.setCodigoAgencia(req.codigoAgencia());
        e.setNombreAgencia(req.nombreAgencia());
        e.setSiglaAgencia(req.siglaAgencia());  // ← SIGLA correcta
        e.setDireccionAgencia(req.direccionAgencia());
        e.setIdDepartamento(req.idDepartamento());
        e.setIdCiudad(req.idCiudad());
        e.setCorreoAgencia(req.correoAgencia());
        e.setCelularAgencia(req.celularAgencia());
        e.setTelefonoAgencia(req.telefonoAgencia());
    }

    public static void applyUpdate(DatosAgencia e, AgenciaUpdateRequest req) {
        e.setCodigoAgencia(req.codigoAgencia());
        e.setNombreAgencia(req.nombreAgencia());
        e.setSiglaAgencia(req.siglaAgencia());  // ← SIGLA correcta
        e.setDireccionAgencia(req.direccionAgencia());
        e.setIdDepartamento(req.idDepartamento());
        e.setIdCiudad(req.idCiudad());
        e.setCorreoAgencia(req.correoAgencia());
        e.setCelularAgencia(req.celularAgencia());
        e.setTelefonoAgencia(req.telefonoAgencia());
    }
}
