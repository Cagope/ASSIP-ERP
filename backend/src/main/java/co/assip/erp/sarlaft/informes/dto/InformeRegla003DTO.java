package co.assip.erp.sarlaft.informes.dto;

public class InformeRegla003DTO {

    public Long idDatosPersonal;

    public String documento;
    public String tipoDocumento;
    public String nombreTipoDocumento;
    public String nombreCompleto;

    public Integer edad;

    // Zona / Subzona
    public String nombreZona;
    public String nombreSubZona;

    // Contacto
    public String telefono;
    public String celularUno;
    public String celularDos;
    public String correoPersonal;

    // Permisos especiales
    public Boolean recibeLlamadas;
    public Boolean recibeMsm;
    public Boolean recibeEmails;
    public Boolean recibeCartas;
    public Boolean recibeRedesSociales;

    // Aportes
    public Double saldoAportes;
    public String fechaAperturaCuenta;

    // Regla
    public String motivo;
}
