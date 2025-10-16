package co.assip.erp.hojavida.service.impl;

import co.assip.erp.hojavida.domain.Ubicacion;
import co.assip.erp.hojavida.dto.UbicacionDTOs;
import co.assip.erp.hojavida.repository.UbicacionRepository;
import co.assip.erp.hojavida.service.UbicacionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

@Service
@Transactional
public class UbicacionServiceImpl implements UbicacionService {

    private final UbicacionRepository repo;

    public UbicacionServiceImpl(UbicacionRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UbicacionDTOs.UbicacionResponse> getByPersona(Integer idDatosPersonal) {
        return repo.findViewByIdDatosPersonal(idDatosPersonal).map(this::toResponse);
    }

    @Override
    public Integer createForPersona(Integer idDatosPersonal, Integer userId, UbicacionDTOs.UbicacionRequest r) {
        if (repo.existsByIdDatosPersonal(idDatosPersonal)) {
            throw new IllegalStateException("Ya existe una ubicación para esta persona.");
        }
        Ubicacion u = new Ubicacion();
        u.setIdDatosPersonal(idDatosPersonal);
        applyReq(u, r, true, userId);
        u = repo.save(u);
        return u.getIdUbicacion();
    }

    @Override
    public void updateForPersona(Integer idDatosPersonal, Integer idUbicacion, Integer userId, UbicacionDTOs.UbicacionRequest r) {
        Ubicacion u = repo.findByIdUbicacionAndIdDatosPersonal(idUbicacion, idDatosPersonal)
                .orElseThrow(() -> new IllegalArgumentException("Ubicación no encontrada para esta persona."));
        applyReq(u, r, false, userId);
        repo.save(u);
    }

    @Override
    public void deleteForPersona(Integer idDatosPersonal, Integer idUbicacion) {
        Ubicacion u = repo.findByIdUbicacionAndIdDatosPersonal(idUbicacion, idDatosPersonal)
                .orElseThrow(() -> new IllegalArgumentException("Ubicación no encontrada para esta persona."));
        repo.delete(u);
    }

    // -------- helpers --------

    private void applyReq(Ubicacion u, UbicacionDTOs.UbicacionRequest r, boolean creating, Integer userId) {
        // Requeridos
        u.setDireccion(safeUpper(r.direccion, 100));
        u.setBarrio(safeUpper(r.barrio, 100));

        // Opcionales -> null si vacío + validar formato si viene
        String tel = emptyToNull(r.telefono);
        if (tel != null && !tel.matches("^[0-9]{7}$")) {
            throw new IllegalArgumentException("El teléfono debe tener exactamente 7 dígitos (0–9).");
        }
        u.setTelefono(tel);

        String cel1 = emptyToNull(r.celularUno);
        if (cel1 != null && !cel1.matches("^[0-9]{10}$")) {
            throw new IllegalArgumentException("El celular (1) debe tener exactamente 10 dígitos (0–9).");
        }
        u.setCelularUno(cel1);

        String cel2 = emptyToNull(r.celularDos);
        if (cel2 != null && !cel2.matches("^[0-9]{10}$")) {
            throw new IllegalArgumentException("El celular (2) debe tener exactamente 10 dígitos (0–9).");
        }
        u.setCelularDos(cel2);

        String correo = emptyToNull(r.correo);
        if (correo != null && !correo.matches("^[^@]+@[^@]+\\.[^@]+$")) {
            throw new IllegalArgumentException("El correo no tiene un formato válido.");
        }
        u.setCorreo(correo != null ? correo.toLowerCase(Locale.ROOT) : null);

        // Jerarquía: NO validar departamento ∈ país (por requerimiento)
        u.setIdPais(r.idPais);

        // Si viene ciudad, validar que pertenece al departamento (si este se envió).
        Integer idDeptFromCity = null;
        if (r.idCiudad != null) {
            idDeptFromCity = repo.findDepartamentoByCiudad(r.idCiudad);
            if (idDeptFromCity == null) {
                throw new IllegalArgumentException("La ciudad indicada no existe.");
            }
        }

        if (r.idDepartamento != null && idDeptFromCity != null && !r.idDepartamento.equals(idDeptFromCity)) {
            throw new IllegalArgumentException("La ciudad no pertenece al departamento seleccionado.");
        }

        // Si no vino departamento pero sí ciudad, auto-asignar el del catálogo (opcional práctico)
        Integer idDepartamentoFinal = r.idDepartamento != null ? r.idDepartamento : idDeptFromCity;
        u.setIdDepartamento(idDepartamentoFinal);
        u.setIdCiudad(r.idCiudad);

        // Sub-zona: validar existencia; zona implícita por sub-zona (no guardamos id_zona en esta tabla)
        if (r.idSubZona != null) {
            int exists = repo.existsSubZona(r.idSubZona);
            if (exists == 0) throw new IllegalArgumentException("La sub-zona indicada no existe.");
        }
        u.setIdSubZona(r.idSubZona);

        // Auditoría
        if (creating) {
            u.setFkSeguridadCreacion(userId);
            u.setFechaCreacion(LocalDateTime.now());
        }
        u.setFkSeguridadActualizacion(userId);
        u.setFechaActualizacion(LocalDateTime.now());
    }

    private UbicacionDTOs.UbicacionResponse toResponse(UbicacionRepository.UbicacionView v) {
        UbicacionDTOs.UbicacionResponse r = new UbicacionDTOs.UbicacionResponse();
        r.id_ubicacion = v.getIdUbicacion();
        r.id_datos_personal = v.getIdDatosPersonal();
        r.direccion = v.getDireccion();
        r.barrio = v.getBarrio();
        r.telefono = v.getTelefono();
        r.celular_uno = v.getCelularUno();
        r.celular_dos = v.getCelularDos();
        r.correo = v.getCorreo();
        r.id_pais = v.getIdPais();
        r.id_departamento = v.getIdDepartamento();
        r.id_ciudad = v.getIdCiudad();
        r.id_sub_zona = v.getIdSubZona();
        r.nombre_pais = v.getNombrePais();
        r.nombre_departamento = v.getNombreDepartamento();
        r.nombre_ciudad = v.getNombreCiudad();
        r.nombre_zona = v.getNombreZona();
        r.nombre_sub_zona = v.getNombreSubZona();
        return r;
    }

    private static String safeUpper(String s, int max) {
        if (s == null) return null;
        String t = s.trim();
        if (t.isEmpty()) return null;
        t = t.toUpperCase(Locale.ROOT);
        return t.length() > max ? t.substring(0, max) : t;
    }

    private static String emptyToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
