package co.assip.erp.hojavida.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DatosFamiliaresDTOs {

    // LIST
    public record FamiliarListItem(
            Integer idDatosFamiliares,
            Integer idDatosPersonal,
            String codigoParentesco,
            String nombreDatosFamiliar,
            String documentoDatosFamiliar,
            String telefonoDatosFamiliar,
            String celularDatosFamiliar,
            Integer idDepartamento,
            Integer idCiudad,
            Boolean referenciaFamiliar,
            LocalDate fechaNacimiento
    ) {}

    // DETAIL
    public record FamiliarDetail(
            Integer idDatosFamiliares,
            Integer idDatosPersonal,
            String codigoParentesco,
            String nombreDatosFamiliar,
            String documentoDatosFamiliar,
            String telefonoDatosFamiliar,
            String celularDatosFamiliar,
            String direccionDatosFamiliar,
            Integer idDepartamento,
            Integer idCiudad,
            LocalDate fechaNacimiento,
            BigDecimal ingresosDatosFamiliar,
            BigDecimal egresosDatosFamiliar,
            Boolean referenciaFamiliar
    ) {}

    // CREATE
    public record FamiliarCreate(
            String codigoParentesco,
            String nombreDatosFamiliar,
            String documentoDatosFamiliar,
            String telefonoDatosFamiliar, // opcional (7 dígitos si viene)
            String celularDatosFamiliar,  // opcional (10 dígitos si viene)
            String direccionDatosFamiliar,
            Integer idDepartamento,
            Integer idCiudad,
            LocalDate fechaNacimiento,
            BigDecimal ingresosDatosFamiliar,
            BigDecimal egresosDatosFamiliar,
            Boolean referenciaFamiliar
    ) {}

    // UPDATE
    public record FamiliarUpdate(
            String codigoParentesco,
            String nombreDatosFamiliar,
            String documentoDatosFamiliar,
            String telefonoDatosFamiliar,
            String celularDatosFamiliar,
            String direccionDatosFamiliar,
            Integer idDepartamento,
            Integer idCiudad,
            LocalDate fechaNacimiento,
            BigDecimal ingresosDatosFamiliar,
            BigDecimal egresosDatosFamiliar,
            Boolean referenciaFamiliar
    ) {}
}
