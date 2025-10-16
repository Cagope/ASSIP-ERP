package co.assip.erp.hojavida.service.impl;

import co.assip.erp.hojavida.domain.Financiero;
import co.assip.erp.hojavida.dto.FinancierosDTOs.FinancieroRequest;
import co.assip.erp.hojavida.dto.FinancierosDTOs.FinancieroResponse;
import co.assip.erp.hojavida.repository.FinancieroRepository;
import co.assip.erp.hojavida.service.FinancieroService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.Optional;

@Service
@Transactional
public class FinancieroServiceImpl implements FinancieroService {

    private final FinancieroRepository repo;

    public FinancieroServiceImpl(FinancieroRepository repo) { this.repo = repo; }

    @Override @Transactional(readOnly = true)
    public Optional<FinancieroResponse> getByPersona(Integer idDatosPersonal) {
        return repo.findByIdDatosPersonal(idDatosPersonal).map(this::toResponse);
    }

    @Override
    public Integer createForPersona(Integer idDatosPersonal, Integer idUsuario, FinancieroRequest r) {
        if (repo.existsByIdDatosPersonal(idDatosPersonal)) {
            throw new IllegalStateException("Ya existe un registro financiero para esta persona.");
        }
        validarReglas(r);
        Financiero e = new Financiero();
        e.setIdDatosPersonal(idDatosPersonal);
        applyReq(e, r, true, idUsuario);
        e = repo.save(e);
        return e.getIdFinanciero();
    }

    @Override
    public void updateForPersona(Integer idDatosPersonal, Integer idFinanciero, Integer idUsuario, FinancieroRequest r) {
        Financiero e = repo.findByIdFinancieroAndIdDatosPersonal(idFinanciero, idDatosPersonal)
                .orElseThrow(() -> new IllegalArgumentException("Registro no encontrado para esta persona."));
        validarReglas(r);
        applyReq(e, r, false, idUsuario);
        repo.save(e);
    }

    @Override
    public void deleteForPersona(Integer idDatosPersonal, Integer idFinanciero) {
        Financiero e = repo.findByIdFinancieroAndIdDatosPersonal(idFinanciero, idDatosPersonal)
                .orElseThrow(() -> new IllegalArgumentException("Registro no encontrado para esta persona."));
        repo.delete(e);
    }

    // -------- helpers --------
    private void validarReglas(FinancieroRequest r) {
        // numéricos >= 0
        checkNonNeg(r.valorSalario, "valor_salario");
        checkNonNeg(r.valorPension, "valor_pension");
        checkNonNeg(r.ingresosArriendo, "ingresos_arriendo");
        checkNonNeg(r.ingresosComisiones, "ingresos_comisiones");
        checkNonNeg(r.otrosIngresos, "otros_ingresos");
        checkNonNeg(r.egresosFamiliares, "egresos_familiares");
        checkNonNeg(r.egresosArriendo, "egresos_arriendo");
        checkNonNeg(r.egresosCredito, "egresos_credito");
        checkNonNeg(r.otrosEgresos, "otros_egresos");
        checkNonNeg(r.totalActivos, "total_activos");
        checkNonNeg(r.totalPasivos, "total_pasivos");
        checkNonNeg(r.deudaRelacionFinanciera, "deuda_relacion_financiera");

        // obligatorios de texto
        reqText(r.origenFondos, "origen_fondos");
        reqText(r.relacionFinanciera, "relacion_financiera");
        reqText(r.comentarioOtrosIngresos, "comentario_otros_ingresos");
        reqText(r.comentarioOtrosEgresos, "comentario_otros_egresos");

        // condicionales:
        if (gtZero(r.otrosIngresos) && isBlank(r.comentarioOtrosIngresos)) {
            throw new IllegalArgumentException("comentario_otros_ingresos es obligatorio cuando otros_ingresos > 0");
        }
        if (gtZero(r.otrosEgresos) && isBlank(r.comentarioOtrosEgresos)) {
            throw new IllegalArgumentException("comentario_otros_egresos es obligatorio cuando otros_egresos > 0");
        }
        if (gtZero(r.egresosCredito)) {
            if (isBlank(r.relacionFinanciera)) {
                throw new IllegalArgumentException("relacion_financiera es obligatoria cuando egresos_credito > 0");
            }
            if (r.deudaRelacionFinanciera == null || r.deudaRelacionFinanciera.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("deuda_relacion_financiera debe ser > 0 cuando egresos_credito > 0");
            }
        }
    }

    private static boolean gtZero(BigDecimal v) { return v != null && v.compareTo(BigDecimal.ZERO) > 0; }
    private static void checkNonNeg(BigDecimal v, String field) {
        if (v == null || v.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(field + " debe ser >= 0");
        }
    }
    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
    private static void reqText(String s, String field) {
        if (isBlank(s)) throw new IllegalArgumentException(field + " es obligatorio");
    }

    private void applyReq(Financiero e, FinancieroRequest r, boolean creating, Integer uid) {
        e.setValorSalario(r.valorSalario);
        e.setValorPension(r.valorPension);
        e.setIngresosArriendo(r.ingresosArriendo);
        e.setIngresosComisiones(r.ingresosComisiones);
        e.setOtrosIngresos(r.otrosIngresos);
        e.setComentarioOtrosIngresos(trimMax(r.comentarioOtrosIngresos, 100));
        e.setEgresosFamiliares(r.egresosFamiliares);
        e.setEgresosArriendo(r.egresosArriendo);
        e.setEgresosCredito(r.egresosCredito);
        e.setOtrosEgresos(r.otrosEgresos);
        e.setComentarioOtrosEgresos(trimMax(r.comentarioOtrosEgresos, 100));
        e.setTotalActivos(r.totalActivos);
        e.setTotalPasivos(r.totalPasivos);
        e.setOrigenFondos(trimMax(r.origenFondos, 100));
        e.setRelacionFinanciera(trimMax(r.relacionFinanciera, 100));
        e.setDeudaRelacionFinanciera(r.deudaRelacionFinanciera);

        if (creating) {
            e.setFkSeguridadCreacion(uid);
            e.setFechaCreacion(LocalDateTime.now());
        }
        e.setFkSeguridadActualizacion(uid);
        e.setFechaActualizacion(LocalDateTime.now());
    }

    private static String trimMax(String s, int max) {
        if (s == null) return null;
        String t = s.trim();
        return t.length() > max ? t.substring(0, max) : t;
    }

    private FinancieroResponse toResponse(Financiero e) {
        FinancieroResponse r = new FinancieroResponse();
        r.id_financiero = e.getIdFinanciero();
        r.id_datos_personal = e.getIdDatosPersonal();
        r.valor_salario = e.getValorSalario();
        r.valor_pension = e.getValorPension();
        r.ingresos_arriendo = e.getIngresosArriendo();
        r.ingresos_comisiones = e.getIngresosComisiones();
        r.otros_ingresos = e.getOtrosIngresos();
        r.comentario_otros_ingresos = e.getComentarioOtrosIngresos();
        r.egresos_familiares = e.getEgresosFamiliares();
        r.egresos_arriendo = e.getEgresosArriendo();
        r.egresos_credito = e.getEgresosCredito();
        r.otros_egresos = e.getOtrosEgresos();
        r.comentario_otros_egresos = e.getComentarioOtrosEgresos();
        r.total_activos = e.getTotalActivos();
        r.total_pasivos = e.getTotalPasivos();
        r.origen_fondos = e.getOrigenFondos();
        r.relacion_financiera = e.getRelacionFinanciera();
        r.deuda_relacion_financiera = e.getDeudaRelacionFinanciera();
        return r;
    }
}
