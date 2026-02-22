package co.assip.erp.nomina.empleado_contratos.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmpleadoContratoListViewDTO {

    // =========================
    // PK + relaciones base
    // =========================
    private Integer idContrato;
    private Integer idEmpleado;

    // (opcional para futuro: si decides decodificar empleado en el back)
    private Integer idDatosPersonal;
    private String documentoEmpleado;
    private String nombreEmpleado;

    // =========================
    // Fechas / contrato
    // =========================
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    private Integer idTipoContrato;
    private String tipoContratoNombre; // (si lo quieres decodificado; si no, queda null)

    private String periodoPago;

    // =========================
    // Sección / Cargo
    // =========================
    private Integer idSeccion;
    private String nombreSeccion;

    private Integer idCargo;
    private String nombreCargo;

    // =========================
    // Valores
    // =========================
    private BigDecimal salarioBase;
    private Boolean salarioIntegral;

    // =========================
    // Afiliaciones (ID + nombre)
    // =========================
    private Integer idEps;
    private String nombreEps;

    private Integer idAfp;
    private String nombreAfp;

    private Integer idCesantias;
    private String nombreCesantias;

    private Integer idArl;
    private String nombreArl;

    private Integer idCajaCompensacion;
    private String nombreCajaCompensacion;

    // =========================
    // Cuenta nómina (solo ID + display)
    // =========================
    private Long idCuentaAhorroNomina;
    private String cuentaNominaDisplay;

    // (si luego quieres “id forma + nombre forma” en columnas separadas)
    private Integer idFormaAhorroNomina;
    private String nombreFormaAhorroNomina;

    // =========================
    // ARL / Renovación
    // =========================
    private LocalDate fechaEnvioNotaRenovacion;

    private Short claseRiesgoArl;
    private BigDecimal porcentajeArl;

    // =========================
    // Estado
    // =========================
    private Boolean activo;
}
