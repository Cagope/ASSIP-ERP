package co.assip.erp.hojavida.service.impl;

import co.assip.erp.hojavida.domain.Sarlaft;
import co.assip.erp.hojavida.dto.SarlaftDTOs.SarlaftRequest;
import co.assip.erp.hojavida.dto.SarlaftDTOs.SarlaftResponse;
import co.assip.erp.hojavida.repository.SarlaftRepository;
import co.assip.erp.hojavida.service.SarlaftService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class SarlaftServiceImpl implements SarlaftService {

    private final SarlaftRepository repo;

    public SarlaftServiceImpl(SarlaftRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SarlaftResponse> getByPersona(Integer idDatosPersonal) {
        return repo.findByIdDatosPersonal(idDatosPersonal).map(this::toResponse);
    }

    @Override
    public Integer createForPersona(Integer idDatosPersonal, Integer idUsuario, SarlaftRequest r) {
        if (repo.existsByIdDatosPersonal(idDatosPersonal)) {
            throw new IllegalStateException("Ya existe un registro SARLAFT para esta persona.");
        }
        Sarlaft e = new Sarlaft();
        e.setIdDatosPersonal(idDatosPersonal);
        applyReq(e, r, true, idUsuario);
        e = repo.save(e);
        return e.getIdSarlaft();
    }

    @Override
    public void updateForPersona(Integer idDatosPersonal, Integer idSarlaft, Integer idUsuario, SarlaftRequest r) {
        Sarlaft e = repo.findByIdSarlaftAndIdDatosPersonal(idSarlaft, idDatosPersonal)
                .orElseThrow(() -> new IllegalArgumentException("SARLAFT no encontrado para esta persona."));
        applyReq(e, r, false, idUsuario);
        repo.save(e);
    }

    @Override
    public void deleteForPersona(Integer idDatosPersonal, Integer idSarlaft) {
        Sarlaft e = repo.findByIdSarlaftAndIdDatosPersonal(idSarlaft, idDatosPersonal)
                .orElseThrow(() -> new IllegalArgumentException("SARLAFT no encontrado para esta persona."));
        repo.delete(e);
    }

    // ====== Reglas de normalización/validación ======
    private void applyReq(Sarlaft e, SarlaftRequest r, boolean creating, Integer userId) {
        final LocalDate today = LocalDate.now();

        // --- Exoneración UIAF ---
        boolean exUiaf = bool(r.exoneracionUiaf);
        e.setExoneracionUiaf(exUiaf);
        if (exUiaf) {
            require(r.fechaExoneracion != null, "fecha_exoneracion es obligatoria cuando exoneracion_uiaf = true");
            e.setFechaExoneracion(r.fechaExoneracion);
        } else {
            e.setFechaExoneracion(today);
        }

        // --- Asociado PEPs ---
        boolean asocPeps = bool(r.asociadoPeps);
        e.setAsociadoPeps(asocPeps);
        if (asocPeps) {
            require(notBlank(r.tipoPeps), "tipo_peps es obligatorio cuando asociado_peps = true");
            require(notBlank(r.observacionesPeps), "observaciones_peps es obligatoria cuando asociado_peps = true");
            require(r.fechaInicialPeps != null, "fecha_inicial_peps es obligatoria cuando asociado_peps = true");
            require(r.fechaFinalPeps != null, "fecha_final_peps es obligatoria cuando asociado_peps = true");
            require(!r.fechaInicialPeps.isAfter(r.fechaFinalPeps),
                    "fecha_inicial_peps no puede ser mayor que fecha_final_peps");

            e.setTipoPeps(trimLen(r.tipoPeps, 5));
            e.setObservacionesPeps(trimLen(r.observacionesPeps, 300));
            e.setFechaInicialPeps(r.fechaInicialPeps);
            e.setFechaFinalPeps(r.fechaFinalPeps);
        } else {
            e.setTipoPeps("000");
            e.setObservacionesPeps("No aplica");
            e.setFechaInicialPeps(today);
            e.setFechaFinalPeps(today);
        }

        // --- Familiar PEPs ---
        boolean famPeps = bool(r.familiaPeps);
        e.setFamiliaPeps(famPeps);
        if (famPeps) {
            require(notBlank(r.tipoFamiliaPeps), "tipo_familia_peps es obligatorio cuando familia_peps = true");
            require(notBlank(r.cedulaFamiliaPeps), "cedula_familia_peps es obligatoria cuando familia_peps = true");
            require(r.cedulaFamiliaPeps.matches("^[0-9]+$"), "cedula_familia_peps debe ser numérica");
            require(notBlank(r.nombreFamiliaPeps), "nombre_familia_peps es obligatorio cuando familia_peps = true");
            require(notBlank(r.codigoParentesco), "codigo_parentesco es obligatorio cuando familia_peps = true");

            e.setTipoFamiliaPeps(trimLen(r.tipoFamiliaPeps, 5));
            e.setCedulaFamiliaPeps(trimLen(r.cedulaFamiliaPeps, 20));
            e.setNombreFamiliaPeps(trimLen(r.nombreFamiliaPeps, 100));
            e.setCodigoParentesco(trimLen(r.codigoParentesco, 2));
        } else {
            e.setTipoFamiliaPeps("000");
            e.setCedulaFamiliaPeps("0");
            e.setNombreFamiliaPeps("No aplica");
            e.setCodigoParentesco("0");
        }

        // --- Moneda extranjera (negocios) ---
        boolean monExt = bool(r.monedaExtranjera);
        e.setMonedaExtranjera(monExt);
        if (monExt) {
            require(notBlank(r.observacionMonedaExtranjera),
                    "observacion_moneda_extranjera es obligatoria cuando moneda_extranjera = true");
            e.setObservacionMonedaExtranjera(trimLen(r.observacionMonedaExtranjera, 200));
        } else {
            e.setObservacionMonedaExtranjera("NO POSEE");
        }

        // --- Cuentas en el extranjero ---
        boolean ctaExt = bool(r.cuentaExtranjero);
        e.setCuentaExtranjero(ctaExt);
        if (ctaExt) {
            require(notBlank(r.tipoMonedaExtranjera), "tipo_moneda_extranjera es obligatorio cuando cuenta_extranjero = true");
            require(notBlank(r.numeroCuentaExtranjero), "numero_cuenta_extranjero es obligatorio cuando cuenta_extranjero = true");
            require(notBlank(r.nombreBancoExtranjero), "nombre_banco_extranjero es obligatorio cuando cuenta_extranjero = true");
            require(notBlank(r.ciudadCuentaExtranjero), "ciudad_cuenta_extranjero es obligatorio cuando cuenta_extranjero = true");
            require(notBlank(r.paisCuentaExtranjero), "pais_cuenta_extranjero es obligatorio cuando cuenta_extranjero = true");

            e.setTipoMonedaExtranjera(trimLen(r.tipoMonedaExtranjera, 20));
            e.setNumeroCuentaExtranjero(trimLen(r.numeroCuentaExtranjero, 30));
            e.setNombreBancoExtranjero(trimLen(r.nombreBancoExtranjero, 100));
            e.setCiudadCuentaExtranjero(trimLen(r.ciudadCuentaExtranjero, 50));
            e.setPaisCuentaExtranjero(trimLen(r.paisCuentaExtranjero, 50));
        } else {
            e.setTipoMonedaExtranjera("NO POSEE");
            e.setNumeroCuentaExtranjero("NO POSEE");
            e.setNombreBancoExtranjero("NO POSEE");
            e.setCiudadCuentaExtranjero("NO POSEE");
            e.setPaisCuentaExtranjero("NO POSEE");
        }

        // Auditoría
        if (creating) {
            e.setFkSeguridadCreacion(userId);
            e.setFechaCreacion(LocalDateTime.now());
        }
        e.setFkSeguridadActualizacion(userId);
        e.setFechaActualizacion(LocalDateTime.now());
    }

    private SarlaftResponse toResponse(Sarlaft e) {
        SarlaftResponse r = new SarlaftResponse();
        r.id_sarlaft = e.getIdSarlaft();
        r.id_datos_personal = e.getIdDatosPersonal();

        r.exoneracion_uiaf = bool(e.getExoneracionUiaf());
        r.fecha_exoneracion = toStr(e.getFechaExoneracion());

        r.asociado_peps = bool(e.getAsociadoPeps());
        r.tipo_peps = nullSafe(e.getTipoPeps());
        r.observaciones_peps = nullSafe(e.getObservacionesPeps());
        r.fecha_inicial_peps = toStr(e.getFechaInicialPeps());
        r.fecha_final_peps = toStr(e.getFechaFinalPeps());

        r.familia_peps = bool(e.getFamiliaPeps());
        r.tipo_familia_peps = nullSafe(e.getTipoFamiliaPeps());
        r.cedula_familia_peps = nullSafe(e.getCedulaFamiliaPeps());
        r.codigo_parentesco = nullSafe(e.getCodigoParentesco());
        r.nombre_familia_peps = nullSafe(e.getNombreFamiliaPeps());

        r.moneda_extranjera = bool(e.getMonedaExtranjera());
        r.observacion_moneda_extranjera = nullSafe(e.getObservacionMonedaExtranjera());

        r.cuenta_extranjero = bool(e.getCuentaExtranjero());
        r.tipo_moneda_extranjera = nullSafe(e.getTipoMonedaExtranjera());
        r.numero_cuenta_extranjero = nullSafe(e.getNumeroCuentaExtranjero());
        r.nombre_banco_extranjero = nullSafe(e.getNombreBancoExtranjero());
        r.ciudad_cuenta_extranjero = nullSafe(e.getCiudadCuentaExtranjero());
        r.pais_cuenta_extranjero = nullSafe(e.getPaisCuentaExtranjero());

        return r;
    }

    // ==== helpers ====
    private static boolean bool(Boolean b) { return b != null && b; }
    private static String trimLen(String s, int max) { return s == null ? null : (s.length() <= max ? s.trim() : s.trim().substring(0, max)); }
    private static boolean notBlank(String s) { return s != null && !s.trim().isEmpty(); }
    private static void require(boolean cond, String msg) { if (!cond) throw new IllegalArgumentException(msg); }
    private static String toStr(LocalDate d) { return d == null ? null : d.toString(); }
    private static String nullSafe(String s) { return s == null ? "" : s; }
}
