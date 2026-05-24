package co.assip.erp.depositos.informes.estadisticos_asociados;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class EstadisticosAsociadosRow {

    private String documento;

    private String nombreCompleto;

    private String nombreAgencia;

    private String codigoForma;

    private String nombreForma;

    private String ciudad;

    private String celular;

    private String correo;

    private String genero;

    private String estadoCivil;

    private String cabezaFamilia;

    private String escolaridad;

    private String tipoVivienda;

    private String ocupacion;

    private String sectorEconomico;

    private BigDecimal saldoAportes;

}