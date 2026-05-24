package co.assip.erp.general.agencia;

import co.assip.erp.seguridad.service.UsuarioSesionService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AgenciaService {

    private final AgenciaRepository repository;
    private final UsuarioSesionService usuarioSesionService;

    public AgenciaService(
            AgenciaRepository repository,
            UsuarioSesionService usuarioSesionService
    ) {
        this.repository = repository;
        this.usuarioSesionService = usuarioSesionService;
    }

    public List<Agencia> listar() {
        return repository.findAll();
    }

    public Agencia obtenerPorId(Integer id) {
        return repository.findById(id).orElseThrow(
                () -> new RuntimeException("Agencia no encontrada con id: " + id)
        );
    }

    public Agencia guardar(Agencia agencia) {

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        // 🔐 Auditoría
        agencia.setFkSeguridadCreacion(idUsuario);
        agencia.setFkSeguridadEdicion(idUsuario);

        return repository.save(agencia);
    }

    public Agencia actualizar(Integer id, Agencia entrada) {

        Agencia actual = obtenerPorId(id);

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        actual.setCodigoAgencia(entrada.getCodigoAgencia());
        actual.setNombreAgencia(entrada.getNombreAgencia());
        actual.setSiglaAgencia(entrada.getSiglaAgencia());
        actual.setDireccionAgencia(entrada.getDireccionAgencia());
        actual.setIdDepartamento(entrada.getIdDepartamento());
        actual.setIdCiudad(entrada.getIdCiudad());
        actual.setCorreoAgencia(entrada.getCorreoAgencia());
        actual.setCelularAgencia(entrada.getCelularAgencia());
        actual.setTelefonoAgencia(entrada.getTelefonoAgencia());

        // 🔐 Auditoría explícita
        actual.setFkSeguridadEdicion(idUsuario);

        return repository.save(actual);
    }

    public void eliminar(Integer id) {
        repository.deleteById(id);
    }
}
