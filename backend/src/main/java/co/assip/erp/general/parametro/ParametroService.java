package co.assip.erp.general.parametro;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

/**
 * 💼 Servicio — Parametros
 * ------------------------------------------------------------
 * Gestiona la lógica de negocio para la tabla general.parametros.
 */
@Service
public class ParametroService {

    private final ParametroRepository repository;

    public ParametroService(ParametroRepository repository) {
        this.repository = repository;
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
        return repository.save(parametro);
    }

    /** 🔹 Actualizar un parámetro existente */
    public Parametro actualizar(Integer id, Parametro entrada) {
        Parametro actual = obtenerPorId(id);

        actual.setIdAgencia(entrada.getIdAgencia());
        actual.setCodigoParametro(entrada.getCodigoParametro());
        actual.setNombreParametro(entrada.getNombreParametro());
        actual.setValorParametro(entrada.getValorParametro());
        actual.setTipoValor(entrada.getTipoValor());

        // Auditoría manejada por BaseAudit
        return repository.save(actual);
    }

    /** 🔹 Eliminar parámetro por ID */
    public void eliminar(Integer id) {
        repository.deleteById(id);
    }

    // =========================================================
    // 🔸 NUEVO MÉTODO: Buscar por agencia y código
    // =========================================================
    /**
     * 🔍 Buscar un parámetro por idAgencia y código.
     * Devuelve Optional.empty() si no existe.
     */
    public Optional<Parametro> obtenerPorAgenciaYCodigo(Integer idAgencia, Integer codigoParametro) {
        return repository.findByIdAgenciaAndCodigoParametro(idAgencia, codigoParametro);
    }
}
