package co.assip.erp.nomina.desprendible.dto;

import java.math.BigDecimal;

public class DesprendibleConceptoDTO {

    private String codigoConcepto;
    private String nombreConcepto;
    private BigDecimal cantidad;
    private BigDecimal valor;

    public DesprendibleConceptoDTO(
            String codigoConcepto,
            String nombreConcepto,
            BigDecimal cantidad,
            BigDecimal valor
    ) {
        this.codigoConcepto = codigoConcepto;
        this.nombreConcepto = nombreConcepto;
        this.cantidad = cantidad;
        this.valor = valor;
    }

    public String getCodigoConcepto() { return codigoConcepto; }
    public String getNombreConcepto() { return nombreConcepto; }
    public BigDecimal getCantidad() { return cantidad; }
    public BigDecimal getValor() { return valor; }
}