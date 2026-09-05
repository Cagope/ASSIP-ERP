package co.assip.erp.cartera.analisis.reciprocidadaportes.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReciprocidadAportesPersonaDTO(
        Integer idCierreCartera,
        LocalDate fechaCorte,
        Long idDatosPersonal,
        String tipoDocumento,
        String documento,
        String nombreCompleto,
        Long cantidadCreditos,
        BigDecimal saldoCartera,
        BigDecimal saldoAportes,
        BigDecimal aportesDistribuidos,
        BigDecimal diferenciaDistribucionAportes,
        BigDecimal porcentajeReciprocidad,
        BigDecimal apalancamiento,
        BigDecimal exposicionNetaAportes,
        BigDecimal excedenteAportes,
        Boolean aportesCubrenCartera,
        String rangoReciprocidad
) {}
