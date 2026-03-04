package co.assip.erp.nomina.desprendible.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class DesprendibleEmpleadoDTO {

    private Integer idPeriodo;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String periodoDescripcion;

    private Integer idEmpleado;
    private String documento;
    private String nombreCompleto;

    private Integer idContrato;
    private BigDecimal salarioBase;
    private String cargo;

    private List<DesprendibleConceptoDTO> devengados;
    private List<DesprendibleConceptoDTO> deducciones;
    private DesprendibleTotalesDTO totales;

    public DesprendibleEmpleadoDTO(
            Integer idPeriodo,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            String periodoDescripcion,
            Integer idEmpleado,
            String documento,
            String nombreCompleto,
            Integer idContrato,
            BigDecimal salarioBase,
            String cargo,
            List<DesprendibleConceptoDTO> devengados,
            List<DesprendibleConceptoDTO> deducciones,
            DesprendibleTotalesDTO totales
    ) {
        this.idPeriodo = idPeriodo;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.periodoDescripcion = periodoDescripcion;
        this.idEmpleado = idEmpleado;
        this.documento = documento;
        this.nombreCompleto = nombreCompleto;
        this.idContrato = idContrato;
        this.salarioBase = salarioBase;
        this.cargo = cargo;
        this.devengados = devengados;
        this.deducciones = deducciones;
        this.totales = totales;
    }

    public Integer getIdPeriodo() { return idPeriodo; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public String getPeriodoDescripcion() { return periodoDescripcion; }
    public Integer getIdEmpleado() { return idEmpleado; }
    public String getDocumento() { return documento; }
    public String getNombreCompleto() { return nombreCompleto; }
    public Integer getIdContrato() { return idContrato; }
    public BigDecimal getSalarioBase() { return salarioBase; }
    public String getCargo() { return cargo; }
    public List<DesprendibleConceptoDTO> getDevengados() { return devengados; }
    public List<DesprendibleConceptoDTO> getDeducciones() { return deducciones; }
    public DesprendibleTotalesDTO getTotales() { return totales; }
}