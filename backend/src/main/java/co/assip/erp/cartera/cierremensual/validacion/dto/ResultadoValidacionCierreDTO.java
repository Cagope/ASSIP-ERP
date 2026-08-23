package co.assip.erp.cartera.cierremensual.validacion.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ResultadoValidacionCierreDTO {

    private Integer idCierreCartera;

    // =========================================================
    // RESULTADO GENERAL
    // =========================================================

    private boolean valido;

    private String estado;

    private String mensaje;

    // =========================================================
    // CANTIDADES PRINCIPALES
    // =========================================================

    private Integer cantidadCreditosCabecera;

    private Integer cantidadCreditosFotografia;

    private Integer cantidadResultados;

    private Integer cantidadResultadosA1;

    private Integer cantidadResultadosPe;

    private Integer cantidadPePersistidos;

    // =========================================================
    // VALIDACIONES DE INTEGRIDAD
    // =========================================================

    private Integer creditosSinResultado;

    private Integer creditosConResultadoDuplicado;

    private Integer metodosInvalidos;

    private Integer peSinResultadoPe;

    private Integer peDuplicados;

    private Integer peHuerfanos;

    // =========================================================
    // VALIDACIONES DE EDADES
    // =========================================================

    private Integer edadesNulas;

    private Integer edadesInvalidas;

    private Integer edadesPeNulas;

    private Integer edadesPeInvalidas;

    // =========================================================
    // VALIDACIONES MONETARIAS
    // =========================================================

    private Integer valoresNegativos;

    private Integer valoresPeNegativos;

    private Integer piPdiFueraRango;

    private Integer piPdiPeFueraRango;

    private Integer peDescuadrados;

    // =========================================================
    // PROCESOS
    // =========================================================

    private boolean procesoPeFinalizado;

    // =========================================================
    // CUADRE CONSOLIDADO FINAL
    // =========================================================

    private boolean cuadreConsolidadoOk;

    private String estadoCuadreConsolidado;

    // =========================================================
    // DETALLE
    // =========================================================

    private List<String> errores;

    private List<String> advertencias;
}