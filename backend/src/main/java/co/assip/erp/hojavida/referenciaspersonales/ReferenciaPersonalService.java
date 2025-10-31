package co.assip.erp.hojavida.referenciaspersonales;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 💼 Servicio — Gestión de Referencias Personales
 * ------------------------------------------------------------
 * Encapsula la lógica de negocio y validaciones para la tabla:
 * hoja_vida.referencias_personales
 */
@Service
@Transactional
public class ReferenciaPersonalService {

    private final ReferenciaPersonalRepository repository;

    public ReferenciaPersonalService(ReferenciaPersonalRepository repository) {
        this.repository = repository;
    }

    /** 🔹 Listar todos ordenados por fecha de edición descendente */
    public List<ReferenciaPersonal> listar() {
        return repository.findAllByOrderByFechaEdicionDesc();
    }

    /** 🔹 Buscar por ID */
    public Optional<ReferenciaPersonal> buscarPorId(Integer id) {
        return repository.findById(id);
    }

    /** 🔹 Listar por persona */
    public List<ReferenciaPersonal> listarPorPersona(Integer idDatosPersonal) {
        return repository.findByIdDatosPersonal(idDatosPersonal);
    }

    /** 🔹 Listar por departamento */
    public List<ReferenciaPersonal> listarPorDepartamento(Integer idDepartamento) {
        return repository.findByIdDepartamento(idDepartamento);
    }

    /** 🔹 Listar por ciudad */
    public List<ReferenciaPersonal> listarPorCiudad(Integer idCiudad) {
        return repository.findByIdCiudad(idCiudad);
    }

    /** 🔹 Crear nuevo registro */
    public ReferenciaPersonal crear(ReferenciaPersonal nueva) {
        validarContactos(nueva);

        nueva.setFechaCreacion(LocalDateTime.now());
        nueva.setFechaEdicion(LocalDateTime.now());
        return repository.save(nueva);
    }

    /** 🔹 Actualizar registro existente */
    public Optional<ReferenciaPersonal> actualizar(Integer id, ReferenciaPersonal actualizada) {
        return repository.findById(id).map(existente -> {
            validarContactos(actualizada);

            actualizada.setIdReferenciaPersonal(id);
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

    private void validarContactos(ReferenciaPersonal r) {
        if (r.getTelefonoReferenciaPersonal() != null && !r.getTelefonoReferenciaPersonal().isBlank()) {
            if (!r.getTelefonoReferenciaPersonal().matches("^[0-9]{7}$")) {
                throw new IllegalArgumentException("El teléfono debe tener exactamente 7 dígitos numéricos.");
            }
        } else {
            r.setTelefonoReferenciaPersonal(null);
        }

        if (r.getCelularReferenciaPersonal() != null && !r.getCelularReferenciaPersonal().isBlank()) {
            if (!r.getCelularReferenciaPersonal().matches("^[0-9]{10}$")) {
                throw new IllegalArgumentException("El celular debe tener exactamente 10 dígitos numéricos.");
            }
        } else {
            r.setCelularReferenciaPersonal(null);
        }

        if (r.getDireccionReferenciaPersonal() == null || r.getDireccionReferenciaPersonal().isBlank()) {
            throw new IllegalArgumentException("La dirección es obligatoria.");
        }

        if (r.getNombreReferenciaPersonal() == null || r.getNombreReferenciaPersonal().isBlank()) {
            throw new IllegalArgumentException("El nombre de la referencia es obligatorio.");
        }
    }
}
