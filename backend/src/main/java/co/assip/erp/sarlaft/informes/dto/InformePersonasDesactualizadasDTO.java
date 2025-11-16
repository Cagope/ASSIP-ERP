package co.assip.erp.sarlaft.informes.dto;

public class InformePersonasDesactualizadasDTO {

    public Long idDatosPersonal;
    public String documento;
    public String nombreCompleto;
    public String fechaActualizacion;
    public Integer diasDesactualizado;
    public Double saldoAportes;
    public String estado;  // ACTUALIZADO / DESACTUALIZADO

    // 🔹 Datos de contacto (para Excel)
    public String telefono;
    public String celularUno;
    public String celularDos;
    public String correoPersonal;

    // 🔹 Zona / Subzona
    public String nombreZona;
    public String nombreSubZona;

    // 🔹 Permisos especiales
    public Boolean recibeLlamadas;
    public Boolean recibeMsm;
    public Boolean recibeEmails;
    public Boolean recibeCartas;
    public Boolean recibeRedesSociales;

    // 🔹 Datos de la cuenta de aportes (para control)
    public String fechaAperturaCuenta;
}
