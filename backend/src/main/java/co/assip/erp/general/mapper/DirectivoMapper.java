package co.assip.erp.general.mapper;

import co.assip.erp.general.domain.Directivo;
import co.assip.erp.general.dto.DirectivoDTOs.*;

public final class DirectivoMapper {
    private DirectivoMapper(){}

    public static DirectivoDetailDTO toDetail(Directivo e) {
        var p = e.getPersona();
        var nombre = (p.getTipoPersona()!=null && p.getTipoPersona().equals("2"))
                ? p.getNombres()
                : (p.getNombres()+" "+p.getPrimerApellido()+" "+(p.getSegundoApellido()==null?"":p.getSegundoApellido())).trim();

        return new DirectivoDetailDTO(
                e.getIdDirectivo(),
                p.getIdDatosPersonal(),
                p.getDocumento(),
                nombre,
                e.getCodigoTipoDirectivo(),
                e.getCalidadDirectivo(),
                e.getEstadoDirectivo(),
                e.getActaAsamblea(),
                e.getFechaAsamblea(),
                e.getResolucionSes(),
                e.getFechaResolucion(),
                e.getFechaRetiro(),
                e.getPeriodosVigencia()==null?0:e.getPeriodosVigencia()
        );
    }
}
