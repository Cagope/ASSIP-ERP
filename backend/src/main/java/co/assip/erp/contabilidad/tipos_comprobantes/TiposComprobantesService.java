package co.assip.erp.contabilidad.tipos_comprobantes;

import co.assip.erp.seguridad.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TiposComprobantesService {

    private final TiposComprobantesRepository repository;

    public List<TipoComprobante> listar(Integer idAgencia, Boolean soloActivos) {
        // Validar que el usuario tenga acceso a la agencia
        SecurityUtils.validarAgencia(idAgencia);
        return repository.listar(idAgencia, soloActivos);
    }

    public TipoComprobante obtener(String tipoComprobante, Integer idAgencia) {
        SecurityUtils.validarAgencia(idAgencia);
        return repository.obtener(tipoComprobante, idAgencia);
    }

    public void crear(TipoComprobante t) {
        Integer idUsuario = SecurityUtils.getIdUsuario();
        if (idUsuario == null) {
            throw new RuntimeException("USUARIO_NO_AUTENTICADO");
        }

        SecurityUtils.validarAgencia(t.getIdAgencia());

        t.setFkSeguridadCreacion(idUsuario);
        t.setFkSeguridadEdicion(idUsuario);

        repository.insertar(t);
    }

    public void actualizar(TipoComprobante t) {
        Integer idUsuario = SecurityUtils.getIdUsuario();
        if (idUsuario == null) {
            throw new RuntimeException("USUARIO_NO_AUTENTICADO");
        }

        SecurityUtils.validarAgencia(t.getIdAgencia());

        // Nunca permitir null en edición
        t.setFkSeguridadEdicion(idUsuario);

        repository.actualizar(t);
    }
}
