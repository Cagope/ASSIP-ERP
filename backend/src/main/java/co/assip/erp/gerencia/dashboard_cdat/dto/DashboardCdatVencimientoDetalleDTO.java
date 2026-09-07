package co.assip.erp.gerencia.dashboard_cdat.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class DashboardCdatVencimientoDetalleDTO {

    // CDAT
    private Long idCuentaCdat;
    private String codigoCdat;
    private String agencia;

    // Asociado
    private Long idDatosPersonal;
    private String tipoDocumento;
    private String documento;
    private String nombreCompleto;

    // Contacto
    private String telefono;
    private String celularUno;
    private String celularDos;
    private String correoPersonal;

    // Condiciones del CDAT
    private LocalDate fechaApertura;
    private LocalDate fechaVencimiento;
    private Integer diasParaVencer;
    private Integer plazoMeses;
    private BigDecimal tasaNominalAnual;
    private BigDecimal saldoActual;
}