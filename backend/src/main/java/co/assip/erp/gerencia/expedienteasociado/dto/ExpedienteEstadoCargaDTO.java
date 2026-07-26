package co.assip.erp.gerencia.expedienteasociado.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ExpedienteEstadoCargaDTO {

    // =========================================================
    // Estado general
    // =========================================================
    private Boolean cargaCompleta;
    private Boolean presentaErrores;
    private Boolean presentaAdvertencias;

    // =========================================================
    // Estado por bloque: persona
    // =========================================================
    private Boolean resumenGeneralCargado;
    private Boolean afiliacionCargada;
    private Boolean contactoCargado;
    private Boolean informacionFinancieraCargada;
    private Boolean sarlaftCargado;

    // =========================================================
    // Estado por bloque: participación institucional
    // =========================================================
    private Boolean participacionInstitucionalCargada;

    // =========================================================
    // Estado por bloque: captaciones y cartera
    // =========================================================
    private Boolean cuentasAhorroCargadas;
    private Boolean cdatsCargados;
    private Boolean creditosCargados;

    // =========================================================
    // Estado por bloque: bienes
    // =========================================================
    private Boolean bienesInmueblesCargados;
    private Boolean bienesVehiculosCargados;
    private Boolean bienesMaquinariaCargados;
    private Boolean bienesInversionesCargados;

    // =========================================================
    // Estado por bloque: consolidación
    // =========================================================
    private Boolean garantiasCargadas;
    private Boolean indicadoresCalculados;
    private Boolean alertasCalculadas;

    // =========================================================
    // Cantidades de novedades
    // =========================================================
    private Integer cantidadBloquesCargados;
    private Integer cantidadBloquesConError;

    // =========================================================
    // Detalle
    // =========================================================
    private List<String> advertencias;
    private List<String> errores;

    // =========================================================
    // Constructor
    // =========================================================
    public ExpedienteEstadoCargaDTO() {

        this.cargaCompleta = Boolean.FALSE;
        this.presentaErrores = Boolean.FALSE;
        this.presentaAdvertencias = Boolean.FALSE;

        this.resumenGeneralCargado = Boolean.FALSE;
        this.afiliacionCargada = Boolean.FALSE;
        this.contactoCargado = Boolean.FALSE;
        this.informacionFinancieraCargada = Boolean.FALSE;
        this.sarlaftCargado = Boolean.FALSE;

        this.participacionInstitucionalCargada = Boolean.FALSE;

        this.cuentasAhorroCargadas = Boolean.FALSE;
        this.cdatsCargados = Boolean.FALSE;
        this.creditosCargados = Boolean.FALSE;

        this.bienesInmueblesCargados = Boolean.FALSE;
        this.bienesVehiculosCargados = Boolean.FALSE;
        this.bienesMaquinariaCargados = Boolean.FALSE;
        this.bienesInversionesCargados = Boolean.FALSE;

        this.garantiasCargadas = Boolean.FALSE;
        this.indicadoresCalculados = Boolean.FALSE;
        this.alertasCalculadas = Boolean.FALSE;

        this.cantidadBloquesCargados = 0;
        this.cantidadBloquesConError = 0;

        this.advertencias = new ArrayList<>();
        this.errores = new ArrayList<>();
    }

    // =========================================================
    // Registro de advertencias
    // =========================================================
    public void agregarAdvertencia(String advertencia) {

        if (advertencia == null || advertencia.isBlank()) {
            return;
        }

        asegurarListasInicializadas();

        this.advertencias.add(advertencia.trim());
        this.presentaAdvertencias = Boolean.TRUE;
    }

    // =========================================================
    // Registro de errores
    // =========================================================
    public void agregarError(String error) {

        if (error == null || error.isBlank()) {
            return;
        }

        asegurarListasInicializadas();

        this.errores.add(error.trim());
        this.presentaErrores = Boolean.TRUE;
        this.cantidadBloquesConError =
                valorEnteroSeguro(this.cantidadBloquesConError) + 1;
    }

    // =========================================================
    // Consolidación del estado general
    // =========================================================
    public void actualizarEstadoGeneral() {

        asegurarListasInicializadas();

        this.cantidadBloquesCargados =
                contarBloquesCargados();

        this.presentaErrores =
                !errores.isEmpty();

        this.presentaAdvertencias =
                !advertencias.isEmpty();

        this.cargaCompleta =
                Boolean.TRUE.equals(resumenGeneralCargado)
                        && Boolean.TRUE.equals(afiliacionCargada)
                        && Boolean.TRUE.equals(contactoCargado)
                        && Boolean.TRUE.equals(
                        informacionFinancieraCargada
                )
                        && Boolean.TRUE.equals(sarlaftCargado)
                        && Boolean.TRUE.equals(
                        participacionInstitucionalCargada
                )
                        && Boolean.TRUE.equals(
                        cuentasAhorroCargadas
                )
                        && Boolean.TRUE.equals(cdatsCargados)
                        && Boolean.TRUE.equals(creditosCargados)
                        && Boolean.TRUE.equals(
                        bienesInmueblesCargados
                )
                        && Boolean.TRUE.equals(
                        bienesVehiculosCargados
                )
                        && Boolean.TRUE.equals(
                        bienesMaquinariaCargados
                )
                        && Boolean.TRUE.equals(
                        bienesInversionesCargados
                )
                        && Boolean.TRUE.equals(garantiasCargadas)
                        && Boolean.TRUE.equals(
                        indicadoresCalculados
                )
                        && Boolean.TRUE.equals(alertasCalculadas)
                        && !Boolean.TRUE.equals(presentaErrores);
    }

    // =========================================================
    // Conteo de bloques cargados
    // =========================================================
    private int contarBloquesCargados() {

        int total = 0;

        total += valor(resumenGeneralCargado);
        total += valor(afiliacionCargada);
        total += valor(contactoCargado);
        total += valor(informacionFinancieraCargada);
        total += valor(sarlaftCargado);

        total += valor(participacionInstitucionalCargada);

        total += valor(cuentasAhorroCargadas);
        total += valor(cdatsCargados);
        total += valor(creditosCargados);

        total += valor(bienesInmueblesCargados);
        total += valor(bienesVehiculosCargados);
        total += valor(bienesMaquinariaCargados);
        total += valor(bienesInversionesCargados);

        total += valor(garantiasCargadas);
        total += valor(indicadoresCalculados);
        total += valor(alertasCalculadas);

        return total;
    }

    // =========================================================
    // Utilidades internas
    // =========================================================
    private void asegurarListasInicializadas() {

        if (advertencias == null) {
            advertencias = new ArrayList<>();
        }

        if (errores == null) {
            errores = new ArrayList<>();
        }
    }

    private int valor(Boolean estado) {
        return Boolean.TRUE.equals(estado) ? 1 : 0;
    }

    private int valorEnteroSeguro(Integer valor) {
        return valor == null ? 0 : valor;
    }
}