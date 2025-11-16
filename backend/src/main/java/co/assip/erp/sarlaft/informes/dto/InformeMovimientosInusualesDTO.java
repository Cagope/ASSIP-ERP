package co.assip.erp.sarlaft.informes.dto;

public class InformeMovimientosInusualesDTO {

    // Identificación
    public Long idDatosPersonal;
    public String documento;
    public String nombreCompleto;

    // Zonas
    public String nombreZona;
    public String nombreSubZona;

    // Agencia y aportes
    public String nombreAgencia;
    public Double saldoAportes;

    // Ingresos y egresos mensuales
    public Double ingresosMensuales;
    public Double egresosMensuales;

    // Total movimientos del período
    public Double totalConsignaciones;

    // Estado: INUSUAL / NORMAL
    public String estadoInusual;

    // Porcentajes comparación
    public Double pctVsIngresos;
    public Double pctVsEgresos;
}
