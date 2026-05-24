package co.assip.erp.depositos.informes.cuentasnr;

import co.assip.erp.depositos.informes.cuentasnr.dto.CuentasNRRequestDTO;
import org.springframework.stereotype.Service;

@Service
public class CuentasNRService {

    private final CuentasNRRepository repo;

    public CuentasNRService(CuentasNRRepository repo) {
        this.repo = repo;
    }

    public Object resolver(CuentasNRRequestDTO req) {

        if ("NUEVAS".equalsIgnoreCase(req.getTipoInforme())) {
            return repo.cuentasNuevas(req);
        }

        if ("RETIRADAS".equalsIgnoreCase(req.getTipoInforme())) {
            return repo.cuentasRetiradas(req);
        }

        return null;
    }
}
