package co.assip.erp.contabilidad.registro.dto;

/**
 * Trazabilidad del ORIGEN del comprobante contable.
 * Se guarda en contabilidad.origen_comprobantes.
 *
 * - moduloOrigen: texto descriptivo (ej: "ACTIVOS_FIJOS", "DEPOSITOS")
 * - procesoOrigen: texto descriptivo (ej: "DEPRECIACION", "APERTURA_CUENTA")
 * - tablaOrigen / idOrigen: referencia opcional al registro que generó el comprobante
 */
public class OrigenComprobanteDTO {

    // =========================
    // COMPROBANTE (LLAVE LÓGICA)
    // =========================
    private Integer idAgencia;
    private String tipoComprobante;     // CHAR(2)
    private String numeroComprobante;   // CHAR(10)

    // =========================
    // ORIGEN
    // =========================
    private String origenTipo;          // AUTO | MANUAL
    private String moduloOrigen;        // texto descriptivo
    private String procesoOrigen;       // opcional
    private String tablaOrigen;         // opcional
    private Long idOrigen;              // opcional

    // =========================
    // GETTERS / SETTERS
    // =========================
    public Integer getIdAgencia() {
        return idAgencia;
    }

    public void setIdAgencia(Integer idAgencia) {
        this.idAgencia = idAgencia;
    }

    public String getTipoComprobante() {
        return tipoComprobante;
    }

    public void setTipoComprobante(String tipoComprobante) {
        this.tipoComprobante = tipoComprobante;
    }

    public String getNumeroComprobante() {
        return numeroComprobante;
    }

    public void setNumeroComprobante(String numeroComprobante) {
        this.numeroComprobante = numeroComprobante;
    }

    public String getOrigenTipo() {
        return origenTipo;
    }

    public void setOrigenTipo(String origenTipo) {
        this.origenTipo = origenTipo;
    }

    public String getModuloOrigen() {
        return moduloOrigen;
    }

    public void setModuloOrigen(String moduloOrigen) {
        this.moduloOrigen = moduloOrigen;
    }

    public String getProcesoOrigen() {
        return procesoOrigen;
    }

    public void setProcesoOrigen(String procesoOrigen) {
        this.procesoOrigen = procesoOrigen;
    }

    public String getTablaOrigen() {
        return tablaOrigen;
    }

    public void setTablaOrigen(String tablaOrigen) {
        this.tablaOrigen = tablaOrigen;
    }

    public Long getIdOrigen() {
        return idOrigen;
    }

    public void setIdOrigen(Long idOrigen) {
        this.idOrigen = idOrigen;
    }
}
