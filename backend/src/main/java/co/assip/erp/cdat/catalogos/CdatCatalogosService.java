package co.assip.erp.cdat.catalogos;

import co.assip.erp.cdat.catalogos.dto.CdatAmortizacionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CdatCatalogosService {

    private final CdatCatalogosRepository repository;

    public List<CdatAmortizacionDTO> listarAmortizaciones() {
        return repository.listarAmortizaciones();
    }
}