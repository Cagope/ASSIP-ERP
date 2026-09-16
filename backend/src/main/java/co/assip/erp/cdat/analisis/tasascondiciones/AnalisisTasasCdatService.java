package co.assip.erp.cdat.analisis.tasascondiciones;

import co.assip.erp.cdat.analisis.tasascondiciones.dto.AnalisisTasasCdatCondicionDTO;
import co.assip.erp.cdat.analisis.tasascondiciones.dto.AnalisisTasasCdatCorteDTO;
import co.assip.erp.cdat.analisis.tasascondiciones.dto.AnalisisTasasCdatDetalleDTO;
import co.assip.erp.cdat.analisis.tasascondiciones.dto.AnalisisTasasCdatRangoSaldoDTO;
import co.assip.erp.cdat.analisis.tasascondiciones.dto.AnalisisTasasCdatResumenDTO;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class AnalisisTasasCdatService {

    private final AnalisisTasasCdatRepository repository;

    public AnalisisTasasCdatService(
            AnalisisTasasCdatRepository repository
    ) {
        this.repository = repository;
    }


    // =========================================================
    // CORTES DISPONIBLES
    // =========================================================

    public List<AnalisisTasasCdatCorteDTO> listarCortes() {

        return repository.listarCortes();
    }


    // =========================================================
    // RESUMEN
    // =========================================================

    public AnalisisTasasCdatResumenDTO obtenerResumen(
            LocalDate fechaCorte,
            Integer idAgencia,
            Integer plazoMeses,
            String amortizacion,
            String rangoSaldo
    ) {

        validarFechaCorte(fechaCorte);

        return repository.obtenerResumen(
                fechaCorte,
                normalizarId(idAgencia),
                normalizarId(plazoMeses),
                normalizarTexto(amortizacion),
                normalizarTexto(rangoSaldo)
        );
    }


    // =========================================================
    // CONDICIONES DE CAPTACIÓN
    // =========================================================

    public List<AnalisisTasasCdatCondicionDTO> obtenerCondiciones(
            LocalDate fechaCorte,
            Integer idAgencia,
            Integer plazoMeses,
            String amortizacion,
            String rangoSaldo
    ) {

        validarFechaCorte(fechaCorte);

        return repository.obtenerCondiciones(
                fechaCorte,
                normalizarId(idAgencia),
                normalizarId(plazoMeses),
                normalizarTexto(amortizacion),
                normalizarTexto(rangoSaldo)
        );
    }


    // =========================================================
    // RANGOS DE SALDO
    // =========================================================

    public List<AnalisisTasasCdatRangoSaldoDTO> obtenerRangosSaldo(
            LocalDate fechaCorte,
            Integer idAgencia,
            Integer plazoMeses,
            String amortizacion,
            String rangoSaldo
    ) {

        validarFechaCorte(fechaCorte);

        return repository.obtenerRangosSaldo(
                fechaCorte,
                normalizarId(idAgencia),
                normalizarId(plazoMeses),
                normalizarTexto(amortizacion),
                normalizarTexto(rangoSaldo)
        );
    }


    // =========================================================
    // DETALLE
    // =========================================================

    public List<AnalisisTasasCdatDetalleDTO> obtenerDetalle(
            LocalDate fechaCorte,
            Integer idAgencia,
            Integer plazoMeses,
            String amortizacion,
            String rangoSaldo
    ) {

        validarFechaCorte(fechaCorte);

        return repository.obtenerDetalle(
                fechaCorte,
                normalizarId(idAgencia),
                normalizarId(plazoMeses),
                normalizarTexto(amortizacion),
                normalizarTexto(rangoSaldo)
        );
    }


    // =========================================================
    // VALIDACIÓN
    // =========================================================

    private void validarFechaCorte(
            LocalDate fechaCorte
    ) {

        if (fechaCorte == null) {
            throw new IllegalArgumentException(
                    "La fecha de corte es obligatoria"
            );
        }
    }


    // =========================================================
    // NORMALIZACIÓN DE FILTROS
    // =========================================================

    private Integer normalizarId(
            Integer valor
    ) {

        if (valor == null || valor <= 0) {
            return null;
        }

        return valor;
    }


    private String normalizarTexto(
            String valor
    ) {

        if (valor == null) {
            return null;
        }

        String texto = valor.trim();

        if (texto.isEmpty()) {
            return null;
        }

        return texto;
    }
}