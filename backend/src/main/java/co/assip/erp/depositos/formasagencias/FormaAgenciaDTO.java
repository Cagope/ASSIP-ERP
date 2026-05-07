package co.assip.erp.depositos.formasagencias;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FormaAgenciaDTO(

        Integer idFormaAhorro,
        String codigoForma,
        String nombreForma,

        LocalDate fechaUltimaLiquidacion,

        BigDecimal tasaInteresForma,
        BigDecimal valorMinimo,

        String tiempoLiquidacion,

        Integer idCuentaGasto,
        String codigoCuentaGasto,
        String nombreCuentaGasto,

        Integer idCuentaFormaCorto,
        String codigoCuentaFormaCorto,
        String nombreCuentaFormaCorto,

        Integer idCuentaRetencionFuente,
        String codigoCuentaRetencionFuente,
        String nombreCuentaRetencionFuente

) {}