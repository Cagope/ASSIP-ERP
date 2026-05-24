package co.assip.erp.cdat.cierre_mensual_cdat;

import co.assip.erp.cdat.cierre_mensual_cdat.dto.CdatCierreMensualEntradaDTO;
import co.assip.erp.cdat.cierre_mensual_cdat.dto.CdatCierreMensualItemDTO;
import co.assip.erp.cdat.cierre_mensual_cdat.dto.CdatCierreMensualPreviewDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CdatCierreMensualService {

    private final CdatCierreMensualRepository repository;

    public CdatCierreMensualPreviewDTO preview(CdatCierreMensualEntradaDTO dto) {
        validar(dto);

        boolean existe = repository.existeCierre(
                dto.getIdAgencia(),
                dto.getFechaCorte()
        );

        List<CdatCierreMensualItemDTO> items = repository.obtenerCdatsActivos(
                dto.getIdAgencia(),
                dto.getFechaCorte()
        );

        BigDecimal totalValorApertura = sumarValorApertura(items);
        BigDecimal totalCapital = sumarCapital(items);

        return CdatCierreMensualPreviewDTO.builder()
                .idAgencia(dto.getIdAgencia())
                .fechaCorte(dto.getFechaCorte())
                .anio(dto.getFechaCorte().getYear())
                .mes(dto.getFechaCorte().getMonthValue())
                .totalCdats(items.size())
                .totalValorApertura(totalValorApertura)
                .totalCapital(totalCapital)
                .totalCapitalAportes(BigDecimal.ZERO)
                .totalCapitalAhorros(BigDecimal.ZERO)
                .existeCierre(existe)
                .estado(existe ? "YA_EXISTE" : "PREVIEW")
                .observacion(null)
                .items(items)
                .build();
    }

    @Transactional
    public CdatCierreMensualPreviewDTO aplicar(
            CdatCierreMensualEntradaDTO dto,
            Integer usuarioCreacion
    ) {
        validar(dto);

        if (repository.existeCierre(dto.getIdAgencia(), dto.getFechaCorte())) {
            throw new RuntimeException("Ya existe cierre mensual CDAT para la agencia y fecha de corte seleccionada.");
        }

        List<CdatCierreMensualItemDTO> items = repository.obtenerCdatsActivos(
                dto.getIdAgencia(),
                dto.getFechaCorte()
        );

        if (items.isEmpty()) {
            throw new RuntimeException("No hay CDAT activos para generar el cierre mensual.");
        }

        BigDecimal totalValorApertura = sumarValorApertura(items);
        BigDecimal totalCapital = sumarCapital(items);

        Long idCierre = repository.crearEncabezado(
                dto.getIdAgencia(),
                dto.getFechaCorte(),
                dto.getFechaCorte().getYear(),
                dto.getFechaCorte().getMonthValue(),
                items.size(),
                totalCapital,
                usuarioCreacion
        );

        for (CdatCierreMensualItemDTO item : items) {
            repository.insertarDetalle(idCierre, item);
        }

        return CdatCierreMensualPreviewDTO.builder()
                .idCierreMensualCdat(idCierre)
                .idAgencia(dto.getIdAgencia())
                .fechaCorte(dto.getFechaCorte())
                .anio(dto.getFechaCorte().getYear())
                .mes(dto.getFechaCorte().getMonthValue())
                .totalCdats(items.size())
                .totalValorApertura(totalValorApertura)
                .totalCapital(totalCapital)
                .totalCapitalAportes(BigDecimal.ZERO)
                .totalCapitalAhorros(BigDecimal.ZERO)
                .existeCierre(true)
                .estado("GENERADO")
                .observacion(null)
                .items(items)
                .build();
    }

    public List<CdatCierreMensualPreviewDTO> listar() {
        return repository.listar();
    }

    public CdatCierreMensualPreviewDTO obtenerPorId(Long idCierre) {
        return repository.obtenerPorId(idCierre)
                .orElseThrow(() -> new RuntimeException("No se encontró el cierre mensual CDAT."));
    }

    private BigDecimal sumarCapital(List<CdatCierreMensualItemDTO> items) {
        return items.stream()
                .map(CdatCierreMensualItemDTO::getSaldoActualCdat)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumarValorApertura(List<CdatCierreMensualItemDTO> items) {
        return items.stream()
                .map(CdatCierreMensualItemDTO::getValorAperturaCdat)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void validar(CdatCierreMensualEntradaDTO dto) {
        if (dto == null) {
            throw new RuntimeException("La solicitud no puede estar vacía.");
        }

        if (dto.getIdAgencia() == null) {
            throw new RuntimeException("Debe seleccionar la agencia.");
        }

        if (dto.getFechaCorte() == null) {
            throw new RuntimeException("Debe ingresar la fecha de corte.");
        }

        LocalDate ultimoDiaMes = dto.getFechaCorte()
                .withDayOfMonth(dto.getFechaCorte().lengthOfMonth());

        if (!dto.getFechaCorte().equals(ultimoDiaMes)) {
            throw new RuntimeException("La fecha de corte debe ser el último día del mes.");
        }
    }

    @Transactional
    public void eliminar(Long idCierre) {

        if (repository.tieneCausacionMensual(idCierre)) {
            throw new RuntimeException(
                    "No se puede eliminar el cierre mensual porque ya tiene causación mensual asociada."
            );
        }

        repository.eliminar(idCierre);
    }
}