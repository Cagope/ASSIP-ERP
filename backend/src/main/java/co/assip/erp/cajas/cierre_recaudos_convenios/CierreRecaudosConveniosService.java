package co.assip.erp.cajas.cierre_recaudos_convenios;

import co.assip.erp.cajas.cierre_recaudos_convenios.dto.CierreRecaudosConveniosItemDTO;
import co.assip.erp.cajas.cierre_recaudos_convenios.dto.CierreRecaudosConveniosPreviewDTO;
import co.assip.erp.cajas.cierre_recaudos_convenios.dto.CierreRecaudosConveniosRequestDTO;
import co.assip.erp.cajas.cierre_recaudos_convenios.dto.CierreRecaudosConveniosResponseDTO;
import co.assip.erp.cajas.recaudos_convenios.dto.RecaudoConvenioConvenioDTO;
import co.assip.erp.seguridad.service.UsuarioSesionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CierreRecaudosConveniosService {

    private static final String TIPO_COMPROBANTE = "CJ";
    private static final String TIPO_MOVIMIENTO_CREDITO = "001";

    private static final String MODULO_ORIGEN = "CAJAS";
    private static final String PROCESO_ORIGEN = "CIERRE_RECAUDOS_CONVENIOS";
    private static final String TABLA_ORIGEN = "cajas.recaudos_convenios";

    private static final String CODIGO_OPERACION_CAJA = "CIERRE_RECAUDOS";

    private final CierreRecaudosConveniosRepository repository;
    private final UsuarioSesionService usuarioSesionService;

    public CierreRecaudosConveniosPreviewDTO preview(
            CierreRecaudosConveniosRequestDTO request
    ) {

        CierreRecaudosConveniosPreviewDTO preview =
                CierreRecaudosConveniosPreviewDTO.builder()
                        .permiteAplicar(false)
                        .valorTotal(BigDecimal.ZERO)
                        .cantidadRecaudos(0)
                        .build();

        validarBasico(request, preview);

        if (!preview.getErrores().isEmpty()) {
            preview.setMensaje("La información del cierre tiene errores.");
            return preview;
        }

        Long idProvision =
                repository.obtenerProvisionAbierta(
                        request.getIdCaja(),
                        request.getFechaContable()
                );

        if (idProvision == null) {
            preview.getErrores().add(
                    "No existe provisión abierta para la caja y fecha digitada."
            );
            preview.setMensaje("No se puede aplicar el cierre.");
            return preview;
        }

        RecaudoConvenioConvenioDTO convenio =
                repository.obtenerConvenioActivo(
                        request.getIdConvenio()
                );

        if (convenio == null) {
            preview.getErrores().add("El convenio no existe o no está activo.");
            preview.setMensaje("No se puede aplicar el cierre.");
            return preview;
        }

        String documento =
                normalizarDocumentoSoporte(
                        request.getDocumentoSoporte()
                );

        List<CierreRecaudosConveniosItemDTO> items =
                repository.listarRecaudosPendientes(
                        idProvision,
                        request.getIdConvenio()
                );

        BigDecimal total =
                items.stream()
                        .map(i -> nvl(i.getValorRecaudo()))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        preview.setIdProvision(idProvision);
        preview.setIdCaja(request.getIdCaja());
        preview.setIdAgencia(convenio.getIdAgencia());
        preview.setFechaContable(request.getFechaContable());

        preview.setIdConvenio(convenio.getIdConvenio());
        preview.setCodigoConvenio(convenio.getCodigoConvenio());
        preview.setNombreConvenio(convenio.getNombreConvenio());

        preview.setIdCuentaAhorro(convenio.getIdCuentaAhorro());
        preview.setCodigoCuenta(convenio.getCodigoCuenta());
        preview.setDocumentoTitular(convenio.getDocumentoTitular());
        preview.setNombreTitular(convenio.getNombreTitular());

        preview.setDocumentoSoporte(documento);
        preview.setItems(items);
        preview.setCantidadRecaudos(items.size());
        preview.setValorTotal(total);

        if (items.isEmpty()) {
            preview.getErrores().add(
                    "No existen recaudos pendientes para este convenio."
            );
        }

        if (!preview.getErrores().isEmpty()) {
            preview.setMensaje("No se puede aplicar el cierre.");
            preview.setPermiteAplicar(false);
            return preview;
        }

        preview.setPermiteAplicar(true);
        preview.setMensaje("Cierre listo para aplicar.");

        return preview;
    }

    @Transactional
    public CierreRecaudosConveniosResponseDTO aplicar(
            CierreRecaudosConveniosRequestDTO request
    ) {

        CierreRecaudosConveniosPreviewDTO preview =
                preview(request);

        if (!preview.getPermiteAplicar()) {
            throw new RuntimeException(
                    preview.getErrores().isEmpty()
                            ? "No se puede aplicar el cierre."
                            : String.join(" ", preview.getErrores())
            );
        }

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        Long idDepartamento =
                repository.obtenerDepartamentoDepositos();

        if (idDepartamento == null) {
            throw new RuntimeException(
                    "No existe departamento operativo para depósitos."
            );
        }

        Long idOperacion =
                repository.obtenerOperacionCaja(
                        CODIGO_OPERACION_CAJA
                );

        if (idOperacion == null) {
            throw new RuntimeException(
                    "No existe operación de caja para el cierre de recaudos."
            );
        }

        String concepto =
                "Cierre recaudos convenio - "
                        + preview.getCodigoConvenio()
                        + " - "
                        + preview.getNombreConvenio()
                        + " - Cuenta "
                        + preview.getCodigoCuenta();

        Long idMovimientoCaja =
                repository.insertarMovimientoCaja(
                        preview.getIdProvision(),
                        preview.getIdCaja(),
                        idDepartamento,
                        idOperacion,
                        MODULO_ORIGEN,
                        PROCESO_ORIGEN,
                        TABLA_ORIGEN,
                        preview.getIdConvenio(),
                        TIPO_COMPROBANTE,
                        preview.getDocumentoSoporte(),
                        preview.getCodigoCuenta(),
                        "INGRESO",
                        "EFECTIVO",
                        preview.getValorTotal(),
                        concepto,
                        idUsuario
                );

        repository.actualizarSaldoCuenta(
                preview.getIdCuentaAhorro(),
                preview.getValorTotal(),
                idUsuario
        );

        repository.insertarExtractoCuentaAhorro(
                preview.getIdCuentaAhorro(),
                preview.getFechaContable(),
                TIPO_COMPROBANTE,
                preview.getDocumentoSoporte(),
                TIPO_MOVIMIENTO_CREDITO,
                preview.getValorTotal(),
                concepto,
                idUsuario
        );

        repository.marcarRecaudosProcesados(
                preview.getIdProvision(),
                preview.getIdConvenio(),
                idUsuario
        );

        return CierreRecaudosConveniosResponseDTO.builder()
                .idProvision(preview.getIdProvision())
                .idCaja(preview.getIdCaja())
                .idAgencia(preview.getIdAgencia())
                .fechaContable(preview.getFechaContable())
                .idConvenio(preview.getIdConvenio())
                .codigoConvenio(preview.getCodigoConvenio())
                .nombreConvenio(preview.getNombreConvenio())
                .idCuentaAhorro(preview.getIdCuentaAhorro())
                .codigoCuenta(preview.getCodigoCuenta())
                .documentoSoporte(preview.getDocumentoSoporte())
                .cantidadRecaudos(preview.getCantidadRecaudos())
                .valorTotal(preview.getValorTotal())
                .idMovimientoCaja(idMovimientoCaja)
                .mensaje("Cierre de recaudos aplicado correctamente.")
                .build();
    }

    private void validarBasico(
            CierreRecaudosConveniosRequestDTO request,
            CierreRecaudosConveniosPreviewDTO preview
    ) {

        if (request == null) {
            preview.getErrores().add("No se recibió información del cierre.");
            return;
        }

        if (request.getIdCaja() == null) {
            preview.getErrores().add("La caja es obligatoria.");
        }

        if (request.getFechaContable() == null) {
            preview.getErrores().add("La fecha contable es obligatoria.");
        }

        if (request.getIdConvenio() == null) {
            preview.getErrores().add("El convenio es obligatorio.");
        }

        if (isBlank(request.getDocumentoSoporte())) {
            preview.getErrores().add("El documento soporte es obligatorio.");
            return;
        }

        String doc =
                request.getDocumentoSoporte().trim();

        if (!doc.matches("\\d+")) {
            preview.getErrores().add("El documento soporte debe ser numérico.");
        }

        if (doc.length() > 7) {
            preview.getErrores().add(
                    "El documento soporte no puede tener más de 7 dígitos."
            );
        }
    }

    private String normalizarDocumentoSoporte(
            String documento
    ) {

        String limpio =
                documento == null
                        ? ""
                        : documento.trim();

        if (limpio.length() >= 7) {
            return limpio;
        }

        return String.format(
                "%7s",
                limpio
        ).replace(' ', '0');
    }

    private BigDecimal nvl(
            BigDecimal value
    ) {
        return value == null
                ? BigDecimal.ZERO
                : value;
    }

    private boolean isBlank(
            String value
    ) {
        return value == null
                || value.trim().isEmpty();
    }
}