package co.assip.erp.hojavida.permisosespeciales;

import co.assip.erp.seguridad.service.UsuarioSesionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PermisosEspecialesService {

    private final PermisosEspecialesRepository repository;
    private final UsuarioSesionService usuarioSesionService;

    public PermisosEspecialesService(
            PermisosEspecialesRepository repository,
            UsuarioSesionService usuarioSesionService
    ) {
        this.repository = repository;
        this.usuarioSesionService = usuarioSesionService;
    }

    public List<PermisosEspeciales> listar() {
        return repository.findAllByOrderByFechaEdicionDesc();
    }

    public Optional<PermisosEspeciales> buscarPorId(Integer id) {
        return repository.findById(id);
    }

    public Optional<PermisosEspeciales> buscarPorPersona(Integer idDatosPersonal) {
        return repository.findByIdDatosPersonal(idDatosPersonal);
    }

    /** 🔹 Crear nuevo registro */
    public PermisosEspeciales crear(PermisosEspeciales nuevo) {
        validarDatos(nuevo);

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        LocalDate hoy = LocalDate.now();
        LocalDateTime ahora = LocalDateTime.now();

        if (Boolean.TRUE.equals(nuevo.getRecibeLlamadas())) nuevo.setFechaLlamadas(hoy);
        if (Boolean.TRUE.equals(nuevo.getRecibeMsm())) nuevo.setFechaSms(hoy);
        if (Boolean.TRUE.equals(nuevo.getRecibeEmails())) nuevo.setFechaEmails(hoy);
        if (Boolean.TRUE.equals(nuevo.getRecibeCartas())) nuevo.setFechaCartas(hoy);
        if (Boolean.TRUE.equals(nuevo.getRecibeRedesSociales())) nuevo.setFechaRedes(hoy);

        nuevo.setFechaCreacion(java.sql.Timestamp.valueOf(ahora));
        nuevo.setFechaEdicion(java.sql.Timestamp.valueOf(ahora));

        // 🔐 Auditoría
        nuevo.setFkSeguridadCreacion(idUsuario);
        nuevo.setFkSeguridadEdicion(idUsuario);

        return repository.save(nuevo);
    }

    /** 🔹 Actualizar registro existente */
    public Optional<PermisosEspeciales> actualizar(Integer id, PermisosEspeciales actualizado) {
        return repository.findById(id).map(existente -> {

            validarDatos(actualizado);

            Integer idUsuario =
                    usuarioSesionService.idUsuario();

            LocalDate hoy = LocalDate.now();
            LocalDateTime ahora = LocalDateTime.now();

            if (!actualizado.getRecibeLlamadas().equals(existente.getRecibeLlamadas()))
                actualizado.setFechaLlamadas(hoy);
            else
                actualizado.setFechaLlamadas(existente.getFechaLlamadas());

            if (!actualizado.getRecibeMsm().equals(existente.getRecibeMsm()))
                actualizado.setFechaSms(hoy);
            else
                actualizado.setFechaSms(existente.getFechaSms());

            if (!actualizado.getRecibeEmails().equals(existente.getRecibeEmails()))
                actualizado.setFechaEmails(hoy);
            else
                actualizado.setFechaEmails(existente.getFechaEmails());

            if (!actualizado.getRecibeCartas().equals(existente.getRecibeCartas()))
                actualizado.setFechaCartas(hoy);
            else
                actualizado.setFechaCartas(existente.getFechaCartas());

            if (!actualizado.getRecibeRedesSociales().equals(existente.getRecibeRedesSociales()))
                actualizado.setFechaRedes(hoy);
            else
                actualizado.setFechaRedes(existente.getFechaRedes());

            actualizado.setIdPermisoEspecial(id);
            actualizado.setFechaCreacion(existente.getFechaCreacion());
            actualizado.setFechaEdicion(java.sql.Timestamp.valueOf(ahora));

            // 🔐 Auditoría
            actualizado.setFkSeguridadCreacion(existente.getFkSeguridadCreacion());
            actualizado.setFkSeguridadEdicion(idUsuario);

            return repository.save(actualizado);
        });
    }

    public boolean eliminar(Integer id) {
        if (!repository.existsById(id)) return false;
        repository.deleteById(id);
        return true;
    }

    // ============================================================== //
    // 🧠 VALIDACIONES DE NEGOCIO                                     //
    // ============================================================== //
    private void validarDatos(PermisosEspeciales p) {

        if (p.getRecibeLlamadas() == null) p.setRecibeLlamadas(false);
        if (p.getRecibeMsm() == null) p.setRecibeMsm(false);
        if (p.getRecibeEmails() == null) p.setRecibeEmails(false);
        if (p.getRecibeCartas() == null) p.setRecibeCartas(false);
        if (p.getRecibeRedesSociales() == null) p.setRecibeRedesSociales(false);

        if (p.getIdDatosPersonal() == null) {
            throw new IllegalArgumentException(
                    "Debe especificar la persona asociada al registro de permisos especiales."
            );
        }
    }
}
