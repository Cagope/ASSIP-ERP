package co.assip.erp.cajas.movimientos;

import co.assip.erp.cajas.medios_pago.dto.MedioPagoChequeDTO;
import co.assip.erp.cajas.medios_pago.dto.MediosPagoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CajasMovimientosService {

    private final CajasMovimientosRepository repository;

    public void registrarAperturaCdat(
            MediosPagoDTO  mediosPago,
            LocalDate fechaContable,
            Long idCuentaCdat,
            String codigoCdat,
            String tipoComprobante,
            String numeroComprobante,
            Integer idUsuario
    ) {
        if (mediosPago == null) {
            return;
        }

        BigDecimal valorEfectivo = nvl(mediosPago.getValorEfectivo());
        BigDecimal valorCheques = nvl(mediosPago.getValorCheques());

        if (valorEfectivo.compareTo(BigDecimal.ZERO) <= 0
                && valorCheques.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        if (mediosPago.getIdCaja() == null) {
            throw new RuntimeException("Debe seleccionar la caja para registrar efectivo o cheques.");
        }

        if (fechaContable == null) {
            throw new RuntimeException("Debe indicar la fecha contable para registrar movimientos de caja.");
        }

        Long idProvision = repository.obtenerProvisionAbierta(
                mediosPago.getIdCaja(),
                fechaContable
        );

        if (idProvision == null) {
            throw new RuntimeException("La caja seleccionada no tiene provisión ABIERTA para la fecha contable.");
        }

        Long idDepartamentoCdat = repository.obtenerIdDepartamento("11");
        if (idDepartamentoCdat == null) {
            throw new RuntimeException("No existe el departamento operativo 11 - MOVIMIENTOS C.D.A.T.");
        }

        Long idOperacionAperturaCdat = repository.obtenerIdOperacion("APERTURA_CDAT");
        if (idOperacionAperturaCdat == null) {
            throw new RuntimeException("No existe la operación de caja APERTURA_CDAT.");
        }

        String concepto = "Apertura CDAT " + (codigoCdat == null ? "" : codigoCdat);

        if (valorEfectivo.compareTo(BigDecimal.ZERO) > 0) {
            repository.insertarMovimientoCaja(
                    idProvision,
                    mediosPago.getIdCaja(),
                    fechaContable,
                    idDepartamentoCdat,
                    idOperacionAperturaCdat,
                    "CDAT",
                    "APERTURA_CDAT",
                    "cdat.cuentas_cdats",
                    idCuentaCdat,
                    codigoCdat,
                    "INGRESO",
                    "EFECTIVO",
                    valorEfectivo,
                    concepto,
                    "PENDIENTE",
                    tipoComprobante,
                    numeroComprobante,
                    idUsuario
            );
        }

        if (valorCheques.compareTo(BigDecimal.ZERO) > 0) {
            validarCheques(mediosPago.getCheques(), valorCheques);

            Long idMovimientoCheque = repository.insertarMovimientoCaja(
                    idProvision,
                    mediosPago.getIdCaja(),
                    fechaContable,
                    idDepartamentoCdat,
                    idOperacionAperturaCdat,
                    "CDAT",
                    "APERTURA_CDAT",
                    "cdat.cuentas_cdats",
                    idCuentaCdat,
                    codigoCdat,
                    "INGRESO",
                    "CHEQUE",
                    valorCheques,
                    concepto,
                    "PENDIENTE",
                    tipoComprobante,
                    numeroComprobante,
                    idUsuario
            );

            for (MedioPagoChequeDTO cheque : mediosPago.getCheques()) {
                repository.insertarChequeRecibido(
                        idMovimientoCheque,
                        mediosPago.getIdCaja(),
                        fechaContable,
                        cheque,
                        numeroComprobante,
                        codigoCdat,
                        "CDAT",
                        "APERTURA_CDAT",
                        "cdat.cuentas_cdats",
                        idCuentaCdat,
                        idUsuario
                );
            }
        }
    }

    public void registrarDesembolsoCredito(
            Long idCaja,
            BigDecimal valorEfectivo,
            LocalDate fechaContable,
            Long idCarteraCredito,
            String pagareCartera,
            String tipoComprobante,
            String numeroComprobante,
            Integer idUsuario
    ) {

        BigDecimal efectivo = nvl(valorEfectivo);

        // Si no existe salida física de efectivo,
        // no corresponde registrar movimiento de Caja.
        if (efectivo.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        if (idCaja == null || idCaja <= 0) {
            throw new RuntimeException(
                    "Debe seleccionar la caja para registrar el desembolso en efectivo."
            );
        }

        if (fechaContable == null) {
            throw new RuntimeException(
                    "Debe indicar la fecha contable para registrar el desembolso."
            );
        }

        if (idCarteraCredito == null || idCarteraCredito <= 0) {
            throw new RuntimeException(
                    "El crédito es obligatorio para registrar el movimiento de caja."
            );
        }

        Long idProvision = repository.obtenerProvisionAbierta(
                idCaja,
                fechaContable
        );

        if (idProvision == null) {
            throw new RuntimeException(
                    "La caja seleccionada no tiene provisión ABIERTA "
                            + "para la fecha contable."
            );
        }

        Long idDepartamentoCartera =
                repository.obtenerIdDepartamento("12");

        if (idDepartamentoCartera == null) {
            throw new RuntimeException(
                    "No existe el departamento operativo "
                            + "12 - MOVIMIENTOS CARTERA."
            );
        }

        Long idOperacionDesembolso =
                repository.obtenerIdOperacion(
                        "DESEMBOLSO_CREDITO"
                );

        if (idOperacionDesembolso == null) {
            throw new RuntimeException(
                    "No existe la operación de caja "
                            + "DESEMBOLSO_CREDITO."
            );
        }

        String concepto =
                "Desembolso crédito "
                        + (pagareCartera == null
                        ? ""
                        : pagareCartera);

        repository.insertarMovimientoCaja(
                idProvision,
                idCaja,
                fechaContable,
                idDepartamentoCartera,
                idOperacionDesembolso,
                "CARTERA",
                "DESEMBOLSO_CREDITO",
                "cartera.carteras_creditos",
                idCarteraCredito,
                pagareCartera,
                "EGRESO",
                "EFECTIVO",
                efectivo,
                concepto,
                "PENDIENTE",
                tipoComprobante,
                numeroComprobante,
                idUsuario
        );
    }

    public void registrarCobrosDesembolsoCredito(
            Long idCaja,
            BigDecimal valorCobros,
            LocalDate fechaContable,
            Long idCarteraCredito,
            String pagareCartera,
            String tipoComprobante,
            String numeroComprobante,
            Integer idUsuario
    ) {

        BigDecimal cobros = nvl(valorCobros);

        // Si el desembolso no genera cobros,
        // no corresponde registrar movimiento de Caja.
        if (cobros.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        if (idCaja == null || idCaja <= 0) {
            throw new RuntimeException(
                    "Debe seleccionar la caja para registrar "
                            + "los cobros del desembolso."
            );
        }

        if (fechaContable == null) {
            throw new RuntimeException(
                    "Debe indicar la fecha contable para registrar "
                            + "los cobros del desembolso."
            );
        }

        if (idCarteraCredito == null || idCarteraCredito <= 0) {
            throw new RuntimeException(
                    "El crédito es obligatorio para registrar "
                            + "los cobros del desembolso."
            );
        }

        Long idProvision = repository.obtenerProvisionAbierta(
                idCaja,
                fechaContable
        );

        if (idProvision == null) {
            throw new RuntimeException(
                    "La caja seleccionada no tiene provisión ABIERTA "
                            + "para la fecha contable."
            );
        }

        Long idDepartamentoCartera =
                repository.obtenerIdDepartamento("12");

        if (idDepartamentoCartera == null) {
            throw new RuntimeException(
                    "No existe el departamento operativo "
                            + "12 - MOVIMIENTOS CARTERA."
            );
        }

        Long idOperacionCobro =
                repository.obtenerIdOperacion(
                        "COBRO_DESEMBOLSO"
                );

        if (idOperacionCobro == null) {
            throw new RuntimeException(
                    "No existe la operación de caja "
                            + "COBRO_DESEMBOLSO."
            );
        }

        String concepto =
                "Cobros desembolso crédito "
                        + (pagareCartera == null
                        ? ""
                        : pagareCartera);

        repository.insertarMovimientoCaja(
                idProvision,
                idCaja,
                fechaContable,
                idDepartamentoCartera,
                idOperacionCobro,
                "CARTERA",
                "DESEMBOLSO_CREDITO",
                "cartera.carteras_creditos",
                idCarteraCredito,
                pagareCartera,
                "INGRESO",
                "EFECTIVO",
                cobros,
                concepto,
                "PENDIENTE",
                tipoComprobante,
                numeroComprobante,
                idUsuario
        );
    }

    public void registrarCancelacionCdat(
            Long idCaja,
            BigDecimal valorEfectivo,
            LocalDate fechaContable,
            Long idCuentaCdat,
            String codigoCdat,
            String tipoComprobante,
            String numeroComprobante,
            Integer idUsuario
    ) {

        BigDecimal efectivo = nvl(valorEfectivo);

        // Si la cancelación no genera salida física de efectivo,
        // no corresponde registrar movimiento de Caja.
        if (efectivo.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        if (idCaja == null || idCaja <= 0) {
            throw new RuntimeException(
                    "Debe seleccionar la caja para registrar la cancelación en efectivo."
            );
        }

        if (fechaContable == null) {
            throw new RuntimeException(
                    "Debe indicar la fecha contable para registrar la cancelación."
            );
        }

        if (idCuentaCdat == null || idCuentaCdat <= 0) {
            throw new RuntimeException(
                    "El CDAT es obligatorio para registrar el movimiento de caja."
            );
        }

        Long idProvision = repository.obtenerProvisionAbierta(
                idCaja,
                fechaContable
        );

        if (idProvision == null) {
            throw new RuntimeException(
                    "La caja seleccionada no tiene provisión ABIERTA "
                            + "para la fecha contable."
            );
        }

        Long idDepartamentoCdat =
                repository.obtenerIdDepartamento("11");

        if (idDepartamentoCdat == null) {
            throw new RuntimeException(
                    "No existe el departamento operativo "
                            + "11 - MOVIMIENTOS C.D.A.T."
            );
        }

        Long idOperacionCancelacionCdat =
                repository.obtenerIdOperacion(
                        "CANCELACION_CDAT"
                );

        if (idOperacionCancelacionCdat == null) {
            throw new RuntimeException(
                    "No existe la operación de caja "
                            + "CANCELACION_CDAT."
            );
        }

        String concepto =
                "Cancelación CDAT "
                        + (codigoCdat == null ? "" : codigoCdat);

        repository.insertarMovimientoCaja(
                idProvision,
                idCaja,
                fechaContable,
                idDepartamentoCdat,
                idOperacionCancelacionCdat,
                "CDAT",
                "CANCELACION_CDAT",
                "cdat.cuentas_cdats",
                idCuentaCdat,
                codigoCdat,
                "EGRESO",
                "EFECTIVO",
                efectivo,
                concepto,
                "PENDIENTE",
                tipoComprobante,
                numeroComprobante,
                idUsuario
        );
    }

    public void registrarRetiroIntegralAsociado(
            Long idCaja, BigDecimal valorCaja, LocalDate fechaContable,
            Integer idDatosPersonal, String codigoReferencia,
            String tipoComprobante, String numeroComprobante, Integer idUsuario) {
        if (valorCaja == null || valorCaja.signum() <= 0) {
            throw new IllegalArgumentException("El valor de Caja debe ser positivo.");
        }
        if (idCaja == null || idCaja <= 0 || fechaContable == null
                || idDatosPersonal == null || idDatosPersonal <= 0
                || idUsuario == null || idUsuario <= 0) {
            throw new IllegalArgumentException("Datos incompletos para el retiro en Caja.");
        }
        Long idProvision = repository.obtenerProvisionAbierta(idCaja, fechaContable);
        if (idProvision == null) {
            throw new IllegalStateException("La caja no tiene provisión ABIERTA para la fecha.");
        }
        Long departamento = repository.obtenerIdDepartamento("00");
        Long operacion = repository.obtenerIdOperacion("RETIRO_AHORRO");
        if (departamento == null || operacion == null) {
            throw new IllegalStateException("Falta parametrizar el retiro de ahorro en Caja.");
        }
        repository.insertarMovimientoCaja(
                idProvision, idCaja, fechaContable, departamento, operacion,
                "DEPOSITOS", "RETIRO_INTEGRAL_ASOCIADO", "hoja_vida.datos_personales",
                idDatosPersonal.longValue(), codigoReferencia, "EGRESO", "EFECTIVO",
                valorCaja, "Retiro integral asociado " + idDatosPersonal,
                "PENDIENTE", tipoComprobante, numeroComprobante, idUsuario);
    }

    /**
     * Registra un recaudo de cuotas de Cartera pendiente
     * de confirmación por el cajero.
     *
     * No aplica el pago al crédito ni genera el extracto.
     *
     * La aplicación financiera debe realizarse cuando
     * se confirme el recaudo, dentro de la misma transacción.
     *
     * @return identificador del movimiento pendiente de Caja.
     */
    /**
     * Registra un recaudo de cuotas de Cartera pendiente
     * de confirmación por el cajero.
     *
     * La referencia de origen es idPago, no el identificador
     * del crédito. Esto permite varios pagos sobre un mismo
     * crédito sin confundir las operaciones.
     *
     * El valorRecaudo corresponde únicamente al efectivo.
     *
     * No actualiza el crédito ni genera comprobantes.
     */
    public Long registrarRecaudoCuotasCartera(
            Long idCaja,
            BigDecimal valorRecaudo,
            LocalDate fechaContable,
            Long idPago,
            String pagareCartera,
            String tipoComprobante,
            String numeroComprobante,
            Integer idUsuario
    ) {

        if (idCaja == null || idCaja <= 0) {
            throw new IllegalArgumentException(
                    "Debe seleccionar una caja válida.");
        }

        if (valorRecaudo == null
                || valorRecaudo.signum() <= 0
                || valorRecaudo.stripTrailingZeros().scale() > 0) {
            throw new IllegalArgumentException(
                    "El recaudo en efectivo debe ser positivo y expresado en pesos enteros.");
        }

        if (fechaContable == null) {
            throw new IllegalArgumentException(
                    "Debe indicar la fecha contable.");
        }

        if (idPago == null || idPago <= 0) {
            throw new IllegalArgumentException(
                    "Debe indicar una operación de pago válida.");
        }

        if (idUsuario == null || idUsuario <= 0) {
            throw new IllegalArgumentException(
                    "El usuario de sesión es obligatorio.");
        }

        Long idProvision = repository.obtenerProvisionAbierta(
                idCaja,
                fechaContable
        );

        if (idProvision == null) {
            throw new IllegalStateException(
                    "La caja seleccionada no tiene provisión ABIERTA para la fecha contable.");
        }

        Long idDepartamento = repository.obtenerIdDepartamento("12");

        if (idDepartamento == null) {
            throw new IllegalStateException(
                    "No existe el departamento operativo 12 - MOVIMIENTOS CARTERA.");
        }

        Long idOperacion = repository.obtenerIdOperacion("PAGO_CUOTAS");

        if (idOperacion == null) {
            throw new IllegalStateException(
                    "No existe la operación de Caja PAGO_CUOTAS.");
        }

        String concepto = "Pago de cuotas crédito "
                + (pagareCartera == null ? "" : pagareCartera);

        return repository.insertarMovimientoCaja(
                idProvision,
                idCaja,
                fechaContable,
                idDepartamento,
                idOperacion,
                "CARTERA",
                "PAGO_CUOTAS",
                "cartera.pagos_cuotas",
                idPago,
                pagareCartera,
                "INGRESO",
                "EFECTIVO",
                valorRecaudo,
                concepto,
                "PENDIENTE",
                tipoComprobante,
                numeroComprobante,
                idUsuario
        );
    }

    private void validarCheques(List<MedioPagoChequeDTO> cheques, BigDecimal valorCheques) {
        if (cheques == null || cheques.isEmpty()) {
            throw new RuntimeException("Debe ingresar el detalle de los cheques.");
        }

        BigDecimal total = BigDecimal.ZERO;

        for (MedioPagoChequeDTO cheque : cheques) {
            if (cheque.getCodigoBanco() == null || cheque.getCodigoBanco().isBlank()) {
                throw new RuntimeException("Debe ingresar el código del banco en todos los cheques.");
            }

            if (cheque.getNumeroCheque() == null || cheque.getNumeroCheque().isBlank()) {
                throw new RuntimeException("Debe ingresar el número en todos los cheques.");
            }

            if (cheque.getValorCheque() == null || cheque.getValorCheque().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("El valor de cada cheque debe ser mayor a cero.");
            }

            total = total.add(cheque.getValorCheque());
        }

        if (total.compareTo(valorCheques) != 0) {
            throw new RuntimeException("La suma del detalle de cheques no coincide con el valor total de cheques.");
        }
    }

    public Integer obtenerCuentaContableCaja(
            Long idCaja
    ) {

        if (idCaja == null || idCaja <= 0) {
            throw new IllegalArgumentException(
                    "La caja es obligatoria para obtener la cuenta contable."
            );
        }

        Integer idCatalogoCuenta =
                repository.obtenerCuentaContableCaja(idCaja);

        if (idCatalogoCuenta == null || idCatalogoCuenta <= 0) {
            throw new IllegalStateException(
                    "La caja seleccionada no tiene configurada "
                            + "una cuenta contable."
            );
        }

        return idCatalogoCuenta;
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

}