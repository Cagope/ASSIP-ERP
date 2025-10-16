package co.assip.erp.general.mapper;

import co.assip.erp.general.domain.Parametro;
import co.assip.erp.general.dto.ParametroDTOs;

public class ParametroMapper {

    public static ParametroDTOs.ParametroListDTO toListDTO(Parametro p) {
        return new ParametroDTOs.ParametroListDTO(
                p.getIdParametro(),
                p.getAgencia() != null ? p.getAgencia().getIdAgencia() : null,
                p.getAgencia() != null ? p.getAgencia().getNombreAgencia() : null,
                p.getCodigoParametro(),
                p.getNombreParametro(),
                p.getValorParametro(),
                p.getTipoValor()
        );
    }

    public static ParametroDTOs.ParametroDetailDTO toDetailDTO(Parametro p) {
        return new ParametroDTOs.ParametroDetailDTO(
                p.getIdParametro(),
                p.getAgencia() != null ? p.getAgencia().getIdAgencia() : null,
                p.getAgencia() != null ? p.getAgencia().getNombreAgencia() : null,
                p.getCodigoParametro(),
                p.getNombreParametro(),
                p.getValorParametro(),
                p.getTipoValor()
        );
    }
}
