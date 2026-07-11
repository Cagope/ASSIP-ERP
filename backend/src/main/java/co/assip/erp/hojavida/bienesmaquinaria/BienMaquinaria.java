package co.assip.erp.hojavida.bienesmaquinaria;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class BienMaquinaria {

    private Long idBienPersona;
    private Long idDatosPersonal;
    private BigDecimal porcentajePropiedad;

    private Long idBien;
    private Long idTipoBien;
    private String codigoTipoBien;
    private String nombreTipoBien;
    private String descripcionGeneral;
    private BigDecimal valorComercial;
    private BigDecimal valorGravamen;

    private Long idBienMaquinaria;

    private Long idTipoMaquinaria;
    private String nombreTipoMaquinaria;

    private String marca;
    private String modelo;
    private String serial;
    private String referencia;
    private String descripcionTecnica;
    private String ubicacion;
    private String estadoOperativo;
    private LocalDate fechaAdquisicion;

    private Long idTipoGravamen;
    private String nombreGravamen;

    private String observaciones;

    private String tipoDocumento;
    private String nombreTipoDocumento;
    private String documento;
    private String nombreCompleto;

    private BigDecimal valorNetoBien;
    private BigDecimal valorPropiedadAsociado;
    private BigDecimal valorGravamenAsociado;
    private BigDecimal valorNetoAsociado;

    private Integer fkSeguridadCreacion;
    private LocalDateTime fechaCreacion;

    private Integer fkSeguridadEdicion;
    private LocalDateTime fechaEdicion;
}