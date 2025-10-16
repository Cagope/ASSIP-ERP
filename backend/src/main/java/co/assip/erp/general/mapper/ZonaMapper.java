package co.assip.erp.general.mapper;

import co.assip.erp.general.domain.Zona;
import co.assip.erp.general.dto.ZonaDTOs;

public class ZonaMapper {

    public static ZonaDTOs.ZonaListDTO toListDTO(Zona z) {
        return new ZonaDTOs.ZonaListDTO(
                z.getIdZona(),
                z.getCodigoZona(),
                z.getNombreZona(),
                z.getComentarioZona()
        );
    }

    public static ZonaDTOs.ZonaDetailDTO toDetailDTO(Zona z) {
        return new ZonaDTOs.ZonaDetailDTO(
                z.getIdZona(),
                z.getCodigoZona(),
                z.getNombreZona(),
                z.getComentarioZona()
        );
    }
}
