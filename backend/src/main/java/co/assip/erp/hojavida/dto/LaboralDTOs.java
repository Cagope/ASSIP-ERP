package co.assip.erp.hojavida.dto;

import java.time.LocalDate;

public class LaboralDTOs {

    // LIST (si más adelante quieres listar por persona u otro criterio)
    public record LaboralListItem(
            Integer idLaboral,
            Integer idDatosPersonal,
            String  nombreEmpresa,
            String  direccion,
            Integer idPais,
            Integer idDepartamento,
            Integer idCiudad,
            Boolean empleadoEntidad,
            String  nombreContacto,
            LocalDate fechaVinculacion
    ) {}

    // DETAIL (usado por el GET del controller)
    public record LaboralDetail(
            Integer idLaboral,
            Integer idDatosPersonal,
            String  nombreEmpresa,
            String  direccion,
            Integer idPais,
            Integer idDepartamento,
            Integer idCiudad,
            String  telefonoEmpresa,     // 7 dígitos si viene
            String  celularEmpresa,      // 10 dígitos si viene
            String  correoEmpresa,
            String  codigoTipoEmpresa,
            Boolean empleadoEntidad,
            String  codigoTipoContrato,
            String  codigoJornada,
            String  nombreContacto,
            String  celularContacto,     // 10 dígitos si viene
            LocalDate fechaVinculacion,
            String fechaCreacion,        // <-- cambiado a String
            String fechaActualizacion    // <-- cambiado a String
    ) {}

    // CREATE (para POST)
    public record LaboralCreate(
            String  nombreEmpresa,
            String  direccion,
            Integer idPais,
            Integer idDepartamento,
            Integer idCiudad,
            String  telefonoEmpresa,
            String  celularEmpresa,
            String  correoEmpresa,
            String  codigoTipoEmpresa,
            Boolean empleadoEntidad,
            String  codigoTipoContrato,
            String  codigoJornada,
            String  nombreContacto,
            String  celularContacto,
            LocalDate fechaVinculacion
    ) {}

    // UPDATE (para PUT)
    public record LaboralUpdate(
            String  nombreEmpresa,
            String  direccion,
            Integer idPais,
            Integer idDepartamento,
            Integer idCiudad,
            String  telefonoEmpresa,
            String  celularEmpresa,
            String  correoEmpresa,
            String  codigoTipoEmpresa,
            Boolean empleadoEntidad,
            String  codigoTipoContrato,
            String  codigoJornada,
            String  nombreContacto,
            String  celularContacto,
            LocalDate fechaVinculacion
    ) {}
}
