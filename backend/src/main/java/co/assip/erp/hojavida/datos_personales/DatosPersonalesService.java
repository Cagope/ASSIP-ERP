package co.assip.erp.hojavida.datos_personales;

import co.assip.erp.seguridad.utils.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DatosPersonalesService {

    private final DatosPersonalesRepository repository;

    public DatosPersonalesService(DatosPersonalesRepository repository) {
        this.repository = repository;
    }

    // ==========================================================
    // 🔹 CONSULTAS
    // ==========================================================

    public List<DatosPersonales> listar() {
        return repository.findAll();
    }

    public Optional<DatosPersonales> buscarPorId(Integer id) {
        return repository.findById(id);
    }

    // ==========================================================
    // 🔹 CREACIÓN
    // ==========================================================

    public DatosPersonales crear(DatosPersonales nuevo) {
        normalizarCampos(nuevo);
        aplicarReglasGenero(nuevo);

        Integer idUsuario = SecurityUtils.getIdUsuario();

        LocalDate hoy = LocalDate.now();
        nuevo.setFechaCreacion(LocalDateTime.now());
        nuevo.setFechaEdicion(LocalDateTime.now());
        if (nuevo.getFechaApertura() == null) nuevo.setFechaApertura(hoy);
        if (nuevo.getFechaActualizacion() == null) nuevo.setFechaActualizacion(hoy);

        // 🔐 Auditoría
        nuevo.setFkSeguridadCreacion(idUsuario);
        nuevo.setFkSeguridadEdicion(idUsuario);

        return repository.save(nuevo);
    }

    // ==========================================================
    // 🔹 ACTUALIZACIÓN
    // ==========================================================

    public Optional<DatosPersonales> actualizar(Integer id, DatosPersonales actualizado) {
        return repository.findById(id).map(existente -> {
            normalizarCampos(actualizado);
            aplicarReglasGenero(actualizado);

            Integer idUsuario = SecurityUtils.getIdUsuario();

            actualizado.setIdDatosPersonal(id);
            actualizado.setFechaCreacion(existente.getFechaCreacion());
            actualizado.setFechaApertura(
                    actualizado.getFechaApertura() != null
                            ? actualizado.getFechaApertura()
                            : existente.getFechaApertura()
            );
            actualizado.setFechaActualizacion(LocalDate.now());
            actualizado.setFechaEdicion(LocalDateTime.now());

            // 🔐 Auditoría
            actualizado.setFkSeguridadCreacion(existente.getFkSeguridadCreacion());
            actualizado.setFkSeguridadEdicion(idUsuario);

            return repository.save(actualizado);
        });
    }

    // ==========================================================
    // 🔹 ELIMINACIÓN
    // ==========================================================

    public boolean eliminar(Integer id) {
        if (!repository.existsById(id)) return false;
        repository.deleteById(id);
        return true;
    }

    // ==========================================================
    // 🧩 REGLAS DE NEGOCIO INTERNAS
    // ==========================================================

    private void normalizarCampos(DatosPersonales p) {
        if (p.getNombres() != null)
            p.setNombres(p.getNombres().trim().toUpperCase());
        if (p.getPrimerApellido() != null)
            p.setPrimerApellido(p.getPrimerApellido().trim().toUpperCase());
        if (p.getSegundoApellido() != null && !p.getSegundoApellido().isBlank())
            p.setSegundoApellido(p.getSegundoApellido().trim().toUpperCase());
        else
            p.setSegundoApellido(null);
        if (p.getComentario() != null)
            p.setComentario(p.getComentario().trim());

        if (Boolean.FALSE.equals(p.getTieneRut())) {
            p.setDigitoVerificacion(null);
        }
    }

    private void aplicarReglasGenero(DatosPersonales p) {
        if ("1".equals(p.getCodigoGenero())) {
            p.setCabezaFamilia("0");
        }
    }
}
