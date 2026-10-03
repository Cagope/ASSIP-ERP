package co.assip.erp.cdat.informes.consulta_cdats;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ConsultaCdatsService {

    private final ConsultaCdatsRepository repository;

    public List<Map<String, Object>> consultarCdats(
            String documento,
            String nombres,
            String primerApellido,
            String segundoApellido,
            String codigoCdat
    ) {

        return repository.consultarCdats(
                documento,
                nombres,
                primerApellido,
                segundoApellido,
                codigoCdat
        );
    }

    public List<Map<String, Object>> consultarExtracto(
            String codigoCdat,
            LocalDate fechaInicial,
            LocalDate fechaFinal
    ) {

        return repository.consultarExtracto(
                codigoCdat,
                fechaInicial,
                fechaFinal
        );
    }
}