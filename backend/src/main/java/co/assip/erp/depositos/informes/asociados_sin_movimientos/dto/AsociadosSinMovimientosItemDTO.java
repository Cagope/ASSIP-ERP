package co.assip.erp.depositos.informes.asociados_sin_movimientos.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class AsociadosSinMovimientosItemDTO {

    private Integer idCuentaAhorro;
    private Integer idDatosPersonal;

    private String codigoCuenta;

    private String documento;
    private String nombreCompleto;

    private String codigoForma;
    private String nombreForma;

    private String codigoAgencia;
    private String nombreAgencia;

    private LocalDate fechaAperturaCuenta;
    private LocalDate fechaUltimoMovimiento;

    private Integer diasSinMovimiento;

    private BigDecimal saldoActual;

    private String estadoCuenta;

}