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
                    "INGRESO",
                    "EFECTIVO",
                    valorEfectivo,
                    concepto,
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
                    "INGRESO",
                    "CHEQUE",
                    valorCheques,
                    concepto,
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

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

}