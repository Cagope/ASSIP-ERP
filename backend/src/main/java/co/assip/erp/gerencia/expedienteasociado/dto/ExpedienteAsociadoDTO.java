package co.assip.erp.gerencia.expedienteasociado.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ExpedienteAsociadoDTO {

    // =========================================================
    // Resultado general de la consulta
    // =========================================================
    private boolean encontrado;
    private String codigoResultado;
    private String mensaje;

    // =========================================================
    // Identificación principal
    // =========================================================
    private Long idDatosPersonal;
    private String tipoDocumento;
    private String nombreTipoDocumento;
    private String documento;
    private String nombreCompleto;

    // =========================================================
    // Datos centrales del expediente
    // =========================================================
    private ExpedienteResumenGeneralDTO resumenGeneral;
    private ExpedienteAfiliacionDTO afiliacion;
    private ExpedienteContactoDTO contacto;
    private ExpedienteInformacionFinancieraDTO informacionFinanciera;
    private ExpedienteSarlaftDTO sarlaft;

    // =========================================================
    // Participación institucional
    //
    // Puede contener simultáneamente:
    // - cargos directivos;
    // - participación en uno o varios comités;
    // - condición de asociado privilegiado.
    // =========================================================
    private List<ExpedienteParticipacionInstitucionalDTO>
            participacionInstitucional =
            new ArrayList<>();

    // =========================================================
    // Productos de depósitos
    // =========================================================
    private List<ExpedienteCuentaAhorroDTO> cuentasAhorro =
            new ArrayList<>();

    private List<ExpedienteCdatDTO> cdats =
            new ArrayList<>();

    // =========================================================
    // Cartera
    // Se implementará en la última fase, pero el contrato queda
    // preparado desde el inicio.
    // =========================================================
    private List<ExpedienteCreditoDTO> creditos =
            new ArrayList<>();

    // =========================================================
    // Bienes del asociado
    // =========================================================
    private List<ExpedienteBienInmuebleDTO> bienesInmuebles =
            new ArrayList<>();

    private List<ExpedienteBienVehiculoDTO> bienesVehiculos =
            new ArrayList<>();

    private List<ExpedienteBienMaquinariaDTO> bienesMaquinaria =
            new ArrayList<>();

    private List<ExpedienteBienInversionDTO> bienesInversiones =
            new ArrayList<>();

    // =========================================================
    // Garantías
    // Se completará cuando se incorpore Cartera.
    // =========================================================
    private List<ExpedienteGarantiaDTO> garantias =
            new ArrayList<>();

    // =========================================================
    // Alertas gerenciales
    // =========================================================
    private List<ExpedienteAlertaDTO> alertas =
            new ArrayList<>();

    // =========================================================
    // Indicadores consolidados
    // =========================================================
    private ExpedienteIndicadoresDTO indicadores;

    // =========================================================
    // Conteos de registros
    // =========================================================
    private Integer cantidadParticipacionesInstitucionales;
    private Integer cantidadCuentasAhorro;
    private Integer cantidadCdats;
    private Integer cantidadCreditos;
    private Integer cantidadBienes;
    private Integer cantidadGarantias;
    private Integer cantidadAlertas;

    private Integer cantidadAlertasCriticas;
    private Integer cantidadAlertasAdvertencia;
    private Integer cantidadAlertasInformativas;

    // =========================================================
    // Estado de carga de cada módulo
    // Permite identificar si un bloque fue consultado
    // correctamente o si presentó alguna novedad.
    // =========================================================
    private ExpedienteEstadoCargaDTO estadoCarga;

    // =========================================================
    // Metadatos de la consulta
    // =========================================================
    private LocalDateTime fechaHoraConsulta;
    private Integer idUsuarioConsulta;
    private Integer idAgenciaConsulta;

    // =========================================================
    // Constructor
    // =========================================================
    public ExpedienteAsociadoDTO() {
        this.encontrado = false;
        this.codigoResultado = "SIN_CONSULTAR";
        this.mensaje = null;

        this.cantidadParticipacionesInstitucionales = 0;
        this.cantidadCuentasAhorro = 0;
        this.cantidadCdats = 0;
        this.cantidadCreditos = 0;
        this.cantidadBienes = 0;
        this.cantidadGarantias = 0;
        this.cantidadAlertas = 0;

        this.cantidadAlertasCriticas = 0;
        this.cantidadAlertasAdvertencia = 0;
        this.cantidadAlertasInformativas = 0;

        this.fechaHoraConsulta = LocalDateTime.now();
        this.estadoCarga = new ExpedienteEstadoCargaDTO();
    }

    // =========================================================
    // Métodos auxiliares de consolidación
    // =========================================================

    /**
     * Actualiza los conteos generales con base en las listas
     * actualmente cargadas.
     *
     * No reemplaza las reglas financieras ni las validaciones
     * del Service. Únicamente consolida cantidades.
     */
    public void actualizarConteos() {

        this.cantidadParticipacionesInstitucionales =
                obtenerCantidad(participacionInstitucional);

        this.cantidadCuentasAhorro =
                obtenerCantidad(cuentasAhorro);

        this.cantidadCdats =
                obtenerCantidad(cdats);

        this.cantidadCreditos =
                obtenerCantidad(creditos);

        int inmuebles =
                obtenerCantidad(bienesInmuebles);

        int vehiculos =
                obtenerCantidad(bienesVehiculos);

        int maquinaria =
                obtenerCantidad(bienesMaquinaria);

        int inversiones =
                obtenerCantidad(bienesInversiones);

        this.cantidadBienes =
                inmuebles
                        + vehiculos
                        + maquinaria
                        + inversiones;

        this.cantidadGarantias =
                obtenerCantidad(garantias);

        this.cantidadAlertas =
                obtenerCantidad(alertas);

        actualizarConteosAlertas();
    }

    /**
     * Consolida las alertas por nivel.
     */
    public void actualizarConteosAlertas() {

        this.cantidadAlertasCriticas = 0;
        this.cantidadAlertasAdvertencia = 0;
        this.cantidadAlertasInformativas = 0;

        if (alertas == null || alertas.isEmpty()) {
            return;
        }

        for (ExpedienteAlertaDTO alerta : alertas) {

            if (alerta == null || alerta.getNivel() == null) {
                continue;
            }

            String nivel =
                    alerta.getNivel()
                            .trim()
                            .toUpperCase();

            switch (nivel) {
                case "CRITICA" ->
                        this.cantidadAlertasCriticas++;

                case "ADVERTENCIA" ->
                        this.cantidadAlertasAdvertencia++;

                case "INFO", "INFORMATIVA" ->
                        this.cantidadAlertasInformativas++;

                default -> {
                    // No se incrementa ningún contador.
                }
            }
        }
    }

    /**
     * Marca el expediente como encontrado.
     */
    public void marcarEncontrado() {
        this.encontrado = true;
        this.codigoResultado = "OK";
        this.mensaje = "Consulta integral generada correctamente.";
    }

    /**
     * Marca el resultado cuando no existe el asociado.
     */
    public void marcarNoEncontrado(Long idDatosPersonal) {
        this.encontrado = false;
        this.codigoResultado = "ASOCIADO_NO_ENCONTRADO";
        this.mensaje =
                "No se encontró información para el asociado con id "
                        + idDatosPersonal
                        + ".";
    }

    /**
     * Marca un resultado con error controlado.
     */
    public void marcarError(String mensajeError) {
        this.encontrado = false;
        this.codigoResultado = "ERROR_CONSULTA";
        this.mensaje =
                mensajeError == null || mensajeError.isBlank()
                        ? "No fue posible generar la consulta integral."
                        : mensajeError.trim();
    }

    private int obtenerCantidad(List<?> lista) {
        return lista == null ? 0 : lista.size();
    }
}