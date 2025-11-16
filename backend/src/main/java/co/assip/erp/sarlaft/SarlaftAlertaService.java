package co.assip.erp.sarlaft;

import co.assip.erp.sarlaft.domain.Alerta;
import co.assip.erp.sarlaft.dto.EvaluacionResultado;
import co.assip.erp.sarlaft.dto.ReglasInput;
import co.assip.erp.sarlaft.engine.AlertaEngine;
import co.assip.erp.sarlaft.repository.AlertaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ⭐ SarlaftAlertaService
 * ------------------------------------------------------------
 * Servicio oficial SARLAFT.
 */
@Service
@Transactional
public class SarlaftAlertaService {

    private final AlertaEngine engine;
    private final AlertaRepository alertaRepository;

    public SarlaftAlertaService(AlertaEngine engine,
                                AlertaRepository alertaRepository) {
        this.engine = engine;
        this.alertaRepository = alertaRepository;
    }

    /**
     * 📌 Método principal.
     */
    public EvaluacionResultado evaluarOperacion(ReglasInput input) {

        // ============================================
        // 🔍 DEBUG — verificar datos avanzados recibidos
        // ============================================
        System.out.println("==== DEBUG SARLAFT SERVICE ====");
        System.out.println("IngresosMensuales = " + input.getIngresosMensuales());
        System.out.println("EgresosMensuales  = " + input.getEgresosMensuales());
        System.out.println("TotalActivos      = " + input.getTotalActivos());
        System.out.println("TotalPasivos      = " + input.getTotalPasivos());
        System.out.println("================================");


        // 1️⃣ Ejecutar motor SARLAFT
        EvaluacionResultado resultado = engine.evaluar(input);

        // 2️⃣ Si no hay alerta, devolver resultado tal cual
        if (!resultado.isAlerta() || !"ROJO".equals(resultado.getSeveridad())) {
            return resultado;
        }

        // 3️⃣ Registrar alerta en BD
        Alerta alerta = new Alerta();
        alerta.setIdDatosPersonal(input.getIdDatosPersonal());
        alerta.setIdAgencia(input.getIdAgencia());
        alerta.setCodigoModulo(input.getCodigoModulo());
        alerta.setSeveridad(resultado.getSeveridad());
        alerta.setDescripcion(resultado.getDescripcion());
        alerta.setAccionSistema("Evaluación SARLAFT");
        alerta.setCodigoRegla(resultado.getNombreRegla());

        Alerta guardada = alertaRepository.save(alerta);

        // 4️⃣ Poner ID generado en la respuesta
        resultado.setIdAlerta(guardada.getIdAlerta());

        return resultado;
    }
}
