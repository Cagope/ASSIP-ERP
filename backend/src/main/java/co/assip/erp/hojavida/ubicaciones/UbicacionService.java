package co.assip.erp.hojavida.ubicaciones;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UbicacionService {

    private final UbicacionRepository repository;

    public UbicacionService(UbicacionRepository repository) {
        this.repository = repository;
    }

    /** 🔹 Listar todos ordenados por fecha de edición descendente */
    public List<Ubicacion> listar() {
        return repository.findAllByOrderByFechaEdicionDesc();
    }

    /** 🔹 Buscar por ID */
    public Optional<Ubicacion> buscarPorId(Integer id) {
        return repository.findById(id);
    }

    /** 🔹 Listar por zona */
    public List<Ubicacion> listarPorZona(Integer idZona) {
        return repository.findByIdZona(idZona);
    }

    /** 🔹 Listar por subzona */
    public List<Ubicacion> listarPorSubZona(Integer idSubZona) {
        return repository.findByIdSubZona(idSubZona);
    }

    /** 🔹 Crear nuevo registro */
    public Ubicacion crear(Ubicacion nueva) {
        validarContactos(nueva);

        nueva.setFechaCreacion(LocalDateTime.now());
        nueva.setFechaEdicion(LocalDateTime.now());
        return repository.save(nueva);
    }

    /** 🔹 Actualizar registro existente */
    public Optional<Ubicacion> actualizar(Integer id, Ubicacion actualizada) {
        return repository.findById(id).map(existente -> {
            validarContactos(actualizada);

            actualizada.setIdUbicacion(id);
            actualizada.setFechaCreacion(existente.getFechaCreacion());
            actualizada.setFechaEdicion(LocalDateTime.now());
            return repository.save(actualizada);
        });
    }

    /** 🔹 Eliminar registro */
    public boolean eliminar(Integer id) {
        if (!repository.existsById(id)) return false;
        repository.deleteById(id);
        return true;
    }

    // ================================================================
    // 🧩 VALIDACIONES DE NEGOCIO
    // ================================================================

    private void validarContactos(Ubicacion u) {
        if (u.getTelefono() != null && !u.getTelefono().isBlank()) {
            if (!u.getTelefono().matches("^[0-9]{7}$")) {
                throw new IllegalArgumentException("El teléfono debe tener exactamente 7 dígitos numéricos.");
            }
        } else {
            u.setTelefono(null);
        }

        if (u.getCelularUno() != null && !u.getCelularUno().isBlank()) {
            if (!u.getCelularUno().matches("^[0-9]{10}$")) {
                throw new IllegalArgumentException("El celular uno debe tener 10 dígitos numéricos.");
            }
        } else {
            u.setCelularUno(null);
        }

        if (u.getCelularDos() != null && !u.getCelularDos().isBlank()) {
            if (!u.getCelularDos().matches("^[0-9]{10}$")) {
                throw new IllegalArgumentException("El celular dos debe tener 10 dígitos numéricos.");
            }
        } else {
            u.setCelularDos(null);
        }

        if (u.getCorreo() != null && !u.getCorreo().isBlank()) {
            if (!u.getCorreo().matches("^[^@]+@[^@]+\\.[^@]+$")) {
                throw new IllegalArgumentException("El correo electrónico no tiene un formato válido.");
            }
        } else {
            u.setCorreo(null);
        }
    }
}
