package co.assip.erp.hojavida.mapper;

import co.assip.erp.hojavida.domain.DatosPersonales;
import co.assip.erp.hojavida.dto.DatosPersonalesDTOs.DatosPersonaCreate;
import co.assip.erp.hojavida.dto.DatosPersonalesDTOs.DatosPersonaUpdate;
import co.assip.erp.hojavida.dto.DatosPersonalesDTOs.DatosPersonalesDetail;
import co.assip.erp.hojavida.dto.DatosPersonalesDTOs.DatosPersonalesListItem;

public final class DatosPersonalesMapper {

    private DatosPersonalesMapper() {}

    public static DatosPersonalesListItem toListItem(DatosPersonales e) {
        if (e == null) return null;
        return new DatosPersonalesListItem(
                e.getIdDatosPersonal(),
                e.getTipoDocumento(),
                e.getDocumento(),
                e.getTieneRut(),
                e.getDigitoVerificacion(),
                e.getNombres(),
                e.getPrimerApellido(),
                e.getFechaNacimiento()
        );
    }

    public static DatosPersonalesDetail toDetail(DatosPersonales e) {
        if (e == null) return null;
        return new DatosPersonalesDetail(
                e.getIdDatosPersonal(),
                e.getTipoDocumento(),
                e.getDocumento(),
                e.getTieneRut(),
                e.getDigitoVerificacion(),
                e.getNombres(),
                e.getPrimerApellido(),
                e.getSegundoApellido(),
                e.getFechaNacimiento(),
                e.getFechaDocumento(),
                e.getFechaApertura(),
                e.getIdPaisDocumento(),
                e.getIdDepartamentoExpedicion(),
                e.getIdCiudadExpedicion(),
                e.getIdPaisNacimiento(),
                e.getIdDepartamentoNacimiento(),
                e.getIdCiudadNacimiento(),
                e.getComentario(),
                e.getCodigoGenero(),
                e.getCodigoEstadoCivil(),
                e.getCodigoEscolaridad(),
                e.getCodigoTipoVivienda(),
                e.getEstratoSocial(),
                e.getNumeroHijos(),
                e.getCodigoOcupacion(),
                e.getCodigoSectorEconomico(),
                e.getCodigoActividadSes(),
                e.getCodigoActividadDian(),
                e.getCodigoRetencion()
        );
    }

    public static void applyFromCreate(DatosPersonales target, DatosPersonaCreate r) {
        if (target == null || r == null) return;

        // Identificación
        target.setTipoDocumento(r.tipoDocumento());
        target.setDocumento(r.documento());
        target.setTieneRut(Boolean.TRUE.equals(r.tieneRut()));
        target.setDigitoVerificacion(r.digitoVerificacion());
        target.setTipoPersona(r.tipoPersona());

        // Fechas
        target.setFechaDocumento(r.fechaDocumento());
        target.setFechaNacimiento(r.fechaNacimiento());

        // País independiente
        target.setIdPaisDocumento(r.idPaisDocumento());
        target.setIdPaisNacimiento(r.idPaisNacimiento());

        // Ciudades (depto se deriva)
        target.setIdCiudadExpedicion(r.idCiudadExpedicion());
        target.setIdCiudadNacimiento(r.idCiudadNacimiento());

        // Nombres
        target.setNombres(r.nombres());
        target.setPrimerApellido(r.primerApellido());
        target.setSegundoApellido(r.segundoApellido());

        // Catálogos / otros
        target.setComentario(r.comentario());
        target.setCodigoGenero(r.codigoGenero());
        target.setCodigoEstadoCivil(r.codigoEstadoCivil());
        target.setCodigoEscolaridad(r.codigoEscolaridad());
        target.setCodigoTipoVivienda(r.codigoTipoVivienda());
        target.setEstratoSocial(r.estratoSocial());
        target.setNumeroHijos(r.numeroHijos());
        target.setCodigoOcupacion(r.codigoOcupacion());
        target.setCodigoSectorEconomico(r.codigoSectorEconomico());
        target.setCodigoActividadSes(r.codigoActividadSes());
        target.setCodigoActividadDian(r.codigoActividadDian());
        target.setCodigoRetencion(r.codigoRetencion());
        target.setCabezaFamilia(r.cabezaFamilia());
    }

    public static void applyFromUpdate(DatosPersonales target, DatosPersonaUpdate r) {
        if (target == null || r == null) return;

        // Identificación
        target.setTipoDocumento(r.tipoDocumento());
        target.setDocumento(r.documento());
        target.setTieneRut(Boolean.TRUE.equals(r.tieneRut()));
        target.setDigitoVerificacion(r.digitoVerificacion());
        target.setTipoPersona(r.tipoPersona());

        // Fechas
        target.setFechaDocumento(r.fechaDocumento());
        target.setFechaNacimiento(r.fechaNacimiento());

        // País independiente
        target.setIdPaisDocumento(r.idPaisDocumento());
        target.setIdPaisNacimiento(r.idPaisNacimiento());

        // Ciudades
        target.setIdCiudadExpedicion(r.idCiudadExpedicion());
        target.setIdCiudadNacimiento(r.idCiudadNacimiento());

        // Nombres
        target.setNombres(r.nombres());
        target.setPrimerApellido(r.primerApellido());
        target.setSegundoApellido(r.segundoApellido());

        // Catálogos / otros
        target.setComentario(r.comentario());
        target.setCodigoGenero(r.codigoGenero());
        target.setCodigoEstadoCivil(r.codigoEstadoCivil());
        target.setCodigoEscolaridad(r.codigoEscolaridad());
        target.setCodigoTipoVivienda(r.codigoTipoVivienda());
        target.setEstratoSocial(r.estratoSocial());
        target.setNumeroHijos(r.numeroHijos());
        target.setCodigoOcupacion(r.codigoOcupacion());
        target.setCodigoSectorEconomico(r.codigoSectorEconomico());
        target.setCodigoActividadSes(r.codigoActividadSes());
        target.setCodigoActividadDian(r.codigoActividadDian());
        target.setCodigoRetencion(r.codigoRetencion());
        target.setCabezaFamilia(r.cabezaFamilia());
    }
}
