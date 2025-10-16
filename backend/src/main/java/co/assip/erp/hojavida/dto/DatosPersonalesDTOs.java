package co.assip.erp.hojavida.dto;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

public class DatosPersonalesDTOs {

    // ===== LIST =====
    public record DatosPersonalesListItem(
            Integer idDatosPersonal,
            String tipoDocumento,
            String documento,
            Boolean tieneRut,
            String digitoVerificacion,
            String nombres,
            String primerApellido,
            LocalDate fechaNacimiento
    ) {}

    // ===== DETAIL =====
    public record DatosPersonalesDetail(
            Integer idDatosPersonal,
            String tipoDocumento,
            String documento,
            Boolean tieneRut,
            String digitoVerificacion,
            String nombres,
            String primerApellido,
            String segundoApellido,
            LocalDate fechaNacimiento,
            LocalDate fechaDocumento,
            LocalDate fechaApertura,
            Integer idPaisDocumento,
            Integer idDepartamentoExpedicion,
            Integer idCiudadExpedicion,
            Integer idPaisNacimiento,
            Integer idDepartamentoNacimiento,
            Integer idCiudadNacimiento,
            String comentario,
            String codigoGenero,
            String codigoEstadoCivil,
            String codigoEscolaridad,
            String codigoTipoVivienda,
            Integer estratoSocial,
            Integer numeroHijos,
            String codigoOcupacion,
            String codigoSectorEconomico,
            String codigoActividadSes,
            String codigoActividadDian,
            String codigoRetencion
    ) {}

    // ===== CREATE =====
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DatosPersonaCreate(
            String tipoDocumento,
            String documento,
            Boolean tieneRut,
            String digitoVerificacion,
            String tipoPersona,
            String nombres,
            String primerApellido,
            String segundoApellido,
            LocalDate fechaNacimiento,
            LocalDate fechaDocumento,

            // PAÍS INDEPENDIENTE
            Integer idPaisDocumento,
            Integer idPaisNacimiento,

            // CIUDADES (depto derivado por backend)
            Integer idCiudadExpedicion,
            Integer idCiudadNacimiento,

            String comentario,
            String codigoGenero,
            String codigoEstadoCivil,
            String codigoEscolaridad,
            String codigoTipoVivienda,
            Integer estratoSocial,
            Integer numeroHijos,
            String codigoOcupacion,
            String codigoSectorEconomico,
            String codigoActividadSes,
            String codigoActividadDian,
            String codigoRetencion,
            String cabezaFamilia
    ) {}

    // ===== UPDATE =====
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DatosPersonaUpdate(
            String tipoDocumento,
            String documento,
            Boolean tieneRut,
            String digitoVerificacion,
            String tipoPersona,
            String nombres,
            String primerApellido,
            String segundoApellido,
            LocalDate fechaNacimiento,
            LocalDate fechaDocumento,

            // PAÍS INDEPENDIENTE
            Integer idPaisDocumento,
            Integer idPaisNacimiento,

            // CIUDADES (depto derivado por backend)
            Integer idCiudadExpedicion,
            Integer idCiudadNacimiento,

            String comentario,
            String codigoGenero,
            String codigoEstadoCivil,
            String codigoEscolaridad,
            String codigoTipoVivienda,
            Integer estratoSocial,
            Integer numeroHijos,
            String codigoOcupacion,
            String codigoSectorEconomico,
            String codigoActividadSes,
            String codigoActividadDian,
            String codigoRetencion,
            String cabezaFamilia
    ) {}
}
