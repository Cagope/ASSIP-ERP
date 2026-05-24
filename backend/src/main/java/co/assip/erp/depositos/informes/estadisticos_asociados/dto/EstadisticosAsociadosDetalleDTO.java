package co.assip.erp.depositos.informes.estadisticos_asociados.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class EstadisticosAsociadosDetalleDTO {

    private String grupo;

    private String categoria;

    private String documento;

    private String nombreCompleto;

    private String nombreAgencia;

    private String codigoForma;

    private String nombreForma;

    private String ciudad;

    private String celular;

    private String correo;

    private BigDecimal saldoAportes;

    private String genero;

    private String estadoCivil;

    private String cabezaFamilia;

    private String escolaridad;

    private String tipoVivienda;

    private String ocupacion;

    private String sectorEconomico;

}