package co.assip.erp.depositos.informes.inconsistencias;

import co.assip.erp.depositos.informes.inconsistencias.dto.InconsistenciasRequest;
import co.assip.erp.depositos.informes.inconsistencias.dto.InconsistenciaSaldoDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InconsistenciasService {

    private final InconsistenciasRepository repo;

    public InconsistenciasService(InconsistenciasRepository repo) {
        this.repo = repo;
    }

    public List<InconsistenciaSaldoDTO> consultar(InconsistenciasRequest r) {

        if (r.getAgencia() == null || r.getAgencia().isBlank())
            r.setAgencia("0");

        if (r.getFechaCorte() == null || r.getFechaCorte().isBlank())
            throw new IllegalArgumentException("Debe enviar fecha de corte");

        return repo.consultar(r.getAgencia(), r.getFechaCorte());
    }
}
