package co.assip.erp.activosfijos.bloques.dto;

public class BloqueSaveDTO {

    private String codigoBloque;
    private String nombreBloque;
    private Integer mesesDepreciacionDefecto;

    public String getCodigoBloque() {
        return codigoBloque;
    }

    public void setCodigoBloque(String codigoBloque) {
        this.codigoBloque = codigoBloque;
    }

    public String getNombreBloque() {
        return nombreBloque;
    }

    public void setNombreBloque(String nombreBloque) {
        this.nombreBloque = nombreBloque;
    }

    public Integer getMesesDepreciacionDefecto() {
        return mesesDepreciacionDefecto;
    }

    public void setMesesDepreciacionDefecto(Integer mesesDepreciacionDefecto) {
        this.mesesDepreciacionDefecto = mesesDepreciacionDefecto;
    }
}
