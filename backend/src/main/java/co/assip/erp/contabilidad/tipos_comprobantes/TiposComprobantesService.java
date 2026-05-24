package co.assip.erp.contabilidad.tipos_comprobantes;

import co.assip.erp.seguridad.service.UsuarioSesionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TiposComprobantesService {

    private final TiposComprobantesRepository repository;
    private final UsuarioSesionService usuarioSesionService;

    public List<TipoComprobante> listar(Integer idAgencia, Boolean soloActivos) {
        // Validar que el usuario tenga acceso a la agencia
        usuarioSesionService.validarAgencia(idAgencia);
        return repository.listar(idAgencia, soloActivos);
    }

    public TipoComprobante obtener(
            String tipoComprobante,
            Integer idAgencia
    ) {

        usuarioSesionService.validarAgencia(
                idAgencia
        );

        return repository.obtener(
                tipoComprobante,
                idAgencia
        );
    }

    public void crear(TipoComprobante t) {
        Integer idUsuario =
                usuarioSesionService.idUsuario();

        usuarioSesionService.validarAgencia(
                t.getIdAgencia()
        );

        t.setFkSeguridadCreacion(idUsuario);
        t.setFkSeguridadEdicion(idUsuario);

        repository.insertar(t);
    }

    public void actualizar(TipoComprobante t) {
        Integer idUsuario =
                usuarioSesionService.idUsuario();

        usuarioSesionService.validarAgencia(
                t.getIdAgencia()
        );

        // Nunca permitir null en edición
        t.setFkSeguridadEdicion(idUsuario);

        repository.actualizar(t);
    }
}
