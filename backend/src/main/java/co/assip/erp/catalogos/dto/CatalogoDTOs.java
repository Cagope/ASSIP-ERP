package co.assip.erp.catalogos.dto;

import java.math.BigDecimal;

public class CatalogoDTOs {

    // === Id / Nombre (opcional "codigo") ===
    public record IdNombreDTO(
            Integer id,
            String nombre,
            String codigo // opcional, puede venir null
    ) {}

    // === Codigo / Nombre ===
    public record CodigoNombreDTO(
            String codigo,
            String nombre
    ) {}

    // Tipos de régimen (con extras útiles)
    public record TipoRegimenDTO(
            String codigo,
            String nombre,
            BigDecimal porcentajeRetencion,
            Boolean baseRetencion
    ) {}

    // Ya usados por depto/ciudad
    public record DepartamentoDTO(
            Integer id,
            String nombre
    ) {}

    public record CiudadDTO(
            Integer id,
            String nombre,
            Integer idDepartamento
    ) {}

    // === Tipos de documentos ===
    public record TipoDocumentoDTO(
            String tipoDocumento,
            String nombreTipoDocumento,
            Boolean tipoDv,
            String tipoDocumentoDian
    ) {}
}
