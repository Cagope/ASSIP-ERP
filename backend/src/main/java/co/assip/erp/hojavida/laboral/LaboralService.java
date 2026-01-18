package co.assip.erp.hojavida.laboral;

import co.assip.erp.seguridad.utils.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class LaboralService {

    private final LaboralRepository repository;

    public LaboralService(LaboralRepository repository) {
        this.repository = repository;
    }

    /** 🔹 Listar todos los registros ordenados por fecha de edición descendente */
    public List<Laboral> listar() {
        return repository.findAllByOrderByFechaEdicionDesc();
    }

    /** 🔹 Buscar por ID */
    public Optional<Laboral> buscarPorId(Integer id) {
        return repository.findById(id);
    }

    /** 🔹 Buscar por persona (idDatosPersonal) */
    public Optional<Laboral> buscarPorPersona(Integer idDatosPersonal) {
        return repository.findByIdDatosPersonal(idDatosPersonal);
    }

    /** 🔹 Crear nuevo registro */
    public Laboral crear(Laboral nuevo) {
        validarDatos(nuevo);

        Integer idUsuario = SecurityUtils.getIdUsuario();

        nuevo.setFechaCreacion(java.sql.Timestamp.valueOf(LocalDateTime.now()));
        nuevo.setFechaEdicion(java.sql.Timestamp.valueOf(LocalDateTime.now()));

        // 🔐 Auditoría
        nuevo.setFkSeguridadCreacion(idUsuario);
        nuevo.setFkSeguridadEdicion(idUsuario);

        return repository.save(nuevo);
    }

    /** 🔹 Actualizar registro existente */
    public Optional<Laboral> actualizar(Integer id, Laboral actualizado) {
        return repository.findById(id).map(existente -> {
            validarDatos(actualizado);

            Integer idUsuario = SecurityUtils.getIdUsuario();

            actualizado.setIdLaboral(id);
            actualizado.setFechaCreacion(existente.getFechaCreacion());
            actualizado.setFechaEdicion(java.sql.Timestamp.valueOf(LocalDateTime.now()));

            // 🔐 Auditoría
            actualizado.setFkSeguridadCreacion(existente.getFkSeguridadCreacion());
            actualizado.setFkSeguridadEdicion(idUsuario);

            return repository.save(actualizado);
        });
    }

    /** 🔹 Eliminar registro */
    public boolean eliminar(Integer id) {
        if (!repository.existsById(id)) return false;
        repository.deleteById(id);
        return true;
    }

    // ================================================================
    // 🧠 VALIDACIONES DE NEGOCIO
    // ================================================================

    private void validarDatos(Laboral l) {

        if (l.getNombreEmpresa() == null || l.getNombreEmpresa().isBlank()) {
            throw new IllegalArgumentException("El nombre de la empresa es obligatorio.");
        }

        if (l.getDireccion() == null || l.getDireccion().isBlank()) {
            throw new IllegalArgumentException("La dirección de la empresa es obligatoria.");
        }

        if (l.getTelefonoEmpresa() != null && !l.getTelefonoEmpresa().isBlank()) {
            if (!l.getTelefonoEmpresa().matches("^[0-9]{7}$")) {
                throw new IllegalArgumentException("El teléfono de la empresa debe tener exactamente 7 dígitos numéricos.");
            }
        } else {
            l.setTelefonoEmpresa(null);
        }

        if (l.getCelularEmpresa() != null && !l.getCelularEmpresa().isBlank()) {
            if (!l.getCelularEmpresa().matches("^[0-9]{10}$")) {
                throw new IllegalArgumentException("El celular de la empresa debe tener 10 dígitos numéricos.");
            }
        } else {
            l.setCelularEmpresa(null);
        }

        if (l.getCelularContacto() != null && !l.getCelularContacto().isBlank()) {
            if (!l.getCelularContacto().matches("^[0-9]{10}$")) {
                throw new IllegalArgumentException("El celular de contacto debe tener 10 dígitos numéricos.");
            }
        } else {
            l.setCelularContacto(null);
        }

        if (l.getCorreoEmpresa() != null && !l.getCorreoEmpresa().isBlank()) {
            if (!l.getCorreoEmpresa().matches("^[^@]+@[^@]+\\.[^@]+$")) {
                throw new IllegalArgumentException("El correo electrónico de la empresa no tiene un formato válido.");
            }
        } else {
            l.setCorreoEmpresa(null);
        }

        if (l.getFechaVinculacion() == null) {
            l.setFechaVinculacion(LocalDateTime.now().toLocalDate());
        }
    }
}
