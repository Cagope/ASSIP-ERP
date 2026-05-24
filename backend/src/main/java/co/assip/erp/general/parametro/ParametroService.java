package co.assip.erp.general.parametro;

import co.assip.erp.seguridad.service.UsuarioSesionService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ParametroService {

    private final ParametroRepository repository;
    private final UsuarioSesionService usuarioSesionService;

    public ParametroService(
            ParametroRepository repository,
            UsuarioSesionService usuarioSesionService
    ) {
        this.repository = repository;
        this.usuarioSesionService = usuarioSesionService;
    }

    /** 🔹 Listar todos los parámetros */
    public List<Parametro> listar() {
        return repository.findAll();
    }

    /** 🔹 Obtener parámetro por ID */
    public Parametro obtenerPorId(Integer id) {
        return repository.findById(id).orElseThrow(
                () -> new RuntimeException("Parametro no encontrado con id: " + id)
        );
    }

    /** 🔹 Crear nuevo parámetro */
    public Parametro guardar(Parametro parametro) {

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        // 🔐 Auditoría
        parametro.setFkSeguridadCreacion(idUsuario);
        parametro.setFkSeguridadEdicion(idUsuario);

        return repository.save(parametro);
    }

    /** 🔹 Actualizar un parámetro existente */
    public Parametro actualizar(Integer id, Parametro entrada) {

        Parametro actual = obtenerPorId(id);

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        actual.setIdAgencia(entrada.getIdAgencia());
        actual.setCodigoParametro(entrada.getCodigoParametro());
        actual.setNombreParametro(entrada.getNombreParametro());
        actual.setValorParametro(entrada.getValorParametro());
        actual.setTipoValor(entrada.getTipoValor());

        // 🔐 Auditoría explícita
        actual.setFkSeguridadEdicion(idUsuario);

        return repository.save(actual);
    }

    /** 🔹 Eliminar parámetro por ID */
    public void eliminar(Integer id) {
        repository.deleteById(id);
    }

    /** 🔍 Buscar un parámetro por idAgencia y código */
    public Optional<Parametro> obtenerPorAgenciaYCodigo(Integer idAgencia, Integer codigoParametro) {
        return repository.findByIdAgenciaAndCodigoParametro(idAgencia, codigoParametro);
    }
}
