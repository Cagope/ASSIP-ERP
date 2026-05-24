package co.assip.erp.depositos.documentos_soporte.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class DocumentosSoporteCuentaDTO {

    private Integer idCuentaAhorro;

    private Integer idFormaAhorro;

    private String codigoCuenta;

    private String documento;

    private String nombreCompleto;

    private String nombreForma;

    private String documentoForma;

    private String descripcionSoporte;

    private Integer cantidadSoporte;

    private BigDecimal saldoActual;

    private String estadoCuenta;

    private boolean tieneDocumentoActivo;

}