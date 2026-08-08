package co.assip.erp.cartera.evaluacion.proceso.resultados.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EvaluacionResultadoMorosidadDTO {

    // =========================================================
    // IDENTIFICACIÓN DEL CRÉDITO
    // =========================================================

    private Integer idCierreCarteraCredito;

    private Integer idCarteraCredito;

    private Integer idDatosPersonal;


    // =========================================================
    // ASOCIADO
    // =========================================================

    private String documento;

    private String nombres;

    private String primerApellido;

    private String segundoApellido;


    // =========================================================
    // AGENCIA
    // =========================================================

    private Integer idAgencia;

    private String codigoAgencia;

    private String nombreAgencia;


    // =========================================================
    // LÍNEA DE CRÉDITO
    // =========================================================

    private Integer idLineaCredito;

    private String codigoLineaCredito;

    private String nombreLineaCredito;

    private String pagareCartera;


    // =========================================================
    // COMPROBANTE CONSOLIDADO
    //
    // El criterio 401 consolida por:
    //
    // id_cartera_credito
    // + tipo_comprobante
    // + numero_comprobante
    // =========================================================

    private String tipoComprobante;

    private String numeroComprobante;


    // =========================================================
    // FECHAS DEL COMPROBANTE
    //
    // Estas fechas se utilizan únicamente como información
    // de auditoría del extracto.
    //
    // No modifican la regla de consolidación del criterio 401.
    // =========================================================

    private LocalDate fechaContableDesde;

    private LocalDate fechaContableHasta;


    // =========================================================
    // VALORES DEL EXTRACTO
    // =========================================================

    private BigDecimal valorCapital;

    private BigDecimal valorMora;


    // =========================================================
    // MOROSIDAD
    // =========================================================

    private Integer diasMora;


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