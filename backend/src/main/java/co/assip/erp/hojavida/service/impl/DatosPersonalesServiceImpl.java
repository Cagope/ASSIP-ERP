package co.assip.erp.hojavida.service.impl;

import co.assip.erp.hojavida.domain.DatosPersonales;
import co.assip.erp.hojavida.dto.DatosPersonalesDTOs.DatosPersonaCreate;
import co.assip.erp.hojavida.dto.DatosPersonalesDTOs.DatosPersonaUpdate;
import co.assip.erp.hojavida.dto.DatosPersonalesDTOs.DatosPersonalesDetail;
import co.assip.erp.hojavida.dto.DatosPersonalesDTOs.DatosPersonalesListItem;
import co.assip.erp.hojavida.repository.DatosPersonalesRepository;
import co.assip.erp.hojavida.service.DatosPersonalesService;

import co.assip.erp.catalogos.domain.CiudadCat;
import co.assip.erp.catalogos.domain.DepartamentoCat;
import co.assip.erp.catalogos.repository.CiudadCatalogoRepository;
import co.assip.erp.catalogos.repository.DepartamentoCatalogoRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
public class DatosPersonalesServiceImpl implements DatosPersonalesService {

    private final DatosPersonalesRepository repo;
    private final CiudadCatalogoRepository ciudadRepo;
    private final DepartamentoCatalogoRepository departamentoRepo;

    public DatosPersonalesServiceImpl(
            DatosPersonalesRepository repo,
            CiudadCatalogoRepository ciudadRepo,
            DepartamentoCatalogoRepository departamentoRepo
    ) {
        this.repo = repo;
        this.ciudadRepo = ciudadRepo;
        this.departamentoRepo = departamentoRepo;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DatosPersonalesListItem> list(String q, Pageable pageable) {
        Page<DatosPersonales> page = (q == null || q.isBlank())
                ? repo.findAll(pageable)
                : repo.search(q, pageable);

        return page.map(dp -> new DatosPersonalesListItem(
                dp.getIdDatosPersonal(),
                dp.getTipoDocumento(),
                dp.getDocumento(),
                dp.getTieneRut(),
                dp.getDigitoVerificacion(),
                dp.getNombres(),
                dp.getPrimerApellido(),
                dp.getFechaNacimiento()
        ));
    }

    @Override
    @Transactional(readOnly = true)
    public DatosPersonalesDetail get(Integer id) {
        DatosPersonales e = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro no encontrado"));

        return new DatosPersonalesDetail(
                e.getIdDatosPersonal(),
                e.getTipoDocumento(),
                e.getDocumento(),
                e.getTieneRut(),
                e.getDigitoVerificacion(),
                e.getNombres(),
                e.getPrimerApellido(),
                e.getSegundoApellido(),
                e.getFechaNacimiento(),
                e.getFechaDocumento(),
                e.getFechaApertura(),
                e.getIdPaisDocumento(),
                e.getIdDepartamentoExpedicion(),
                e.getIdCiudadExpedicion(),
                e.getIdPaisNacimiento(),
                e.getIdDepartamentoNacimiento(),
                e.getIdCiudadNacimiento(),
                e.getComentario(),
                e.getCodigoGenero(),
                e.getCodigoEstadoCivil(),
                e.getCodigoEscolaridad(),
                e.getCodigoTipoVivienda(),
                e.getEstratoSocial(),
                e.getNumeroHijos(),
                e.getCodigoOcupacion(),
                e.getCodigoSectorEconomico(),
                e.getCodigoActividadSes(),
                e.getCodigoActividadDian(),
                e.getCodigoRetencion()
        );
    }

    @Override
    @Transactional
    public Integer create(DatosPersonaCreate req, Integer userId) {
        // 🔎 NUEVO: pre-chequeo de existencia para advertir (409)
        String tipoDoc = req.tipoDocumento() == null ? "" : req.tipoDocumento().trim();
        String doc = req.documento() == null ? "" : req.documento().trim();
        if (repo.existsByTipoDocumentoAndDocumento(tipoDoc, doc)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe una persona con ese tipo y número de documento"
            );
        }

        try {
            DatosPersonales e = new DatosPersonales();

            fillAndValidateCommon(
                    e,
                    req.tipoPersona(),
                    req.tipoDocumento(), req.documento(), req.tieneRut(), req.digitoVerificacion(),
                    req.nombres(), req.primerApellido(), req.segundoApellido(),
                    req.fechaNacimiento(), req.fechaDocumento(),

                    // país independiente (requerido)
                    req.idPaisDocumento(), req.idPaisNacimiento(),

                    // ciudades (requeridas)
                    req.idCiudadExpedicion(), req.idCiudadNacimiento(),

                    req.comentario(),
                    req.codigoGenero(), req.codigoEstadoCivil(), req.codigoEscolaridad(),
                    req.codigoTipoVivienda(), req.estratoSocial(), req.numeroHijos(),
                    req.codigoOcupacion(), req.codigoSectorEconomico(),
                    req.codigoActividadSes(), req.codigoActividadDian(), req.codigoRetencion(),
                    req.cabezaFamilia()
            );

            // Derivar SOLO departamento desde ciudad
            populateDepartamentosFromCiudadesOrFail(e);

            // Fechas de negocio / auditoría
            e.setFechaApertura(LocalDate.now());
            e.setFechaActualiza(LocalDate.now());
            e.setFkSeguridadCreacion(userId);
            e.setFkSeguridadActualizacion(userId);
            e.setFechaCreacion(LocalDateTime.now());
            e.setFechaActualizacion(LocalDateTime.now());

            e = repo.save(e);
            log.info("DatosPersonales creado id={}", e.getIdDatosPersonal());
            return e.getIdDatosPersonal();
        } catch (DataIntegrityViolationException ex) {
            log.error("Violación de integridad al crear datos_personales", ex);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, root(ex));
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error inesperado al crear datos_personales", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, root(ex));
        }
    }

    @Override
    @Transactional
    public void update(Integer id, DatosPersonaUpdate req, Integer userId) {
        DatosPersonales e = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro no encontrado"));

        fillAndValidateCommon(
                e,
                req.tipoPersona(),
                req.tipoDocumento(), req.documento(), req.tieneRut(), req.digitoVerificacion(),
                req.nombres(), req.primerApellido(), req.segundoApellido(),
                req.fechaNacimiento(), req.fechaDocumento(),

                // país independiente (requerido)
                req.idPaisDocumento(), req.idPaisNacimiento(),

                // ciudades (requeridas)
                req.idCiudadExpedicion(), req.idCiudadNacimiento(),

                req.comentario(),
                req.codigoGenero(), req.codigoEstadoCivil(), req.codigoEscolaridad(),
                req.codigoTipoVivienda(), req.estratoSocial(), req.numeroHijos(),
                req.codigoOcupacion(), req.codigoSectorEconomico(),
                req.codigoActividadSes(), req.codigoActividadDian(), req.codigoRetencion(),
                req.cabezaFamilia()
        );

        // Derivar SOLO departamento desde ciudad
        populateDepartamentosFromCiudadesOrFail(e);

        e.setFechaActualiza(LocalDate.now());
        e.setFkSeguridadActualizacion(userId);
        e.setFechaActualizacion(LocalDateTime.now());

        repo.save(e);
        log.info("DatosPersonales actualizado id={}", id);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        if (!repo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro no encontrado");
        }
        repo.deleteById(id);
        log.info("DatosPersonales eliminado id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exists(String tipoDocumento, String documento) {
        String t = tipoDocumento == null ? "" : tipoDocumento.trim();
        String d = documento == null ? "" : documento.trim();
        return repo.existsByTipoDocumentoAndDocumento(t, d);
    }

    // ===== Helpers =====

    private void fillAndValidateCommon(
            DatosPersonales e,
            String tipoPersona,
            String tipoDocumento, String documento, Boolean tieneRut, String digitoVerificacion,
            String nombres, String primerApellido, String segundoApellido,
            LocalDate fechaNacimiento, LocalDate fechaDocumento,

            // país independiente
            Integer idPaisDocumento, Integer idPaisNacimiento,

            // ciudades (depto se deriva)
            Integer idCiudadExpedicion, Integer idCiudadNacimiento,

            String comentario,
            String codigoGenero, String codigoEstadoCivil, String codigoEscolaridad,
            String codigoTipoVivienda, Integer estratoSocial, Integer numeroHijos,
            String codigoOcupacion, String codigoSectorEconomico,
            String codigoActividadSes, String codigoActividadDian, String codigoRetencion,
            String cabezaFamilia
    ) {
        String tp = (tipoPersona == null || tipoPersona.isBlank()) ? "1" : tipoPersona.trim();
        if (!tp.equals("1") && !tp.equals("2")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "tipoPersona inválido (use '1' o '2')");
        }
        e.setTipoPersona(tp);

        e.setTipoDocumento(safe(tipoDocumento, 2, true));
        String doc = safeDigits(documento, 20, true);
        e.setDocumento(doc);

        if ("2".equals(tp) || Boolean.TRUE.equals(tieneRut)) {
            String dv = nullIfBlank(digitoVerificacion);
            if (dv == null || !dv.matches("\\d{1,2}")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "digitoVerificacion es obligatorio (1-2 dígitos) para RUT/jurídica");
            }
            e.setTieneRut(true);
            e.setDigitoVerificacion(dv);
        } else {
            e.setTieneRut(false);
            e.setDigitoVerificacion(null);
        }

        // NOMBRES / APELLIDOS en MAYÚSCULAS
        e.setNombres(safeUpper(nombres, 100, true));
        e.setPrimerApellido(safeUpper(primerApellido, 50, true));
        e.setSegundoApellido(safeUpper(segundoApellido, 50, true));

        if (fechaNacimiento == null || fechaDocumento == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fechaNacimiento y fechaDocumento son obligatorias");
        }
        LocalDate hoy = LocalDate.now();
        if (fechaNacimiento.isAfter(hoy)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha de nacimiento no puede ser futura");
        }
        if (fechaDocumento.isBefore(fechaNacimiento)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fechaDocumento debe ser >= fechaNacimiento");
        }
        if (fechaDocumento.isAfter(hoy)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fechaDocumento no puede ser futura");
        }
        e.setFechaNacimiento(fechaNacimiento);
        e.setFechaDocumento(fechaDocumento);

        // País independiente: OBLIGATORIO
        if (idPaisDocumento == null || idPaisNacimiento == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "idPaisDocumento e idPaisNacimiento son obligatorios");
        }
        e.setIdPaisDocumento(idPaisDocumento);
        e.setIdPaisNacimiento(idPaisNacimiento);

        // Ciudades obligatorias
        if (idCiudadExpedicion == null || idCiudadNacimiento == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "idCiudadExpedicion y idCiudadNacimiento son obligatorios");
        }
        e.setIdCiudadExpedicion(idCiudadExpedicion);
        e.setIdCiudadNacimiento(idCiudadNacimiento);

        e.setComentario(safe(comentario, 250, true));

        e.setCodigoGenero(safe(codigoGenero, 1, true));
        e.setCodigoEstadoCivil(safe(codigoEstadoCivil, 1, true));
        e.setCodigoEscolaridad(safe(codigoEscolaridad, 2, true));
        e.setCodigoTipoVivienda(safe(codigoTipoVivienda, 2, true));
        e.setCodigoOcupacion(safe(codigoOcupacion, 2, true));
        e.setCodigoSectorEconomico(safe(codigoSectorEconomico, 3, true));
        e.setCodigoActividadSes(safe(codigoActividadSes, 4, true));
        e.setCodigoActividadDian(safe(codigoActividadDian, 4, true));
        e.setCodigoRetencion(safe(codigoRetencion, 2, true));

        int estrato = (estratoSocial == null) ? 0 : estratoSocial;
        if (estrato < 0 || estrato > 6) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "estratoSocial debe estar entre 0 y 6");
        e.setEstratoSocial(estrato);

        int hijos = (numeroHijos == null) ? 0 : numeroHijos;
        if (hijos < 0 || hijos > 99) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "numeroHijos debe estar entre 0 y 99");
        e.setNumeroHijos(hijos);

        String cf = (cabezaFamilia == null || cabezaFamilia.isBlank()) ? "0" : cabezaFamilia.trim();
        if (!"0".equals(cf) && !"1".equals(cf)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "cabezaFamilia debe ser '0' o '1'");
        }
        e.setCabezaFamilia(cf);
    }

    // SOLO deriva los departamentos desde las ciudades (país ya viene informado)
    private void populateDepartamentosFromCiudadesOrFail(DatosPersonales e) {
        // EXPEDICIÓN
        CiudadCat ciudadExp = ciudadRepo.findById(e.getIdCiudadExpedicion())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Ciudad de expedición no existe: " + e.getIdCiudadExpedicion()));

        Integer idDeptoExp = extractDepartamentoId(ciudadExp)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "La ciudad de expedición no tiene departamento asociado"));
        DepartamentoCat deptoExp = departamentoRepo.findById(idDeptoExp)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Departamento de expedición no existe: " + idDeptoExp));
        e.setIdDepartamentoExpedicion(idDeptoExp);

        // NACIMIENTO
        CiudadCat ciudadNac = ciudadRepo.findById(e.getIdCiudadNacimiento())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Ciudad de nacimiento no existe: " + e.getIdCiudadNacimiento()));

        Integer idDeptoNac = extractDepartamentoId(ciudadNac)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "La ciudad de nacimiento no tiene departamento asociado"));
        DepartamentoCat deptoNac = departamentoRepo.findById(idDeptoNac)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Departamento de nacimiento no existe: " + idDeptoNac));
        e.setIdDepartamentoNacimiento(idDeptoNac);
    }

    // ====== Helpers de extracción con reflexión segura ======
    private Optional<Integer> extractDepartamentoId(Object ciudad) {
        Integer byRel = invokeChainForInt(ciudad, "getDepartamento|getDepto", "getId|getIdDepartamento");
        if (byRel != null) return Optional.of(byRel);
        Integer byFk = invokeForInt(ciudad, "getIdDepartamento|getDepartamentoId");
        return Optional.ofNullable(byFk);
    }

    private Integer invokeChainForInt(Object root, String firstOptions, String secondOptions) {
        if (root == null) return null;
        Object mid = invokeForObject(root, firstOptions);
        if (mid == null) return null;
        return invokeForInt(mid, secondOptions);
    }

    private Integer invokeForInt(Object target, String options) {
        Object val = invokeForObject(target, options);
        if (val == null) return null;
        if (val instanceof Integer i) return i;
        if (val instanceof Number n) return n.intValue();
        try { return Integer.valueOf(val.toString()); }
        catch (Exception ignored) { return null; }
    }

    private Object invokeForObject(Object target, String options) {
        for (String name : options.split("\\|")) {
            try {
                Method m = target.getClass().getMethod(name.trim());
                return m.invoke(target);
            } catch (Exception ignored) { }
        }
        return null;
    }

    private String safe(String v, int max, boolean required) {
        if (v == null || v.isBlank()) {
            if (required) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campo obligatorio");
            return null;
        }
        String t = v.trim();
        return t.length() > max ? t.substring(0, max) : t;
    }

    private String safeDigits(String v, int max, boolean required) {
        String s = safe(v, max, required);
        if (s == null) return null;
        if (!s.matches("\\d{1," + max + "}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Debe contener solo dígitos y máximo " + max + " caracteres");
        }
        return s;
    }

    private String nullIfBlank(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    // === NUEVO: helper para forzar mayúsculas con misma validación de safe() ===
    private String safeUpper(String v, int max, boolean required) {
        String s = safe(v, max, required);
        return (s == null) ? null : s.toUpperCase(java.util.Locale.ROOT);
    }

    private String root(Throwable t) {
        Throwable r = t;
        while (r.getCause() != null) r = r.getCause();
        return r.getMessage();
    }
}
