    package co.assip.erp.hojavida.service.impl;

    import co.assip.erp.hojavida.domain.DatosFamiliares;
    import co.assip.erp.hojavida.dto.DatosFamiliaresDTOs.*;
    import co.assip.erp.hojavida.repository.DatosFamiliaresRepository;
    import co.assip.erp.hojavida.service.DatosFamiliaresService;

    import org.springframework.dao.DataIntegrityViolationException;
    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.Pageable;
    import org.springframework.http.HttpStatus;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;
    import org.springframework.web.server.ResponseStatusException;

    import java.math.BigDecimal;
    import java.time.LocalDate;
    import java.time.LocalDateTime;
    import java.util.Locale;

    @Service
    public class DatosFamiliaresServiceImpl implements DatosFamiliaresService {

        private final DatosFamiliaresRepository repo;

        public DatosFamiliaresServiceImpl(DatosFamiliaresRepository repo) {
            this.repo = repo;
        }

        @Override
        @Transactional(readOnly = true)
        public Page<FamiliarListItem> list(Integer idPersona, String q, Pageable pageable) {
            Page<DatosFamiliares> page = repo.searchByPersona(idPersona, q, pageable);
            return page.map(f -> new FamiliarListItem(
                    f.getIdDatosFamiliares(),
                    f.getIdDatosPersonal(),
                    f.getCodigoParentesco(),
                    f.getNombreDatosFamiliar(),
                    f.getDocumentoDatosFamiliar(),
                    f.getTelefonoDatosFamiliar(),
                    f.getCelularDatosFamiliar(),
                    f.getIdDepartamento(),
                    f.getIdCiudad(),
                    f.getReferenciaFamiliar(),
                    f.getFechaNacimiento()
            ));
        }

        @Override
        @Transactional(readOnly = true)
        public FamiliarDetail get(Integer idPersona, Integer idFamiliar) {
            DatosFamiliares f = mustFindForPersona(idPersona, idFamiliar);
            return new FamiliarDetail(
                    f.getIdDatosFamiliares(),
                    f.getIdDatosPersonal(),
                    f.getCodigoParentesco(),
                    f.getNombreDatosFamiliar(),
                    f.getDocumentoDatosFamiliar(),
                    f.getTelefonoDatosFamiliar(),
                    f.getCelularDatosFamiliar(),
                    f.getDireccionDatosFamiliar(),
                    f.getIdDepartamento(),
                    f.getIdCiudad(),
                    f.getFechaNacimiento(),
                    f.getIngresosDatosFamiliar(),
                    f.getEgresosDatosFamiliar(),
                    f.getReferenciaFamiliar()
            );
        }

        @Override
        @Transactional
        public Integer create(Integer idPersona, FamiliarCreate req, Integer userId) {
            try {
                DatosFamiliares f = new DatosFamiliares();
                f.setIdDatosPersonal(idPersona);

                applyAndValidate(f,
                        req.codigoParentesco(),
                        req.nombreDatosFamiliar(),
                        req.documentoDatosFamiliar(),
                        req.telefonoDatosFamiliar(),
                        req.celularDatosFamiliar(),
                        req.direccionDatosFamiliar(),
                        req.idDepartamento(),
                        req.idCiudad(),
                        req.fechaNacimiento(),
                        req.ingresosDatosFamiliar(),
                        req.egresosDatosFamiliar(),
                        req.referenciaFamiliar()
                );

                // Auditoría
                f.setFkSeguridadCreacion(userId);
                f.setFkSeguridadActualizacion(userId);
                f.setFechaCreacion(LocalDateTime.now());
                f.setFechaActualizacion(LocalDateTime.now());

                f = repo.save(f);
                return f.getIdDatosFamiliares();
            } catch (DataIntegrityViolationException dive) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, root(dive));
            }
        }

        @Override
        @Transactional
        public void update(Integer idPersona, Integer idFamiliar, FamiliarUpdate req, Integer userId) {
            DatosFamiliares f = mustFindForPersona(idPersona, idFamiliar);

            applyAndValidate(f,
                    req.codigoParentesco(),
                    req.nombreDatosFamiliar(),
                    req.documentoDatosFamiliar(),
                    req.telefonoDatosFamiliar(),
                    req.celularDatosFamiliar(),
                    req.direccionDatosFamiliar(),
                    req.idDepartamento(),
                    req.idCiudad(),
                    req.fechaNacimiento(),
                    req.ingresosDatosFamiliar(),
                    req.egresosDatosFamiliar(),
                    req.referenciaFamiliar()
            );

            f.setFkSeguridadActualizacion(userId);
            f.setFechaActualizacion(LocalDateTime.now());

            repo.save(f);
        }

        @Override
        @Transactional
        public void delete(Integer idPersona, Integer idFamiliar) {
            DatosFamiliares f = mustFindForPersona(idPersona, idFamiliar);
            repo.delete(f);
        }

        // ===== helpers =====

        private DatosFamiliares mustFindForPersona(Integer idPersona, Integer idFamiliar) {
            DatosFamiliares f = repo.findById(idFamiliar)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro no encontrado"));
            if (!idPersona.equals(f.getIdDatosPersonal())) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro no pertenece a la persona indicada");
            }
            return f;
        }

        private void applyAndValidate(
                DatosFamiliares f,
                String codigoParentesco,
                String nombre,
                String documento,
                String telefono,
                String celular,
                String direccion,
                Integer idDepartamento,
                Integer idCiudad,
                LocalDate fechaNacimiento,
                BigDecimal ingresos,
                BigDecimal egresos,
                Boolean referencia
        ) {
            // Requeridos básicos
            f.setCodigoParentesco(reqStr(codigoParentesco, 2));
            f.setNombreDatosFamiliar(toUpper(reqStr(nombre, 100)));
            f.setDocumentoDatosFamiliar(reqDigits(documento, 20));
            f.setDireccionDatosFamiliar(toUpper(reqStr(direccion, 100)));
            f.setIdDepartamento(reqInt(idDepartamento));
            f.setIdCiudad(reqInt(idCiudad));
            f.setFechaNacimiento(reqDate(fechaNacimiento));

            // Tel/Cel: opcionales pero si vienen deben validar
            f.setTelefonoDatosFamiliar(optDigitsExact(telefono, 7));
            f.setCelularDatosFamiliar(optDigitsExact(celular, 10));

            // Números >= 0
            f.setIngresosDatosFamiliar(nonNeg(ingresos));
            f.setEgresosDatosFamiliar(nonNeg(egresos));

            // Referencia (requerido)
            if (referencia == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "referenciaFamiliar es obligatoria");
            }
            f.setReferenciaFamiliar(referencia);
        }

        private String toUpper(String v) {
            return v == null ? null : v.toUpperCase(Locale.ROOT).trim();
        }

        private String reqStr(String v, int max) {
            if (v == null || v.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campo obligatorio");
            String t = v.trim();
            return t.length() > max ? t.substring(0, max) : t;
        }

        private Integer reqInt(Integer v) {
            if (v == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campo obligatorio");
            return v;
        }

        private LocalDate reqDate(LocalDate d) {
            if (d == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fechaNacimiento es obligatoria");
            if (d.isAfter(LocalDate.now())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fechaNacimiento no puede ser futura");
            }
            return d;
        }

        private String reqDigits(String v, int max) {
            String s = reqStr(v, max);
            if (!s.matches("\\d{1," + max + "}")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo dígitos (máx " + max + ")");
            }
            return s;
        }

        private String optDigitsExact(String v, int len) {
            if (v == null || v.isBlank()) return null;
            String s = v.trim();
            if (!s.matches("\\d{" + len + "}")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe tener exactamente " + len + " dígitos");
            }
            return s;
        }

        private BigDecimal nonNeg(BigDecimal n) {
            if (n == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campo numérico obligatorio");
            if (n.signum() < 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puede ser negativo");
            return n.setScale(2, BigDecimal.ROUND_HALF_UP);
        }

        private String root(Throwable t) {
            Throwable r = t;
            while (r.getCause() != null) r = r.getCause();
            return r.getMessage();
        }
    }
