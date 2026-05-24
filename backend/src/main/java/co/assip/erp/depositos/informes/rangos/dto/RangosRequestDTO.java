package co.assip.erp.depositos.informes.rangos;

import co.assip.erp.depositos.informes.rangos.dto.RangosFiltroDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RangosRequestDTO {

    /** Tipo de informe: EDAD | SALDO | ANTIGUEDAD */
    private String tipo;

    /** 0 = todas */
    private Integer idAgencia;

    /** 0 = todas */
    private String codigoForma;

    /** Fecha de corte YYYY-MM-DD */
    private String fechaCorte;

    /** Rangos */
    private List<RangosFiltroDTO> rangos;

}