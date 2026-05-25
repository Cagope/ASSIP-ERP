package co.assip.erp.depositos.movimientos.cuentasahorro.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CuentaMovimientoDTO {

    private Integer idCuentaAhorro;

    private Integer idAgencia;
    private String codigoAgencia;
    private String nombreAgencia;

    private Integer idFormaAhorro;
    private String codigoForma;
    private String nombreForma;

    private String codigoCuenta;

    private Integer idDatosPersonal;
    private String documento;
    private String nombreAsociado;

    private BigDecimal saldoActualCuenta;

    private String estadoCuenta;
    private String cuentaActiva;
    private String gmfCuenta;

    private LocalDate fechaAperturaCuenta;

    private String descripcionEstadoCuenta;
    private Boolean estadoOperativo;
    private String mensajeOperativo;

    private String numeroInicialLibreta;
    private String numeroFinalLibreta;
    private String tipoDocumentoSoporte;

    private String documentoPoder;
    private String nombrePoder;
    private String telefonoPoder;
    private String celularPoder;

    private Boolean cuentaConjuntaReal;
    private String conjuntos;

    private BigDecimal valorEnCanje;

    public String getCodigoAgencia() {
        return trim(codigoAgencia);
    }

    public String getNombreAgencia() {
        return trim(nombreAgencia);
    }

    public String getCodigoForma() {
        return trim(codigoForma);
    }

    public String getNombreForma() {
        return trim(nombreForma);
    }

    public String getCodigoCuenta() {
        return trim(codigoCuenta);
    }

    public String getDocumento() {
        return trim(documento);
    }

    public String getNombreAsociado() {
        return trim(nombreAsociado);
    }

    public String getEstadoCuenta() {
        return trim(estadoCuenta);
    }

    public String getCuentaActiva() {
        return trim(cuentaActiva);
    }

    public String getGmfCuenta() {
        return trim(gmfCuenta);
    }

    public String getDescripcionEstadoCuenta() {
        return trim(descripcionEstadoCuenta);
    }

    public String getMensajeOperativo() {
        return trim(mensajeOperativo);
    }

    public String getNumeroInicialLibreta() {
        return trim(numeroInicialLibreta);
    }

    public String getNumeroFinalLibreta() {
        return trim(numeroFinalLibreta);
    }

    public String getTipoDocumentoSoporte() {
        return trim(tipoDocumentoSoporte);
    }

    public String getDocumentoPoder() {
        return trim(documentoPoder);
    }

    public String getNombrePoder() {
        return trim(nombrePoder);
    }

    public String getTelefonoPoder() {
        return trim(telefonoPoder);
    }

    public String getCelularPoder() {
        return trim(celularPoder);
    }

    public String getConjuntos() {
        return trim(conjuntos);
    }

    public BigDecimal getSaldoActualCuenta() {
        return nvl(saldoActualCuenta);
    }

    public BigDecimal getValorEnCanje() {
        return nvl(valorEnCanje);
    }

    public Boolean getEstadoOperativo() {
        return estadoOperativo != null && estadoOperativo;
    }

    public Boolean getCuentaConjuntaReal() {
        return cuentaConjuntaReal != null && cuentaConjuntaReal;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}