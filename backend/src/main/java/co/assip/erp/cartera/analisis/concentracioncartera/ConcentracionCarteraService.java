package co.assip.erp.cartera.analisis.concentracioncartera;

import co.assip.erp.cartera.analisis.concentracioncartera.dto.ConcentracionCarteraControlDTO;
import co.assip.erp.cartera.analisis.concentracioncartera.dto.ConcentracionCarteraResumenDTO;
import co.assip.erp.cartera.analisis.concentracioncartera.dto.ConcentracionDetalleCreditoDTO;
import co.assip.erp.cartera.analisis.concentracioncartera.dto.ConcentracionDetalleDeudorDTO;
import co.assip.erp.cartera.analisis.concentracioncartera.dto.ConcentracionRankingDeudorDTO;
import co.assip.erp.cartera.analisis.concentracioncartera.dto.ConcentracionSegmentoDTO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class ConcentracionCarteraService {

    private static final Set<String> EDADES_VALIDAS = Set.of("A", "B", "C", "D", "E", "F");

    private final ConcentracionCarteraRepository repository;

    public ConcentracionCarteraService(ConcentracionCarteraRepository repository) {
        this.repository = repository;
    }

    public ConcentracionCarteraControlDTO control() {
        return repository.control();
    }

    public List<LocalDate> cortes() {
        return repository.cortes();
    }

    public ConcentracionCarteraResumenDTO resumen(
            LocalDate fechaCorte,
            Integer idAgencia,
            Integer idLineaCredito,
            String codigoGarantia,
            String codigoClasificacion,
            String edadContable,
            String codigoDestino
    ) {
        Filtros filtros = prepararFiltros(fechaCorte, idAgencia, idLineaCredito,
                codigoGarantia, codigoClasificacion, edadContable, codigoDestino);

        return repository.resumen(
                filtros.fechaCorte(),
                filtros.idAgencia(),
                filtros.idLineaCredito(),
                filtros.codigoGarantia(),
                filtros.codigoClasificacion(),
                filtros.edadContable(),
                filtros.codigoDestino()
        );
    }

    public List<ConcentracionRankingDeudorDTO> deudoresExposicion(
            LocalDate fechaCorte,
            Integer limite,
            Integer idAgencia,
            Integer idLineaCredito,
            String codigoGarantia,
            String codigoClasificacion,
            String edadContable,
            String codigoDestino
    ) {
        Filtros filtros = prepararFiltros(fechaCorte, idAgencia, idLineaCredito,
                codigoGarantia, codigoClasificacion, edadContable, codigoDestino);

        int limiteSeguro = validarLimite(limite);

        return repository.deudoresExposicion(
                filtros.fechaCorte(), limiteSeguro,
                filtros.idAgencia(), filtros.idLineaCredito(),
                filtros.codigoGarantia(), filtros.codigoClasificacion(),
                filtros.edadContable(), filtros.codigoDestino()
        );
    }

    public List<ConcentracionRankingDeudorDTO> deudoresDeterioro(
            LocalDate fechaCorte,
            Integer limite,
            Integer idAgencia,
            Integer idLineaCredito,
            String codigoGarantia,
            String codigoClasificacion,
            String edadContable,
            String codigoDestino
    ) {
        Filtros filtros = prepararFiltros(fechaCorte, idAgencia, idLineaCredito,
                codigoGarantia, codigoClasificacion, edadContable, codigoDestino);

        int limiteSeguro = validarLimite(limite);

        return repository.deudoresDeterioro(
                filtros.fechaCorte(), limiteSeguro,
                filtros.idAgencia(), filtros.idLineaCredito(),
                filtros.codigoGarantia(), filtros.codigoClasificacion(),
                filtros.edadContable(), filtros.codigoDestino()
        );
    }

    public List<ConcentracionSegmentoDTO> lineas(
            LocalDate fechaCorte,
            Integer idAgencia,
            Integer idLineaCredito,
            String codigoGarantia,
            String codigoClasificacion,
            String edadContable,
            String codigoDestino
    ) {
        Filtros f = prepararFiltros(fechaCorte, idAgencia, idLineaCredito,
                codigoGarantia, codigoClasificacion, edadContable, codigoDestino);
        return repository.lineas(f.fechaCorte(), f.idAgencia(), f.idLineaCredito(),
                f.codigoGarantia(), f.codigoClasificacion(), f.edadContable(), f.codigoDestino());
    }

    public List<ConcentracionSegmentoDTO> agencias(
            LocalDate fechaCorte,
            Integer idAgencia,
            Integer idLineaCredito,
            String codigoGarantia,
            String codigoClasificacion,
            String edadContable,
            String codigoDestino
    ) {
        Filtros f = prepararFiltros(fechaCorte, idAgencia, idLineaCredito,
                codigoGarantia, codigoClasificacion, edadContable, codigoDestino);
        return repository.agencias(f.fechaCorte(), f.idAgencia(), f.idLineaCredito(),
                f.codigoGarantia(), f.codigoClasificacion(), f.edadContable(), f.codigoDestino());
    }

    public List<ConcentracionSegmentoDTO> garantias(
            LocalDate fechaCorte,
            Integer idAgencia,
            Integer idLineaCredito,
            String codigoGarantia,
            String codigoClasificacion,
            String edadContable,
            String codigoDestino
    ) {
        Filtros f = prepararFiltros(fechaCorte, idAgencia, idLineaCredito,
                codigoGarantia, codigoClasificacion, edadContable, codigoDestino);
        return repository.garantias(f.fechaCorte(), f.idAgencia(), f.idLineaCredito(),
                f.codigoGarantia(), f.codigoClasificacion(), f.edadContable(), f.codigoDestino());
    }

    public List<ConcentracionSegmentoDTO> clasificaciones(
            LocalDate fechaCorte,
            Integer idAgencia,
            Integer idLineaCredito,
            String codigoGarantia,
            String codigoClasificacion,
            String edadContable,
            String codigoDestino
    ) {
        Filtros f = prepararFiltros(fechaCorte, idAgencia, idLineaCredito,
                codigoGarantia, codigoClasificacion, edadContable, codigoDestino);
        return repository.clasificaciones(f.fechaCorte(), f.idAgencia(), f.idLineaCredito(),
                f.codigoGarantia(), f.codigoClasificacion(), f.edadContable(), f.codigoDestino());
    }

    public List<ConcentracionSegmentoDTO> edadesContables(
            LocalDate fechaCorte,
            Integer idAgencia,
            Integer idLineaCredito,
            String codigoGarantia,
            String codigoClasificacion,
            String edadContable,
            String codigoDestino
    ) {
        Filtros f = prepararFiltros(fechaCorte, idAgencia, idLineaCredito,
                codigoGarantia, codigoClasificacion, edadContable, codigoDestino);
        return repository.edadesContables(f.fechaCorte(), f.idAgencia(), f.idLineaCredito(),
                f.codigoGarantia(), f.codigoClasificacion(), f.edadContable(), f.codigoDestino());
    }

    public List<ConcentracionSegmentoDTO> destinos(
            LocalDate fechaCorte,
            Integer idAgencia,
            Integer idLineaCredito,
            String codigoGarantia,
            String codigoClasificacion,
            String edadContable,
            String codigoDestino
    ) {
        Filtros f = prepararFiltros(fechaCorte, idAgencia, idLineaCredito,
                codigoGarantia, codigoClasificacion, edadContable, codigoDestino);
        return repository.destinos(f.fechaCorte(), f.idAgencia(), f.idLineaCredito(),
                f.codigoGarantia(), f.codigoClasificacion(), f.edadContable(), f.codigoDestino());
    }

    public List<ConcentracionDetalleDeudorDTO> detalleDeudores(
            LocalDate fechaCorte,
            Integer idAgencia,
            Integer idLineaCredito,
            String codigoGarantia,
            String codigoClasificacion,
            String edadContable,
            String codigoDestino
    ) {
        Filtros f = prepararFiltros(fechaCorte, idAgencia, idLineaCredito,
                codigoGarantia, codigoClasificacion, edadContable, codigoDestino);
        return repository.detalleDeudores(f.fechaCorte(), f.idAgencia(), f.idLineaCredito(),
                f.codigoGarantia(), f.codigoClasificacion(), f.edadContable(), f.codigoDestino());
    }

    public List<ConcentracionDetalleCreditoDTO> detalleCreditos(
            LocalDate fechaCorte,
            Long idDatosPersonal,
            Integer idAgencia,
            Integer idLineaCredito,
            String codigoGarantia,
            String codigoClasificacion,
            String edadContable,
            String codigoDestino
    ) {
        Filtros f = prepararFiltros(fechaCorte, idAgencia, idLineaCredito,
                codigoGarantia, codigoClasificacion, edadContable, codigoDestino);
        return repository.detalleCreditos(f.fechaCorte(), idDatosPersonal,
                f.idAgencia(), f.idLineaCredito(), f.codigoGarantia(),
                f.codigoClasificacion(), f.edadContable(), f.codigoDestino());
    }

    private Filtros prepararFiltros(
            LocalDate fechaCorte,
            Integer idAgencia,
            Integer idLineaCredito,
            String codigoGarantia,
            String codigoClasificacion,
            String edadContable,
            String codigoDestino
    ) {
        LocalDate corte = fechaCorte != null ? fechaCorte : repository.ultimoCorte();

        if (corte == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No existen cortes históricos disponibles para el análisis."
            );
        }

        if (!repository.existeCorte(corte)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La fecha de corte seleccionada no existe en el histórico: " + corte
            );
        }

        String edad = normalizar(edadContable);
        if (edad != null && !EDADES_VALIDAS.contains(edad)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Edad contable no válida. Valores permitidos: A, B, C, D, E, F."
            );
        }

        return new Filtros(
                corte,
                idAgencia,
                idLineaCredito,
                normalizar(codigoGarantia),
                normalizar(codigoClasificacion),
                edad,
                normalizar(codigoDestino)
        );
    }

    private int validarLimite(Integer limite) {
        int valor = limite == null ? 50 : limite;
        if (valor < 1 || valor > 500) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El límite debe estar entre 1 y 500."
            );
        }
        return valor;
    }

    private String normalizar(String valor) {
        if (valor == null) {
            return null;
        }
        String limpio = valor.trim();
        return limpio.isEmpty() ? null : limpio.toUpperCase(Locale.ROOT);
    }

    private record Filtros(
            LocalDate fechaCorte,
            Integer idAgencia,
            Integer idLineaCredito,
            String codigoGarantia,
            String codigoClasificacion,
            String edadContable,
            String codigoDestino
    ) {
    }
}
