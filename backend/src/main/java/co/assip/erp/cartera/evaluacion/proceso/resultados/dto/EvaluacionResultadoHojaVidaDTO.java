package co.assip.erp.cartera.evaluacion.proceso.resultados.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EvaluacionResultadoHojaVidaDTO {

    // =========================================================
    // IDENTIFICACIÓN DE LA FOTOGRAFÍA
    // =========================================================

    private Integer idCierreHojaVidaPersona;

    private Integer idCierreHojaVida;

    private Integer idDatosPersonal;


    // =========================================================
    // IDENTIFICACIÓN DEL ASOCIADO
    // =========================================================

    private String tipoDocumento;

    private String documento;

    private String tipoPersona;

    private Boolean tieneRut;

    private String digitoVerificacion;

    private String nombres;

    private String primerApellido;

    private String segundoApellido;


    // =========================================================
    // FECHAS
    // =========================================================

    private LocalDate fechaNacimiento;

    private LocalDate fechaApertura;

    private LocalDate fechaActualizacion;


    // =========================================================
    // INFORMACIÓN PERSONAL
    // =========================================================

    private String codigoGenero;

    private String codigoEstadoCivil;

    private String codigoEscolaridad;

    private String cabezaFamilia;

    private Integer estratoSocial;

    private String codigoTipoVivienda;

    private Integer numeroHijos;

    private String codigoOcupacion;

    private String codigoSectorEconomico;

    private String codigoActividadSes;

    private String codigoActividadDian;


    // =========================================================
    // UBICACIÓN
    // =========================================================

    private String direccion;

    private String barrio;

    private String telefono;

    private String celularUno;

    private String celularDos;

    private String correo;

    private Integer idPais;

    private Integer idDepartamento;

    private Integer idCiudad;

    private Integer idZona;

    private Integer idSubZona;


    // =========================================================
    // INGRESOS
    // =========================================================

    private BigDecimal valorSalario;

    private BigDecimal valorPension;

    private BigDecimal ingresosArriendo;

    private BigDecimal ingresosComisiones;

    private BigDecimal otrosIngresos;

    private BigDecimal ingresosTotales;


    // =========================================================
    // EGRESOS
    // =========================================================

    private BigDecimal egresosFamiliares;

    private BigDecimal egresosArriendo;

    private BigDecimal egresosCredito;

    private BigDecimal otrosEgresos;

    private BigDecimal egresosTotales;


    // =========================================================
    // PATRIMONIO
    // =========================================================

    private BigDecimal totalActivos;

    private BigDecimal totalPasivos;

    private BigDecimal patrimonioTotal;


    // =========================================================
    // INFORMACIÓN FINANCIERA COMPLEMENTARIA
    // =========================================================

    private BigDecimal deudaRelacionFinanciera;

    private String origenFondos;

    private String relacionFinanciera;

    private BigDecimal ingresoDisponible;


    // =========================================================
    // APOYO PARA PRESENTACIÓN
    // =========================================================

    public String getNombreCompleto() {

        StringBuilder nombre =
                new StringBuilder();

        agregarParte(
                nombre,
                nombres
        );

        agregarParte(
                nombre,
                primerApellido
        );

        agregarParte(
                nombre,
                segundoApellido
        );

        return nombre.toString();
    }


    // =========================================================
    // APOYO INTERNO
    // =========================================================

    private void agregarParte(
            StringBuilder texto,
            String valor
    ) {

        if (
                valor == null
                        || valor.isBlank()
        ) {
            return;
        }

        if (!texto.isEmpty()) {
            texto.append(" ");
        }

        texto.append(
                valor.trim()
        );
    }
}