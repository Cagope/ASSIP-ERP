package co.assip.erp.gerencia.expedienteasociado.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class ExpedienteResumenGeneralDTO {

    // =========================================================
    // Identificación
    // =========================================================

    private Long idDatosPersonal;

    private String tipoPersona;

    private String tipoDocumento;
    private String nombreTipoDocumento;

    private String documento;

    private String nombres;
    private String primerApellido;
    private String segundoApellido;

    private String nombreCompleto;

    // =========================================================
    // Estado del asociado
    // Fuente: cuenta de aportes
    // =========================================================

    private String codigoEstadoAsociado;
    private String nombreEstadoAsociado;

    private String codigoTipoAsociado;
    private String nombreTipoAsociado;

    private String codigoCategoria;
    private String nombreCategoria;

    private Boolean activo;

    // =========================================================
    // Información de afiliación
    // Fuente: cuenta de aportes
    // =========================================================

    private LocalDate fechaAfiliacion;

    private Integer antiguedadDias;
    private Integer antiguedadMeses;
    private Integer antiguedadAnios;

    // =========================================================
    // Información personal
    // =========================================================

    private LocalDate fechaNacimiento;

    private Integer edad;

    private String genero;
    private String estadoCivil;

    private String nombreEscolaridad;

    private String ocupacion;
    private String profesion;

    private String cabezaFamilia;

    private Integer numeroHijos;
    private Integer estratoSocial;

    private String nombreTipoVivienda;

    // =========================================================
    // Lugar de nacimiento
    // =========================================================

    private String paisNacimiento;
    private String departamentoNacimiento;
    private String ciudadNacimiento;

    // =========================================================
    // Actividad económica
    // =========================================================

    private String nombreSectorEconomico;
    private String nombreActividadSes;
    private String nombreActividadDian;

    // =========================================================
    // Contacto
    // =========================================================

    private String direccion;

    private String barrio;

    private String ciudad;
    private String departamento;
    private String pais;

    private String telefono;
    private String celular;

    private String correoElectronico;

    // =========================================================
    // Empresa
    // =========================================================

    private String empresa;

    private String cargo;

    // =========================================================
    // Información económica
    // =========================================================

    private BigDecimal ingresosMensuales;

    private BigDecimal egresosMensuales;

    private BigDecimal activos;

    private BigDecimal pasivos;

    private BigDecimal patrimonio;

    // =========================================================
    // Productos
    // =========================================================

    private Integer numeroCuentasAhorro;

    private Integer numeroCdats;

    private Integer numeroCreditos;

    private Integer numeroBienes;

    // =========================================================
    // Valores
    // =========================================================

    private BigDecimal saldoAportes;

    private BigDecimal saldoAhorros;

    private BigDecimal saldoCdats;

    private BigDecimal saldoCapitalCartera;

    private BigDecimal saldoInteresesCartera;

    private BigDecimal valorBienes;

    private BigDecimal valorGarantias;

    private BigDecimal patrimonioEstimado;

    // =========================================================
    // Riesgo
    // =========================================================

    private Boolean pep;

    private Boolean familiarPep;

    private Boolean monedaExtranjera;

    private Boolean cuentaExterior;

    // =========================================================
    // Alertas
    // =========================================================

    private Integer alertasCriticas;

    private Integer alertasAdvertencia;

    private Integer alertasInformativas;

    // =========================================================
    // Última actualización
    // =========================================================

    private LocalDate fechaActualizacionHojaVida;

    private Integer diasSinActualizar;

    // =========================================================
    // Agencia
    // Fuente: cuenta de aportes
    // =========================================================

    private Integer idAgencia;

    private String codigoAgencia;

    private String nombreAgencia;

    // =========================================================
    // Constructor
    // =========================================================

    public ExpedienteResumenGeneralDTO() {

        this.numeroCuentasAhorro = 0;
        this.numeroCdats = 0;
        this.numeroCreditos = 0;
        this.numeroBienes = 0;

        this.alertasCriticas = 0;
        this.alertasAdvertencia = 0;
        this.alertasInformativas = 0;

        this.ingresosMensuales = BigDecimal.ZERO;
        this.egresosMensuales = BigDecimal.ZERO;

        this.activos = BigDecimal.ZERO;
        this.pasivos = BigDecimal.ZERO;
        this.patrimonio = BigDecimal.ZERO;

        this.saldoAportes = BigDecimal.ZERO;
        this.saldoAhorros = BigDecimal.ZERO;
        this.saldoCdats = BigDecimal.ZERO;

        this.saldoCapitalCartera = BigDecimal.ZERO;
        this.saldoInteresesCartera = BigDecimal.ZERO;

        this.valorBienes = BigDecimal.ZERO;
        this.valorGarantias = BigDecimal.ZERO;

        this.patrimonioEstimado = BigDecimal.ZERO;

        this.activo = null;

        this.pep = Boolean.FALSE;
        this.familiarPep = Boolean.FALSE;
        this.monedaExtranjera = Boolean.FALSE;
        this.cuentaExterior = Boolean.FALSE;
    }

    /**
     * Patrimonio financiero declarado.
     */
    public BigDecimal calcularPatrimonio() {

        BigDecimal valorActivos =
                activos == null
                        ? BigDecimal.ZERO
                        : activos;

        BigDecimal valorPasivos =
                pasivos == null
                        ? BigDecimal.ZERO
                        : pasivos;

        patrimonio =
                valorActivos.subtract(valorPasivos);

        return patrimonio;
    }

    /**
     * Patrimonio estimado considerando productos financieros
     * y bienes registrados.
     */
    public BigDecimal calcularPatrimonioEstimado() {

        BigDecimal financiero =
                patrimonio == null
                        ? BigDecimal.ZERO
                        : patrimonio;

        BigDecimal bienes =
                valorBienes == null
                        ? BigDecimal.ZERO
                        : valorBienes;

        patrimonioEstimado =
                financiero.add(bienes);

        return patrimonioEstimado;
    }
}