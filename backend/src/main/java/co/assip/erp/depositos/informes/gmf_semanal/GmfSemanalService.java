package co.assip.erp.depositos.informes.gmf_semanal;

import co.assip.erp.depositos.informes.gmf_semanal.dto.GmfSemanalItemDTO;
import co.assip.erp.depositos.informes.gmf_semanal.dto.GmfSemanalRequestDTO;
import co.assip.erp.depositos.informes.gmf_semanal.dto.GmfSemanalResponseDTO;
import co.assip.erp.depositos.informes.gmf_semanal.dto.GmfSemanalResumenDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GmfSemanalService {

    private final GmfSemanalRepository repository;

    public GmfSemanalResponseDTO consultar(
            GmfSemanalRequestDTO request
    ) {

        validar(request);

        LocalDate fechaInicial =
                LocalDate.parse(request.getFechaInicial());

        LocalDate fechaFinal =
                LocalDate.parse(request.getFechaFinal());

        List<GmfSemanalItemDTO> items =
                repository.consultar(request);

        GmfSemanalResumenDTO resumen =
                construirResumen(items);

        return GmfSemanalResponseDTO.builder()
                .fechaInicial(fechaInicial)
                .fechaFinal(fechaFinal)
                .idAgencia(request.getIdAgencia())
                .numeroSemana(request.getNumeroSemana())
                .codigoForma(normalizarCodigoForma(request.getCodigoForma()))
                .resumen(resumen)
                .items(items)
                .build();
    }

    private void validar(
            GmfSemanalRequestDTO request
    ) {

        if (request == null) {
            throw new RuntimeException(
                    "Debe indicar los parámetros del informe."
            );
        }

        if (request.getFechaInicial() == null
                || request.getFechaInicial().isBlank()) {

            throw new RuntimeException(
                    "Debe seleccionar la fecha inicial."
            );
        }

        if (request.getFechaFinal() == null
                || request.getFechaFinal().isBlank()) {

            throw new RuntimeException(
                    "Debe seleccionar la fecha final."
            );
        }

        LocalDate fechaInicial;
        LocalDate fechaFinal;

        try {
            fechaInicial =
                    LocalDate.parse(
                            request.getFechaInicial()
                    );

            fechaFinal =
                    LocalDate.parse(
                            request.getFechaFinal()
                    );

        } catch (DateTimeParseException ex) {

            throw new RuntimeException(
                    "Las fechas deben tener formato yyyy-MM-dd."
            );
        }

        if (fechaFinal.isBefore(fechaInicial)) {
            throw new RuntimeException(
                    "La fecha final no puede ser anterior a la fecha inicial."
            );
        }

        if (request.getNumeroSemana() == null) {
            throw new RuntimeException(
                    "Debe indicar el número de semana."
            );
        }

        if (request.getNumeroSemana() <= 0) {
            throw new RuntimeException(
                    "El número de semana debe ser mayor que cero."
            );
        }
    }

    private GmfSemanalResumenDTO construirResumen(
            List<GmfSemanalItemDTO> items
    ) {

        BigDecimal totalBaseAsumido =
                sumar(
                        items,
                        Campo.BASE_ASUMIDO
                );

        BigDecimal totalGmfAsumido =
                sumar(
                        items,
                        Campo.GMF_ASUMIDO
                );

        BigDecimal totalBaseAsociado =
                sumar(
                        items,
                        Campo.BASE_ASOCIADO
                );

        BigDecimal totalGmfAsociado =
                sumar(
                        items,
                        Campo.GMF_ASOCIADO
                );

        BigDecimal totalBaseRetiro =
                sumar(
                        items,
                        Campo.BASE_RETIRO
                );

        BigDecimal totalGmfRetiro =
                sumar(
                        items,
                        Campo.GMF_RETIRO
                );

        BigDecimal totalValorExento =
                sumar(
                        items,
                        Campo.VALOR_EXENTO
                );

        BigDecimal totalBaseChequeAsumido =
                sumar(
                        items,
                        Campo.BASE_CHEQUE_ASUMIDO
                );

        BigDecimal totalGmfChequeAsumido =
                sumar(
                        items,
                        Campo.GMF_CHEQUE_ASUMIDO
                );

        BigDecimal totalBaseChequeExento =
                sumar(
                        items,
                        Campo.BASE_CHEQUE_EXENTO
                );

        BigDecimal totalGmfChequeExento =
                sumar(
                        items,
                        Campo.GMF_CHEQUE_EXENTO
                );

        BigDecimal totalBaseGravada =
                totalBaseAsumido
                        .add(totalBaseAsociado)
                        .add(totalBaseRetiro)
                        .add(totalBaseChequeAsumido);

        BigDecimal totalGmf =
                totalGmfAsumido
                        .add(totalGmfAsociado)
                        .add(totalGmfRetiro)
                        .add(totalGmfChequeAsumido);

        return GmfSemanalResumenDTO.builder()
                .totalBaseAsumido(totalBaseAsumido)
                .totalGmfAsumido(totalGmfAsumido)
                .totalBaseAsociado(totalBaseAsociado)
                .totalGmfAsociado(totalGmfAsociado)
                .totalBaseRetiro(totalBaseRetiro)
                .totalGmfRetiro(totalGmfRetiro)
                .totalValorExento(totalValorExento)
                .totalBaseChequeAsumido(totalBaseChequeAsumido)
                .totalGmfChequeAsumido(totalGmfChequeAsumido)
                .totalBaseChequeExento(totalBaseChequeExento)
                .totalGmfChequeExento(totalGmfChequeExento)
                .totalBaseGravada(totalBaseGravada)
                .totalGmf(totalGmf)
                .build();
    }

    private BigDecimal sumar(
            List<GmfSemanalItemDTO> items,
            Campo campo
    ) {

        return items.stream()
                .map(item -> valor(item, campo))
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }

    private BigDecimal valor(
            GmfSemanalItemDTO item,
            Campo campo
    ) {

        BigDecimal valor = switch (campo) {

            case BASE_ASUMIDO ->
                    item.getBaseAsumido();

            case GMF_ASUMIDO ->
                    item.getGmfAsumido();

            case BASE_ASOCIADO ->
                    item.getBaseAsociado();

            case GMF_ASOCIADO ->
                    item.getGmfAsociado();

            case BASE_RETIRO ->
                    item.getBaseRetiro();

            case GMF_RETIRO ->
                    item.getGmfRetiro();

            case VALOR_EXENTO ->
                    item.getValorExento();

            case BASE_CHEQUE_ASUMIDO ->
                    item.getBaseChequeAsumido();

            case GMF_CHEQUE_ASUMIDO ->
                    item.getGmfChequeAsumido();

            case BASE_CHEQUE_EXENTO ->
                    item.getBaseChequeExento();

            case GMF_CHEQUE_EXENTO ->
                    item.getGmfChequeExento();
        };

        return valor == null
                ? BigDecimal.ZERO
                : valor;
    }

    private String normalizarCodigoForma(
            String codigoForma
    ) {

        if (codigoForma == null
                || codigoForma.isBlank()) {

            return "0";
        }

        return codigoForma.trim();
    }

    private enum Campo {

        BASE_ASUMIDO,
        GMF_ASUMIDO,

        BASE_ASOCIADO,
        GMF_ASOCIADO,

        BASE_RETIRO,
        GMF_RETIRO,

        VALOR_EXENTO,

        BASE_CHEQUE_ASUMIDO,
        GMF_CHEQUE_ASUMIDO,

        BASE_CHEQUE_EXENTO,
        GMF_CHEQUE_EXENTO
    }
}