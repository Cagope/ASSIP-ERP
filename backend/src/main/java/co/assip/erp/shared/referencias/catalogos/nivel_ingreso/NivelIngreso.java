package co.assip.erp.shared.referencias.catalogos.nivel_ingreso;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "niveles_ingresos", schema = "catalogos")
public class NivelIngreso {

    @Id
    @Column(name = "codigo_nivel_ingreso", length = 2)
    private String codigoNivelIngreso;

    @Column(name = "valor1_natural")
    private BigDecimal valor1Natural;

    @Column(name = "valor2_natural")
    private BigDecimal valor2Natural;

    @Column(name = "valor1_juridico")
    private BigDecimal valor1Juridico;

    @Column(name = "valor2_juridico")
    private BigDecimal valor2Juridico;

    // Getters y Setters
    public String getCodigoNivelIngreso() {
        return codigoNivelIngreso;
    }

    public void setCodigoNivelIngreso(String codigoNivelIngreso) {
        this.codigoNivelIngreso = codigoNivelIngreso;
    }

    public BigDecimal getValor1Natural() {
        return valor1Natural;
    }

    public void setValor1Natural(BigDecimal valor1Natural) {
        this.valor1Natural = valor1Natural;
    }

    public BigDecimal getValor2Natural() {
        return valor2Natural;
    }

    public void setValor2Natural(BigDecimal valor2Natural) {
        this.valor2Natural = valor2Natural;
    }

    public BigDecimal getValor1Juridico() {
        return valor1Juridico;
    }

    public void setValor1Juridico(BigDecimal valor1Juridico) {
        this.valor1Juridico = valor1Juridico;
    }

    public BigDecimal getValor2Juridico() {
        return valor2Juridico;
    }

    public void setValor2Juridico(BigDecimal valor2Juridico) {
        this.valor2Juridico = valor2Juridico;
    }
}
