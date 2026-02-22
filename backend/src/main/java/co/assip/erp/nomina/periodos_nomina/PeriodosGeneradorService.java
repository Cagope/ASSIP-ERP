package co.assip.erp.nomina.periodos_nomina;

import co.assip.erp.nomina.periodos_nomina.dto.*;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class PeriodosGeneradorService {

    private final PeriodosGeneradorRepository repo;

    public PeriodosGeneradorService(
            PeriodosGeneradorRepository repo
    ) {
        this.repo = repo;
    }

    // =========================================================
    // GENERAR PERÍODOS
    // =========================================================
    public List<PeriodoGeneradoDTO> generar(
            PeriodosGenerarRequestDTO req,
            Integer idUsuario
    ) {

        if (req == null)
            throw new IllegalArgumentException(
                    "Datos obligatorios"
            );

        if (req.getIdAgencia() == null)
            throw new IllegalArgumentException(
                    "Agencia obligatoria"
            );

        if (req.getAnio() == null)
            throw new IllegalArgumentException(
                    "Año obligatorio"
            );

        // 🔥 BLOQUEAR SI YA EXISTEN
        if (repo.existenPeriodos(
                req.getIdAgencia(),
                req.getAnio()
        )) {
            throw new IllegalStateException(
                    "Ya existen períodos para la agencia y año seleccionados"
            );
        }

        String tipo =
                (req.getTipoPeriodo() == null ||
                        req.getTipoPeriodo().isBlank())
                        ? "MENSUAL"
                        : req.getTipoPeriodo()
                        .trim()
                        .toUpperCase();

        Map<String, Object> calendario =
                repo.obtenerCalendario(
                        req.getIdAgencia()
                );

        if (calendario == null) {

            repo.crearCalendarioDefault(
                    req.getIdAgencia(),
                    tipo,
                    idUsuario
            );

            calendario =
                    repo.obtenerCalendario(
                            req.getIdAgencia()
                    );
        }

        Integer idCalendario =
                (Integer) calendario.get(
                        "id_calendario"
                );

        List<PeriodoGeneradoDTO> generados =
                new ArrayList<>();

        for (int mes = 1; mes <= 12; mes++) {

            YearMonth ym =
                    YearMonth.of(
                            req.getAnio(),
                            mes
                    );

            if ("MENSUAL".equals(tipo)) {

                insertar(
                        req,
                        idUsuario,
                        idCalendario,
                        tipo,
                        mes,
                        1,
                        ym.atDay(1),
                        ym.atEndOfMonth(),
                        generados
                );

            } else if ("QUINCENAL".equals(tipo)) {

                insertar(
                        req,
                        idUsuario,
                        idCalendario,
                        tipo,
                        mes,
                        1,
                        ym.atDay(1),
                        ym.atDay(15),
                        generados
                );

                insertar(
                        req,
                        idUsuario,
                        idCalendario,
                        tipo,
                        mes,
                        2,
                        ym.atDay(16),
                        ym.atEndOfMonth(),
                        generados
                );

            } else {
                throw new IllegalArgumentException(
                        "Tipo no soportado: " + tipo
                );
            }
        }

        return generados;
    }

    // =========================================================
    // HELPER INSERT
    // =========================================================
    private void insertar(
            PeriodosGenerarRequestDTO req,
            Integer idUsuario,
            Integer idCalendario,
            String tipo,
            int mes,
            int numero,
            LocalDate inicio,
            LocalDate fin,
            List<PeriodoGeneradoDTO> generados
    ) {

        String descripcion =
                req.getAnio() + "-"
                        + String.format("%02d", mes)
                        + "-" + numero;

        MapSqlParameterSource params =
                new MapSqlParameterSource()
                        .addValue(
                                "idAgencia",
                                req.getIdAgencia()
                        )
                        .addValue(
                                "idCalendario",
                                idCalendario
                        )
                        .addValue(
                                "anio",
                                req.getAnio()
                        )
                        .addValue(
                                "mes",
                                mes
                        )
                        .addValue(
                                "tipo",
                                tipo
                        )
                        .addValue(
                                "numero",
                                numero
                        )
                        .addValue(
                                "inicio",
                                inicio
                        )
                        .addValue(
                                "fin",
                                fin
                        )
                        .addValue(
                                "descripcion",
                                descripcion
                        )
                        .addValue(
                                "usuario",
                                idUsuario != null
                                        ? idUsuario
                                        : 1
                        );

        repo.insertarPeriodo(params);

        generados.add(
                PeriodoGeneradoDTO.builder()
                        .anio(req.getAnio())
                        .mes(mes)
                        .fechaInicio(inicio)
                        .fechaFin(fin)
                        .descripcion(descripcion)
                        .build()
        );
    }
}
