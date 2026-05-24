package co.assip.erp.sarlaft.evaluacion;

import co.assip.erp.sarlaft.domain.ReglaSarlaft;
import co.assip.erp.sarlaft.evaluacion.dto.EvaluacionResultado;
import co.assip.erp.sarlaft.evaluacion.dto.ReglasInput;
import co.assip.erp.sarlaft.evaluacion.dto.SarlaftAlertaResultadoDTO;
import co.assip.erp.sarlaft.reglas.ReglaSarlaftRepository;
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

    private void agregarAlerta(
            EvaluacionResultado resultado,
            String severidad,
            String codigo,
            String descripcion,
            String accion
    ) {

        SarlaftAlertaResultadoDTO alerta =
                new SarlaftAlertaResultadoDTO();

        alerta.setAlerta(true);
        alerta.setSeveridad(severidad);
        alerta.setDescripcion(descripcion);
        alerta.setNombreRegla(codigo);
        alerta.setBloqueaOperacion(false);
        alerta.setAccionRecomendada(accion);

        resultado.getAlertas().add(alerta);

        resultado.setAlerta(true);
        resultado.setSeveridad(severidad);
    }

    public EvaluacionResultado evaluar(ReglasInput input) {

        EvaluacionResultado resultado =
                new EvaluacionResultado();

        resultado.setAlerta(false);
        resultado.setSeveridad("VERDE");
        resultado.setDescripcion(
                "Evaluación exitosa sin alertas."
        );
        resultado.setBloqueaOperacion(false);

        // =====================================================
        // DEBUG
        // =====================================================

        System.out.println(
                "==== DEBUG SARLAFT CAMPOS AVANZADOS ===="
        );

        System.out.println(
                "ingresosMensuales = "
                        + input.getIngresosMensuales()
        );

        System.out.println(
                "egresosMensuales  = "
                        + input.getEgresosMensuales()
        );

        System.out.println(
                "totalActivos      = "
                        + input.getTotalActivos()
        );

        System.out.println(
                "totalPasivos      = "
                        + input.getTotalPasivos()
        );

        System.out.println(
                "========================================="
        );

        // =====================================================
        // REGLA 001
        // =====================================================

        ReglaSarlaft regla001 =
                reglaRepo.findByCodigo("001");

        Timestamp ultima =
                input.getFechaUltimaActualizacion();

        if (
                regla001 != null
                        && Boolean.TRUE.equals(
                        regla001.getActivo()
                )
                        && ultima != null
                        && regla001.getValorNumerico() != null
        ) {

            long dias = ChronoUnit.DAYS.between(
                    ultima.toLocalDateTime()
                            .toLocalDate(),
                    LocalDate.now()
            );

            if (dias > regla001.getValorNumerico()) {

                agregarAlerta(
                        resultado,
                        "ROJO",
                        "001",
                        "Datos personales desactualizados.",
                        "Actualizar información personal."
                );
            }
        }

        // =====================================================
        // REGLA 002
        // =====================================================

        ReglaSarlaft regla002 =
                reglaRepo.findByCodigo("002");

        if (
                regla002 != null
                        && Boolean.TRUE.equals(
                        regla002.getActivo()
                )
                        && input.getFechaNacimiento() != null
                        && input.getTipoDocumento() != null
        ) {

            int edad = input.getFechaNacimiento()
                    .toLocalDate()
                    .until(LocalDate.now())
                    .getYears();

            String tipo =
                    input.getTipoDocumento()
                            .toUpperCase();

            boolean incoherente = false;

            if (edad >= 18 && tipo.equals("TI")) {
                incoherente = true;
            }

            if (edad < 18 && tipo.equals("CC")) {
                incoherente = true;
            }

            if (incoherente) {

                agregarAlerta(
                        resultado,
                        "ROJO",
                        "002",
                        "Documento no corresponde a la edad (" +
                                edad +
                                " años).",
                        "Corregir tipo de documento."
                );
            }
        }

        // =====================================================
        // REGLA 003
        // =====================================================

        ReglaSarlaft regla003 =
                reglaRepo.findByCodigo("003");

        if (
                regla003 != null
                        && Boolean.TRUE.equals(
                        regla003.getActivo()
                )
                        && "02".equals(
                        input.getCodigoModulo()
                )
                        && input.getCodigoFormaAhorro() != null
                        && input.getFechaNacimiento() != null
                        && input.getCodigoFormaAhorro()
                        .equals(regla003.getValorTexto())
        ) {

            int edad = input.getFechaNacimiento()
                    .toLocalDate()
                    .until(LocalDate.now())
                    .getYears();

            if (
                    regla003.getValorNumerico() != null
                            && edad >
                            regla003.getValorNumerico()
                                    .intValue()
            ) {

                agregarAlerta(
                        resultado,
                        "ROJO",
                        "003",
                        "La forma de ahorro "
                                + regla003.getValorTexto()
                                + " no puede ser usada por mayores de "
                                + regla003.getValorNumerico()
                                + " años.",
                        "Usar una forma de ahorro válida."
                );
            }
        }

        // =====================================================
        // REGLA 004
        // =====================================================

        ReglaSarlaft regla004 =
                reglaRepo.findByCodigo("004");

        if (
                regla004 != null
                        && Boolean.TRUE.equals(
                        regla004.getActivo()
                )
        ) {

            Double ingresos =
                    input.getIngresosMensuales();

            Double monto =
                    input.getMonto();

            if (
                    ingresos != null
                            && ingresos > 0
                            && monto != null
                            && monto > ingresos
            ) {

                agregarAlerta(
                        resultado,
                        "ROJO",
                        "004",
                        "El monto de la operación (" +
                                monto +
                                ") supera los ingresos mensuales declarados (" +
                                ingresos +
                                ").",
                        "Solicitar soportes de ingresos."
                );
            }
        }

        // =====================================================
        // REGLA 005
        // =====================================================

        ReglaSarlaft regla005 =
                reglaRepo.findByCodigo("005");

        if (
                regla005 != null
                        && Boolean.TRUE.equals(
                        regla005.getActivo()
                )
        ) {

            Double egresos =
                    input.getEgresosMensuales();

            Double monto =
                    input.getMonto();

            if (
                    egresos != null
                            && monto != null
                            && monto > egresos
            ) {

                agregarAlerta(
                        resultado,
                        "ROJO",
                        "005",
                        "La operación (" +
                                monto +
                                ") supera los egresos mensuales declarados (" +
                                egresos +
                                ").",
                        "Verificar coherencia económica."
                );
            }
        }

        // =====================================================
        // REGLA 006
        // =====================================================

        ReglaSarlaft regla006 =
                reglaRepo.findByCodigo("006");

        if (
                regla006 != null
                        && Boolean.TRUE.equals(
                        regla006.getActivo()
                )
        ) {

            Double activos =
                    input.getTotalActivos();

            Double monto =
                    input.getMonto();

            if (
                    activos != null
                            && monto != null
                            && monto > activos
            ) {

                agregarAlerta(
                        resultado,
                        "ROJO",
                        "006",
                        "La operación (" +
                                monto +
                                ") supera el total de activos declarados (" +
                                activos +
                                ").",
                        "Validar capacidad patrimonial."
                );
            }
        }

        return resultado;
    }
}