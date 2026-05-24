package co.assip.erp.shared.cuentas_ahorro.cuentas_ahorro;

import co.assip.erp.shared.cuentas_ahorro.cuentas_ahorro.dto.CuentaAhorroSelectDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CuentasAhorroService {

    private final CuentasAhorroRepository repository;

    public CuentasAhorroService(CuentasAhorroRepository repository) {
        this.repository = repository;
    }

    public List<CuentaAhorroSelectDTO> listarPorDatosPersonal(
            Integer idDatosPersonal,
            List<Integer> agencias
    ) {
        return repository.listarPorDatosPersonal(idDatosPersonal, agencias);
    }
}
