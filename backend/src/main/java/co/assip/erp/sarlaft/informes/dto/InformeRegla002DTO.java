package co.assip.erp.sarlaft.informes.dto;

public class InformeRegla002DTO {

    // 🔹 Identificación
    public Long idDatosPersonal;
    public String documento;
    public String tipoDocumento;
    public String nombreTipoDocumento; // td.nombre_tipo_documento
    public String nombreCompleto;

    // 🔹 Validación de edad
    public Integer edad;

    // 🔹 Resultado de la regla (motivo)
    public String motivo;

    // 🔹 Información de aportes y cuenta
    public Double saldoAportes;
    public String fechaAperturaCuenta;

    // 🔹 Zona / Subzona
    public String nombreZona;
    public String nombreSubZona;

    // 🔹 Datos de contacto
    public String telefono;
    public String celularUno;
    public String celularDos;
    public String correoPersonal;

    // 🔹 Permisos especiales
    public Boolean recibeLlamadas;
    public Boolean recibeMsm;
    public Boolean recibeEmails;
    public Boolean recibeCartas;
    public Boolean recibeRedesSociales;
}
