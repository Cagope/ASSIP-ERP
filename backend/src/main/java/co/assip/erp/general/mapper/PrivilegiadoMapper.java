package co.assip.erp.general.mapper;

import co.assip.erp.general.domain.Directivo;
import co.assip.erp.general.domain.Privilegiado;
import co.assip.erp.general.dto.PrivilegiadoDTOs.*;
import co.assip.erp.hojavida.domain.DatosPersonales;

import java.time.LocalDateTime;

public final class PrivilegiadoMapper {
    private PrivilegiadoMapper() {}

    public static PrivilegiadoDetailDTO toDetail(Privilegiado e) {
        var p = e.getPersona();
        String documento = (p != null) ? (p.getDocumento() != null ? p.getDocumento() : "") : "";
        String nombre = buildNombrePersona(p);
        return new PrivilegiadoDetailDTO(
                e.getIdPrivilegiado(),
                (e.getDirectivo() != null ? e.getDirectivo().getIdDirectivo() : null),
                (p != null ? p.getIdDatosPersonal() : null),
                documento,
                nombre,
                e.getCodigoParentesco(),
                null // nombreParentesco se resuelve en queries de repo (o cat controller)
        );
    }

    // Sobrecarga (compat) si en algún punto se llamó con 2 parámetros
    public static PrivilegiadoDetailDTO toDetail(Privilegiado e, String ignored) {
        return toDetail(e);
    }

    public static PrivilegiadoListItemDTO toListItem(Privilegiado e, String nombreParentesco) {
        var p = e.getPersona();
        String documento = (p != null) ? (p.getDocumento() != null ? p.getDocumento() : "") : "";
        return new PrivilegiadoListItemDTO(
                e.getIdPrivilegiado(),
                (e.getDirectivo() != null ? e.getDirectivo().getIdDirectivo() : null),
                (p != null ? p.getIdDatosPersonal() : null),
                documento,
                buildNombrePersona(p),
                e.getCodigoParentesco(),
                nombreParentesco,
                null, // documentoDirectivo (lo llena la proyección del repo)
                null  // nombreDirectivo   (lo llena la proyección del repo)
        );
    }

    public static void applyCreate(
            Privilegiado e,
            PrivilegiadoCreateRequest req,
            Directivo directivo,
            DatosPersonales persona,
            Integer userId,
            LocalDateTime now
    ) {
        e.setDirectivo(directivo);
        e.setPersona(persona);
        e.setCodigoParentesco(req.codigoParentesco());

        e.setFkSeguridadCreacion(userId);
        e.setFkSeguridadActualizacion(userId);
        e.setFechaCreacion(now);
        e.setFechaActualizacion(now);
    }

    public static void applyUpdate(
            Privilegiado e,
            PrivilegiadoUpdateRequest req,
            Directivo directivo,
            DatosPersonales persona,
            Integer userId,
            LocalDateTime now
    ) {
        e.setDirectivo(directivo);
        e.setPersona(persona);
        e.setCodigoParentesco(req.codigoParentesco());

        e.setFkSeguridadActualizacion(userId);
        e.setFechaActualizacion(now);
    }

    private static String buildNombrePersona(DatosPersonales p) {
        if (p == null) return "";
        // Persona jurídica (tipoPersona = '2'): usar nombres directamente
        if ("2".equals(p.getTipoPersona())) return nullToEmpty(p.getNombres());

        String nombres = nullToEmpty(p.getNombres());
        String pa = nullToEmpty(p.getPrimerApellido());
        String sa = nullToEmpty(p.getSegundoApellido());
        return (nombres + " " + pa + " " + sa).replaceAll("\\s+", " ").trim();
    }

    private static String nullToEmpty(String s) { return s == null ? "" : s; }
}
