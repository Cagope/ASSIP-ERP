// src/main/java/co/assip/erp/hojavida/service/impl/ReferenciaPersonalServiceImpl.java
package co.assip.erp.hojavida.service.impl;

import co.assip.erp.hojavida.domain.ReferenciaPersonal;
import co.assip.erp.hojavida.dto.ReferenciasPersonalesDTOs.ReferenciaPersonalRequest;
import co.assip.erp.hojavida.dto.ReferenciasPersonalesDTOs.ReferenciaPersonalResponse;
import co.assip.erp.hojavida.repository.ReferenciaPersonalRepository;
import co.assip.erp.hojavida.service.ReferenciaPersonalService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

@Service
@Transactional
public class ReferenciaPersonalServiceImpl implements ReferenciaPersonalService {

    private final ReferenciaPersonalRepository repo;

    public ReferenciaPersonalServiceImpl(ReferenciaPersonalRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReferenciaPersonalResponse> getByPersona(Integer idDatosPersonal) {
        return repo.findByIdDatosPersonal(idDatosPersonal).map(this::toResponse);
    }

    @Override
    public Integer createForPersona(Integer idDatosPersonal, Integer idUsuario, ReferenciaPersonalRequest req) {
        if (repo.existsByIdDatosPersonal(idDatosPersonal)) {
            throw new IllegalStateException("Ya existe una referencia para esta persona.");
        }
        ReferenciaPersonal e = new ReferenciaPersonal();
        e.setIdDatosPersonal(idDatosPersonal);
        applyReq(e, req, true, idUsuario);
        e = repo.save(e);
        return e.getIdReferenciaPersonal();
    }

    @Override
    public void updateForPersona(Integer idDatosPersonal, Integer idReferencia, Integer idUsuario, ReferenciaPersonalRequest req) {
        ReferenciaPersonal e = repo.findByIdReferenciaPersonalAndIdDatosPersonal(idReferencia, idDatosPersonal)
                .orElseThrow(() -> new IllegalArgumentException("Referencia no encontrada para esta persona."));
        applyReq(e, req, false, idUsuario);
        repo.save(e);
    }

    @Override
    public void deleteForPersona(Integer idDatosPersonal, Integer idReferencia) {
        // Buscar garantizando pertenencia
        ReferenciaPersonal e = repo.findByIdReferenciaPersonalAndIdDatosPersonal(idReferencia, idDatosPersonal)
                .orElseThrow(() -> new IllegalArgumentException("Referencia no encontrada para esta persona."));
        // Borrar por entidad (evita problemas de cast en métodos derivados)
        repo.delete(e);
    }

    // ---------- helpers ----------

    private void applyReq(ReferenciaPersonal e, ReferenciaPersonalRequest r, boolean creating, Integer idUsuario) {
        // OJO: tras @JsonAlias en el DTO, usamos los nombres camelCase
        e.setNombreReferenciaPersonal(safeUpper(r.nombreReferenciaPersonal));
        e.setDireccionReferenciaPersonal(safeUpper(r.direccionReferenciaPersonal));
        e.setIdDepartamento(r.idDepartamento);
        e.setIdCiudad(r.idCiudad);
        e.setTelefonoReferenciaPersonal(emptyToNull(r.telefonoReferenciaPersonal));
        e.setCelularReferenciaPersonal(emptyToNull(r.celularReferenciaPersonal));

        if (creating) {
            e.setFkSeguridadCreacion(idUsuario);
            e.setFechaCreacion(LocalDateTime.now());
        }
        e.setFkSeguridadActualizacion(idUsuario);
        e.setFechaActualizacion(LocalDateTime.now());
    }

    private ReferenciaPersonalResponse toResponse(ReferenciaPersonal e) {
        ReferenciaPersonalResponse r = new ReferenciaPersonalResponse();
        r.id_referencia_personal = e.getIdReferenciaPersonal();
        r.id_datos_personal = e.getIdDatosPersonal();
        r.nombre_referencia_personal = e.getNombreReferenciaPersonal();
        r.direccion_referencia_personal = e.getDireccionReferenciaPersonal();
        r.id_departamento = e.getIdDepartamento();
        r.id_ciudad = e.getIdCiudad();
        r.telefono_referencia_personal = e.getTelefonoReferenciaPersonal();
        r.celular_referencia_personal = e.getCelularReferenciaPersonal();

        // NUEVO: resolver nombre_ciudad si hay id_ciudad
        if (e.getIdCiudad() != null) {
            try {
                String nombre = repo.findNombreCiudadById(e.getIdCiudad());
                r.nombre_ciudad = (nombre != null && !nombre.isBlank()) ? nombre : null;
            } catch (Exception ignored) {
                // Silencioso: si algo falla, dejamos nombre_ciudad = null
            }
        } else {
            r.nombre_ciudad = null;
        }

        return r;
    }

    private static String safeUpper(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t.toUpperCase(Locale.ROOT);
    }

    private static String emptyToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
