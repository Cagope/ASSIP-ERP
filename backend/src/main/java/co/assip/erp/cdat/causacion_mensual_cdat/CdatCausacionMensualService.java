package co.assip.erp.cdat.causacion_mensual_cdat;

import co.assip.erp.cdat.causacion_mensual_cdat.dto.CdatCausacionMensualEntradaDTO;
import co.assip.erp.cdat.causacion_mensual_cdat.dto.CdatCausacionMensualItemDTO;
import co.assip.erp.cdat.causacion_mensual_cdat.dto.CdatCausacionMensualPreviewDTO;
import co.assip.erp.contabilidad.consecutivos_comprobantes.ConsecutivosComprobantesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CdatCausacionMensualService {

    private final CdatCausacionMensualRepository repository;
    private final ConsecutivosComprobantesService consecutivosComprobantesService;

    public CdatCausacionMensualPreviewDTO preview(CdatCausacionMensualEntradaDTO dto) {
        validar(dto, false);

        Long idCierre = repository.buscarCierre(dto.getIdAgencia(), dto.getFechaCorte())
                .orElseThrow(() -> new RuntimeException(
                        "No existe cierre mensual CDAT para la agencia y fecha seleccionada."
                ));

        boolean existe = repository.existeCausacion(
                dto.getIdAgencia(),
                dto.getFechaCorte()
        );

        List<CdatCausacionMensualItemDTO> items = calcularItems(
                repository.obtenerDetalleCierre(idCierre),
                dto.getFechaCorte()
        );

        return construirPreview(
                dto,
                idCierre,
                null,
                existe ? "YA_EXISTE" : "PREVIEW",
                existe,
                items
        );
    }

    @Transactional
    public CdatCausacionMensualPreviewDTO aplicar(
            CdatCausacionMensualEntradaDTO dto,
            Integer usuarioCreacion
    ) {
        validar(dto, true);

        Long idCierre = repository.buscarCierre(dto.getIdAgencia(), dto.getFechaCorte())
                .orElseThrow(() -> new RuntimeException(
                        "No existe cierre mensual CDAT para la agencia y fecha seleccionada."
                ));

        if (repository.existeCausacion(dto.getIdAgencia(), dto.getFechaCorte())) {
            throw new RuntimeException(
                    "Ya existe causación mensual CDAT para la agencia y fecha de corte seleccionada."
            );
        }

        String numeroComprobante =
                consecutivosComprobantesService.generarNumeroDefinitivo(
                        dto.getTipoComprobante(),
                        dto.getIdAgencia(),
                        usuarioCreacion
                );

        dto.setNumeroComprobante(numeroComprobante);

        List<CdatCausacionMensualItemDTO> items = calcularItems(
                repository.obtenerDetalleCierre(idCierre),
                dto.getFechaCorte()
        );

        if (items.isEmpty()) {
            throw new RuntimeException("No hay CDAT para causar intereses.");
        }

        CdatCausacionMensualPreviewDTO preview = construirPreview(
                dto,
                idCierre,
                null,
                "GENERADO",
                true,
                items
        );

        Long idCausacion = repository.crearEncabezado(
                idCierre,
                dto.getIdAgencia(),
                dto.getFechaCorte(),
                dto.getFechaContable(),
                dto.getFechaCorte().getYear(),
                dto.getFechaCorte().getMonthValue(),
                dto.getTipoComprobante(),
                dto.getNumeroComprobante(),
                preview.getTotalCdats(),
                preview.getTotalCapital(),
                preview.getTotalInteres(),
                preview.getTotalRetencion(),
                preview.getTotalNeto(),
                usuarioCreacion
        );

        for (CdatCausacionMensualItemDTO item : items) {
            repository.insertarDetalle(idCausacion, item);
        }

        return construirPreview(
                dto,
                idCierre,
                idCausacion,
                "GENERADO",
                true,
                items
        );
    }

    public List<CdatCausacionMensualPreviewDTO> listar() {
        return repository.listar();
    }

    public CdatCausacionMensualPreviewDTO obtenerPorId(Long idCausacion) {
        return repository.obtenerPorId(idCausacion)
                .orElseThrow(() -> new RuntimeException(
                        "No se encontró la causación mensual CDAT."
                ));
    }

    private List<CdatCausacionMensualItemDTO> calcularItems(
            List<CdatCausacionMensualItemDTO> base,
            LocalDate fechaCorte
    ) {
        return base.stream()
                .map(item -> calcularItem(item, fechaCorte))
                .toList();
    }

    private CdatCausacionMensualItemDTO calcularItem(
            CdatCausacionMensualItemDTO item,
            LocalDate fechaCorte
    ) {
        LocalDate fechaInicio = item.getFechaUltimaLiquidacion();

        if (fechaInicio == null || fechaInicio.isBefore(item.getFechaAperturaCdat())) {
            fechaInicio = item.getFechaAperturaCdat();
        }

        long diasLong = ChronoUnit.DAYS.between(fechaInicio, fechaCorte);

        if (diasLong < 0) {
            diasLong = 0;
        }

        if (diasLong > 30) {
            diasLong = 30;
        }

        int dias = (int) diasLong;

        BigDecimal saldo = nvl(item.getSaldoBase());
        BigDecimal tasa = nvl(item.getTasaNominalAnual());

        BigDecimal interes = saldo
                .multiply(BigDecimal.valueOf(dias))
                .multiply(tasa)
                .divide(BigDecimal.valueOf(36000), 0, RoundingMode.HALF_UP);

        BigDecimal retencion = BigDecimal.ZERO;

        if (Boolean.TRUE.equals(item.getAplicaRetencion())
                && interes.compareTo(BigDecimal.ZERO) > 0) {

            retencion = interes
                    .multiply(BigDecimal.valueOf(0.07))
                    .setScale(0, RoundingMode.HALF_UP);
        }

        BigDecimal neto = interes.subtract(retencion);

        return CdatCausacionMensualItemDTO.builder()
                .idCierreMensualCdatDetalle(item.getIdCierreMensualCdatDetalle())
                .idCuentaCdat(item.getIdCuentaCdat())
                .codigoCdat(item.getCodigoCdat())
                .idDatosPersonal(item.getIdDatosPersonal())
                .documento(item.getDocumento())
                .nombreCompleto(item.getNombreCompleto())
                .idDatosPersonalCotitular(item.getIdDatosPersonalCotitular())
                .documentoCotitular(item.getDocumentoCotitular())
                .nombreCotitular(item.getNombreCotitular())
                .fechaAperturaCdat(item.getFechaAperturaCdat())
                .fechaVencimientoCdat(item.getFechaVencimientoCdat())
                .fechaUltimaLiquidacion(item.getFechaUltimaLiquidacion())
                .saldoBase(saldo)
                .tasaNominalAnual(tasa)
                .diasCausados(dias)
                .valorInteres(interes)
                .valorRetencion(retencion)
                .valorNeto(neto)
                .aplicaRetencion(item.getAplicaRetencion())
                .modalidadCdat(item.getModalidadCdat())
                .amortizacionDeposito(item.getAmortizacionDeposito())
                .idCuentaAportes(item.getIdCuentaAportes())
                .codigoCuentaAportes(item.getCodigoCuentaAportes())
                .idCuentaAhorro(item.getIdCuentaAhorro())
                .codigoCuentaAhorro(item.getCodigoCuentaAhorro())
                .build();
    }

    private CdatCausacionMensualPreviewDTO construirPreview(
            CdatCausacionMensualEntradaDTO dto,
            Long idCierre,
            Long idCausacion,
            String estado,
            boolean existe,
            List<CdatCausacionMensualItemDTO> items
    ) {
        BigDecimal totalCapital = sumar(items, "capital");
        BigDecimal totalInteres = sumar(items, "interes");
        BigDecimal totalRetencion = sumar(items, "retencion");
        BigDecimal totalNeto = sumar(items, "neto");

        return CdatCausacionMensualPreviewDTO.builder()
                .idCausacionMensualInteres(idCausacion)
                .idCierreMensualCdat(idCierre)
                .idAgencia(dto.getIdAgencia())
                .fechaCorte(dto.getFechaCorte())
                .fechaContable(dto.getFechaContable())
                .anio(dto.getFechaCorte().getYear())
                .mes(dto.getFechaCorte().getMonthValue())
                .tipoComprobante(dto.getTipoComprobante())
                .numeroComprobante(dto.getNumeroComprobante())
                .totalCdats(items.size())
                .totalCapital(totalCapital)
                .totalInteres(totalInteres)
                .totalRetencion(totalRetencion)
                .totalNeto(totalNeto)
                .existeCausacion(existe)
                .estado(estado)
                .items(items)
                .build();
    }

    private BigDecimal sumar(List<CdatCausacionMensualItemDTO> items, String campo) {
        return items.stream()
                .map(item -> switch (campo) {
                    case "capital" -> item.getSaldoBase();
                    case "interes" -> item.getValorInteres();
                    case "retencion" -> item.getValorRetencion();
                    case "neto" -> item.getValorNeto();
                    default -> BigDecimal.ZERO;
                })
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void validar(CdatCausacionMensualEntradaDTO dto, boolean aplicar) {
        if (dto == null) {
            throw new RuntimeException("La solicitud no puede estar vacía.");
        }

        if (dto.getIdAgencia() == null) {
            throw new RuntimeException("Debe seleccionar la agencia.");
        }

        if (dto.getFechaCorte() == null) {
            throw new RuntimeException("Debe ingresar la fecha de corte.");
        }

        if (dto.getFechaContable() == null) {
            throw new RuntimeException("Debe ingresar la fecha contable.");
        }

        if (aplicar) {
            if (dto.getTipoComprobante() == null || dto.getTipoComprobante().isBlank()) {
                throw new RuntimeException("Debe seleccionar el tipo de comprobante.");
            }
        }
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}