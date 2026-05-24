package co.assip.erp.hojavida.datosfamiliares;

import co.assip.erp.seguridad.service.UsuarioSesionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DatosFamiliarService {

    private final DatosFamiliarRepository repository;
    private final UsuarioSesionService usuarioSesionService;

    public DatosFamiliarService(
            DatosFamiliarRepository repository,
            UsuarioSesionService usuarioSesionService
    ) {
        this.repository = repository;
        this.usuarioSesionService = usuarioSesionService;
    }

    /** 🔹 Listar todos ordenados por fecha de edición */
    public List<DatosFamiliar> listar() {
        return repository.findAllByOrderByFechaEdicionDesc();
    }

    /** 🔹 Buscar por ID */
    public Optional<DatosFamiliar> buscarPorId(Integer id) {
        return repository.findById(id);
    }

    /** 🔹 Listar por persona */
    public List<DatosFamiliar> listarPorPersona(Integer idDatosPersonal) {
        return repository.findByIdDatosPersonal(idDatosPersonal);
    }

    /** 🔹 Listar por parentesco */
    public List<DatosFamiliar> listarPorParentesco(String codigoParentesco) {
        return repository.findByCodigoParentesco(codigoParentesco);
    }

    /** 🔹 Listar solo referencias familiares marcadas */
    public List<DatosFamiliar> listarReferencias() {
        return repository.findByReferenciaFamiliarTrue();
    }

    /** 🔹 Crear nuevo registro */
    public DatosFamiliar crear(DatosFamiliar nuevo) {
        validarTelefonos(nuevo);

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        nuevo.setFechaCreacion(LocalDateTime.now());
        nuevo.setFechaEdicion(LocalDateTime.now());

        // 🔐 Auditoría
        nuevo.setFkSeguridadCreacion(idUsuario);
        nuevo.setFkSeguridadEdicion(idUsuario);

        return repository.save(nuevo);
    }

    /** 🔹 Actualizar existente */
    public Optional<DatosFamiliar> actualizar(Integer id, DatosFamiliar actualizado) {
        return repository.findById(id).map(existente -> {
            validarTelefonos(actualizado);

            Integer idUsuario =
                    usuarioSesionService.idUsuario();

            actualizado.setIdDatosFamiliares(id);
            actualizado.setFechaCreacion(existente.getFechaCreacion());
            actualizado.setFechaEdicion(LocalDateTime.now());

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

    // ==============================================================
    // 🧩 VALIDACIONES
    // ==============================================================

    private void validarTelefonos(DatosFamiliar d) {
        if (d.getTelefonoDatosFamiliar() != null && !d.getTelefonoDatosFamiliar().isBlank()) {
            if (!d.getTelefonoDatosFamiliar().matches("^[0-9]{7}$")) {
                throw new IllegalArgumentException("El teléfono debe tener exactamente 7 dígitos numéricos.");
            }
        } else {
            d.setTelefonoDatosFamiliar(null);
        }

        if (d.getCelularDatosFamiliar() != null && !d.getCelularDatosFamiliar().isBlank()) {
            if (!d.getCelularDatosFamiliar().matches("^[0-9]{10}$")) {
                throw new IllegalArgumentException("El celular debe tener 10 dígitos numéricos.");
            }
        } else {
            d.setCelularDatosFamiliar(null);
        }
    }
}
