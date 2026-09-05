package co.assip.erp.cartera.calculosprevios.dto;

public record ResumenControlesCalculosDTO(

        Integer cantidadResultados,

        Integer sinEdadMora,

        Integer diasMoraNegativos,

        Integer edadesRiesgoInvalidas,

        Integer edadesReestructuracionInvalidas,

        Integer noReestructuradosInconsistentes,

        Integer cantidadReestructurados,

        Integer cantidadUnaSolaCuota,

        Boolean procesoConsistente

) {
}