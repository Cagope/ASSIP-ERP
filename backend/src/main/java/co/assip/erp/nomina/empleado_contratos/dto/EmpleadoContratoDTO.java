package co.assip.erp.nomina.empleado_contratos.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmpleadoContratoDTO {

    private Integer idContrato;

    private Integer idEmpleado;

    // =========================================================
    // 🔑 RELACIÓN PERSONA (CLAVE PARA EXCEL / REPORTES)
    // =========================================================
    private Long idDatosPersonal;

    private Integer idSeccion;

    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    private Integer idTipoContrato;
    private Integer idCargo;

    private BigDecimal salarioBase;
    private Boolean salarioIntegral;

    private String periodoPago;

    private Integer idEps;
    private Integer idAfp;
    private Integer idCesantias;
    private Integer idArl;
    private Integer idCajaCompensacion;

    // ✅ Solo se guarda el ID de la cuenta
    private Long idCuentaAhorroNomina;

    private LocalDate fechaEnvioNotaRenovacion;

    private Short claseRiesgoArl;
    private BigDecimal porcentajeArl;

    private Boolean activo;

    // =========================================================
    // ✅ PARA UI / REPORTES (NO cambia lógica de negocio)
    // =========================================================
    private String documentoEmpleado;
    private String nombreEmpleado;

    // =========================================================
    // 🔥 REGLAS DESDE TIPO DE CONTRATO
    // =========================================================
    private Boolean aplicaSalud;
    private Boolean aplicaPension;
    private Boolean aplicaArl;
    private Boolean aplicaCajaCompensacion;
    private Boolean aplicaCesantias;
    private Boolean aplicaPrima;
    private Boolean aplicaVacaciones;
    private Boolean aplicaParafiscales;
}