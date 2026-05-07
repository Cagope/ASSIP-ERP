package co.assip.erp.cdat.cdats;

import co.assip.erp.cdat.cdats.contabilizacion.CdatAperturaContabilizacionService;
import co.assip.erp.depositos.movimientos.DepositosMovimientoService;
import co.assip.erp.depositos.movimientos.dto.DepositosMovimientoDTO;
import co.assip.erp.cdat.cdats.dto.CdatFormDTO;
import co.assip.erp.cdat.cdats.dto.CdatListDTO;
import co.assip.erp.cdat.cdats.dto.CdatSaveDTO;
import co.assip.erp.cdat.cdats.dto.CdatSaveResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import co.assip.erp.cdat.cdats.dto.CdatAsociadoValidacionDTO;


@Service
@RequiredArgsConstructor
public class CdatService {

    private final CdatRepository repository;
    private final CdatAperturaContabilizacionService contabilizacionService;
    private final DepositosMovimientoService depositosMovimientoService;

    public List<CdatListDTO> listar() {
        return repository.listar();
    }

    public CdatFormDTO obtenerPorId(Long idCuentaCdat) {
        return repository.obtenerPorId(idCuentaCdat)
                .orElseThrow(() -> new RuntimeException("No se encontró el CDAT."));
    }

    @Transactional
    public CdatSaveResponseDTO guardar(CdatSaveDTO dto, Integer idUsuario, Integer idAgencia) {

        validar(dto);

        if (dto.getIdCuentaCdat() != null) {
            throw new RuntimeException("El CDAT no puede editarse desde este proceso. Debe usarse anulación o reverso.");
        }

        dto.setIdAgencia(idAgencia);

        if (dto.getIdProductoCdat() == null) {
            dto.setIdProductoCdat(1);
        }

        String codigoCdat = generarCodigoCdat(idAgencia, idUsuario);

        dto.setCodigoCdat(codigoCdat);

        LocalDate fechaVencimiento = calcularFechaVencimiento(dto);
        LocalDate fechaProximaLiquidacion = calcularProximaLiquidacion(dto);
        LocalDate fechaProximoTraslado = calcularProximoTraslado(dto);

        dto.setTasaNominalMensual(calcularTasaNominalMensual(dto));
        dto.setTasaEfectivaMensual(calcularTasaEfectivaMensual(dto));

        Long idCuentaCdat = repository.crear(
                dto,
                fechaVencimiento,
                fechaProximaLiquidacion,
                fechaProximoTraslado,
                idUsuario
        );

        repository.guardarBeneficiarios(
                idCuentaCdat,
                dto.getBeneficiarios(),
                idUsuario
        );

        registrarMovimientosApertura(idCuentaCdat, dto, idUsuario);

        contabilizacionService.contabilizarApertura(
                idCuentaCdat,
                dto,
                idUsuario
        );

        return CdatSaveResponseDTO.builder()
                .idCuentaCdat(idCuentaCdat)
                .codigoCdat(codigoCdat)
                .tipoComprobante(dto.getTipoComprobante())
                .numeroComprobante(dto.getNumeroComprobante())
                .mensaje("CDAT creado correctamente.")
                .build();
    }

    private void registrarMovimientosApertura(Long idCuentaCdat, CdatSaveDTO dto, Integer idUsuario) {

        repository.crearExtracto(
                idCuentaCdat,
                dto,
                "001",
                BigDecimal.ZERO,
                dto.getValorAperturaCdat(),
                idUsuario
        );

        if (dto.getMediosPago() != null
                && dto.getMediosPago().getDepositos() != null
                && !dto.getMediosPago().getDepositos().isEmpty()) {

            dto.getMediosPago().getDepositos().forEach(d -> {

                if (mayorCero(d.getValorDebitar())) {
                    DepositosMovimientoDTO movDeposito = new DepositosMovimientoDTO();

                    movDeposito.setIdCuentaAhorro(d.getIdCuentaAhorro());
                    movDeposito.setFechaMovimiento(dto.getFechaAperturaCdat());
                    movDeposito.setTipoComprobante(dto.getTipoComprobante());
                    movDeposito.setNumeroComprobante(dto.getNumeroComprobante());
                    movDeposito.setTipoMovimiento("773");
                    movDeposito.setModulo("04");
                    movDeposito.setTarjeta("N");
                    movDeposito.setEstablecimiento("APERTURA CDAT");
                    movDeposito.setValorDebito(d.getValorDebitar());
                    movDeposito.setValorCredito(BigDecimal.ZERO);

                    movDeposito.setModuloOrigen("CDAT");
                    movDeposito.setProcesoOrigen("APERTURA_CDAT");
                    movDeposito.setIdOrigen(idCuentaCdat);

                    depositosMovimientoService.registrarDebito(movDeposito, idUsuario);
                }
            });
        }
    }

    private void validar(CdatSaveDTO dto) {

        if (dto.getIdDatosPersonal() == null) {
            throw new RuntimeException("El titular es obligatorio.");
        }

        if (dto.getIdCuentaAportes() == null) {
            throw new RuntimeException("La cuenta de aportes es obligatoria.");
        }

        if (dto.getIdCuentaAhorro() == null) {
            throw new RuntimeException("La cuenta de ahorro es obligatoria.");
        }

        if (dto.getFechaAperturaCdat() == null) {
            throw new RuntimeException("La fecha de apertura es obligatoria.");
        }

        if (dto.getPlazoMeses() == null || dto.getPlazoMeses() <= 0) {
            throw new RuntimeException("El plazo en meses debe ser mayor a 0.");
        }

        if (!mayorCero(dto.getValorAperturaCdat())) {
            throw new RuntimeException("El valor del CDAT debe ser mayor a 0.");
        }

        if (dto.getTipoComprobante() == null || dto.getTipoComprobante().isBlank()) {
            throw new RuntimeException("El tipo de comprobante es obligatorio.");
        }

        if (dto.getFechaComprobante() == null) {
            throw new RuntimeException("La fecha contable es obligatoria.");
        }

        if (dto.getAmortizacionDeposito() == null || dto.getAmortizacionDeposito().isBlank()) {
            throw new RuntimeException("La amortización del depósito es obligatoria.");
        }

        BigDecimal suma = nvl(dto.getValorEfectivo())
                .add(nvl(dto.getValorCheque()))
                .add(nvl(dto.getValorDepositos()))
                .add(nvl(dto.getValorBanco()))
                .add(nvl(dto.getValorTrasladosAgencias()));

        if (suma.compareTo(nvl(dto.getValorAperturaCdat())) != 0) {
            throw new RuntimeException("La suma de los valores no coincide con el valor del CDAT.");
        }

        if ("R".equals(dto.getOrigenCdat()) && dto.getIdCuentaCdatOrigen() == null) {
            throw new RuntimeException("Debe indicar el CDAT origen para renovación.");
        }
    }

    private String generarCodigoCdat(Integer idAgencia, Integer idUsuario) {
        Integer consecutivo = repository.obtenerSiguienteConsecutivo(idAgencia, idUsuario);
        return String.format("%06d", consecutivo);
    }

    private LocalDate calcularFechaVencimiento(CdatSaveDTO dto) {
        return dto.getFechaAperturaCdat().plusMonths(dto.getPlazoMeses());
    }

    private LocalDate calcularProximaLiquidacion(CdatSaveDTO dto) {
        return dto.getFechaAperturaCdat().plusMonths(1);
    }

    private BigDecimal calcularTasaNominalMensual(CdatSaveDTO dto) {
        if (dto.getTasaNominalAnual() == null) {
            return BigDecimal.ZERO;
        }

        return dto.getTasaNominalAnual()
                .divide(BigDecimal.valueOf(12), 6, RoundingMode.HALF_UP);
    }

    private boolean mayorCero(BigDecimal value) {
        return nvl(value).compareTo(BigDecimal.ZERO) > 0;
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    public CdatAsociadoValidacionDTO validarAsociado(String documento) {

        return repository.validarAsociado(documento)
                .orElseGet(() -> CdatAsociadoValidacionDTO.builder()
                        .idDatosPersonal(null)
                        .documento(documento)
                        .nombreCompleto(null)
                        .tieneAportesActivos(false)
                        .datosActualizados(false)
                        .mensajeError("No se encontró el asociado.")
                        .build());
    }

    public String obtenerProximoCodigo(Integer idAgencia) {
        Integer consecutivo = repository.obtenerProximoConsecutivo(idAgencia);
        return String.format("%06d", consecutivo);
    }

    public CdatAsociadoValidacionDTO buscarCotitular(String documento) {
        return repository.buscarCotitular(documento)
                .orElseThrow(() -> new RuntimeException("No se encontró el cotitular."));
    }

    private LocalDate calcularProximoTraslado(CdatSaveDTO dto) {

        if ("V".equals(dto.getAmortizacionDeposito())) {
            return calcularFechaVencimiento(dto);
        }

        Integer meses = repository.obtenerMesesAmortizacion(dto.getAmortizacionDeposito());

        if (meses == null || meses <= 0) {
            throw new RuntimeException("No se encontró la amortización del CDAT.");
        }

        return dto.getFechaAperturaCdat().plusMonths(meses);
    }

    private BigDecimal calcularTasaEfectivaMensual(CdatSaveDTO dto) {

        if (dto.getTasaEfectivaAnual() == null ||
                dto.getTasaEfectivaAnual().compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal tasaAnualDecimal = dto.getTasaEfectivaAnual()
                .divide(BigDecimal.valueOf(100), 12, RoundingMode.HALF_UP);

        double tasaMensual = Math.pow(
                BigDecimal.ONE.add(tasaAnualDecimal).doubleValue(),
                1.0 / 12.0
        ) - 1.0;

        return BigDecimal.valueOf(tasaMensual)
                .multiply(BigDecimal.valueOf(100))
                .setScale(6, RoundingMode.HALF_UP);
    }
}