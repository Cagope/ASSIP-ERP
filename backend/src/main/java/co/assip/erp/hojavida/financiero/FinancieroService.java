package co.assip.erp.hojavida.financieros;

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

    public FinancieroService(FinancieroRepository repository) {
        this.repository = repository;
    }

    // ================================================================
    // 🔹 OPERACIONES BÁSICAS
    // ================================================================

    /** Listar todos los registros ordenados por fecha de edición */
    public List<Financiero> listar() {
        return repository.findAllByOrderByFechaEdicionDesc();
    }

    /** Buscar por ID */
    public Optional<Financiero> buscarPorId(Integer id) {
        return repository.findById(id);
    }

    /** Buscar por persona */
    public Optional<Financiero> buscarPorPersona(Integer idDatosPersonal) {
        return repository.findByIdDatosPersonal(idDatosPersonal);
    }

    /** Crear nuevo registro */
    public Financiero crear(Financiero nuevo) {
        validar(nuevo);
        nuevo.setFechaCreacion(Timestamp.valueOf(LocalDateTime.now()));
        nuevo.setFechaEdicion(Timestamp.valueOf(LocalDateTime.now()));
        return repository.save(nuevo);
    }

    /** Actualizar registro existente */
    public Optional<Financiero> actualizar(Integer id, Financiero actualizado) {
        return repository.findById(id).map(existente -> {
            validar(actualizado);
            actualizado.setIdFinanciero(id);
            actualizado.setFechaCreacion(existente.getFechaCreacion());
            actualizado.setFechaEdicion(Timestamp.valueOf(LocalDateTime.now()));
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
        // Comentario de ingresos solo si hay valor
        if (isPositive(f.getOtrosIngresos()) && (f.getComentarioOtrosIngresos() == null || f.getComentarioOtrosIngresos().isBlank())) {
            throw new IllegalArgumentException("Debe indicar un comentario para 'otros ingresos'.");
        }

        // Comentario de egresos solo si hay valor
        if (isPositive(f.getOtrosEgresos()) && (f.getComentarioOtrosEgresos() == null || f.getComentarioOtrosEgresos().isBlank())) {
            throw new IllegalArgumentException("Debe indicar un comentario para 'otros egresos'.");
        }

        // Relación financiera solo si hay deuda
        if (isPositive(f.getDeudaRelacionFinanciera()) && (f.getRelacionFinanciera() == null || f.getRelacionFinanciera().isBlank())) {
            throw new IllegalArgumentException("Debe indicar una descripción para 'relación financiera'.");
        }

        // Origen de fondos opcional pero recomendable
        if (f.getOrigenFondos() != null && f.getOrigenFondos().length() > 100) {
            throw new IllegalArgumentException("El campo 'origen de fondos' no puede superar los 100 caracteres.");
        }

        // Totales nunca negativos
        if (isNegative(f.getTotalActivos()) || isNegative(f.getTotalPasivos())) {
            throw new IllegalArgumentException("Los totales de activos y pasivos no pueden ser negativos.");
        }

        // Campos de auditoría mínimos
        if (f.getFkSeguridadCreacion() == null) f.setFkSeguridadCreacion(1);
        if (f.getFkSeguridadEdicion() == null) f.setFkSeguridadEdicion(1);
    }

    private boolean isPositive(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }

    private boolean isNegative(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) < 0;
    }
}
