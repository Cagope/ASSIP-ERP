package co.assip.erp.sarlaft.engine;

import co.assip.erp.sarlaft.dto.EvaluacionResultado;
import co.assip.erp.sarlaft.dto.ReglasInput;
import co.assip.erp.sarlaft.domain.ReglaSarlaft;
import co.assip.erp.sarlaft.repository.ReglaSarlaftRepository;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class AlertaEngine {

    private final ReglaSarlaftRepository reglaRepo;

    public AlertaEngine(ReglaSarlaftRepository reglaRepo) {
        this.reglaRepo = reglaRepo;
    }

    private boolean procesarRegla(ReglaSarlaft regla,
                                  boolean condicion,
                                  EvaluacionResultado resultado,
                                  String codigo,
                                  String descripcion,
                                  String accion) {

        if (regla == null || !Boolean.TRUE.equals(regla.getActivo())) {
            return false;
        }

        if (condicion) {
            resultado.setAlerta(true);
            resultado.setSeveridad("ROJO");
            resultado.setDescripcion(descripcion);
            resultado.setNombreRegla(codigo);
            resultado.setBloqueaOperacion(true);
            resultado.setAccionRecomendada(accion);
            return true;
        }

        return false;
    }

    public EvaluacionResultado evaluar(ReglasInput input) {

        EvaluacionResultado resultado = new EvaluacionResultado();

        // ============================================
        // 🔍 DEBUG — valores avanzados (no afecta reglas actuales)
        // ============================================
        System.out.println("==== DEBUG SARLAFT CAMPOS AVANZADOS ====");
        System.out.println("ingresosMensuales = " + input.getIngresosMensuales());
        System.out.println("egresosMensuales  = " + input.getEgresosMensuales());
        System.out.println("totalActivos      = " + input.getTotalActivos());
        System.out.println("totalPasivos      = " + input.getTotalPasivos());
        System.out.println("=========================================");


        resultado.setAlerta(false);
        resultado.setSeveridad("VERDE");
        resultado.setDescripcion("Evaluación exitosa sin alertas.");
        resultado.setIdAlerta(null);

        // ============================================
        // 🔥 REGLA 001 — DATOS_DESACTUALIZADOS
        // ============================================
        ReglaSarlaft regla001 = reglaRepo.findByCodigo("001");

        Timestamp ultima = input.getFechaUltimaActualizacion();
        boolean condicion001 = false;

        if (ultima != null && regla001 != null && regla001.getValorNumerico() != null) {
            long dias = ChronoUnit.DAYS.between(
                    ultima.toLocalDateTime().toLocalDate(),
                    LocalDate.now()
            );
            condicion001 = dias > regla001.getValorNumerico();
        }

        // 🚀 Nuevo motor de evaluación estandarizado
        if (procesarRegla(
                regla001,
                condicion001,
                resultado,
                "001",
                "Datos personales desactualizados.",
                "Actualizar información personal."
        )) {
            return resultado;
        }

        // ============================================
        // 🔥 REGLA 002 — DOCUMENTO NO CORRESPONDE A EDAD
        // ============================================
        ReglaSarlaft regla002 = reglaRepo.findByCodigo("002");

        boolean condicion002 = false;
        int edadCalculada = -1;

        if (input.getFechaNacimiento() != null && input.getTipoDocumento() != null) {

            edadCalculada = input.getFechaNacimiento()
                    .toLocalDate()
                    .until(LocalDate.now())
                    .getYears();

            String tipoDoc = input.getTipoDocumento().toUpperCase();

            boolean incoherente = false;

            if (edadCalculada >= 18 && tipoDoc.equals("TI")) {
                incoherente = true;
            }
            if (edadCalculada < 18 && tipoDoc.equals("CC")) {
                incoherente = true;
            }

            condicion002 = incoherente;
        }

        // 📝 Mensaje dinámico con la edad (si la pudimos calcular)
        String mensaje002 = "Documento no corresponde a la edad.";
        if (edadCalculada >= 0) {
            mensaje002 = "Documento no corresponde a la edad (" + edadCalculada + " años).";
        }

        // 🚀 Usar motor estándar
        if (procesarRegla(
                regla002,
                condicion002,
                resultado,
                "002",
                mensaje002,
                "Corregir tipo de documento según edad."
        )) {
            return resultado;
        }


        // ============================================
        // 🔥 REGLA 003 — FORMA 03 PROHIBIDA PARA MAYORES
        // ============================================
        ReglaSarlaft regla003 = reglaRepo.findByCodigo("003");

        boolean condicion003 = false;
        String mensaje003 = "La forma de ahorro no es permitida para esta edad.";

        // Solo aplica si el módulo es depósitos
        if (regla003 != null &&
                Boolean.TRUE.equals(regla003.getActivo()) &&
                "02".equals(input.getCodigoModulo()) &&
                input.getCodigoFormaAhorro() != null &&
                input.getCodigoFormaAhorro().equals(regla003.getValorTexto()) &&
                input.getFechaNacimiento() != null) {

            int edad = input.getFechaNacimiento()
                    .toLocalDate()
                    .until(LocalDate.now())
                    .getYears();

            // criterio: edad > valorNumerico definido en la regla
            if (regla003.getValorNumerico() != null &&
                    edad > regla003.getValorNumerico().intValue()) {

                condicion003 = true;

                mensaje003 =
                        "La forma de ahorro " + regla003.getValorTexto() +
                                " no puede ser usada por mayores de " +
                                regla003.getValorNumerico() +
                                " años. (Edad: " + edad + ")";
            }
        }

        // 🚀 Usar motor estándar
        if (procesarRegla(
                regla003,
                condicion003,
                resultado,
                "003",
                mensaje003,
                "El asociado debe usar una forma de ahorro permitida según su edad."
        )) {
            return resultado;
        }


        // ============================================
        // 🔥 REGLAS AVANZADAS (estructura lista, pero INACTIVAS)
        // ============================================

        // ============================================
        // 🔥 REGLA 004 — MONTO MAYOR QUE INGRESOS MENSUALES
        // ============================================
        ReglaSarlaft regla004 = reglaRepo.findByCodigo("004");

        if (regla004 != null && Boolean.TRUE.equals(regla004.getActivo())) {

            Double ingresos = input.getIngresosMensuales();
            Double monto = input.getMonto();

            if (ingresos != null && ingresos > 0 &&
                    monto != null && monto > 0 &&
                    monto > ingresos) {

                resultado.setAlerta(true);
                resultado.setSeveridad("ROJO");
                resultado.setDescripcion(
                        "El monto de la operación (" + monto +
                                ") supera los ingresos mensuales declarados (" + ingresos + ")."
                );
                resultado.setNombreRegla("004");
                resultado.setBloqueaOperacion(true);
                resultado.setAccionRecomendada(
                        "Solicitar soportes de ingresos o revisar operación sospechosa."
                );
                return resultado;
            }
        }


        // ============================================
        // 🔥 REGLA 005 — OPERACIÓN > EGRESOS MENSUALES
        // ============================================
        ReglaSarlaft regla005 = reglaRepo.findByCodigo("005");

        if (regla005 != null && Boolean.TRUE.equals(regla005.getActivo())) {

            Double egresos = input.getEgresosMensuales();
            Double monto   = input.getMonto();

            // Aplica solo si ambos valores existen
            if (egresos != null && monto != null) {

                // Si monto es mayor a egresos declarados
                if (monto > egresos) {

                    resultado.setAlerta(true);
                    resultado.setSeveridad("ROJO");
                    resultado.setDescripcion(
                            "La operación (" + monto +
                                    ") supera los egresos mensuales declarados (" + egresos + ")."
                    );
                    resultado.setNombreRegla("005");
                    resultado.setBloqueaOperacion(true);
                    resultado.setAccionRecomendada(
                            "Verificar coherencia entre movimiento y capacidad económica del asociado."
                    );
                    return resultado;
                }
            }
        }


        // ============================================
        // 🔥 REGLA 006 — OPERACIÓN > TOTAL DE ACTIVOS
        // ============================================
        ReglaSarlaft regla006 = reglaRepo.findByCodigo("006");

        if (regla006 != null && Boolean.TRUE.equals(regla006.getActivo())) {

            Double activos = input.getTotalActivos();
            Double monto   = input.getMonto();

            if (activos != null && monto != null) {

                if (monto > activos) {

                    resultado.setAlerta(true);
                    resultado.setSeveridad("ROJO");
                    resultado.setDescripcion(
                            "La operación (" + monto +
                                    ") supera el total de activos declarados (" + activos + ")."
                    );
                    resultado.setNombreRegla("006");
                    resultado.setBloqueaOperacion(true);
                    resultado.setAccionRecomendada(
                            "Validar la capacidad patrimonial y solicitar documentos de soporte."
                    );
                    return resultado;
                }
            }
        }


        // ============================================
        // 🔥 REGLA 007 — PREPAGO DE CARTERA > INGRESOS
        // ============================================
        ReglaSarlaft regla007 = reglaRepo.findByCodigo("007");

        if (regla007 != null && Boolean.TRUE.equals(regla007.getActivo())) {

            // Solo aplica al módulo de cartera
            if ("03".equals(input.getCodigoModulo()) &&
                    "PREPAGO".equalsIgnoreCase(input.getAccion())) {

                Double ingresos = input.getIngresosMensuales();
                Double monto    = input.getMonto();

                if (ingresos != null && monto != null) {

                    if (monto > ingresos) {

                        resultado.setAlerta(true);
                        resultado.setSeveridad("ROJO");
                        resultado.setDescripcion(
                                "El prepago (" + monto +
                                        ") supera los ingresos mensuales declarados (" + ingresos + ")."
                        );
                        resultado.setNombreRegla("007");
                        resultado.setBloqueaOperacion(true);
                        resultado.setAccionRecomendada(
                                "Solicitar justificación y soportes del origen de fondos."
                        );
                        return resultado;
                    }
                }
            }
        }


        // ============================================
        // 🔥 REGLA 008 — CDTA > TOTAL ACTIVOS
        // ============================================
        ReglaSarlaft regla008 = reglaRepo.findByCodigo("008");

        if (regla008 != null && Boolean.TRUE.equals(regla008.getActivo())) {

            // Solo aplica en módulo de inversiones (CDTA)
            if ("04".equals(input.getCodigoModulo()) &&
                    ("CONSTITUCION".equalsIgnoreCase(input.getAccion()) ||
                            "RENOVACION".equalsIgnoreCase(input.getAccion()))) {

                Double activos = input.getTotalActivos();
                Double monto   = input.getMonto();

                if (activos != null && monto != null) {

                    if (monto > activos) {

                        resultado.setAlerta(true);
                        resultado.setSeveridad("ROJO");
                        resultado.setDescripcion(
                                "El valor del CDTA (" + monto +
                                        ") supera el total de activos declarados (" + activos + ")."
                        );
                        resultado.setNombreRegla("008");
                        resultado.setBloqueaOperacion(true);
                        resultado.setAccionRecomendada(
                                "Solicitar soporte adicional de fondos o actualización de información patrimonial."
                        );
                        return resultado;
                    }
                }
            }
        }


        return resultado;
    }
}
