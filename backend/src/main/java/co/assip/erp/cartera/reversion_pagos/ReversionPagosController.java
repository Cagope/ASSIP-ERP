
package co.assip.erp.cartera.reversion_pagos;

import co.assip.erp.cartera.pago_cuotas_v1.PagoCuotasV1Repository;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;
import java.util.List;

/** Controla las consultas y confirmaciones de reversión de pagos de Cartera. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/cartera/reversion-pagos")
public class ReversionPagosController {
    private static final String PERMISO_CONSULTAR = "CARTERA_PAGO_CUOTAS_REVERSION_VIEW";
    private static final String PERMISO_REVERSAR = "CARTERA_PAGO_CUOTAS_REVERSAR";
    private final ReversionPagosService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    /** Lista los créditos del asociado previa validación de acceso. */
    @GetMapping("/asociados/{idAsociado}/creditos")
    public List<PagoCuotasV1Repository.CreditoResumen> listarCreditos(@PathVariable Integer idAsociado) {
        validarAutorizacion(PERMISO_CONSULTAR);
        return service.listarCreditos(idAsociado);
    }

    /** Consulta la elegibilidad del pago seleccionado. */
    @GetMapping("/pagos/{idPago}/vista-previa")
    public ReversionPagosService.VistaPrevia vistaPrevia(@PathVariable Long idPago) {
        validarAutorizacion(PERMISO_CONSULTAR);
        return service.consultarVistaPrevia(idPago);
    }

    /** Consulta los movimientos del comprobante contable contrario. */
    @GetMapping("/pagos/{idPago}/comprobante-contrario")
    public ReversionPagosService.ComprobanteContrario comprobanteContrario(@PathVariable Long idPago) {
        validarAutorizacion(PERMISO_CONSULTAR);
        return service.consultarComprobanteContrario(idPago);
    }

    /** Consulta las condiciones de restitución del estado del crédito. */
    @GetMapping("/pagos/{idPago}/estado-restitucion")
    public ReversionPagosRepository.EstadoRestitucion estadoRestitucion(@PathVariable Long idPago) {
        validarAutorizacion(PERMISO_CONSULTAR);
        return service.consultarEstadoRestitucion(idPago);
    }

    /** Lista los pagos originales del crédito seleccionado. */
    @GetMapping("/creditos/{idCredito}/pagos")
    public List<ReversionPagosRepository.PagoOriginal> listarPagos(@PathVariable Integer idCredito) {
        validarAutorizacion(PERMISO_CONSULTAR);
        return service.listarPagos(idCredito);
    }

    /** Confirma la reversión integral del pago con autorización de ejecución. */
    @PostMapping("/pagos/{idPago}/confirmar")
    public ReversionPagosService.ResultadoReversion confirmar(
            @PathVariable Long idPago,
            @Valid @RequestBody ConfirmarReversionRequest request) {
        validarAutorizacion(PERMISO_REVERSAR);
        return service.confirmarReversion(
                idPago,
                request.fechaReversion(),
                request.motivo().trim()
        );
    }

    /** Exige el permiso funcional de cualquiera de los roles vigentes del usuario. */
    private void validarAutorizacion(String codigoPermiso) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(usuarioSesionService.idUsuario());
        accessValidator.validarAcceso(usuario, codigoPermiso);
    }

    /** Recibe la fecha y el motivo obligatorios de la reversión. */
    public record ConfirmarReversionRequest(
            @NotNull LocalDate fechaReversion,
            @NotBlank String motivo
    ) {}
}
