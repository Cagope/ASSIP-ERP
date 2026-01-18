package co.assip.erp.general.agencia;

import co.assip.erp.seguridad.utils.SecurityUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AgenciaService {

    private final AgenciaRepository repository;

    public AgenciaService(AgenciaRepository repository) {
        this.repository = repository;
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

        Integer idUsuario = SecurityUtils.getIdUsuario();
        if (idUsuario == null) {
            throw new RuntimeException("USUARIO_NO_AUTENTICADO");
        }

        // 🔐 Auditoría
        agencia.setFkSeguridadCreacion(idUsuario);
        agencia.setFkSeguridadEdicion(idUsuario);

        return repository.save(agencia);
    }

    public Agencia actualizar(Integer id, Agencia entrada) {

        Agencia actual = obtenerPorId(id);

        Integer idUsuario = SecurityUtils.getIdUsuario();
        if (idUsuario == null) {
            throw new RuntimeException("USUARIO_NO_AUTENTICADO");
        }

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
