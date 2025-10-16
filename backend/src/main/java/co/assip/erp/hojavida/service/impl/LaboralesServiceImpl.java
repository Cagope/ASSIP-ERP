package co.assip.erp.hojavida.service.impl;

import co.assip.erp.hojavida.domain.Laboral;
import co.assip.erp.hojavida.dto.LaboralDTOs.*;
import co.assip.erp.hojavida.repository.LaboralRepository;
import co.assip.erp.hojavida.service.LaboralesService;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class LaboralesServiceImpl implements LaboralesService {

    private final LaboralRepository repo;

    public LaboralesServiceImpl(LaboralRepository repo) {
        this.repo = repo;
    }

    // ====== LECTURA ======
    @Override
    @Transactional(readOnly = true)
    public LaboralDetail getByPersona(Integer idPersona) {
        return tryGetByPersona(idPersona)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe laboral para la persona"));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<LaboralDetail> tryGetByPersona(Integer idPersona) {
        return repo.findByIdDatosPersonal(idPersona).map(this::toDetail);
    }

    // ====== CREAR ======
    @Override
    @Transactional
    public Integer create(Integer idPersona, LaboralCreate req, Integer userId) {
        if (repo.existsByIdDatosPersonal(idPersona)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe información laboral para esta persona");
        }
        try {
            Laboral l = new Laboral();
            l.setIdDatosPersonal(idPersona);

            applyAndValidate(l,
                    req.nombreEmpresa(),
                    req.direccion(),
                    req.idPais(),
                    req.idDepartamento(),
                    req.idCiudad(),
                    req.telefonoEmpresa(),
                    req.celularEmpresa(),
                    req.correoEmpresa(),
                    req.codigoTipoEmpresa(),
                    req.empleadoEntidad(),
                    req.codigoTipoContrato(),
                    req.codigoJornada(),
                    req.nombreContacto(),
                    req.celularContacto(),
                    req.fechaVinculacion()
            );

            // Auditoría
            l.setFkSeguridadCreacion(userId);
            l.setFkSeguridadActualizacion(userId);
            l.setFechaCreacion(LocalDateTime.now());
            l.setFechaActualizacion(LocalDateTime.now());

            l = repo.save(l);
            return l.getIdLaboral();
        } catch (DataIntegrityViolationException dive) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, root(dive));
        }
    }

    // ====== ACTUALIZAR ======
    @Override
    @Transactional
    public void update(Integer idPersona, Integer idLaboral, LaboralUpdate req, Integer userId) {
        Laboral l = mustFindForPersona(idPersona, idLaboral);

        applyAndValidate(l,
                req.nombreEmpresa(),
                req.direccion(),
                req.idPais(),
                req.idDepartamento(),
                req.idCiudad(),
                req.telefonoEmpresa(),
                req.celularEmpresa(),
                req.correoEmpresa(),
                req.codigoTipoEmpresa(),
                req.empleadoEntidad(),
                req.codigoTipoContrato(),
                req.codigoJornada(),
                req.nombreContacto(),
                req.celularContacto(),
                req.fechaVinculacion()
        );

        l.setFkSeguridadActualizacion(userId);
        l.setFechaActualizacion(LocalDateTime.now());
        repo.save(l);
    }

    // ====== ELIMINAR ======
    @Override
    @Transactional
    public void delete(Integer idPersona, Integer idLaboral) {
        Laboral l = mustFindForPersona(idPersona, idLaboral);
        repo.delete(l);
    }

    // ====== Helpers ======
    private Laboral mustFindForPersona(Integer idPersona, Integer idLaboral) {
        Laboral l = repo.findById(idLaboral)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro no encontrado"));
        if (!idPersona.equals(l.getIdDatosPersonal())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro no pertenece a la persona indicada");
        }
        return l;
    }

    private LaboralDetail toDetail(Laboral l) {
        return new LaboralDetail(
                l.getIdLaboral(),
                l.getIdDatosPersonal(),
                l.getNombreEmpresa(),
                l.getDireccion(),
                l.getIdPais(),
                l.getIdDepartamento(),
                l.getIdCiudad(),
                l.getTelefonoEmpresa(),
                l.getCelularEmpresa(),
                l.getCorreoEmpresa(),
                l.getCodigoTipoEmpresa(),
                l.getEmpleadoEntidad(),
                l.getCodigoTipoContrato(),
                l.getCodigoJornada(),
                l.getNombreContacto(),
                l.getCelularContacto(),
                l.getFechaVinculacion(),
                // String (ISO) para evitar 500 por serialización
                l.getFechaCreacion()     != null ? l.getFechaCreacion().toString()     : null,
                l.getFechaActualizacion() != null ? l.getFechaActualizacion().toString() : null
        );
    }

    private void applyAndValidate(
            Laboral l,
            String nombreEmpresa,
            String direccion,
            Integer idPais,
            Integer idDepartamento,
            Integer idCiudad,
            String telefonoEmpresa,
            String celularEmpresa,
            String correoEmpresa,
            String codigoTipoEmpresa,
            Boolean empleadoEntidad,
            String codigoTipoContrato,
            String codigoJornada,
            String nombreContacto,
            String celularContacto,
            LocalDate fechaVinculacion
    ) {
        // Requeridos básicos
        l.setNombreEmpresa(toUpper(reqStr(nombreEmpresa, 100)));
        l.setDireccion(toUpper(reqStr(direccion, 100)));
        l.setNombreContacto(toUpper(reqStr(nombreContacto, 100)));
        l.setEmpleadoEntidad(empleadoEntidad != null ? empleadoEntidad : Boolean.FALSE);

        // Opcionales (FKs/códigos)
        l.setIdPais(idPais);
        l.setIdDepartamento(idDepartamento);
        l.setIdCiudad(idCiudad);
        l.setCodigoTipoEmpresa(trimToMax(codigoTipoEmpresa, 2));
        l.setCodigoTipoContrato(trimToMax(codigoTipoContrato, 2));
        l.setCodigoJornada(trimToMax(codigoJornada, 2));

        // Teléfonos / correo
        l.setTelefonoEmpresa(optDigitsExact(telefonoEmpresa, 7));
        l.setCelularEmpresa(optDigitsExact(celularEmpresa, 10));
        l.setCelularContacto(optDigitsExact(celularContacto, 10));
        l.setCorreoEmpresa(optEmail(correoEmpresa, 100));

        // Fecha (no futura)
        if (fechaVinculacion != null && fechaVinculacion.isAfter(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fechaVinculacion no puede ser futura");
        }
        l.setFechaVinculacion(fechaVinculacion);
    }

    private String toUpper(String v) {
        return v == null ? null : v.toUpperCase(Locale.ROOT).trim();
    }
    private String reqStr(String v, int max) {
        if (v == null || v.isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campo obligatorio");
        String t = v.trim();
        return t.length() > max ? t.substring(0, max) : t;
    }
    private String trimToMax(String v, int max) {
        if (v == null) return null;
        String t = v.trim();
        return t.length() > max ? t.substring(0, max) : t;
    }
    private String optDigitsExact(String v, int len) {
        if (v == null || v.isBlank()) return null;
        String s = v.trim();
        if (!s.matches("\\d{" + len + "}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe tener exactamente " + len + " dígitos");
        }
        return s;
    }

    private static final Pattern EMAIL_RX = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private String optEmail(String v, int max) {
        if (v == null || v.isBlank()) return null;
        String s = v.trim();
        if (s.length() > max) s = s.substring(0, max);
        if (!EMAIL_RX.matcher(s).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "correoEmpresa inválido");
        }
        return s;
    }

    private String root(Throwable t) {
        Throwable r = t;
        while (r.getCause() != null) r = r.getCause();
        return r.getMessage() != null ? r.getMessage() : r.toString();
    }
}
