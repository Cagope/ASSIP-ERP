package co.assip.erp.cartera.calculosprevios.dto;

import java.math.BigDecimal;

public record ResumenEdadMoraDTO(

        String codigoClasificacion,
        String nombreClasificacion,

        String edadMora,

        Integer cantidadCreditos,

        BigDecimal saldoCapital,

        BigDecimal porcentajeCantidad,

        BigDecimal porcentajeSaldo

) {
}