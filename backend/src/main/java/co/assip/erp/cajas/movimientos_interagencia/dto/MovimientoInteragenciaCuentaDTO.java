package co.assip.erp.cajas.movimientos_interagencia.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoInteragenciaCuentaDTO {

    private Long idCuentaAhorro;

    private Integer idAgenciaCuenta;
    private String codigoAgenciaCuenta;
    private String nombreAgenciaCuenta;

    private String codigoCuenta;

    private Long idDatosPersonal;
    private String tipoDocumento;
    private String documento;
    private String nombreAsociado;

    private Integer idFormaAhorro;
    private String codigoForma;
    private String nombreForma;

    private BigDecimal saldoActual;
    private BigDecimal valorCanje;
    private BigDecimal saldoDisponible;

    private String estadoCuenta;
    private Boolean estadoOperativo;
    private String mensajeOperativo;
}