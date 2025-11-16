package co.assip.erp.sarlaft.informes;

import java.util.Map;

public class InformeSarlaftRequest {

    private InformeSarlaftTipo tipo;
    private Map<String, Object> filtros;

    public InformeSarlaftTipo getTipo() {
        return tipo;
    }

    public void setTipo(InformeSarlaftTipo tipo) {
        this.tipo = tipo;
    }

    public Map<String, Object> getFiltros() {
        return filtros;
    }

    public void setFiltros(Map<String, Object> filtros) {
        this.filtros = filtros;
    }
}
