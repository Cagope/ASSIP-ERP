package co.assip.erp.cdat.informes.simulador_cdat;

import co.assip.erp.cdat.informes.simulador_cdat.dto.CdatSimuladorEntradaDTO;
import co.assip.erp.cdat.informes.simulador_cdat.dto.CdatSimuladorItemDTO;
import co.assip.erp.cdat.informes.simulador_cdat.dto.CdatSimuladorPreviewDTO;
import co.assip.erp.cdat.liquidacion_diaria.CdatLiquidacionDiariaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CdatSimuladorService {

    private final CdatLiquidacionDiariaRepository repository;

    public CdatSimuladorPreviewDTO generarPreview(
            CdatSimuladorEntradaDTO input
    ) {

        BigDecimal baseRetencion =
                repository.obtenerValorBaseRetencion(input.getIdAgencia());

        BigDecimal porcentajeRetencion =
                repository.obtenerPorcentajeRetencion(input.getIdAgencia());

        List<CdatSimuladorItemDTO> items = new ArrayList<>();

        BigDecimal totalBruto = BigDecimal.ZERO;
        BigDecimal totalRetencion = BigDecimal.ZERO;
        BigDecimal totalNeto = BigDecimal.ZERO;

        BigDecimal capital = input.getValorCdat();

        LocalDate fechaVencimiento =
                input.getFechaApertura()
                        .plusMonths(input.getPlazoMeses());

        for (int i = 1; i <= input.getPlazoMeses(); i++) {

            int dias = 30;

            BigDecimal interesBruto =
                    capital
                            .multiply(input.getTasaNominalAnual())
                            .multiply(BigDecimal.valueOf(dias))
                            .divide(BigDecimal.valueOf(36000), 2, RoundingMode.HALF_UP);

            BigDecimal valorRetencion = BigDecimal.ZERO;

            if (
                    Boolean.TRUE.equals(input.getAplicaRetencion())
                            && interesBruto.compareTo(baseRetencion) >= 0
            ) {

                valorRetencion =
                        interesBruto
                                .multiply(porcentajeRetencion)
                                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            }

            BigDecimal interesNeto =
                    interesBruto.subtract(valorRetencion);

            LocalDate fechaPago =
                    input.getFechaApertura().plusMonths(i);

            totalBruto = totalBruto.add(interesBruto);
            totalRetencion = totalRetencion.add(valorRetencion);
            totalNeto = totalNeto.add(interesNeto);

            boolean ultimoPeriodo =
                    i == input.getPlazoMeses();

            BigDecimal pagoCliente;

            if ("MENSUAL".equalsIgnoreCase(input.getFormaPagoInteres())) {

                pagoCliente = interesNeto;

            } else {

                pagoCliente =
                        ultimoPeriodo
                                ? totalNeto
                                : BigDecimal.ZERO;
            }

            BigDecimal saldoFinal =
                    "VENCIMIENTO".equalsIgnoreCase(input.getFormaPagoInteres())
                            ? ultimoPeriodo
                            ? capital.add(totalNeto)
                            : capital
                            : capital;

            items.add(
                    CdatSimuladorItemDTO.builder()
                            .periodo(i)
                            .fechaPago(fechaPago)
                            .dias(dias)
                            .capital(capital)
                            .interesBruto(interesBruto)
                            .baseRetencion(baseRetencion)
                            .porcentajeRetencion(porcentajeRetencion)
                            .valorRetencion(valorRetencion)
                            .interesNeto(interesNeto)
                            .pagoCliente(pagoCliente)
                            .saldoFinal(saldoFinal)
                            .build()
            );
        }

        return CdatSimuladorPreviewDTO.builder()
                .capital(capital)
                .tasaNominalAnual(input.getTasaNominalAnual())
                .baseRetencion(baseRetencion)
                .porcentajeRetencion(porcentajeRetencion)
                .fechaApertura(input.getFechaApertura())
                .fechaVencimiento(fechaVencimiento)
                .plazoMeses(input.getPlazoMeses())
                .formaPagoInteres(input.getFormaPagoInteres())
                .totalInteresBruto(totalBruto)
                .totalRetencion(totalRetencion)
                .totalInteresNeto(totalNeto)
                .totalPagoCliente(totalNeto)
                .valorAlVencimiento(
                        "VENCIMIENTO".equalsIgnoreCase(input.getFormaPagoInteres())
                                ? capital.add(totalNeto)
                                : capital
                )
                .items(items)
                .build();
    }
}