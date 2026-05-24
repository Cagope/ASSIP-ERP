package co.assip.erp.hojavida.financieros;

import co.assip.erp.seguridad.service.UsuarioSesionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class FinancieroService {

    private final FinancieroRepository repository;
    private final UsuarioSesionService usuarioSesionService;

    public FinancieroService(
            FinancieroRepository repository,
            UsuarioSesionService usuarioSesionService
    ) {
        this.repository = repository;
        this.usuarioSesionService = usuarioSesionService;
    }

    // ================================================================
    // 🔹 OPERACIONES BÁSICAS
    // ================================================================

    public List<Financiero> listar() {
        return repository.findAllByOrderByFechaEdicionDesc();
    }

    public Optional<Financiero> buscarPorId(Integer id) {
        return repository.findById(id);
    }

    public Optional<Financiero> buscarPorPersona(Integer idDatosPersonal) {
        return repository.findByIdDatosPersonal(idDatosPersonal);
    }

    /** Crear nuevo registro */
    public Financiero crear(Financiero nuevo) {
        validar(nuevo);

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        nuevo.setFechaCreacion(Timestamp.valueOf(LocalDateTime.now()));
        nuevo.setFechaEdicion(Timestamp.valueOf(LocalDateTime.now()));

        // 🔐 Auditoría
        nuevo.setFkSeguridadCreacion(idUsuario);
        nuevo.setFkSeguridadEdicion(idUsuario);

        return repository.save(nuevo);
    }

    /** Actualizar registro existente */
    public Optional<Financiero> actualizar(Integer id, Financiero actualizado) {
        return repository.findById(id).map(existente -> {
            validar(actualizado);

            Integer idUsuario =
                    usuarioSesionService.idUsuario();

            actualizado.setIdFinanciero(id);
            actualizado.setFechaCreacion(existente.getFechaCreacion());
            actualizado.setFechaEdicion(Timestamp.valueOf(LocalDateTime.now()));

            // 🔐 Auditoría
            actualizado.setFkSeguridadCreacion(existente.getFkSeguridadCreacion());
            actualizado.setFkSeguridadEdicion(idUsuario);

            return repository.save(actualizado);
        });
    }

    /** Eliminar registro */
    public boolean eliminar(Integer id) {
        if (!repository.existsById(id)) return false;
        repository.deleteById(id);
        return true;
    }

    // ================================================================
    // 🧩 VALIDACIONES DE NEGOCIO
    // ================================================================

    private void validar(Financiero f) {

        if (isPositive(f.getOtrosIngresos()) &&
                (f.getComentarioOtrosIngresos() == null || f.getComentarioOtrosIngresos().isBlank())) {
            throw new IllegalArgumentException("Debe indicar un comentario para 'otros ingresos'.");
        }

        if (isPositive(f.getOtrosEgresos()) &&
                (f.getComentarioOtrosEgresos() == null || f.getComentarioOtrosEgresos().isBlank())) {
            throw new IllegalArgumentException("Debe indicar un comentario para 'otros egresos'.");
        }

        if (isPositive(f.getDeudaRelacionFinanciera()) &&
                (f.getRelacionFinanciera() == null || f.getRelacionFinanciera().isBlank())) {
            throw new IllegalArgumentException("Debe indicar una descripción para 'relación financiera'.");
        }

        if (f.getOrigenFondos() != null && f.getOrigenFondos().length() > 100) {
            throw new IllegalArgumentException("El campo 'origen de fondos' no puede superar los 100 caracteres.");
        }

        if (isNegative(f.getTotalActivos()) || isNegative(f.getTotalPasivos())) {
            throw new IllegalArgumentException("Los totales de activos y pasivos no pueden ser negativos.");
        }
    }

    private boolean isPositive(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }

    private boolean isNegative(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) < 0;
    }
}
