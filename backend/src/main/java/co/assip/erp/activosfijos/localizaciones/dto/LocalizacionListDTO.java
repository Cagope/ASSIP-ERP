package co.assip.erp.activosfijos.localizaciones.dto;

public class LocalizacionListDTO {

    private Long idLocalizacion;
    private String nombre;
    private String telefono;
    private Integer idAgencia;

    public Long getIdLocalizacion() {
        return idLocalizacion;
    }

    public void setIdLocalizacion(Long idLocalizacion) {
        this.idLocalizacion = idLocalizacion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public Integer getIdAgencia() {
        return idAgencia;
    }

    public void setIdAgencia(Integer idAgencia) {
        this.idAgencia = idAgencia;
    }
}
