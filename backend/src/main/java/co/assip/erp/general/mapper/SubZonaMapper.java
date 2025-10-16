package co.assip.erp.general.mapper;

import co.assip.erp.general.domain.SubZona;
import co.assip.erp.general.dto.SubZonaDTOs;

public class SubZonaMapper {

    public static SubZonaDTOs.SubZonaListDTO toListDTO(SubZona s) {
        return new SubZonaDTOs.SubZonaListDTO(
                s.getIdSubZona(),
                s.getZona() != null ? s.getZona().getIdZona() : null,
                s.getCodigoSubZona(),
                s.getNombreSubZona(),
                s.getComentarioSubZona(),
                s.getZona() != null ? s.getZona().getNombreZona() : null
        );
    }

    public static SubZonaDTOs.SubZonaDetailDTO toDetailDTO(SubZona s) {
        return new SubZonaDTOs.SubZonaDetailDTO(
                s.getIdSubZona(),
                s.getZona() != null ? s.getZona().getIdZona() : null,
                s.getCodigoSubZona(),
                s.getNombreSubZona(),
                s.getComentarioSubZona(),
                s.getZona() != null ? s.getZona().getNombreZona() : null
        );
    }
}
