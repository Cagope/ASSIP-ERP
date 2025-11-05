package co.assip.erp.hojavida.permisosespeciales;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 💬 Servicio — Permisos Especiales
 * ------------------------------------------------------------
 * Gestiona los permisos de comunicación (llamadas, SMS, emails,
 * cartas, redes sociales) de cada persona (1:1 con datos_personales).
 *
 * Cada canal tiene su fecha individual de autorización o cambio.
 */
@Service
@Transactional
public class PermisosEspecialesService {

    private final PermisosEspecialesRepository repository;

    public PermisosEspecialesService(PermisosEspecialesRepository repository) {
        this.repository = repository;
    }

    /** 🔹 Listar todos los registros ordenados por fecha de edición descendente */
    public List<PermisosEspeciales> listar() {
        return repository.findAllByOrderByFechaEdicionDesc();
    }

    /** 🔹 Buscar por ID */
    public Optional<PermisosEspeciales> buscarPorId(Integer id) {
        return repository.findById(id);
    }

    /** 🔹 Buscar por persona (id_datos_personal) */
    public Optional<PermisosEspeciales> buscarPorPersona(Integer idDatosPersonal) {
        return repository.findByIdDatosPersonal(idDatosPersonal);
    }

    /** 🔹 Crear nuevo registro */
    public PermisosEspeciales crear(PermisosEspeciales nuevo) {
        validarDatos(nuevo);

        LocalDate hoy = LocalDate.now();
        LocalDateTime ahora = LocalDateTime.now();

        // 🟢 Inicializa fechas según los permisos activos
        if (Boolean.TRUE.equals(nuevo.getRecibeLlamadas())) nuevo.setFechaLlamadas(hoy);
        if (Boolean.TRUE.equals(nuevo.getRecibeMsm())) nuevo.setFechaSms(hoy);
        if (Boolean.TRUE.equals(nuevo.getRecibeEmails())) nuevo.setFechaEmails(hoy);
        if (Boolean.TRUE.equals(nuevo.getRecibeCartas())) nuevo.setFechaCartas(hoy);
        if (Boolean.TRUE.equals(nuevo.getRecibeRedesSociales())) nuevo.setFechaRedes(hoy);

        // 🕓 Auditoría
        nuevo.setFechaCreacion(java.sql.Timestamp.valueOf(ahora));
        nuevo.setFechaEdicion(java.sql.Timestamp.valueOf(ahora));

        return repository.save(nuevo);
    }

    /** 🔹 Actualizar registro existente */
    public Optional<PermisosEspeciales> actualizar(Integer id, PermisosEspeciales actualizado) {
        return repository.findById(id).map(existente -> {

            validarDatos(actualizado);
            LocalDate hoy = LocalDate.now();
            LocalDateTime ahora = LocalDateTime.now();

            // 🔸 Actualiza fecha solo si cambió el valor del permiso
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

            // 🧩 Auditoría
            actualizado.setIdPermisoEspecial(id);
            actualizado.setFechaCreacion(existente.getFechaCreacion());
            actualizado.setFechaEdicion(java.sql.Timestamp.valueOf(ahora));

            return repository.save(actualizado);
        });
    }

    /** 🔹 Eliminar registro */
    public boolean eliminar(Integer id) {
        if (!repository.existsById(id)) return false;
        repository.deleteById(id);
        return true;
    }

    // ============================================================== //
    // 🧠 VALIDACIONES DE NEGOCIO                                     //
    // ============================================================== //
    private void validarDatos(PermisosEspeciales p) {

        // 🟩 Si algún valor booleano viene nulo, establecerlo en falso
        if (p.getRecibeLlamadas() == null) p.setRecibeLlamadas(false);
        if (p.getRecibeMsm() == null) p.setRecibeMsm(false);
        if (p.getRecibeEmails() == null) p.setRecibeEmails(false);
        if (p.getRecibeCartas() == null) p.setRecibeCartas(false);
        if (p.getRecibeRedesSociales() == null) p.setRecibeRedesSociales(false);

        // 🧾 Validación de relación obligatoria
        if (p.getIdDatosPersonal() == null) {
            throw new IllegalArgumentException("Debe especificar la persona asociada al registro de permisos especiales.");
        }

        // 🧩 Auditoría
        if (p.getFkSeguridadCreacion() == null) p.setFkSeguridadCreacion(1);
        if (p.getFkSeguridadEdicion() == null) p.setFkSeguridadEdicion(1);
    }
}
