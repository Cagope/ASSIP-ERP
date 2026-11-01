package co.assip.erp.cartera.prorrogas_v1;

import co.assip.erp.cartera.prorrogas_v1.dto.ProrrogasV1RequestDTO;
import co.assip.erp.cartera.pago_cuotas_v1.PagoCuotasV1Service;
import co.assip.erp.cartera.pago_cuotas_v1.motor.PagoCuotasMotorRepository;
import co.assip.erp.cartera.prorrogas_v1.dto.ProrrogasV1ResponseDTO;
import co.assip.erp.contabilidad.auxiliares_contables.dto.MovimientoContableDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import java.time.LocalDate;
import java.math.BigDecimal;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Expone la liquidación, previsualización y ejecución exclusiva de Prórrogas V1. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/cartera/prorrogas-v1")
public class ProrrogasV1Controller {
    private static final String VIEW = "CARTERA_PAGO_CUOTAS_VIEW";
    private static final String EXECUTE = "CARTERA_PAGO_CUOTAS_EXECUTE";
    private final ProrrogasV1LiquidacionService liquidacionService;
    private final ProrrogasV1Service service;
    private final UsuarioSesionService sesion;
    private final AccessValidator accessValidator;
    private final JdbcTemplate jdbc;
    private final PagoCuotasV1Service pagoCuotasV1Service;
    private final PagoCuotasMotorRepository pagoCuotasMotorRepository;

    /** Liquida los conceptos sin modificar el crédito ni registrar pagos. */
    @GetMapping("/creditos/{idCredito}/liquidacion")
    public ProrrogasV1LiquidacionService.LiquidacionProrroga liquidar(
            @PathVariable Integer idCredito,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaLiquidacion) {
        permiso(VIEW);
        return liquidacionService.liquidar(idCredito, fechaLiquidacion);
    }

    /** Resuelve la cuenta CxC activa de la agencia del crédito, sin pedir IDs al funcionario. */
    @GetMapping("/creditos/{idCredito}/cuenta-cxc")
    public CuentaCxcDTO consultarCuentaCxc(@PathVariable Integer idCredito) {
        permiso(VIEW);
        var credito = pagoCuotasV1Service.consultarCredito(idCredito);
        sesion.validarAgencia(credito.idAgencia());
        var cuenta = pagoCuotasMotorRepository.resolverParametroContable(
                credito.idAgencia(), "RECAUDO_BANCARIO");
        return new CuentaCxcDTO(cuenta.idCatalogoCuenta().longValue(), cuenta.codigoCuenta());
    }

    /** Datos internos para construir el medio CXP; no son un campo editable. */
    public record CuentaCxcDTO(Long idCuentaPorPagar, String codigoCuenta) {}

    /** Valida el recaudo y muestra el CC antes de la confirmación. */
    @PostMapping("/comprobante/preview")
    public List<MovimientoVistaDTO> previsualizar(
            @RequestBody ProrrogasV1RequestDTO solicitud,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaContable) {
        permiso(EXECUTE);
        validarCxcSolicitud(solicitud);
        List<MovimientoContableDTO> movimientos =
                service.previsualizarComprobante(solicitud, fechaContable);
        return movimientos.stream().map(m -> {
            CuentaVista cuenta = jdbc.queryForObject(
                    "SELECT codigo_cuenta, nombre_cuenta FROM contabilidad.catalogo_cuentas "
                            + "WHERE id_catalogo_cuenta = ? AND id_agencia = ?",
                    (rs, row) -> new CuentaVista(
                            rs.getString("codigo_cuenta"),
                            rs.getString("nombre_cuenta")),
                    m.getIdCatalogoCuenta(), m.getIdAgencia());
            if (cuenta == null) {
                throw new IllegalStateException("Cuenta contable no encontrada para el comprobante.");
            }
            return new MovimientoVistaDTO(
                    cuenta.codigo(), cuenta.nombre(),
                    m.getDetalleMovimiento(), m.getValorDebito(), m.getValorCredito());
        }).toList();
    }

    /** Datos de presentación: no se exponen identificadores internos al funcionario. */
    public record MovimientoVistaDTO(
            String codigoCuenta,
            String nombreCuenta,
            String detalleMovimiento,
            BigDecimal valorDebito,
            BigDecimal valorCredito) {}

    private record CuentaVista(String codigo, String nombre) {}

    /** Aplica la prórroga y el recaudo en la transacción definida por el servicio. */
    @PostMapping("/confirmar")
    public ProrrogasV1ResponseDTO confirmar(
            @RequestBody ProrrogasV1RequestDTO solicitud,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaContable) {
        permiso(EXECUTE);
        validarCxcSolicitud(solicitud);
        return service.ejecutarProrroga(solicitud, fechaContable);
    }

    /** Impide sustituir la cuenta CxC parametrizada por un identificador arbitrario. */
    private void validarCxcSolicitud(ProrrogasV1RequestDTO solicitud) {
        var credito = pagoCuotasV1Service.consultarCredito(solicitud.idCarteraCredito());
        sesion.validarAgencia(credito.idAgencia());
        if (solicitud.mediosRecaudo() == null) return;
        boolean contieneCxc = solicitud.mediosRecaudo().stream().anyMatch(m ->
                m != null && m.tipoMedio() ==
                        co.assip.erp.cartera.pago_cuotas_v1.motor.dto.PagoCuotasMedioRecaudoDTO.TipoMedioRecaudo.CXP);
        if (!contieneCxc) return;
        long cuentaEsperada = pagoCuotasMotorRepository.resolverParametroContable(
                credito.idAgencia(), "RECAUDO_BANCARIO").idCatalogoCuenta().longValue();
        for (var medio : solicitud.mediosRecaudo()) {
            if (medio != null && medio.tipoMedio() ==
                    co.assip.erp.cartera.pago_cuotas_v1.motor.dto.PagoCuotasMedioRecaudoDTO.TipoMedioRecaudo.CXP
                    && !java.util.Objects.equals(medio.idCuentaPorPagar(), cuentaEsperada)) {
                throw new IllegalArgumentException("La cuenta CxC no corresponde a la parametrización activa de la agencia.");
            }
        }
    }

    /** Valida permisos funcionales del usuario autenticado, no nombres de roles. */
    private void permiso(String codigo) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(sesion.idUsuario());
        accessValidator.validarAcceso(usuario, codigo);
    }
}
