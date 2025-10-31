package co.assip.erp.hojavida.sarlaft;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SarlaftService {

    private final SarlaftRepository repository;

    public SarlaftService(SarlaftRepository repository) {
        this.repository = repository;
    }

    /** 🔹 Listar todos los registros ordenados por fecha de edición descendente */
    public List<Sarlaft> listar() {
        return repository.findAllByOrderByFechaEdicionDesc();
    }

    /** 🔹 Buscar por ID */
    public Optional<Sarlaft> buscarPorId(Integer id) {
        return repository.findById(id);
    }

    /** 🔹 Buscar por persona (idDatosPersonal) */
    public Optional<Sarlaft> buscarPorPersona(Integer idDatosPersonal) {
        return repository.findByIdDatosPersonal(idDatosPersonal);
    }

    /** 🔹 Crear nuevo registro */
    public Sarlaft crear(Sarlaft nuevo) {
        validarDatos(nuevo);

        nuevo.setFechaCreacion(java.sql.Timestamp.valueOf(LocalDateTime.now()));
        nuevo.setFechaEdicion(java.sql.Timestamp.valueOf(LocalDateTime.now()));
        return repository.save(nuevo);
    }

    /** 🔹 Actualizar registro existente */
    public Optional<Sarlaft> actualizar(Integer id, Sarlaft actualizado) {
        return repository.findById(id).map(existente -> {
            validarDatos(actualizado);

            actualizado.setIdSarlaft(id);
            actualizado.setFechaCreacion(existente.getFechaCreacion());
            actualizado.setFechaEdicion(java.sql.Timestamp.valueOf(LocalDateTime.now()));
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
    // 🧩 VALIDACIONES DE NEGOCIO
    // ================================================================
    private void validarDatos(Sarlaft s) {
        LocalDate hoy = LocalDate.now();

        // 🟩 Exoneración
        if (Boolean.TRUE.equals(s.getExoneracionUiaf()) && s.getFechaExoneracion() == null) {
            s.setFechaExoneracion(hoy);
        }

        // 🟦 Asociado PEPS
        if (Boolean.TRUE.equals(s.getAsociadoPeps())) {
            if (s.getTipoPeps() == null || s.getTipoPeps().isBlank()) {
                throw new IllegalArgumentException("Debe seleccionar el tipo de PEPS del asociado.");
            }
            if (s.getFechaInicialPeps() == null) s.setFechaInicialPeps(hoy);
            if (s.getFechaFinalPeps() == null) s.setFechaFinalPeps(hoy);
        } else {
            s.setTipoPeps("000");
            s.setObservacionesPeps(null);
            s.setFechaInicialPeps(null);
            s.setFechaFinalPeps(null);
        }

        // 🟨 Familiares PEPS
        if (Boolean.TRUE.equals(s.getFamiliaPeps())) {
            if (s.getTipoFamiliaPeps() == null || s.getTipoFamiliaPeps().isBlank()) {
                throw new IllegalArgumentException("Debe seleccionar el tipo de PEPS del familiar.");
            }
            if (s.getCodigoParentesco() == null || s.getCodigoParentesco().isBlank()) {
                throw new IllegalArgumentException("Debe seleccionar el parentesco del familiar PEPS.");
            }
        } else {
            s.setTipoFamiliaPeps("000");
            s.setCodigoParentesco("0");
            s.setCedulaFamiliaPeps(null);
            s.setNombreFamiliaPeps(null);
        }

        // 💱 Moneda extranjera
        if (Boolean.FALSE.equals(s.getMonedaExtranjera())) {
            s.setObservacionMonedaExtranjera(null);
        }

        // 🌎 Cuentas en el extranjero
        if (Boolean.FALSE.equals(s.getCuentaExtranjero())) {
            s.setTipoMonedaExtranjera(null);
            s.setNumeroCuentaExtranjero(null);
            s.setNombreBancoExtranjero(null);
            s.setCiudadCuentaExtranjero(null);
            s.setPaisCuentaExtranjero(null);
        }

        // 🧾 Seguridad y auditoría
        if (s.getFkSeguridadCreacion() == null) s.setFkSeguridadCreacion(1);
        if (s.getFkSeguridadEdicion() == null) s.setFkSeguridadEdicion(1);
    }
}
