package co.assip.erp.gerencia.expedienteasociado.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ExpedienteContactoDTO {

    // =========================================================
    // Identificación
    // =========================================================
    private Long idDatosPersonal;

    private String tipoDocumento;
    private String nombreTipoDocumento;
    private String documento;

    private String nombres;
    private String primerApellido;
    private String segundoApellido;
    private String nombreCompleto;

    // =========================================================
    // Dirección principal
    // =========================================================
    private Long idUbicacion;

    private String direccionPrincipal;
    private String complementoDireccion;
    private String barrioVereda;

    private Long idPais;
    private String codigoPais;
    private String nombrePais;

    private Long idDepartamento;
    private String codigoDepartamento;
    private String nombreDepartamento;

    private Long idCiudad;
    private String codigoCiudad;
    private String nombreCiudad;

    private String codigoPostal;

    private Boolean direccionPrincipalActiva;

    // =========================================================
    // Teléfonos
    // =========================================================
    private String telefonoResidencia;
    private String telefonoTrabajo;
    private String telefonoAlterno;

    private String celularPrincipal;
    private String celularAlterno;

    // =========================================================
    // Correos electrónicos
    // =========================================================
    private String correoPrincipal;
    private String correoAlterno;

    // =========================================================
    // Redes y medios de contacto
    // =========================================================
    private String sitioWeb;

    private String usuarioWhatsapp;
    private String usuarioTelegram;

    // =========================================================
    // Preferencias de contacto
    // =========================================================
    private String medioContactoPreferido;

    private Boolean autorizaCorreoElectronico;
    private Boolean autorizaMensajesTexto;
    private Boolean autorizaWhatsapp;
    private Boolean autorizaLlamadasTelefonicas;

    // =========================================================
    // Información laboral para contacto
    // =========================================================
    private String empresa;

    private String cargo;

    private String direccionEmpresa;
    private String telefonoEmpresa;

    private String extensionEmpresa;

    // =========================================================
    // Ubicación geográfica
    // =========================================================
    private Boolean resideExterior;

    private Boolean direccionCorrespondenciaDiferente;

    private String direccionCorrespondencia;

    private String ciudadCorrespondencia;
    private String departamentoCorrespondencia;
    private String paisCorrespondencia;

    // =========================================================
    // Calidad de la información
    // =========================================================
    private Boolean tieneDireccion;
    private Boolean tieneTelefono;
    private Boolean tieneCelular;
    private Boolean tieneCorreo;

    private Boolean contactoCompleto;

    private Integer porcentajeCompletitud;

    // =========================================================
    // Validaciones gerenciales
    // =========================================================
    private Boolean direccionActualizada;

    private Boolean telefonoValido;
    private Boolean celularValido;
    private Boolean correoValido;

    private LocalDate fechaUltimaActualizacion;
    private Integer diasSinActualizar;

    // =========================================================
    // Alertas
    // =========================================================
    private Boolean presentaNovedades;

    private Integer cantidadNovedades;

    private String nivelNovedad;
    private String resumenNovedades;

    // =========================================================
    // Observaciones
    // =========================================================
    private String observaciones;

    // =========================================================
    // Auditoría
    // =========================================================
    private Integer fkSeguridadCreacion;
    private LocalDate fechaCreacion;

    private Integer fkSeguridadEdicion;
    private LocalDate fechaEdicion;

    // =========================================================
    // Constructor
    // =========================================================
    public ExpedienteContactoDTO() {

        this.direccionPrincipalActiva = Boolean.TRUE;

        this.resideExterior = Boolean.FALSE;
        this.direccionCorrespondenciaDiferente = Boolean.FALSE;

        this.autorizaCorreoElectronico = Boolean.FALSE;
        this.autorizaMensajesTexto = Boolean.FALSE;
        this.autorizaWhatsapp = Boolean.FALSE;
        this.autorizaLlamadasTelefonicas = Boolean.FALSE;

        this.tieneDireccion = Boolean.FALSE;
        this.tieneTelefono = Boolean.FALSE;
        this.tieneCelular = Boolean.FALSE;
        this.tieneCorreo = Boolean.FALSE;

        this.contactoCompleto = Boolean.FALSE;

        this.direccionActualizada = Boolean.FALSE;

        this.telefonoValido = Boolean.FALSE;
        this.celularValido = Boolean.FALSE;
        this.correoValido = Boolean.FALSE;

        this.presentaNovedades = Boolean.FALSE;
        this.cantidadNovedades = 0;

        this.porcentajeCompletitud = 0;
        this.diasSinActualizar = 0;
    }

    // =========================================================
    // Métodos auxiliares
    // =========================================================

    /**
     * Evalúa si la información mínima de contacto está completa.
     */
    public boolean evaluarContactoCompleto() {

        this.tieneDireccion =
                direccionPrincipal != null
                        && !direccionPrincipal.isBlank();

        this.tieneTelefono =
                telefonoResidencia != null
                        && !telefonoResidencia.isBlank();

        this.tieneCelular =
                celularPrincipal != null
                        && !celularPrincipal.isBlank();

        this.tieneCorreo =
                correoPrincipal != null
                        && !correoPrincipal.isBlank();

        this.contactoCompleto =
                tieneDireccion
                        && tieneCelular
                        && tieneCorreo;

        return contactoCompleto;
    }

    /**
     * Calcula un porcentaje simple de completitud de la información
     * de contacto.
     */
    public int calcularPorcentajeCompletitud() {

        int total = 4;
        int completos = 0;

        if (Boolean.TRUE.equals(tieneDireccion)) {
            completos++;
        }

        if (Boolean.TRUE.equals(tieneTelefono)
                || Boolean.TRUE.equals(tieneCelular)) {
            completos++;
        }

        if (Boolean.TRUE.equals(tieneCorreo)) {
            completos++;
        }

        if (nombreCiudad != null && !nombreCiudad.isBlank()) {
            completos++;
        }

        porcentajeCompletitud =
                (completos * 100) / total;

        return porcentajeCompletitud;
    }

    /**
     * Consolida el estado general del bloque.
     */
    public void actualizarEstadoContacto() {

        evaluarContactoCompleto();
        calcularPorcentajeCompletitud();

        int novedades = 0;

        if (!Boolean.TRUE.equals(contactoCompleto)) {
            novedades++;
        }

        if (!Boolean.TRUE.equals(correoValido)) {
            novedades++;
        }

        if (!Boolean.TRUE.equals(celularValido)) {
            novedades++;
        }

        if (!Boolean.TRUE.equals(direccionActualizada)) {
            novedades++;
        }

        cantidadNovedades = novedades;
        presentaNovedades = novedades > 0;

        if (novedades == 0) {

            nivelNovedad = "NORMAL";

            resumenNovedades =
                    "La información de contacto está completa.";

        } else if (novedades == 1) {

            nivelNovedad = "ADVERTENCIA";

            resumenNovedades =
                    "Existe una novedad en la información de contacto.";

        } else {

            nivelNovedad = "CRITICA";

            resumenNovedades =
                    "La información de contacto requiere actualización.";
        }
    }

    /**
     * Indica si es posible contactar al asociado.
     */
    public boolean esContactable() {

        return Boolean.TRUE.equals(tieneCelular)
                || Boolean.TRUE.equals(tieneTelefono)
                || Boolean.TRUE.equals(tieneCorreo);
    }

}