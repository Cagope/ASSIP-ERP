package co.assip.erp.shared.referencias.catalogos.acciones_cuentas_conjuntas;

import co.assip.erp.shared.referencias.catalogos.acciones_cuentas_conjuntas.dto.AccionCuentaConjuntaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccionesCuentasConjuntasService {

    private final AccionesCuentasConjuntasRepository repository;

    public List<AccionCuentaConjuntaDTO> listar() {
        return repository.listar();
    }

}