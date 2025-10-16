package co.assip.erp.hojavida.service.impl;

import co.assip.erp.hojavida.domain.PermisoEspecial;
import co.assip.erp.hojavida.dto.PermisosEspecialesDTOs.PermisoEspecialRequest;
import co.assip.erp.hojavida.dto.PermisosEspecialesDTOs.PermisoEspecialResponse;
import co.assip.erp.hojavida.repository.PermisoEspecialRepository;
import co.assip.erp.hojavida.service.PermisoEspecialService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class PermisoEspecialServiceImpl implements PermisoEspecialService {

    private final PermisoEspecialRepository repo;

    public PermisoEspecialServiceImpl(PermisoEspecialRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PermisoEspecialResponse> getByPersona(Integer idDatosPersonal) {
        return repo.findByIdDatosPersonal(idDatosPersonal).map(this::toResponse);
    }

    @Override
    public Integer createForPersona(Integer idDatosPersonal, Integer idUsuario, PermisoEspecialRequest r) {
        if (repo.existsByIdDatosPersonal(idDatosPersonal)) {
            throw new IllegalStateException("Ya existe un permiso especial para esta persona.");
        }
        PermisoEspecial e = new PermisoEspecial();
        e.setIdDatosPersonal(idDatosPersonal);
        applyReq(e, r, true, idUsuario);
        e = repo.save(e);
        return e.getIdPermisoEspecial();
    }

    @Override
    public void updateForPersona(Integer idDatosPersonal, Integer idPermiso, Integer idUsuario, PermisoEspecialRequest r) {
        PermisoEspecial e = repo.findByIdPermisoEspecialAndIdDatosPersonal(idPermiso, idDatosPersonal)
                .orElseThrow(() -> new IllegalArgumentException("Permiso no encontrado para esta persona."));
        applyReq(e, r, false, idUsuario);
        repo.save(e);
    }

    @Override
    public void deleteForPersona(Integer idDatosPersonal, Integer idPermiso) {
        PermisoEspecial e = repo.findByIdPermisoEspecialAndIdDatosPersonal(idPermiso, idDatosPersonal)
                .orElseThrow(() -> new IllegalArgumentException("Permiso no encontrado para esta persona."));
        repo.delete(e);
    }

    // ------ helpers ------
    private void applyReq(PermisoEspecial e, PermisoEspecialRequest r, boolean creating, Integer userId) {
        // Por defecto para guardar = FALSE (si viene null → false)
        e.setRecibeLlamadas(booleanOrFalse(r.recibeLlamadas));
        e.setRecibeMsm(booleanOrFalse(r.recibeMsm));
        e.setRecibeEmails(booleanOrFalse(r.recibeEmails));
        e.setRecibeCartas(booleanOrFalse(r.recibeCartas));
        e.setRecibeRedesSociales(booleanOrFalse(r.recibeRedesSociales));

        if (creating) {
            e.setFkSeguridadCreacion(userId);
            e.setFechaCreacion(LocalDateTime.now());
        }
        e.setFkSeguridadActualizacion(userId);
        e.setFechaActualizacion(LocalDateTime.now());
    }

    private static boolean booleanOrFalse(Boolean b) { return b != null && b; }

    private PermisoEspecialResponse toResponse(PermisoEspecial e) {
        PermisoEspecialResponse r = new PermisoEspecialResponse();
        r.id_permiso_especial = e.getIdPermisoEspecial();
        r.id_datos_personal = e.getIdDatosPersonal();
        r.recibe_llamadas = booleanOrFalse(e.getRecibeLlamadas());
        r.recibe_msm = booleanOrFalse(e.getRecibeMsm());
        r.recibe_emails = booleanOrFalse(e.getRecibeEmails());
        r.recibe_cartas = booleanOrFalse(e.getRecibeCartas());
        r.recibe_redes_sociales = booleanOrFalse(e.getRecibeRedesSociales());
        return r;
    }
}
