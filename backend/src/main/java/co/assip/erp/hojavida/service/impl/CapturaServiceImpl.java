package co.assip.erp.hojavida.service.impl;

import co.assip.erp.hojavida.domain.*;
import co.assip.erp.hojavida.repository.*;
import co.assip.erp.hojavida.service.CapturaService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * Servicio centralizado para guardar toda la captura de hoja de vida en una sola transacción.
 * Incluye ubicación, laboral, financiero, familiar, referencia, SARLAFT y permisos.
 * El ID base de datos personales puede venir en:
 *  - data["id_datos_personal"]  / data["idDatosPersonal"] / data["id"]
 *  - o dentro de data["datosPersonales"] con cualquiera de esas variantes.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CapturaServiceImpl implements CapturaService {

    private final DatosPersonalesRepository datosPersonalesRepo;
    private final UbicacionRepository ubicacionRepo;
    private final LaboralRepository laboralRepo;
    private final FinancieroRepository financieroRepo;
    private final DatosFamiliaresRepository familiaresRepo;
    private final ReferenciaPersonalRepository referenciaRepo;
    private final SarlaftRepository sarlaftRepo;
    private final PermisoEspecialRepository permisoRepo;

    // ============================
    // FINALIZAR CAPTURA COMPLETA
    // ============================
    @Override
    @Transactional
    public Integer finalizarCaptura(Map<String, Object> data, Integer idUsuario) {
        log.info("Iniciando captura finalizada completa por usuario {}", idUsuario);

        try {
            // 1) Obtener ID persona de forma tolerante
            Integer idDatosPersonal = extractIdDatosPersonal(data);
            if (idDatosPersonal == null) {
                throw new IllegalStateException("No se recibió un id_datos_personal válido.");
            }

            // 2) Validar existencia del registro base
            if (!datosPersonalesRepo.existsById(idDatosPersonal)) {
                throw new IllegalStateException("No existe registro base de datos personales con id=" + idDatosPersonal);
            }

            // 3) Procesar módulos (todos en la misma transacción)
            if (data.containsKey("ubicaciones")) {
                Map<String, Object> u = asMap(data.get("ubicaciones"));
                upsertUbicacion(idDatosPersonal, idUsuario, u);
            }

            if (data.containsKey("laborales")) {
                Map<String, Object> l = asMap(data.get("laborales"));
                upsertLaboral(idDatosPersonal, idUsuario, l);
            }

            if (data.containsKey("financieros")) {
                Map<String, Object> f = asMap(data.get("financieros"));
                upsertFinanciero(idDatosPersonal, idUsuario, f);
            }

            if (data.containsKey("datosFamiliares")) {
                Map<String, Object> fam = asMap(data.get("datosFamiliares"));
                upsertDatosFamiliares(idDatosPersonal, idUsuario, fam);
            }

            if (data.containsKey("referencias")) {
                Map<String, Object> ref = asMap(data.get("referencias"));
                upsertReferenciaPersonal(idDatosPersonal, idUsuario, ref);
            }

            if (data.containsKey("sarlaft")) {
                Map<String, Object> s = asMap(data.get("sarlaft"));
                upsertSarlaft(idDatosPersonal, idUsuario, s);
            }

            if (data.containsKey("permisos")) {
                Map<String, Object> p = asMap(data.get("permisos"));
                upsertPermisos(idDatosPersonal, idUsuario, p);
            }

            log.info("Captura finalizada correctamente para persona {}", idDatosPersonal);
            return idDatosPersonal;

        } catch (IllegalStateException ex) {
            log.warn("Error validando/guardando captura: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("Error en finalizarCaptura: {}", ex.getMessage(), ex);
            throw new IllegalStateException("Error procesando la captura final: " + ex.getMessage(), ex);
        }
    }

    // ======================================================
    // MÉTODOS PRIVADOS DE UPSERT POR BLOQUE
    // ======================================================

    private void upsertUbicacion(Integer idPersona, Integer idUsuario, Map<String, Object> u) {
        Optional<Ubicacion> existing = ubicacionRepo.findByIdDatosPersonal(idPersona);
        Ubicacion entity = existing.orElseGet(Ubicacion::new);

        entity.setIdDatosPersonal(idPersona);
        entity.setDireccion(nonEmptyOrDefault(str(u, "direccion"), "N/A"));
        entity.setBarrio(nonEmptyOrDefault(str(u, "barrio"), "N/A"));

        // Evitar romper CHECK: si viene vacío, poner null (no string vacío)
        entity.setTelefono(digitsOrNull(str(u, "telefono"), 7));
        entity.setCelularUno(digitsOrNull(str(u, "celular_uno"), 10));
        entity.setCelularDos(digitsOrNull(str(u, "celular_dos"), 10));

        entity.setCorreo(emptyToNull(str(u, "correo")));
        entity.setIdPais(intOrNull(u.get("id_pais")));
        entity.setIdDepartamento(intOrNull(u.get("id_departamento")));
        entity.setIdCiudad(intOrNull(u.get("id_ciudad")));
        entity.setIdSubZona(intOrNull(u.get("id_sub_zona")));

        setAudit(entity, existing.isPresent(), idUsuario);
        ubicacionRepo.save(entity);
    }

    private void upsertLaboral(Integer idPersona, Integer idUsuario, Map<String, Object> l) {
        Optional<Laboral> existing = laboralRepo.findByIdDatosPersonal(idPersona);
        Laboral entity = existing.orElseGet(Laboral::new);

        entity.setIdDatosPersonal(idPersona);
        entity.setNombreEmpresa(nonEmptyOrDefault(str(l, "nombreEmpresa"), "INDEPENDIENTE"));
        entity.setDireccion(nonEmptyOrDefault(str(l, "direccion"), "N/A"));

        entity.setIdPais(intOrNull(l.get("idPais")));
        entity.setIdDepartamento(intOrNull(l.get("idDepartamento")));
        entity.setIdCiudad(intOrNull(l.get("idCiudad")));

        entity.setTelefonoEmpresa(digitsOrNull(str(l, "telefonoEmpresa"), 7));
        entity.setCelularEmpresa(digitsOrNull(str(l, "celularEmpresa"), 10));
        entity.setCorreoEmpresa(emptyToNull(str(l, "correoEmpresa")));

        entity.setCodigoTipoEmpresa(emptyToNull(str(l, "codigoTipoEmpresa")));
        entity.setEmpleadoEntidad(boolOrDefault(l.get("empleadoEntidad"), false));
        entity.setCodigoTipoContrato(emptyToNull(str(l, "codigoTipoContrato")));
        entity.setCodigoJornada(emptyToNull(str(l, "codigoJornada")));

        entity.setNombreContacto(nonEmptyOrDefault(str(l, "nombreContacto"), "N/A"));
        entity.setCelularContacto(digitsOrNull(str(l, "celularContacto"), 10));
        entity.setFechaVinculacion(parseDate(l.get("fechaVinculacion")));

        setAudit(entity, existing.isPresent(), idUsuario);
        laboralRepo.save(entity);
    }

    private void upsertFinanciero(Integer idPersona, Integer idUsuario, Map<String, Object> f) {
        Optional<Financiero> existing = financieroRepo.findByIdDatosPersonal(idPersona);
        Financiero entity = existing.orElseGet(Financiero::new);

        entity.setIdDatosPersonal(idPersona);
        entity.setValorSalario(toDecimal(f.get("valorSalario")));
        entity.setValorPension(toDecimal(f.get("valorPension")));
        entity.setIngresosArriendo(toDecimal(f.get("ingresosArriendo")));
        entity.setIngresosComisiones(toDecimal(f.get("ingresosComisiones")));
        entity.setOtrosIngresos(toDecimal(f.get("otrosIngresos")));
        entity.setComentarioOtrosIngresos(nonNull(str(f, "comentarioOtrosIngresos")));
        entity.setEgresosFamiliares(toDecimal(f.get("egresosFamiliares")));
        entity.setEgresosArriendo(toDecimal(f.get("egresosArriendo")));
        entity.setEgresosCredito(toDecimal(f.get("egresosCredito")));
        entity.setOtrosEgresos(toDecimal(f.get("otrosEgresos")));
        entity.setComentarioOtrosEgresos(nonNull(str(f, "comentarioOtrosEgresos")));
        entity.setTotalActivos(toDecimal(f.get("totalActivos")));
        entity.setTotalPasivos(toDecimal(f.get("totalPasivos")));
        entity.setOrigenFondos(nonNull(str(f, "origenFondos")));
        entity.setRelacionFinanciera(nonNull(str(f, "relacionFinanciera")));
        entity.setDeudaRelacionFinanciera(toDecimal(f.get("deudaRelacionFinanciera")));

        setAudit(entity, existing.isPresent(), idUsuario);
        financieroRepo.save(entity);
    }

    private void upsertDatosFamiliares(Integer idPersona, Integer idUsuario, Map<String, Object> f) {
        DatosFamiliares entity = new DatosFamiliares();

        entity.setIdDatosPersonal(idPersona);
        entity.setCodigoParentesco(nonNull(str(f, "codigo_parentesco")));
        entity.setNombreDatosFamiliar(nonEmptyOrDefault(str(f, "nombre_datos_familiar"), "N/A"));
        entity.setDocumentoDatosFamiliar(emptyToNull(str(f, "documento_datos_familiar")));
        entity.setFechaNacimiento(parseDate(f.get("fecha_nacimiento")));
        entity.setTelefonoDatosFamiliar(digitsOrNull(str(f, "telefono_datos_familiar"), 7));
        entity.setCelularDatosFamiliar(digitsOrNull(str(f, "celular_datos_familiar"), 10));
        entity.setDireccionDatosFamiliar(nonEmptyOrDefault(str(f, "direccion_datos_familiar"), "N/A"));
        entity.setIdDepartamento(intOrNull(f.get("id_departamento")));
        entity.setIdCiudad(intOrNull(f.get("id_ciudad")));
        entity.setIngresosDatosFamiliar(toDecimal(f.get("ingresos_datos_familiar")));
        entity.setEgresosDatosFamiliar(toDecimal(f.get("egresos_datos_familiar")));
        entity.setReferenciaFamiliar(boolOrDefault(f.get("referencia_familiar"), false));

        setAudit(entity, false, idUsuario);
        familiaresRepo.save(entity);
    }

    private void upsertReferenciaPersonal(Integer idPersona, Integer idUsuario, Map<String, Object> r) {
        Optional<ReferenciaPersonal> existing = referenciaRepo.findByIdDatosPersonal(idPersona);
        ReferenciaPersonal entity = existing.orElseGet(ReferenciaPersonal::new);

        entity.setIdDatosPersonal(idPersona);
        entity.setNombreReferenciaPersonal(nonEmptyOrDefault(str(r, "nombre_referencia_personal"), "N/A"));
        entity.setDireccionReferenciaPersonal(nonEmptyOrDefault(str(r, "direccion_referencia_personal"), "N/A"));
        entity.setIdDepartamento(intOrNull(r.get("id_departamento")));
        entity.setIdCiudad(intOrNull(r.get("id_ciudad")));
        entity.setTelefonoReferenciaPersonal(digitsOrNull(str(r, "telefono_referencia_personal"), 7));
        entity.setCelularReferenciaPersonal(digitsOrNull(str(r, "celular_referencia_personal"), 10));

        setAudit(entity, existing.isPresent(), idUsuario);
        referenciaRepo.save(entity);
    }

    private void upsertSarlaft(Integer idPersona, Integer idUsuario, Map<String, Object> s) {
        Optional<Sarlaft> existing = sarlaftRepo.findByIdDatosPersonal(idPersona);
        Sarlaft entity = existing.orElseGet(Sarlaft::new);

        entity.setIdDatosPersonal(idPersona);
        entity.setExoneracionUiaf(boolOrDefault(s.get("exoneracionUiaf"), false));
        entity.setFechaExoneracion(parseDate(s.get("fechaExoneracion")));
        entity.setAsociadoPeps(boolOrDefault(s.get("asociadoPeps"), false));
        entity.setTipoPeps(emptyToNull(str(s, "tipoPeps")));
        entity.setObservacionesPeps(nonNull(str(s, "observacionesPeps")));
        entity.setFechaInicialPeps(parseDate(s.get("fechaInicialPeps")));
        entity.setFechaFinalPeps(parseDate(s.get("fechaFinalPeps")));
        entity.setFamiliaPeps(boolOrDefault(s.get("familiaPeps"), false));
        entity.setTipoFamiliaPeps(emptyToNull(str(s, "tipoFamiliaPeps")));
        entity.setCedulaFamiliaPeps(emptyToNull(str(s, "cedulaFamiliaPeps")));
        entity.setCodigoParentesco(emptyToNull(str(s, "codigoParentesco")));
        entity.setNombreFamiliaPeps(nonNull(str(s, "nombreFamiliaPeps")));
        entity.setMonedaExtranjera(boolOrDefault(s.get("monedaExtranjera"), false));
        entity.setObservacionMonedaExtranjera(nonNull(str(s, "observacionMonedaExtranjera")));
        entity.setCuentaExtranjero(boolOrDefault(s.get("cuentaExtranjero"), false));
        entity.setTipoMonedaExtranjera(nonNull(str(s, "tipoMonedaExtranjera")));
        entity.setNumeroCuentaExtranjero(nonNull(str(s, "numeroCuentaExtranjero")));
        entity.setNombreBancoExtranjero(nonNull(str(s, "nombreBancoExtranjero")));
        entity.setCiudadCuentaExtranjero(nonNull(str(s, "ciudadCuentaExtranjero")));
        entity.setPaisCuentaExtranjero(nonNull(str(s, "paisCuentaExtranjero")));

        setAudit(entity, existing.isPresent(), idUsuario);
        sarlaftRepo.save(entity);
    }

    private void upsertPermisos(Integer idPersona, Integer idUsuario, Map<String, Object> p) {
        Optional<PermisoEspecial> existing = permisoRepo.findByIdDatosPersonal(idPersona);
        PermisoEspecial entity = existing.orElseGet(PermisoEspecial::new);

        entity.setIdDatosPersonal(idPersona);
        entity.setRecibeLlamadas(boolOrDefault(p.get("recibeLlamadas"), false));
        entity.setRecibeMsm(boolOrDefault(p.get("recibeMsm"), false));
        entity.setRecibeEmails(boolOrDefault(p.get("recibeEmails"), false));
        entity.setRecibeCartas(boolOrDefault(p.get("recibeCartas"), false));
        entity.setRecibeRedesSociales(boolOrDefault(p.get("recibeRedesSociales"), false));

        setAudit(entity, existing.isPresent(), idUsuario);
        permisoRepo.save(entity);
    }

    // =============================
    // UTILIDADES INTERNAS
    // =============================

    private Integer extractIdDatosPersonal(Map<String, Object> data) {
        // Prioridad: raíz
        Integer id = intOrNull(data.get("id_datos_personal"));
        if (id == null) id = intOrNull(data.get("idDatosPersonal"));
        if (id == null) id = intOrNull(data.get("id"));
        if (id != null && id > 0) return id;

        // Alternativa: dentro de "datosPersonales"
        Object dpObj = data.get("datosPersonales");
        if (dpObj instanceof Map<?, ?> dp) {
            id = intOrNull(dp.get("id_datos_personal"));
            if (id == null) id = intOrNull(dp.get("idDatosPersonal"));
            if (id == null) id = intOrNull(dp.get("id"));
            if (id != null && id > 0) return id;
        }
        return null;
    }

    private Map<String, Object> asMap(Object o) {
        @SuppressWarnings("unchecked")
        Map<String, Object> m = (Map<String, Object>) o;
        return m;
    }

    private void setAudit(Object entity, boolean updating, Integer idUsuario) {
        LocalDateTime now = LocalDateTime.now();
        try {
            entity.getClass().getMethod("setFkSeguridadActualizacion", Integer.class).invoke(entity, idUsuario);
            entity.getClass().getMethod("setFechaActualizacion", LocalDateTime.class).invoke(entity, now);
            if (!updating) {
                entity.getClass().getMethod("setFkSeguridadCreacion", Integer.class).invoke(entity, idUsuario);
                entity.getClass().getMethod("setFechaCreacion", LocalDateTime.class).invoke(entity, now);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Error asignando auditoría: " + e.getMessage(), e);
        }
    }

    private LocalDate parseDate(Object val) {
        if (val == null) return null;
        try {
            String s = val.toString().trim();
            if (s.isEmpty()) return null;
            // Espera yyyy-MM-dd
            return LocalDate.parse(s);
        } catch (Exception e) {
            return null;
        }
    }

    private BigDecimal toDecimal(Object val) {
        if (val == null) return BigDecimal.ZERO;
        try {
            String s = val.toString().trim();
            if (s.isEmpty()) return BigDecimal.ZERO;
            return new BigDecimal(s);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private Integer intOrNull(Object o) {
        if (o == null) return null;
        try {
            if (o instanceof Number n) return n.intValue();
            String s = o.toString().trim();
            if (s.isEmpty()) return null;
            return Integer.valueOf(s);
        } catch (Exception e) {
            return null;
        }
    }

    private String str(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return (v == null) ? null : v.toString();
    }

    private boolean boolOrDefault(Object o, boolean def) {
        if (o == null) return def;
        if (o instanceof Boolean b) return b;
        String s = o.toString().trim().toLowerCase();
        if (s.isEmpty()) return def;
        if ("true".equals(s) || "1".equals(s) || "si".equals(s) || "sí".equals(s) || "y".equals(s) || "yes".equals(s)) return true;
        if ("false".equals(s) || "0".equals(s) || "no".equals(s) || "n".equals(s)) return false;
        return def;
    }

    private String nonEmptyOrDefault(String s, String def) {
        if (s == null) return def;
        String t = s.trim();
        return t.isEmpty() ? def : t;
    }

    private String emptyToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private String nonNull(String s) {
        return (s == null) ? "" : s;
    }

    private String digitsOrNull(String s, int maxLen) {
        if (s == null) return null;
        String digits = s.replaceAll("\\D+", "");
        if (digits.isEmpty()) return null;
        return digits.length() > maxLen ? digits.substring(0, maxLen) : digits;
    }
}
