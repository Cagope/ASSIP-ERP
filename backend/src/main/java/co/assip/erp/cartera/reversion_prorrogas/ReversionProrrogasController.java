package co.assip.erp.cartera.reversion_prorrogas;

import co.assip.erp.cartera.pago_cuotas_v1.PagoCuotasV1Repository;
import co.assip.erp.cartera.pago_cuotas_v1.PagoCuotasV1Service;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Consultas y confirmación de reversión de Prórrogas V1. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/cartera/reversion-prorrogas")
public class ReversionProrrogasController {
    private static final String VIEW = "CARTERA_PAGO_CUOTAS_REVERSION_VIEW";
    private static final String EXECUTE = "CARTERA_PAGO_CUOTAS_REVERSAR";
    private final ReversionProrrogasService service;
    private final PagoCuotasV1Service pagoCuotasService;
    private final UsuarioSesionService sesion;
    private final AccessValidator accessValidator;
    private final JdbcTemplate jdbc;

    /** Reutiliza la búsqueda de créditos y aplica los permisos de consulta. */
    @GetMapping("/asociados/{idAsociado}/creditos")
    public List<PagoCuotasV1Repository.CreditoResumen> listarCreditos(
            @PathVariable Integer idAsociado) {
        permiso(VIEW);
        if (idAsociado == null || idAsociado <= 0) {
            throw new IllegalArgumentException("Asociado inválido.");
        }
        return pagoCuotasService.listarCreditosPorAsociado(idAsociado);
    }

    /** Lista prórrogas del crédito autorizado sin consultar datos de otras agencias. */
    @GetMapping("/creditos/{idCredito}/prorrogas")
    public List<ProrrogaResumen> listarProrrogas(@PathVariable Integer idCredito) {
        permiso(VIEW);
        validarCredito(idCredito);
        return jdbc.query("""
                SELECT id_credito_prorroga, numero_prorroga, estado_prorroga,
                       fecha_aplicacion, valor_total_pagado,
                       tipo_comprobante, numero_comprobante,
                       proxima_fecha_capital_anterior, proxima_fecha_capital_nueva,
                       proxima_fecha_interes_anterior, proxima_fecha_interes_nueva,
                       fecha_vencimiento_anterior, fecha_vencimiento_nueva
                FROM cartera.creditos_prorrogas
                WHERE id_cartera_credito = ?
                ORDER BY numero_prorroga DESC, id_credito_prorroga DESC
                """, (rs, row) -> new ProrrogaResumen(
                rs.getLong("id_credito_prorroga"),
                rs.getInt("numero_prorroga"),
                rs.getString("estado_prorroga"),
                rs.getObject("fecha_aplicacion", LocalDate.class),
                rs.getBigDecimal("valor_total_pagado"),
                rs.getString("tipo_comprobante"),
                rs.getString("numero_comprobante"),
                rs.getObject("proxima_fecha_capital_anterior", LocalDate.class),
                rs.getObject("proxima_fecha_capital_nueva", LocalDate.class),
                rs.getObject("proxima_fecha_interes_anterior", LocalDate.class),
                rs.getObject("proxima_fecha_interes_nueva", LocalDate.class),
                rs.getObject("fecha_vencimiento_anterior", LocalDate.class),
                rs.getObject("fecha_vencimiento_nueva", LocalDate.class)), idCredito);
    }


    /**
     * Consulta la vista previa financiera de una prórroga.
     * No modifica saldos, comprobantes ni movimientos.
     */
    @GetMapping("/creditos/{idCredito}/prorrogas/{idProrroga}/vista-previa")
    public ReversionProrrogasService.VistaPrevia consultarVistaPrevia(
            @PathVariable Integer idCredito,
            @PathVariable Long idProrroga) {
        permiso(VIEW);
        validarCredito(idCredito);
        return service.consultarVistaPrevia(idCredito, idProrroga);
    }


    /** Ejecuta la reversión atómica validada por el servicio financiero. */
    @PostMapping("/creditos/{idCredito}/prorrogas/{idProrroga}/confirmar")
    public ReversionProrrogasService.ResultadoReversion confirmar(
            @PathVariable Integer idCredito,
            @PathVariable Long idProrroga,
            @Valid @RequestBody ConfirmarRequest solicitud) {
        permiso(EXECUTE);
        validarCredito(idCredito);
        return service.confirmarReversion(idCredito, idProrroga,
                solicitud.fechaReversion(), solicitud.motivo().trim());
    }

    /** Comprueba la agencia autorizada antes de consultar o modificar una prórroga. */
    private void validarCredito(Integer idCredito) {
        if (idCredito == null || idCredito <= 0) {
            throw new IllegalArgumentException("Crédito inválido.");
        }
        var credito = pagoCuotasService.consultarCredito(idCredito);
        sesion.validarAgencia(credito.idAgencia());
    }

    /** Exige permisos funcionales de los roles vigentes del usuario. */
    private void permiso(String codigo) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(sesion.idUsuario());
        accessValidator.validarAcceso(usuario, codigo);
    }

    /** Datos visibles de una prórroga, sin exponer identificadores en formularios. */
    public record ProrrogaResumen(Long idProrroga, Integer numeroProrroga,
                                  String estado, LocalDate fechaAplicacion, BigDecimal valorTotal,
                                  String tipoComprobante, String numeroComprobante,
                                  LocalDate capitalAnterior, LocalDate capitalNueva,
                                  LocalDate interesAnterior, LocalDate interesNueva,
                                  LocalDate vencimientoAnterior, LocalDate vencimientoNueva) {}

    /** Fecha contable y justificación obligatoria de la reversión. */
    public record ConfirmarRequest(@NotNull LocalDate fechaReversion,
                                   @NotBlank String motivo) {}
}
